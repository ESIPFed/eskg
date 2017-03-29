package org.esipfed.eskg.portalservice;

import java.io.*;
import java.lang.Exception;
import java.lang.String;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class OntoUploader {
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
      rd = new BufferedReader(
          new InputStreamReader(conn.getInputStream()));
      while ((line = rd.readLine()) != null) {
        result += line;
      }
      rd.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  //post text in json format
  private static String postJSON(String urlToPost, String body) {
    URL url;
    HttpURLConnection conn;

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

      DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
      wr.write(body.getBytes());
      wr.flush();
      wr.close();
      conn.disconnect();

      InputStream is;
      boolean error = false;
      if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 400) {
        is = conn.getInputStream();
      } else {
        error = true;
        is = conn.getErrorStream();
      }

      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
      while ((line = rd.readLine()) != null) {
        result += line;
      }
      rd.close();

      if (error) throw new Exception(result);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }
  
  //post text file
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
      writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
      writer.append(CRLF).flush();
      Files.copy(ontologyFile.toPath(), output);
      output.flush(); // Important before continuing with writer!
      writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

      InputStream is;
      boolean error = false;
      if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 400) {
        is = conn.getInputStream();
      } else {
        error = true;
        is = conn.getErrorStream();
      }

      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
      while ((line = rd.readLine()) != null) {
        result += line;
      }
      rd.close();

      if (error) throw new Exception(result);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }

  public static void main(String[] args) {
    String response_get = OntoUploader.get(REST_URL + "/ontologies/GCMD-DIF/submissions");
    System.out.println(response_get);
    
    //http://data.bioontology.org/documentation#OntologySubmission
    //need to figure out the format of each parameter in the post body
    String response_post = OntoUploader.postFile(REST_URL + "/ontologies/GCMD-DIF/submissions", null, "/Users/yjiang/Documents/mudrod_data/ontotest.owl");
    System.out.println(response_post);
  }

}