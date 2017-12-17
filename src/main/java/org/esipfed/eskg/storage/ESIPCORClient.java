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
package org.esipfed.eskg.storage;

import java.util.Properties;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.jena.ontology.OntModel;

/**
 * Enables clients to interact with the ESIP <a
 * href="https://cor.esipfed.org">Community Ontology Repository</a>.
 * 
 * @author lewismc
 * 
 */
public class ESIPCORClient implements StorageClient {

  public ESIPCORClient() {
    // default constructor
  }

  @Override
  public void write(OntModel ontModel, Properties props) {
    LocalFileClient fileClient = new LocalFileClient(props);
    fileClient.write(ontModel, props);
    WebClient client = WebClient.create(props.getProperty("eskg.cor.endpoint", "http://cor.esipfed.org/ont")).path("upload").query("file", "").query("format", "TURTLE");
    if (getClass().getResource(props.getProperty("eskg.file.name", "podaacDatasets.ttl")).getFile() != null) {
      client.post(getClass().getResource(props.getProperty("eskg.file.name", "podaacDatasets.ttl")).getFile());
    }
    
  }

  // /**
  // * @param args
  // */
  // public static void main(String[] args) {
  // // TODO Auto-generated method stub
  //
  // }

}
