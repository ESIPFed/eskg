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
package org.esipfed.eskg.aquisition;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A thin client for interacting with the <a
 * href="https://podaac.jpl.nasa.gov/ws">NASA JPL PO.DAAC WebServices</a>.
 * 
 * @author lewismc
 */
public class PODAACWebServiceClient {

  private static final Logger LOG = LoggerFactory.getLogger(PODAACWebServiceClient.class);

  /*
   * Dataset Search service searches PO.DAAC's dataset catalog, over Level 2,
   * Level 3, and Level 4 datasets, using the following parameters: datasetId,
   * shortName, startTime, endTime, bbox, and others. In this case we set the
   * 'q' parameter to wildcard and increase the 'itemsPerPage' to more than the
   * number of Datasets we know to be present within PO.DAAC. Additionally, we
   * request the response to be serialized as ATOM such that we can extract
   * individual dataset records.
   */
  private static final String DATASET_SEARCH = "https://podaac.jpl.nasa.gov/ws/search/dataset/?q=*:*&itemsPerPage=1000&format=atom";

  private static final String DATASET_METADATA = "http://podaac.jpl.nasa.gov/ws/metadata/dataset/";

  private ArrayList<String> datasetIds;

  /**
   * Default constructor
   */
  private PODAACWebServiceClient() {
    // default constructor
  }

  /**
   * Core function which encapsulates all data acquisition and model mapping for
   * PO.DAAC Dataset Search and Dataset Metadata WebServices.
   * 
   * @throws IOException
   */
  public void fetchDatasets() throws IOException {
    try {
      executeDatasetSearch();
    } catch (IOException e) {
      LOG.error("Error executing PO.DAAC Dataset Search: {} {}", DATASET_SEARCH, e);
      throw new IOException(e);
    }
    fetchDatasetMetadata();
  }

  /**
   * This method utilizes the ArrayList populated by
   * {@link #parseXML(ByteArrayInputStream)} to aquire the metadata (in GCMD
   * format) for each dataset present within PO.DAAC. The query used to do this
   * can be seen in
   */
  private void fetchDatasetMetadata() {
    // TODO Auto-generated method stub

  }

  private void executeDatasetSearch() throws IOException {
    HttpClient client = HttpClientBuilder.create().build();
    HttpGet request = new HttpGet(DATASET_SEARCH);

    // add request header
    request.addHeader("User-Agent", "ESKG PO.DAAC WebService Client");
    HttpResponse response = client.execute(request);

    LOG.info("Response Code : " + response.getStatusLine().getStatusCode());

    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Charset.defaultCharset()));

    StringBuilder result = new StringBuilder();
    String line = "";
    while ((line = rd.readLine()) != null) {
      result.append(line);
    }
    parseXML(new ByteArrayInputStream(result.toString().getBytes(StandardCharsets.UTF_8)));
  }

  /**
   * This function accepts the result of querying the PO.DAAC Dataset Search
   * WebService using the query provided in the DATASET_SEARCH constant. The
   * response is in Atom XML, from each entry result, we therefore simply
   * extract all instances of 'podaac:datasetId' and add these to an ArrayList.
   * We use this List to obtain GMCD Metadata for each dataset.
   * 
   * @param byteArrayInputStream
   * @return
   */
  private void parseXML(ByteArrayInputStream byteArrayInputStream) {
    try {

      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(byteArrayInputStream);
      doc.getDocumentElement().normalize();

      NodeList nList = doc.getElementsByTagName("entry");

      datasetIds = new ArrayList<>();
      for (int temp = 0; temp < nList.getLength(); temp++) {

        Node nNode = nList.item(temp);

        if (nNode.getNodeType() == Node.ELEMENT_NODE) {

          Element eElement = (Element) nNode;

          LOG.debug("Adding following DatasetID to Dataset list: ", eElement.getElementsByTagName("podaac:datasetId").item(0).getTextContent());
          datasetIds.add(eElement.getElementsByTagName("podaac:datasetId").item(0).getTextContent());

        }
      }
    } catch (Exception e) {
      LOG.error("Error whilst parsing Atom XML response from Dataset Search: ", e);
    }
  }

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    PODAACWebServiceClient client = new PODAACWebServiceClient();
    client.fetchDatasets();
  }

}
