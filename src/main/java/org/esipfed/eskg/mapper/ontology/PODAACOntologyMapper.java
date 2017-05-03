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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.jena.datatypes.RDFDatatype;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lewismc
 *
 */
public class PODAACOntologyMapper implements ObjectMapper {

  private static final Logger LOG = LoggerFactory.getLogger(PODAACOntologyMapper.class);

  private static final String SWEET_REPR_DATA_PRODUCT = 
          "https://sweet.jpl.nasa.gov/2.3/reprDataProduct.owl";
  private static final String SWEET_REPR_DATA_PRODUCT_NS = SWEET_REPR_DATA_PRODUCT + "#";
  private static final String MUDROD_GCMD_DIF_9_8_2 = 
          "https://raw.githubusercontent.com/mudrod/mudrod_ontologies/master/dif_v9.8.2.owl";
  private static final String MUDROD_GCMD_DIF_9_8_2_NS = MUDROD_GCMD_DIF_9_8_2 + "#";
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
  public void map(List<DIF> pojoList) {
    // create the base model
    OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    ontModel.read(SWEET_REPR_DATA_PRODUCT, "RDF/XML");

    // get the reprDataProduct.owl#Dataset class reference
    Resource dataset = ontModel.getResource(SWEET_REPR_DATA_PRODUCT_NS + "Dataset");
    // create the reprDataProduct.owl#PODAACDataset class reference
    OntClass podaacDataset = ontModel.createClass(SWEET_REPR_DATA_PRODUCT_NS + "PODAACDataset");
    // make reprDataProduct.owl#PODAACDataset a subclass of reprDataProduct.owl#Dataset
    podaacDataset.addSuperClass(dataset);
    // create an individual for each DIF POJO with the  
    for (DIF dif : pojoList) {
      Individual gcmdDif = podaacDataset.createIndividual(SWEET_REPR_DATA_PRODUCT_NS + dif.getEntryID());
      buildIndividual(dif, gcmdDif, ontModel);
    }
    writeOntologyModelToFile(ontModel);
  }

  private void writeOntologyModelToFile(OntModel ontModel) {
    try (OutputStream fos = new FileOutputStream(String.format(Locale.getDefault(), 
            "podaac_datasets_%s.owl", new Date(System.currentTimeMillis())));
            Writer writer = new OutputStreamWriter(fos, Charset.defaultCharset())){
      ontModel.write(writer);
    } catch (IOException e) {
      LOG.error("Error whilst writing Ontology to file.", e);
    }
    LOG.info("Successfully wrote Ontology Model.");
  }

  private void buildIndividual(DIF dif, Individual gcmdDif, OntModel ontModel) {

    //TODO use Data Types within Literal value assignments
    //Entry_ID
    gcmdDif.addLiteral(p("hasEntryID"), l(dif.getEntryID()));
    //Entry_Title
    gcmdDif.addLiteral(p("hasEntryTitle"), l(dif.getEntryTitle()));
    //ISO_Topic_Category
    for (String isoTopicCategory : dif.getISOTopicCategory()) {
      gcmdDif.addLiteral(p("hasISOTopicCategory"), l(isoTopicCategory));
    }
    //Access_Constraints
    gcmdDif.addLiteral(p("hasAccessConstraints"), l(dif.getAccessConstraints()));
    //Use_Constraints
    gcmdDif.addLiteral(p("hasUseConstraints"), l(dif.getUseConstraints()));
    //Data_Set_Language
    gcmdDif.addLiteral(p("hasDataSetLanguage"), l(dif.getDataSetLanguage()));
    //Originating_Center
    gcmdDif.addLiteral(p("hasOriginatingCenter"), l(dif.getOriginatingCenter()));
    //Metadata_Name
    gcmdDif.addLiteral(p("hasMetadataName"), l(dif.getMetadataName()));
    //Metadata_Version
    gcmdDif.addLiteral(p("hasMetadataVersion"), l(dif.getMetadataVersion()));
    //DIF_Creation_Date
    gcmdDif.addLiteral(p("hasDIFCreationDate"), l(dif.getDIFCreationDate()));
    //Last_DIF_Revision_Date
    gcmdDif.addLiteral(p("hasLastDIFRevisionDate"), l(dif.getLastDIFRevisionDate()));
    //DIF_Revision_History
    gcmdDif.addLiteral(p("hasDIFRevisionHistory"), l(dif.getDIFRevisionHistory()));

    //Data_Set_Citation
    for (DataSetCitation dataSetCitation : dif.getDataSetCitation()) {
      gcmdDif.addLiteral(p("hasDataSetCitationDatasetCreator"), l(dataSetCitation.getDatasetCreator()));
      gcmdDif.addLiteral(p("hasDataSetCitationDatasetTitle"), l(dataSetCitation.getDatasetTitle()));
      gcmdDif.addLiteral(p("hasDataSetCitationDatasetSeriesName"), l(dataSetCitation.getDatasetSeriesName()));
      gcmdDif.addLiteral(p("hasDataSetCitationDatasetReleaseDate"), l(dataSetCitation.getDatasetReleaseDate()));
      gcmdDif.addLiteral(p("hasDataSetCitationDatasetReleasePlace"), l(dataSetCitation.getDatasetReleasePlace()));
      gcmdDif.addLiteral(p("hasDataSetCitationDatasetPublisher"), l(dataSetCitation.getDatasetPublisher()));
      gcmdDif.addLiteral(p("hasDataSetCitationVersion"), l(dataSetCitation.getVersion()));
      if (dataSetCitation.getOtherCitationDetails() != null) {
        gcmdDif.addLiteral(p("hasDataSetCitationOtherCitationDetails"), l(dataSetCitation.getOtherCitationDetails()));
      }
      gcmdDif.addLiteral(p("hasDataSetCitationOnlineResource"), l(dataSetCitation.getOnlineResource()));
    }

    //Personnel
    for (Personnel personnel : dif.getPersonnel()) {
      gcmdDif.addLiteral(p("hasPersonnelRole"), l(personnel.getRole()));
      gcmdDif.addLiteral(p("hasPersonnelFirstName"), l(personnel.getFirstName()));
      gcmdDif.addLiteral(p("hasPersonnelLastName"), l(personnel.getLastName()));
      gcmdDif.addLiteral(p("hasPersonnelEmail"), l(personnel.getEmail()));
      gcmdDif.addLiteral(p("hasPersonnelFax"), l(personnel.getFax()));
    }

    //Parameters
    for (Parameters parameter : dif.getParameters()) {
      gcmdDif.addLiteral(p("hasParameterCategory"), l(parameter.getCategory()));
      gcmdDif.addLiteral(p("hasParameterTopic"), l(parameter.getTopic()));
      gcmdDif.addLiteral(p("hasParameterTerm"), l(parameter.getTerm()));
      gcmdDif.addLiteral(p("hasParameterVariableLevel1"), l(parameter.getVariableLevel1()));
      if (parameter.getVariableLevel2() != null) {
        gcmdDif.addLiteral(p("hasParameterVariableLevel2"), l(parameter.getVariableLevel2()));
      }
      if (parameter.getVariableLevel3() != null) {
        gcmdDif.addLiteral(p("hasParameterVariableLevel3"), l(parameter.getVariableLevel3()));
      }
    }

    //Sensor_Name
    for (SensorName sensorName : dif.getSensorName()) {
      gcmdDif.addLiteral(p("hasSensorNameShortName"), l(sensorName.getShortName()));
      gcmdDif.addLiteral(p("hasSensorNameLongName"), l(sensorName.getLongName()));
    }

    //Source_Name
    for (SourceName sourceName : dif.getSourceName()) {
      gcmdDif.addLiteral(p("hasSourceNameShortName"), l(sourceName.getShortName()));
      gcmdDif.addLiteral(p("hasSourceNameLongName"), l(sourceName.getLongName()));
    }

    //Temporal_Coverage
    for (TemporalCoverage temporalCoverage : dif.getTemporalCoverage()) {
      if (temporalCoverage.getStartDate() != null) {
        gcmdDif.addLiteral(p("hasTemporalCoverageStartDate"), l(temporalCoverage.getStartDate()));
      }
      if (temporalCoverage.getStopDate() != null) {
        gcmdDif.addLiteral(p("hasTemporalCoverageStopDate"), l(temporalCoverage.getStopDate()));
      }
    }

    //Spatial_Coverage
    for (SpatialCoverage spatialCoverage : dif.getSpatialCoverage()) {
      gcmdDif.addLiteral(p("hasSpatialCoverageEasternmostLongitude"), l(spatialCoverage.getEasternmostLongitude()));
      if (spatialCoverage.getMaximumAltitude() != null) {
        gcmdDif.addLiteral(p("hasSpatialCoverageMaximumAltitude"), l(spatialCoverage.getMaximumAltitude()));
      }
      if (spatialCoverage.getMaximumDepth() != null) {
        gcmdDif.addLiteral(p("hasSpatialCoverageMaximumDepth"), l(spatialCoverage.getMaximumDepth()));
      }
      if (spatialCoverage.getMinimumAltitude() != null) {
        gcmdDif.addLiteral(p("hasSpatialCoverageMinimumAltitude"), l(spatialCoverage.getMinimumAltitude()));
      }
      if (spatialCoverage.getMinimumDepth() != null) {
        gcmdDif.addLiteral(p("hasSpatialCoverageMinimumDepth"), l(spatialCoverage.getMinimumDepth()));
      }
      gcmdDif.addLiteral(p("hasSpatialCoverageNorthernmostLatitude"), l(spatialCoverage.getNorthernmostLatitude()));
      gcmdDif.addLiteral(p("hasSpatialCoverageSouthernmostLatitude"), l(spatialCoverage.getSouthernmostLatitude()));
      gcmdDif.addLiteral(p("hasSpatialCoverageWesternmostLongitude"), l(spatialCoverage.getWesternmostLongitude()));
    }

    //Location
    for (Location location : dif.getLocation()) {
      if (location.getDetailedLocation() != null) {
        gcmdDif.addLiteral(p("hasLocationDetailedLocation"), l(location.getDetailedLocation()));
      }
      gcmdDif.addLiteral(p("hasLocationLocationCategory"), l(location.getLocationCategory()));
      if (location.getLocationSubregion1() != null) {
        gcmdDif.addLiteral(p("hasLocationLocationSubregion1"), l(location.getLocationSubregion1()));
      }
      if (location.getLocationSubregion2() != null) {
        gcmdDif.addLiteral(p("hasLocationLocationSubregion2"), l(location.getLocationSubregion2()));
      }
      if (location.getLocationSubregion3() != null) {
        gcmdDif.addLiteral(p("hasLocationLocationSubregion3"), l(location.getLocationSubregion3()));
      }
      gcmdDif.addLiteral(p("hasLocationLocationType"), l(location.getLocationType()));
    }

    //Data_Resolution
    for (DataResolution dataResolution : dif.getDataResolution()) {
      if (dataResolution.getHorizontalResolutionRange() != null) {
        gcmdDif.addLiteral(p("hasDataResolutionHorizontalResolutionRange"), l(dataResolution.getHorizontalResolutionRange()));
      }
      if (dataResolution.getLatitudeResolution() != null) {
        gcmdDif.addLiteral(p("hasDataResolutionLatitudeResolution"), l(dataResolution.getLatitudeResolution()));
      }
      if (dataResolution.getLongitudeResolution() != null) {
        gcmdDif.addLiteral(p("hasDataResolutionLongitudeResolution"), l(dataResolution.getLongitudeResolution()));
      }
      if (dataResolution.getTemporalResolution() != null) {
        gcmdDif.addLiteral(p("hasDataResolutionTemporalResolution"), l(dataResolution.getTemporalResolution()));
      }
      if (dataResolution.getTemporalResolutionRange() != null) {
        gcmdDif.addLiteral(p("hasDataResolutionTemporalResolutionRange"), l(dataResolution.getTemporalResolutionRange()));
      }
      if (dataResolution.getVerticalResolution() != null) {
        gcmdDif.addLiteral(p("hasDataResolutionVerticalResolution"), l(dataResolution.getVerticalResolution()));
      }
      if (dataResolution.getVerticalResolutionRange() != null) {
        gcmdDif.addLiteral(p("hasDataResolutionVerticalResolutionRange"), l(dataResolution.getVerticalResolutionRange()));
      }
    }

    //Project
    for (Project project : dif.getProject()) {
      gcmdDif.addLiteral(p("hasProjectLongName"), l(project.getLongName()));
      gcmdDif.addLiteral(p("hasProjectShortName"), l(project.getShortName()));
    }

    //Data_Center
    for (DataCenter dataCenter : dif.getDataCenter()) {
      DataCenterName dataCenterName = dataCenter.getDataCenterName();
      gcmdDif.addLiteral(p("hasDataCenterDataCenterNameLongName"), l(dataCenterName.getLongName()));
      gcmdDif.addLiteral(p("hasDataCenterDataCenterNameShortName"), l(dataCenterName.getShortName()));
      gcmdDif.addLiteral(p("hasDataCenterDataCenterURL"), l(dataCenter.getDataCenterURL()));
      List<Personnel> personnelList = dataCenter.getPersonnel();
      for (Personnel personnel : personnelList) {
        gcmdDif.addLiteral(p("hasDataCenterPersonnelFirstName"), l(personnel.getFirstName()));
        gcmdDif.addLiteral(p("hasDataCenterPersonnelLastName"), l(personnel.getLastName()));
        if (personnel.getMiddleName() != null) {
          gcmdDif.addLiteral(p("hasDataCenterPersonnelMiddleName"), l(personnel.getMiddleName()));
        }
        if (personnel.getContactAddress() != null) {
          gcmdDif.addLiteral(p("hasDataCenterPersonnelContactAddress"), l(personnel.getContactAddress()));
        }
        if (personnel.getEmail() != null) {
          gcmdDif.addLiteral(p("hasDataCenterPersonnelEmail"), l(personnel.getEmail()));
        }
        if (personnel.getFax() != null) {
          gcmdDif.addLiteral(p("hasDataCenterPersonnelFax"), l(personnel.getFax()));
        }
        if (personnel.getPhone() != null) {
          gcmdDif.addLiteral(p("hasDataCenterPersonnelPhone"), l(personnel.getPhone()));
        }
        if (personnel.getRole() != null) {
          gcmdDif.addLiteral(p("hasDataCenterPersonnelRole"), l(personnel.getRole()));
        }
      }
    }

    //Reference
    for (Reference reference : dif.getReference()) {
      gcmdDif.addLiteral(p("hasReference"), l(reference.toString()));
    }

    //Summary
    Summary summary = dif.getSummary();
    if (summary != null) {
      for (Iterator iterator = summary.getContent().iterator(); iterator.hasNext();) {
        //type type = (type) iterator.next();
        gcmdDif.addLiteral(p("hasSummary"), l(iterator.next()));
      }
      
    }

    //IDN_Node
    for (IDNNode idnNode : dif.getIDNNode()) {
      if (idnNode.getLongName() != null) {
        gcmdDif.addLiteral(p("hasIDNNodeLongName"), l(idnNode.getLongName()));
      }
      if (idnNode.getShortName() != null) {
        gcmdDif.addLiteral(p("hasIDNNodeShortName"), l(idnNode.getShortName()));
      }
    }

    // create the property references
    // the first grouping below only have one value, all others may have
    // more than one value.
    //ObjectProperty hasEntryId = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Entry_ID");
    //    ObjectProperty hasEntryTitle = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Entry_Title");
    //    ObjectProperty hasAccessConstraints = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Access_Constraints");
    //    ObjectProperty hasUseConstraints = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Use_Constraints");
    //    ObjectProperty hasDataSetLanguage = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Data_Set_Language");
    //    ObjectProperty hasOriginatingCenter = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Originating_Center");
    //    ObjectProperty hasMetadataName = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Metadata_Name");
    //    ObjectProperty hasMetadataVersion = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Metadata_Version");
    //    ObjectProperty hasDIFCreationDate = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "DIF_Creation_Date");
    //    ObjectProperty hasLastDIFRevisionDate = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Last_DIF_Revision_Date");
    //    ObjectProperty hasDIFRevisionHistory = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "DIF_Revision_History");
    //
    //    //Data_Set_Citation
    //    ObjectProperty hasDataSetCitationDatasetCreator = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Dataset_Creator");
    //    ObjectProperty hasDataSetCitationDatasetTitle = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Dataset_Title");
    //    ObjectProperty hasDataSetCitationDatasetSeriesName = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Dataset_Series_Name");
    //    ObjectProperty hasDataSetCitationDatasetReleaseDate = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Dataset_Release_Date");
    //    ObjectProperty hasDataSetCitationDatasetReleasePlace = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Dataset_Release_Place");
    //    ObjectProperty hasDataSetCitationDatasetPublisher = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Dataset_Publisher");
    //    ObjectProperty hasDataSetCitationVersion = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Version");
    //    ObjectProperty hasDataSetCitationOtherCitation_Details = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Other_Citation_Details");
    //    ObjectProperty hasDataSetCitationOnlineResource = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Online_Resource");
    //
    //    //Personnel
    //    ObjectProperty hasPersonnelRole = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Role");
    //    ObjectProperty hasPersonnelFirstName = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "First_Name");
    //    ObjectProperty hasPersonnelLastName = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Last_Name");
    //    ObjectProperty hasPersonnelEmail = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Email");
    //
    //    //Parameters
    //    ObjectProperty hasParametersCategory = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Category");
    //    ObjectProperty hasParametersTopic = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Topic");
    //    ObjectProperty hasParametersTerm = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Term");
    //    ObjectProperty hasParametersVariableLevel1 = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Variable_Level_1");
    //    ObjectProperty hasParametersVariableLevel2 = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Variable_Level_2");
    //    ObjectProperty hasParametersVariableLevel3 = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Variable_Level_3");
    //
    //    //ISO_Topic_Category
    //    ObjectProperty hasISOTopicCategory = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "ISO_Topic_Category");
    //
    //    //Sensor_Name
    //    ObjectProperty hasSensorNameShortName = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Short_Name");
    //    ObjectProperty hasSensorNameLongName = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Long_Name");
    //
    //    //Source_Name
    //    ObjectProperty hasSourceNameShortName = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Short_Name");
    //    ObjectProperty hasSourceNameLongName = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Long_Name");
    //
    //    //Temporal_Coverage
    //    ObjectProperty hasTemporalCoverageStartDate = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Start_Date");
    //    ObjectProperty hasTemporalCoverageStopDate = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Stop_Date");
    //
    //    //Spatial_Coverage
    //    ObjectProperty hasSpatialCoverageSouthernmostLatitude = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Southernmost_Latitude");
    //    ObjectProperty hasSpatialCoverageNorthernmostLatitude = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Northernmost_Latitude");
    //    ObjectProperty hasSpatialCoverageWesternmostLongitude = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Westernmost_Longitude");
    //    ObjectProperty hasSpatialCoverageEasternmostLongitude = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Easternmost_Longitude");
    //    ObjectProperty hasSpatialCoverageMinimumAltitude = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Minimum_Altitude");
    //    ObjectProperty hasSpatialCoverageMaximumAltitude = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Maximum_Altitude");
    //    ObjectProperty hasSpatialCoverageMinimumDepth = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Minimum_Depth");
    //    ObjectProperty hasSpatialCoverageMaximumDepth = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Maximum_Depth");
    //
    //    //Location
    //    ObjectProperty hasLocationLocationCategory = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Location_Category");
    //    ObjectProperty hasLocationLocationType = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Location_Type");
    //
    //    //Data_Resolution
    //    ObjectProperty hasDataResolutionLatitudeResolution = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Latitude_Resolution");
    //    ObjectProperty hasDataResolutionLongitudeResolution = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Longitude_Resolution");
    //    ObjectProperty hasDataResolutionTemporalResolution = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Temporal_Resolution");
    //
    //    //Project
    //    ObjectProperty hasProjectShortName = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Short_Name");
    //    ObjectProperty hasProjectLongName = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Long_Name");
    //
    //    //Data_Center
    //    /////////////
    //    //Data_Center_Name
    //    ObjectProperty hasDataCenterDataCenterNameShortName = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Short_Name");
    //    ObjectProperty hasDataCenterDataCenterNameLongName = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Long_Name");
    //    //Data_Center_URL
    //    ObjectProperty hasDataCenterDataCenterURL = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Data_Center_URL");
    //    //Personnel
    //    ObjectProperty hasDataCenterPersonnelRole = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Role");
    //    ObjectProperty hasDataCenterPersonnelFirstName = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "First_Name");
    //    ObjectProperty hasDataCenterPersonnelLastName = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Last_Name");
    //    ObjectProperty hasDataCenterPersonnelEmail = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Email");
    //
    //    //Reference
    //    ObjectProperty hasReference = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Reference");
    //
    //    //Summary
    //    ObjectProperty hasSummaryAbstract = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Abstract");
    //
    //    //IDN_Node
    //    ObjectProperty hasIDNNodeshortName = ontModel.createObjectProperty(MUDROD_GCMD_DIF_v9_8_2_NS + "Short_Name");

  }

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

  private static Literal l(String lexicalform, RDFDatatype datatype) {
    return ResourceFactory.createTypedLiteral(lexicalform, datatype);
  }

  public static void main(String[] args) {
    List<DIF> difList = new ArrayList<>();
    DIF dif = new DIF();
    dif.setEntryID("Sample_Entry_ID");
    difList.add(dif);
    PODAACOntologyMapper mapper = new PODAACOntologyMapper();
    mapper.map(difList);
  }
}
