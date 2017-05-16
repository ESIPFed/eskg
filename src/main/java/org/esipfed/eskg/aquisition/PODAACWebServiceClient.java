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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.esipfed.eskg.mapper.PODAACWebServiceObjectMapper;
import org.esipfed.eskg.mapper.ObjectMapper.MapperID;
import org.esipfed.eskg.mapper.ontology.PODAACOntologyMapper;
import org.esipfed.eskg.structures.DIF;
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
  private static final String DATASET_SEARCH = 
          "https://podaac.jpl.nasa.gov/ws/search/dataset/?q=*:*&itemsPerPage=1000&format=atom";
  
  private static final String DATASET_SEARCH_2 = 
          "https://podaac.jpl.nasa.gov/ws/search/dataset/?q=*:*&itemsPerPage=1000&format=atom&startIndex=400";

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
    List<String> gcmdDatasetList = new ArrayList<>();
    try {
      for (int i = 0; i < 2; i++) {
        if (i == 0) {
          gcmdDatasetList.addAll(parseDatasetSearchAtomXML(executePODAACQuery(DATASET_SEARCH)));
        } else if (i == 1) {
          gcmdDatasetList.addAll(parseDatasetSearchAtomXML(executePODAACQuery(DATASET_SEARCH_2)));
        }
      }
    } catch (IOException e) {
      LOG.error("Error executing PO.DAAC Dataset Search: {} {}", DATASET_SEARCH, e);
      throw new IOException(e);
    }
    PODAACOntologyMapper ontologyMapper = new PODAACOntologyMapper();
    ontologyMapper.map(retrieveGCMDRecords(gcmdDatasetList));
  }

  private ByteArrayInputStream executePODAACQuery(String queryString) throws IOException {
    HttpClient client = HttpClientBuilder.create().build();
    HttpGet request = new HttpGet(queryString);

    // add request header
    request.addHeader("User-Agent", "ESKG PO.DAAC WebService Client");
    LOG.info("Executing GET request: {}", request.toString());
    HttpResponse response = client.execute(request);

    LOG.info("Response Code : {}", response.getStatusLine().getStatusCode());

    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Charset.defaultCharset()));

    StringBuilder result = new StringBuilder();
    String line = "";
    while ((line = rd.readLine()) != null) {
      result.append(line);
    }
    return new ByteArrayInputStream(result.toString().getBytes(StandardCharsets.UTF_8));
    
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
  private List<String> parseDatasetSearchAtomXML(ByteArrayInputStream byteArrayInputStream) {
    List<String> datasetGCMDList = new ArrayList<>();
    try {

      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(byteArrayInputStream);
      doc.getDocumentElement().normalize();
      Element root = doc.getDocumentElement();
      NodeList firstChildNodes = root.getChildNodes();

      for (int i = 0; i < firstChildNodes.getLength(); i++) {
        if ("entry".equals(firstChildNodes.item(i).getNodeName())) {
          Node entryNode = firstChildNodes.item(i);

          if (entryNode.getNodeType() == Node.ELEMENT_NODE) {

            Element entryElement = (Element) entryNode;
            NodeList entryElementChildren = entryElement.getChildNodes();
            for (int j = 0; j < entryElementChildren.getLength(); j++) {
              Node entryElementChild = entryElementChildren.item(j);
              if ("link".equals(entryElementChild.getNodeName())) {
                Element linkNode = (Element)entryElementChild;
                if (linkNode.getAttribute("title") != null && linkNode.getAttribute("title").contentEquals("GCMD Metadata")) {
                  String gcmdHrefValue = linkNode.getAttributes().getNamedItem("href").getNodeValue();
                  datasetGCMDList.add(gcmdHrefValue);
                  LOG.info("Added new Dataset record to list: {}", gcmdHrefValue);
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Error whilst parsing Atom XML response from Dataset Search: ", e);
    }
    LOG.info("Total number of dataset's retrieved: {}", datasetGCMDList.size());
    return datasetGCMDList;
  }

  /**
   * Method accepts a list of URLs which point to individual
   * GCMD manifestations of PO.DAAC Datasets. These URLs are fetched
   * and the XML results are mapped individually into a PO.DAAC Datasets
   * Ontology.
   * @param gcmdDatasetList an {@link java.util.List<String>} of URLs which
   * represent GCMD manifestations of PO.DAAC datasets. An example would be 
   * http://podaac.jpl.nasa.gov/ws/metadata/dataset&ampdatasetId=PODAAC-PATHF-5DD50&ampformat=gcmd
   * @return 
   */
  private List<DIF> retrieveGCMDRecords(List<String> gcmdDatasetList) {
    List<DIF> gcmdXMLPOJORecords = new ArrayList<>();
    for (int i = 0; i < gcmdDatasetList.size(); i++) {
      try {
        gcmdXMLPOJORecords.add((DIF) parseGCMDXML(executePODAACQuery(gcmdDatasetList.get(i))));
      } catch (IOException e) {
        LOG.error("Error executing PO.DAAC query for GCMD record: {} {}", gcmdDatasetList.get(i), e);
      }
    }
    return gcmdXMLPOJORecords;
    
  }
  private DIF parseGCMDXML(ByteArrayInputStream gcmdXmlByteArrayInputStream) {
    PODAACWebServiceObjectMapper objectMapper = new PODAACWebServiceObjectMapper();
    return (DIF) objectMapper.map(MapperID.PODAAC_GCMD.name(), gcmdXmlByteArrayInputStream);
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
