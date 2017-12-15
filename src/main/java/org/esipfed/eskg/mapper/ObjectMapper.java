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
package org.esipfed.eskg.mapper;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Properties;

import org.esipfed.eskg.structures.DIF;

/**
 * Map's various data into POJO's. Individual mapping implementations
 * should implement this interface such as {@link org.esipfed.eskg.mapper.PODAACWebServiceObjectMapper}.
 */
public interface ObjectMapper {

  public enum MapperID {
    PODAAC_GCMD,
  }

  /**
   * Map the {@link java.io.ByteArrayInputStream} to the POJO defined
   * by the mapperId.
   * @param mapperId the {@link org.esipfed.eskg.mapper.ObjectMapper.MapperID}
   * @param inputStream a {@link java.io.ByteArrayInputStream} representing the 
   * content to be mapped to the Ontology Model.
   * @return a mapped Object, an example being {@link org.esipfed.eskg.structures.DIF}
   */
  public Object map(String mapperId, ByteArrayInputStream inputStream);

  void map(List<DIF> pojoList, Properties props);
}
