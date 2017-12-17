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
    ontModel.read(SWEET_REPR_DATA_PRODUCT, null, "TURTLE");

    // get the https://sweetontology.net/reprDataProduct/Dataset class reference
    Resource dataset = ontModel.getResource(SWEET_REPR_DATA_PRODUCT_NS + "Dataset");
    // create the https://sweetontology.net/reprDataProduct/PODAACDataset class
    // reference
    OntClass podaacDataset = ontModel.createClass(SWEET_REPR_DATA_PRODUCT_NS + "PODAACDataset");
    // make PODAACDataset a subclass of Dataset
    podaacDataset.addSuperClass(dataset);
    // create an individual for each DIF POJO
    for (DIF dif : pojoList) {
      Individual gcmdDif = podaacDataset.createIndividual("http://cor.esipfed.org/ont/eskg/" + dif.getEntryID());
      buildIndividual(dif, gcmdDif);
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

  private void buildIndividual(DIF dif, Individual gcmdDif) {

    gcmdDif.addVersionInfo(new Timestamp(System.currentTimeMillis()).toInstant().toString());
    // Entry_ID
    gcmdDif.addProperty(p("hasEntryID"), l(dif.getEntryID()));
    // Entry_Title
    gcmdDif.addProperty(p("hasEntryTitle"), dif.getEntryTitle(), "en");
    // ISO_Topic_Category
    for (String isoTopicCategory : dif.getISOTopicCategory()) {
      gcmdDif.addProperty(p("hasISOTopicCategory"), isoTopicCategory, "en");
    }
    // Access_Constraints
    gcmdDif.addProperty(p("hasAccessConstraints"), dif.getAccessConstraints(), "en");
    // Use_Constraints
    gcmdDif.addProperty(p("hasUseConstraints"), dif.getUseConstraints(), "en");
    // Data_Set_Language
    for (int i = 0; i < dif.getDataSetLanguage().size(); i++) {
      gcmdDif.addProperty(p("hasDataSetLanguage"), dif.getDataSetLanguage().get(i), "en");
    }
    // Originating_Center
    if (dif.getOriginatingCenter() != null) {
      gcmdDif.addProperty(p("hasOriginatingCenter"), dif.getOriginatingCenter(), "en");
    }
    // Metadata_Name
    gcmdDif.addProperty(p("hasMetadataName"), dif.getMetadataName(), "en");
    // Metadata_Version
    gcmdDif.addLiteral(p("hasMetadataVersion"), Float.parseFloat(dif.getMetadataVersion()));
    // DIF_Creation_Date
    gcmdDif.addProperty(p("hasDIFCreationDate"), dif.getDIFCreationDate(), XSDDatatype.XSDdate);
    // Last_DIF_Revision_Date
    gcmdDif.addProperty(p("hasLastDIFRevisionDate"), dif.getLastDIFRevisionDate(), XSDDatatype.XSDdate);
    // DIF_Revision_History
    gcmdDif.addProperty(p("hasDIFRevisionHistory"), dif.getDIFRevisionHistory(), "en");

    // Data_Set_Citation
    for (DataSetCitation dataSetCitation : dif.getDataSetCitation()) {
      gcmdDif.addProperty(p("hasDataSetCitationDatasetCreator"), dataSetCitation.getDatasetCreator(), "en");
      gcmdDif.addProperty(p("hasDataSetCitationDatasetTitle"), dataSetCitation.getDatasetTitle(), "en");
      if (dataSetCitation.getDatasetSeriesName() != null) {
        gcmdDif.addProperty(p("hasDataSetCitationDatasetSeriesName"), dataSetCitation.getDatasetSeriesName(), "en");
      }
      gcmdDif.addProperty(p("hasDataSetCitationDatasetReleaseDate"), dataSetCitation.getDatasetReleaseDate(), XSDDatatype.XSDdate);
      if (dataSetCitation.getDatasetReleasePlace() != null) {
        gcmdDif.addProperty(p("hasDataSetCitationDatasetReleasePlace"), dataSetCitation.getDatasetReleasePlace(), "en");
      }
      if (dataSetCitation.getDatasetPublisher() != null) {
        gcmdDif.addProperty(p("hasDataSetCitationDatasetPublisher"), dataSetCitation.getDatasetPublisher(), "en");
      }
      try {
        gcmdDif.addLiteral(p("hasDataSetCitationVersion"), Float.parseFloat(dataSetCitation.getVersion()));
      } catch (NumberFormatException e) {
        gcmdDif.addLiteral(p("hasDataSetCitationVersion"), l(dataSetCitation.getVersion()));
      }
      
      if (dataSetCitation.getOtherCitationDetails() != null) {
        gcmdDif.addProperty(p("hasDataSetCitationOtherCitationDetails"), dataSetCitation.getOtherCitationDetails(), "en");
      }
      //possibility of URL data type?
      gcmdDif.addProperty(p("hasDataSetCitationOnlineResource"), l(dataSetCitation.getOnlineResource()));
    }

    // Personnel
    for (Personnel personnel : dif.getPersonnel()) {
      for (int i = 0; i < personnel.getFax().size(); i++) {
        gcmdDif.addProperty(p("hasPersonnelRole"), personnel.getRole().get(i), "en");
      }
      gcmdDif.addProperty(p("hasPersonnelFirstName"), personnel.getFirstName(), "en");
      gcmdDif.addProperty(p("hasPersonnelLastName"), personnel.getLastName(), "en");
      for (int i = 0; i < personnel.getEmail().size(); i++) {
        gcmdDif.addProperty(p("hasPersonnelEmail"), personnel.getEmail().get(i), "en");
      }
      for (int i = 0; i < personnel.getFax().size(); i++) {
        gcmdDif.addProperty(p("hasPersonnelFax"), l(personnel.getFax().get(i)));
      }
    }

    // Parameters
    for (Parameters parameter : dif.getParameters()) {
      gcmdDif.addProperty(p("hasParameterCategory"), parameter.getCategory(), "en");
      gcmdDif.addProperty(p("hasParameterTopic"), parameter.getTopic(), "en");
      gcmdDif.addProperty(p("hasParameterTerm"), parameter.getTerm(), "en");
      gcmdDif.addProperty(p("hasParameterVariableLevel1"), parameter.getVariableLevel1(), "en");
      if (parameter.getVariableLevel2() != null) {
        gcmdDif.addProperty(p("hasParameterVariableLevel2"), parameter.getVariableLevel2(), "en");
      }
      if (parameter.getVariableLevel3() != null) {
        gcmdDif.addProperty(p("hasParameterVariableLevel3"), parameter.getVariableLevel3(), "en");
      }
    }

    // Sensor_Name
    for (SensorName sensorName : dif.getSensorName()) {
      gcmdDif.addProperty(p("hasSensorNameShortName"), sensorName.getShortName(), "en");
      gcmdDif.addProperty(p("hasSensorNameLongName"), sensorName.getLongName(), "en");
    }

    // Source_Name
    for (SourceName sourceName : dif.getSourceName()) {
      gcmdDif.addProperty(p("hasSourceNameShortName"), sourceName.getShortName(), "en");
      gcmdDif.addProperty(p("hasSourceNameLongName"), sourceName.getLongName(), "en");
    }

    // Temporal_Coverage
    for (TemporalCoverage temporalCoverage : dif.getTemporalCoverage()) {
      if (temporalCoverage.getStartDate() != null) {
        gcmdDif.addProperty(p("hasTemporalCoverageStartDate"), temporalCoverage.getStartDate(), XSDDatatype.XSDdate);
      }
      if (temporalCoverage.getStopDate() != null) {
        gcmdDif.addProperty(p("hasTemporalCoverageStopDate"), temporalCoverage.getStopDate(), XSDDatatype.XSDdate);
      }
    }

    // Spatial_Coverage
    for (SpatialCoverage spatialCoverage : dif.getSpatialCoverage()) {
      try {
        if (spatialCoverage.getEasternmostLongitude() != null) {
          gcmdDif.addLiteral(p("hasSpatialCoverageEasternmostLongitude"), Float.parseFloat(spatialCoverage.getEasternmostLongitude()));
        }
      } catch (NumberFormatException e) {
        gcmdDif.addLiteral(p("hasSpatialCoverageEasternmostLongitude"), l(spatialCoverage.getEasternmostLongitude()));
      }
      
      if (spatialCoverage.getMaximumAltitude() != null) {
        gcmdDif.addLiteral(p("hasSpatialCoverageMaximumAltitude"), Float.parseFloat(spatialCoverage.getMaximumAltitude()));
      }
      if (spatialCoverage.getMaximumDepth() != null) {
        gcmdDif.addLiteral(p("hasSpatialCoverageMaximumDepth"), Float.parseFloat(spatialCoverage.getMaximumDepth()));
      }
      if (spatialCoverage.getMinimumAltitude() != null) {
        gcmdDif.addLiteral(p("hasSpatialCoverageMinimumAltitude"), Float.parseFloat(spatialCoverage.getMinimumAltitude()));
      }
      if (spatialCoverage.getMinimumDepth() != null) {
        gcmdDif.addLiteral(p("hasSpatialCoverageMinimumDepth"), Float.parseFloat(spatialCoverage.getMinimumDepth()));
      }
      try {
        if (spatialCoverage.getNorthernmostLatitude() != null) {
          gcmdDif.addLiteral(p("hasSpatialCoverageNorthernmostLatitude"), Float.parseFloat(spatialCoverage.getNorthernmostLatitude()));
        }
      } catch (NumberFormatException e) {
        gcmdDif.addLiteral(p("hasSpatialCoverageNorthernmostLatitude"), l(spatialCoverage.getNorthernmostLatitude()));
      }
      try {
        if (spatialCoverage.getSouthernmostLatitude() != null) {
          gcmdDif.addLiteral(p("hasSpatialCoverageSouthernmostLatitude"), Float.parseFloat(spatialCoverage.getSouthernmostLatitude()));
        }
      } catch (NumberFormatException e) {
        gcmdDif.addLiteral(p("hasSpatialCoverageSouthernmostLatitude"), l(spatialCoverage.getSouthernmostLatitude()));
      }
      try {
        if (spatialCoverage.getWesternmostLongitude() != null) {
          gcmdDif.addLiteral(p("hasSpatialCoverageWesternmostLongitude"), Float.parseFloat(spatialCoverage.getWesternmostLongitude()));
        }
      } catch (NumberFormatException e) {
        gcmdDif.addLiteral(p("hasSpatialCoverageWesternmostLongitude"), l(spatialCoverage.getWesternmostLongitude()));
      }
    }

    // Location
    for (Location location : dif.getLocation()) {
      if (location.getDetailedLocation() != null) {
        gcmdDif.addProperty(p("hasLocationDetailedLocation"), location.getDetailedLocation(), "en");
      }
      gcmdDif.addProperty(p("hasLocationLocationCategory"), location.getLocationCategory(), "en");
      if (location.getLocationSubregion1() != null) {
        gcmdDif.addProperty(p("hasLocationLocationSubregion1"), location.getLocationSubregion1(), "en");
      }
      if (location.getLocationSubregion2() != null) {
        gcmdDif.addProperty(p("hasLocationLocationSubregion2"), location.getLocationSubregion2(), "en");
      }
      if (location.getLocationSubregion3() != null) {
        gcmdDif.addProperty(p("hasLocationLocationSubregion3"), location.getLocationSubregion3(), "en");
      }
      gcmdDif.addProperty(p("hasLocationLocationType"), location.getLocationType(), "en");
    }

    // Data_Resolution
    for (DataResolution dataResolution : dif.getDataResolution()) {
      if (dataResolution.getHorizontalResolutionRange() != null) {
        gcmdDif.addProperty(p("hasDataResolutionHorizontalResolutionRange"), dataResolution.getHorizontalResolutionRange(), "en");
      }
      if (dataResolution.getLatitudeResolution() != null) {
        gcmdDif.addLiteral(p("hasDataResolutionLatitudeResolution"), Float.parseFloat(dataResolution.getLatitudeResolution()));
      }
      if (dataResolution.getLongitudeResolution() != null) {
        gcmdDif.addLiteral(p("hasDataResolutionLongitudeResolution"), Float.parseFloat(dataResolution.getLongitudeResolution()));
      }
      if (dataResolution.getTemporalResolution() != null) {
        gcmdDif.addProperty(p("hasDataResolutionTemporalResolution"), dataResolution.getTemporalResolution(), "en");
      }
      if (dataResolution.getTemporalResolutionRange() != null) {
        gcmdDif.addProperty(p("hasDataResolutionTemporalResolutionRange"), dataResolution.getTemporalResolutionRange(), "en");
      }
      if (dataResolution.getVerticalResolution() != null) {
        gcmdDif.addProperty(p("hasDataResolutionVerticalResolution"), dataResolution.getVerticalResolution(), "en");
      }
      if (dataResolution.getVerticalResolutionRange() != null) {
        gcmdDif.addProperty(p("hasDataResolutionVerticalResolutionRange"), dataResolution.getVerticalResolutionRange(), "en");
      }
    }

    // Project
    for (Project project : dif.getProject()) {
      gcmdDif.addProperty(p("hasProjectLongName"), project.getLongName(), "en");
      gcmdDif.addProperty(p("hasProjectShortName"), project.getShortName(), "en");
    }

    // Data_Center
    for (DataCenter dataCenter : dif.getDataCenter()) {
      DataCenterName dataCenterName = dataCenter.getDataCenterName();
      gcmdDif.addProperty(p("hasDataCenterDataCenterNameLongName"), dataCenterName.getLongName(), "en");
      gcmdDif.addProperty(p("hasDataCenterDataCenterNameShortName"), dataCenterName.getShortName(), "en");
      gcmdDif.addProperty(p("hasDataCenterDataCenterURL"), l(dataCenter.getDataCenterURL()));
      List<Personnel> personnelList = dataCenter.getPersonnel();
      for (Personnel personnel : personnelList) {
        gcmdDif.addProperty(p("hasDataCenterPersonnelFirstName"), personnel.getFirstName(), "en");
        gcmdDif.addProperty(p("hasDataCenterPersonnelLastName"), personnel.getLastName(), "en");
        if (personnel.getMiddleName() != null) {
          gcmdDif.addProperty(p("hasDataCenterPersonnelMiddleName"), personnel.getMiddleName(), "en");
        }
        if (personnel.getContactAddress() != null) {
          ContactAddress contactAddress = personnel.getContactAddress();
          gcmdDif.addProperty(p("hasDataCenterPersonnelContactAddressCity"), contactAddress.getCity(), "en");
          gcmdDif.addProperty(p("hasDataCenterPersonnelContactAddressCountry"), contactAddress.getCountry(), "en");
          gcmdDif.addProperty(p("hasDataCenterPersonnelContactAddressPostalCode"), contactAddress.getPostalCode(), "en");
          gcmdDif.addProperty(p("hasDataCenterPersonnelContactAddressProvinceOrState"), contactAddress.getProvinceOrState(), "en");
        }
        if (personnel.getEmail() != null) {
          for (int i = 0; i < personnel.getEmail().size(); i++) {
            gcmdDif.addProperty(p("hasDataCenterPersonnelEmail"), personnel.getEmail().get(i), "en");
          }
        }
        if (personnel.getFax() != null) {
          for (int i = 0; i < personnel.getFax().size(); i++) {
            gcmdDif.addProperty(p("hasDataCenterPersonnelFax"), l(personnel.getFax().get(i)));
          }
        }
        if (personnel.getPhone() != null) {
          for (int i = 0; i < personnel.getPhone().size(); i++) {
            gcmdDif.addProperty(p("hasDataCenterPersonnelPhone"), l(personnel.getPhone().get(i)));
          }
        }
        if (personnel.getRole() != null) {
          for (int i = 0; i < personnel.getRole().size(); i++) {
            gcmdDif.addProperty(p("hasDataCenterPersonnelRole"), personnel.getRole().get(i), "en");
          }
        }
      }
    }

    // Reference
    for (Reference reference : dif.getReference()) {
      gcmdDif.addProperty(p("hasReference"), reference.toString(), "en");
    }

    // Summary
    Summary summary = dif.getSummary();
    if (summary != null) {
      gcmdDif.addProperty(p("hasSummaryAbsract"), summary.getAbstract(), "en");
    }


    // IDN_Node
    for (IDNNode idnNode : dif.getIDNNode()) {
      if (idnNode.getLongName() != null) {
        gcmdDif.addProperty(p("hasIDNNodeLongName"), l(idnNode.getLongName()));
      }
      if (idnNode.getShortName() != null) {
        gcmdDif.addProperty(p("hasIDNNodeShortName"), l(idnNode.getShortName()));
      }
    }

  }

  @SuppressWarnings("unused")
  private static Resource r(String localname) {
    return ResourceFactory.createResource(MUDROD_GCMD_DIF_9_8_2_NS + localname);
  }

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
