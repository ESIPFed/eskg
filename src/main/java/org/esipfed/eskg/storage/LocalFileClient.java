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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client for interacting with local Ontology files.
 * 
 * @author lewismc
 */
public class LocalFileClient implements StorageClient {

  private static final Logger LOG = LoggerFactory.getLogger(LocalFileClient.class);

  private Properties props;

  public LocalFileClient() {
    // default constructor
  }

  public LocalFileClient(Properties props) {
    if (props != null) {
      this.setProps(props);
    } else {
      this.setProps(new Properties());
    }
  }

  @Override
  public void write(OntModel ontModel, Properties props) {
    String ontFile = props.getProperty("eskg.file.name", "target/classes/podaacDatasets.ttl");
    try (OutputStream fos = new FileOutputStream(ontFile);
            Writer writer = new OutputStreamWriter(fos, Charset.defaultCharset())){
      ontModel.write(writer, "TURTLE");
    } catch (IOException e) {
      LOG.error("Error whilst writing Ontology Model to {}.", ontFile, e);
    }
    LOG.info("Successfully wrote Ontology Model to {}.", ontFile);
  }

  /**
   * @return the props
   */
  public Properties getProps() {
    return props;
  }

  /**
   * @param props
   *          the props to set
   */
  public void setProps(Properties props) {
    this.props = props;
  }

  // /**
  // * @param args
  // */
  // public static void main(String[] args) {}
}
