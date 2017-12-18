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
package org.esipfed.eskg.mapper.ontology;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.esipfed.eskg.mapper.ObjectMapper;
import org.esipfed.eskg.storage.ESIPCORClient;
//import org.esipfed.eskg.storage.ESIPSemanticPortalClient;
import org.esipfed.eskg.storage.LocalFileClient;
import org.esipfed.eskg.structures.ContactAddress;
import org.esipfed.eskg.structures.DIF;
import org.esipfed.eskg.structures.DataCenter;
import org.esipfed.eskg.structures.DataCenterName;
import org.esipfed.eskg.structures.DataResolution;
import org.esipfed.eskg.structures.DataSetCitation;
import org.esipfed.eskg.structures.IDNNode;
import org.esipfed.eskg.structures.Location;
import org.esipfed.eskg.structures.Parameters;
import org.esipfed.eskg.structures.Personnel;
import org.esipfed.eskg.structures.Project;
import org.esipfed.eskg.structures.Reference;
import org.esipfed.eskg.structures.SensorName;
import org.esipfed.eskg.structures.SourceName;
import org.esipfed.eskg.structures.SpatialCoverage;
import org.esipfed.eskg.structures.Summary;
import org.esipfed.eskg.structures.TemporalCoverage;

/**
 * @author lewismc
 */
public class PODAACOntologyMapper implements ObjectMapper {

  private static final String SWEET_REPR_DATA_PRODUCT = "http://sweetontology.net/reprDataProduct";
  private static final String SWEET_REPR_DATA_PRODUCT_NS = SWEET_REPR_DATA_PRODUCT + "/";
  private static final String MUDROD_GCMD_DIF_9_8_2 = "https://raw.githubusercontent.com/mudrod/mudrod_ontologies/master/dif_v9.8.2.owl";
  private static final String MUDROD_GCMD_DIF_9_8_2_NS = MUDROD_GCMD_DIF_9_8_2 + "/";
  private static final String PODAAC_DATASET = "http://cor.esipfed.org/ont/eskg/";

  /**
   * 
   */
  public PODAACOntologyMapper() {
    // default constructor
  }

  @Override
  public Object map(String mapperId, ByteArrayInputStream inputStream) {
    return null;
  }

  @Override
  public void map(List<DIF> pojoList, Properties props) {
    // create the base model
    OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    ontModel.setNsPrefix("dif_v9.8.2", MUDROD_GCMD_DIF_9_8_2);
    ontModel.setNsPrefix("geo", "http://www.opengis.net/ont/geosparql#");
    ontModel.read(SWEET_REPR_DATA_PRODUCT, null, "TURTLE");

    // get the https://sweetontology.net/reprDataProduct/Dataset class reference
    Resource dataset = ontModel.getResource(SWEET_REPR_DATA_PRODUCT_NS + "Dataset");
    // create the https://sweetontology.net/reprDataProduct/PODAACDataset class
    // reference
    OntClass podaacDataset = ontModel.createClass(PODAAC_DATASET + "PODAACDataset");
    // make PODAACDataset a subclass of Dataset
    podaacDataset.addSuperClass(dataset);
    // create an individual for each DIF POJO
    for (DIF dif : pojoList) {
      Individual gcmdDif = podaacDataset.createIndividual(PODAAC_DATASET + dif.getEntryID());
      buildIndividual(ontModel, dif, gcmdDif);
    }
    writeOntologyModel(ontModel, props);
  }

  private void writeOntologyModel(OntModel ontModel, Properties props) {
    switch (props.getProperty("eskg.storage", "file")) {
      case "cor":
      ESIPCORClient corClient = new ESIPCORClient();
      corClient.write(ontModel, props);
      break;
      case "semanticportal":
      // ESIPSemanticPortalClient.write(ontModel, props);
      break;
      default:
      LocalFileClient lClient = new LocalFileClient();
      lClient.write(ontModel, props);
      break;
    }
  }

  private void buildIndividual(OntModel ontModel, DIF dif, Individual gcmdDif) {

    gcmdDif.addVersionInfo(new Timestamp(System.currentTimeMillis()).toInstant().toString());
    // Entry_ID
    gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Entry_ID"), l(dif.getEntryID()));
    // Entry_Title
    gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Entry_Title"), dif.getEntryTitle(), "en");
    // ISO_Topic_Category
    for (String isoTopicCategory : dif.getISOTopicCategory()) {
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "ISO_Topic_Category"), isoTopicCategory, "en");
    }
    // Access_Constraints
    gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Access_Constraints"), dif.getAccessConstraints(), "en");
    // Use_Constraints
    gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Use_Constraints"), dif.getUseConstraints(), "en");
    // Data_Set_Language
    for (int i = 0; i < dif.getDataSetLanguage().size(); i++) {
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Data_Set_Language"), dif.getDataSetLanguage().get(i), "en");
    }
    // Originating_Center
    if (dif.getOriginatingCenter() != null) {
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Originating_Center"), dif.getOriginatingCenter(), "en");
    }
    // Metadata_Name
    gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Metadata_Name"), dif.getMetadataName(), "en");
    // Metadata_Version
    gcmdDif.addLiteral(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Metadata_Version"), Float.parseFloat(dif.getMetadataVersion()));
    // DIF_Creation_Date
    gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "DIF_Creation_Date"), dif.getDIFCreationDate(), XSDDatatype.XSDdate);
    // Last_DIF_Revision_Date
    gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Last_DIF_Revision_Date"), dif.getLastDIFRevisionDate(), XSDDatatype.XSDdate);
    // DIF_Revision_History
    gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "DIF_Revision_History"), dif.getDIFRevisionHistory(), "en");

    // Data_Set_Citation
    for (DataSetCitation dataSetCitation : dif.getDataSetCitation()) {
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Dataset_Creator"), dataSetCitation.getDatasetCreator(), "en");
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Dataset_Title"), dataSetCitation.getDatasetTitle(), "en");
      if (dataSetCitation.getDatasetSeriesName() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Dataset_Series_Name"), dataSetCitation.getDatasetSeriesName(), "en");
      }
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Dataset_Release_Date"), dataSetCitation.getDatasetReleaseDate(), XSDDatatype.XSDdate);
      if (dataSetCitation.getDatasetReleasePlace() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Dataset_Release_Place"), dataSetCitation.getDatasetReleasePlace(), "en");
      }
      if (dataSetCitation.getDatasetPublisher() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Dataset_Publisher"), dataSetCitation.getDatasetPublisher(), "en");
      }
      try {
        gcmdDif.addLiteral(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Version"), Float.parseFloat(dataSetCitation.getVersion()));
      } catch (NumberFormatException e) {
        gcmdDif.addLiteral(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Version"), l(dataSetCitation.getVersion()));
      }
      
      if (dataSetCitation.getOtherCitationDetails() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Other_Citation_Details"), dataSetCitation.getOtherCitationDetails(), "en");
      }
      //possibility of URL data type?
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Online_Resource"), l(dataSetCitation.getOnlineResource()));
    }

    // Personnel
    for (Personnel personnel : dif.getPersonnel()) {
      for (int i = 0; i < personnel.getFax().size(); i++) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Role"), personnel.getRole().get(i), "en");
      }
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "First_Name"), personnel.getFirstName(), "en");
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Last_Name"), personnel.getLastName(), "en");
      for (int i = 0; i < personnel.getEmail().size(); i++) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Email"), personnel.getEmail().get(i), "en");
      }
      for (int i = 0; i < personnel.getFax().size(); i++) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Fax"), l(personnel.getFax().get(i)));
      }
    }

    // Parameters
    for (Parameters parameter : dif.getParameters()) {
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Category"), parameter.getCategory(), "en");
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Topic"), parameter.getTopic(), "en");
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Term"), parameter.getTerm(), "en");
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Variable_Level_1"), parameter.getVariableLevel1(), "en");
      if (parameter.getVariableLevel2() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Variable_Level_2"), parameter.getVariableLevel2(), "en");
      }
      if (parameter.getVariableLevel3() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Variable_Level_3"), parameter.getVariableLevel3(), "en");
      }
    }

    // Sensor_Name
    for (SensorName sensorName : dif.getSensorName()) {
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Short_Name"), sensorName.getShortName(), "en");
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Long_Name"), sensorName.getLongName(), "en");
    }

    // Source_Name
    for (SourceName sourceName : dif.getSourceName()) {
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Short_Name"), sourceName.getShortName(), "en");
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Long_Name"), sourceName.getLongName(), "en");
    }

    // Temporal_Coverage
    for (TemporalCoverage temporalCoverage : dif.getTemporalCoverage()) {
      if (temporalCoverage.getStartDate() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Start_Date"), temporalCoverage.getStartDate(), XSDDatatype.XSDdate);
      }
      if (temporalCoverage.getStopDate() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Stop_Date"), temporalCoverage.getStopDate(), XSDDatatype.XSDdate);
      }
    }

    // Spatial_Coverage
    for (SpatialCoverage spatialCoverage : dif.getSpatialCoverage()) {
      try {
        if (spatialCoverage.getEasternmostLongitude() != null) {
          gcmdDif.addLiteral(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Easternmost_Longitude"), Float.parseFloat(spatialCoverage.getEasternmostLongitude()));
        }
      } catch (NumberFormatException e) {
        gcmdDif.addLiteral(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Easternmost_Longitude"), l(spatialCoverage.getEasternmostLongitude()));
      }
      
      if (spatialCoverage.getMaximumAltitude() != null) {
        gcmdDif.addLiteral(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Maximum_Altitude"), Float.parseFloat(spatialCoverage.getMaximumAltitude()));
      }
      if (spatialCoverage.getMaximumDepth() != null) {
        gcmdDif.addLiteral(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Maximum_Depth"), Float.parseFloat(spatialCoverage.getMaximumDepth()));
      }
      if (spatialCoverage.getMinimumAltitude() != null) {
        gcmdDif.addLiteral(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Minimum_Altitude"), Float.parseFloat(spatialCoverage.getMinimumAltitude()));
      }
      if (spatialCoverage.getMinimumDepth() != null) {
        gcmdDif.addLiteral(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Minimum_Depth"), Float.parseFloat(spatialCoverage.getMinimumDepth()));
      }
      try {
        if (spatialCoverage.getNorthernmostLatitude() != null) {
          gcmdDif.addLiteral(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Northernmost_Latitude"), Float.parseFloat(spatialCoverage.getNorthernmostLatitude()));
        }
      } catch (NumberFormatException e) {
        gcmdDif.addLiteral(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Northernmost_Latitude"), l(spatialCoverage.getNorthernmostLatitude()));
      }
      try {
        if (spatialCoverage.getSouthernmostLatitude() != null) {
          gcmdDif.addLiteral(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Southernmost_Latitude"), Float.parseFloat(spatialCoverage.getSouthernmostLatitude()));
        }
      } catch (NumberFormatException e) {
        gcmdDif.addLiteral(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Southernmost_Latitude"), l(spatialCoverage.getSouthernmostLatitude()));
      }
      try {
        if (spatialCoverage.getWesternmostLongitude() != null) {
          gcmdDif.addLiteral(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Westernmost_Longitude"), Float.parseFloat(spatialCoverage.getWesternmostLongitude()));
        }
      } catch (NumberFormatException e) {
        gcmdDif.addLiteral(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Westernmost_Longitude"), l(spatialCoverage.getWesternmostLongitude()));
      }
    }

    // Location
    for (Location location : dif.getLocation()) {
      if (location.getDetailedLocation() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Detailed_Location"), location.getDetailedLocation(), "en");
      }
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Location_Category"), location.getLocationCategory(), "en");
      if (location.getLocationSubregion1() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Location_Subregion1"), location.getLocationSubregion1(), "en");
      }
      if (location.getLocationSubregion2() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Location_Subregion2"), location.getLocationSubregion2(), "en");
      }
      if (location.getLocationSubregion3() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Location_Subregion3"), location.getLocationSubregion3(), "en");
      }
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Location_Type"), location.getLocationType(), "en");
    }

    // Data_Resolution
    for (DataResolution dataResolution : dif.getDataResolution()) {
      if (dataResolution.getHorizontalResolutionRange() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Horizontal_Resolution_Range"), dataResolution.getHorizontalResolutionRange(), "en");
      }
      if (dataResolution.getLatitudeResolution() != null) {
        gcmdDif.addLiteral(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Latitude_Resolution"), Float.parseFloat(dataResolution.getLatitudeResolution()));
      }
      if (dataResolution.getLongitudeResolution() != null) {
        gcmdDif.addLiteral(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Longitude_Resolution"), Float.parseFloat(dataResolution.getLongitudeResolution()));
      }
      if (dataResolution.getTemporalResolution() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Temporal_Resolution"), dataResolution.getTemporalResolution(), "en");
      }
      if (dataResolution.getTemporalResolutionRange() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Temporal_Resolution_Range"), dataResolution.getTemporalResolutionRange(), "en");
      }
      if (dataResolution.getVerticalResolution() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Vertical_Resolution"), dataResolution.getVerticalResolution(), "en");
      }
      if (dataResolution.getVerticalResolutionRange() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Vertical_Resolution_Range"), dataResolution.getVerticalResolutionRange(), "en");
      }
    }

    // Project
    for (Project project : dif.getProject()) {
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Long_Name"), project.getLongName(), "en");
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Short_Name"), project.getShortName(), "en");
    }

    // Data_Center
    for (DataCenter dataCenter : dif.getDataCenter()) {
      DataCenterName dataCenterName = dataCenter.getDataCenterName();
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Long_Name"), dataCenterName.getLongName(), "en");
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Short_Name"), dataCenterName.getShortName(), "en");
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Data_Center_URL"), l(dataCenter.getDataCenterURL()));
      List<Personnel> personnelList = dataCenter.getPersonnel();
      for (Personnel personnel : personnelList) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "First_Name"), personnel.getFirstName(), "en");
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Last_Name"), personnel.getLastName(), "en");
        if (personnel.getMiddleName() != null) {
          gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Middle_Name"), personnel.getMiddleName(), "en");
        }
        if (personnel.getContactAddress() != null) {
          ContactAddress contactAddress = personnel.getContactAddress();
          gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "City"), contactAddress.getCity(), "en");
          gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Country"), contactAddress.getCountry(), "en");
          gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Postal_Code"), contactAddress.getPostalCode(), "en");
          gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Province_Or_State"), contactAddress.getProvinceOrState(), "en");
        }
        if (personnel.getEmail() != null) {
          for (int i = 0; i < personnel.getEmail().size(); i++) {
            gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Email"), personnel.getEmail().get(i), "en");
          }
        }
        if (personnel.getFax() != null) {
          for (int i = 0; i < personnel.getFax().size(); i++) {
            gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Fax"), l(personnel.getFax().get(i)));
          }
        }
        if (personnel.getPhone() != null) {
          for (int i = 0; i < personnel.getPhone().size(); i++) {
            gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Phone"), l(personnel.getPhone().get(i)));
          }
        }
        if (personnel.getRole() != null) {
          for (int i = 0; i < personnel.getRole().size(); i++) {
            gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Role"), personnel.getRole().get(i), "en");
          }
        }
      }
    }

    // Reference
    for (Reference reference : dif.getReference()) {
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Reference"), reference.toString(), "en");
    }

    // Summary
    Summary summary = dif.getSummary();
    if (summary != null) {
      gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Absract"), summary.getAbstract(), "en");
    }


    // IDN_Node
    for (IDNNode idnNode : dif.getIDNNode()) {
      if (idnNode.getLongName() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Long_Name"), idnNode.getLongName(), "en");
      }
      if (idnNode.getShortName() != null) {
        gcmdDif.addProperty(ontModel.createDatatypeProperty(MUDROD_GCMD_DIF_9_8_2_NS + "Short_Name"), idnNode.getShortName(), "en");
      }
    }

  }

  @SuppressWarnings("unused")
  private static Resource r(String localname) {
    return ResourceFactory.createResource(MUDROD_GCMD_DIF_9_8_2_NS + localname);
  }

  @SuppressWarnings("unused")
  private static Property p(String localname) {
    return ResourceFactory.createProperty(MUDROD_GCMD_DIF_9_8_2_NS, localname);
  }

  private static Literal l(Object value) {
    if (value != null) {
      return ResourceFactory.createTypedLiteral(value);
    } else {
      return ResourceFactory.createTypedLiteral("");
    }
  }

  @SuppressWarnings("unused")
  private static Literal l(String lexicalform, RDFDatatype datatype) {
    return ResourceFactory.createTypedLiteral(lexicalform, datatype);
  }

  public static void main(String[] args) {
    List<DIF> difList = new ArrayList<>();
    DIF dif = new DIF();
    dif.setEntryID("Sample_Entry_ID");
    difList.add(dif);
    PODAACOntologyMapper mapper = new PODAACOntologyMapper();
    mapper.map(difList, new Properties());
  }
}
