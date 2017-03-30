/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you 
 * may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esipfed.eskg.portalservice;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OntoUploader {

  private static final Logger LOG = LoggerFactory.getLogger(OntoUploader.class);

  static final String REST_URL = "http://semanticportal.esipfed.org:8080";

  private static final String API_KEY = "59f9f403-51f3-417e-9294-f9be7a737f50";

  private static String get(String urlToGet) {
    URL url;
    HttpURLConnection conn;
    BufferedReader rd;
    String line;
    String result = "";
    try {
      url = new URL(urlToGet);
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Authorization", "apikey token=" + API_KEY);
      conn.setRequestProperty("Accept", "application/json");
      rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.defaultCharset()));
      while ((line = rd.readLine()) != null) {
        result += line;
      }
      rd.close();
    } catch (Exception e) {
      LOG.error("Error attempting to execute GET request: {} {}", urlToGet, e);
    }
    return result;
  }

  // post text in json format
  private static String postJSON(String urlToPost, String body) throws IOException {
    URL url = null;
    HttpURLConnection conn = null;

    String line;
    String result = "";

    try {
      url = new URL(urlToPost);
    } catch (MalformedURLException e) {
      LOG.error("Error in URL construction: {} {}", urlToPost ,e);
    }
    try {
      conn = (HttpURLConnection) url.openConnection();
    } catch (IOException e) {
      LOG.error("Error attempting to open URL connection: ", e);
    }
    conn.setDoOutput(true);
    conn.setDoInput(true);
    conn.setInstanceFollowRedirects(false);
    try {
      conn.setRequestMethod("POST");
    } catch (ProtocolException e) {
      LOG.error("Error setting POST request method: ", e);
    }
    conn.setRequestProperty("Authorization", "apikey token=" + API_KEY);
    conn.setRequestProperty("Accept", "application/json");
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestProperty("charset", "utf-8");
    conn.setUseCaches(false);

    try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())){
      wr.write(body.getBytes(Charset.defaultCharset()));
      conn.disconnect();
    }

    InputStream is;
    boolean error = false;
    if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 400) {
      is = conn.getInputStream();
    } else {
      error = true;
      is = conn.getErrorStream();
    }

    BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()));
    while ((line = rd.readLine()) != null) {
      result += line;
    }
    rd.close();

    if (error)
      try {
        throw new Exception(result);
      } catch (Exception e) {
        LOG.error("Error retrieving response: ", e);
      }
    return result;
  }

  // post text file
  private static String postFile(String urlToPost, String body, String filePath) {
    URL url;
    HttpURLConnection conn;

    String charset = "UTF-8";
    File ontologyFile = new File(filePath);
    String CRLF = "\r\n"; // Line separator required by multipart/form-data.

    String line;
    String result = "";
    try {
      url = new URL(urlToPost);
      conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setDoInput(true);
      conn.setInstanceFollowRedirects(false);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Authorization", "apikey token=" + API_KEY);
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("charset", "utf-8");
      conn.setUseCaches(false);

      OutputStream output = conn.getOutputStream();
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
      writer.append("Content-Disposition: form-data; name=\"textFile\"; filename=\"" + ontologyFile.getName() + "\"").append(CRLF);
      writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text
                                                                                  // file
                                                                                  // itself
                                                                                  // must
                                                                                  // be
                                                                                  // saved
                                                                                  // in
                                                                                  // this
                                                                                  // charset!
      writer.append(CRLF).flush();
      Files.copy(ontologyFile.toPath(), output);
      output.flush(); // Important before continuing with writer!
      writer.append(CRLF).flush(); // CRLF is important! It indicates end of
                                   // boundary.

      InputStream is;
      boolean error = false;
      if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 400) {
        is = conn.getInputStream();
      } else {
        error = true;
        is = conn.getErrorStream();
      }

      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()));
      while ((line = rd.readLine()) != null) {
        result += line;
      }
      rd.close();

      if (error)
        throw new Exception(result);
    } catch (Exception e) {
      LOG.error("");
    }

    return result;
  }

  public static void main(String[] args) {
    String getResponse = OntoUploader.get(REST_URL + "/ontologies/GCMD-DIF/submissions");
    LOG.info(getResponse);

    // http://data.bioontology.org/documentation#OntologySubmission
    // need to figure out the format of each parameter in the post body
    String postResponse = OntoUploader.postFile(REST_URL + "/ontologies/GCMD-DIF/submissions", null, "/Users/yjiang/Documents/mudrod_data/ontotest.owl");
    LOG.info(postResponse);
  }

}