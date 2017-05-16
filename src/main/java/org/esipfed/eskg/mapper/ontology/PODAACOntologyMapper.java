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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
      buildIndividual(dif, gcmdDif);
    }
    writeOntologyModelToFile(ontModel);
  }

  private void writeOntologyModelToFile(OntModel ontModel) {
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.getDefault());
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    try (OutputStream fos = new FileOutputStream(String.format(Locale.getDefault(), 
            "podaac_datasets_%s.owl", sdf.format(timestamp)));
            Writer writer = new OutputStreamWriter(fos, Charset.defaultCharset())){
      ontModel.write(writer);
    } catch (IOException e) {
      LOG.error("Error whilst writing Ontology to file.", e);
    }
    LOG.info("Successfully wrote Ontology Model.");
  }

  private void buildIndividual(DIF dif, Individual gcmdDif) {

    //TODO use Data Types within Literal value assignments
    //Entry_ID
    gcmdDif.addProperty(p("hasEntryID"), l(dif.getEntryID()));
    //Entry_Title
    gcmdDif.addProperty(p("hasEntryTitle"), l(dif.getEntryTitle()));
    //ISO_Topic_Category
    for (String isoTopicCategory : dif.getISOTopicCategory()) {
      gcmdDif.addProperty(p("hasISOTopicCategory"), l(isoTopicCategory));
    }
    //Access_Constraints
    gcmdDif.addProperty(p("hasAccessConstraints"), l(dif.getAccessConstraints()));
    //Use_Constraints
    gcmdDif.addProperty(p("hasUseConstraints"), l(dif.getUseConstraints()));
    //Data_Set_Language
    gcmdDif.addProperty(p("hasDataSetLanguage"), l(dif.getDataSetLanguage()));
    //Originating_Center
    gcmdDif.addProperty(p("hasOriginatingCenter"), l(dif.getOriginatingCenter()));
    //Metadata_Name
    gcmdDif.addProperty(p("hasMetadataName"), l(dif.getMetadataName()));
    //Metadata_Version
    gcmdDif.addProperty(p("hasMetadataVersion"), l(dif.getMetadataVersion()));
    //DIF_Creation_Date
    gcmdDif.addProperty(p("hasDIFCreationDate"), l(dif.getDIFCreationDate()));
    //Last_DIF_Revision_Date
    gcmdDif.addProperty(p("hasLastDIFRevisionDate"), l(dif.getLastDIFRevisionDate()));
    //DIF_Revision_History
    gcmdDif.addProperty(p("hasDIFRevisionHistory"), l(dif.getDIFRevisionHistory()));

    //Data_Set_Citation
    for (DataSetCitation dataSetCitation : dif.getDataSetCitation()) {
      gcmdDif.addProperty(p("hasDataSetCitationDatasetCreator"), l(dataSetCitation.getDatasetCreator()));
      gcmdDif.addProperty(p("hasDataSetCitationDatasetTitle"), l(dataSetCitation.getDatasetTitle()));
      gcmdDif.addProperty(p("hasDataSetCitationDatasetSeriesName"), l(dataSetCitation.getDatasetSeriesName()));
      gcmdDif.addProperty(p("hasDataSetCitationDatasetReleaseDate"), l(dataSetCitation.getDatasetReleaseDate()));
      gcmdDif.addProperty(p("hasDataSetCitationDatasetReleasePlace"), l(dataSetCitation.getDatasetReleasePlace()));
      gcmdDif.addProperty(p("hasDataSetCitationDatasetPublisher"), l(dataSetCitation.getDatasetPublisher()));
      gcmdDif.addProperty(p("hasDataSetCitationVersion"), l(dataSetCitation.getVersion()));
      if (dataSetCitation.getOtherCitationDetails() != null) {
        gcmdDif.addProperty(p("hasDataSetCitationOtherCitationDetails"), l(dataSetCitation.getOtherCitationDetails()));
      }
      gcmdDif.addProperty(p("hasDataSetCitationOnlineResource"), l(dataSetCitation.getOnlineResource()));
    }

    //Personnel
    for (Personnel personnel : dif.getPersonnel()) {
      gcmdDif.addProperty(p("hasPersonnelRole"), l(personnel.getRole()));
      gcmdDif.addProperty(p("hasPersonnelFirstName"), l(personnel.getFirstName()));
      gcmdDif.addProperty(p("hasPersonnelLastName"), l(personnel.getLastName()));
      gcmdDif.addProperty(p("hasPersonnelEmail"), l(personnel.getEmail()));
      gcmdDif.addProperty(p("hasPersonnelFax"), l(personnel.getFax()));
    }

    //Parameters
    for (Parameters parameter : dif.getParameters()) {
      gcmdDif.addProperty(p("hasParameterCategory"), l(parameter.getCategory()));
      gcmdDif.addProperty(p("hasParameterTopic"), l(parameter.getTopic()));
      gcmdDif.addProperty(p("hasParameterTerm"), l(parameter.getTerm()));
      gcmdDif.addProperty(p("hasParameterVariableLevel1"), l(parameter.getVariableLevel1()));
      if (parameter.getVariableLevel2() != null) {
        gcmdDif.addProperty(p("hasParameterVariableLevel2"), l(parameter.getVariableLevel2()));
      }
      if (parameter.getVariableLevel3() != null) {
        gcmdDif.addProperty(p("hasParameterVariableLevel3"), l(parameter.getVariableLevel3()));
      }
    }

    //Sensor_Name
    for (SensorName sensorName : dif.getSensorName()) {
      gcmdDif.addProperty(p("hasSensorNameShortName"), l(sensorName.getShortName()));
      gcmdDif.addProperty(p("hasSensorNameLongName"), l(sensorName.getLongName()));
    }

    //Source_Name
    for (SourceName sourceName : dif.getSourceName()) {
      gcmdDif.addProperty(p("hasSourceNameShortName"), l(sourceName.getShortName()));
      gcmdDif.addProperty(p("hasSourceNameLongName"), l(sourceName.getLongName()));
    }

    //Temporal_Coverage
    for (TemporalCoverage temporalCoverage : dif.getTemporalCoverage()) {
      if (temporalCoverage.getStartDate() != null) {
        gcmdDif.addProperty(p("hasTemporalCoverageStartDate"), l(temporalCoverage.getStartDate()));
      }
      if (temporalCoverage.getStopDate() != null) {
        gcmdDif.addProperty(p("hasTemporalCoverageStopDate"), l(temporalCoverage.getStopDate()));
      }
    }

    //Spatial_Coverage
    for (SpatialCoverage spatialCoverage : dif.getSpatialCoverage()) {
      gcmdDif.addProperty(p("hasSpatialCoverageEasternmostLongitude"), l(spatialCoverage.getEasternmostLongitude()));
      if (spatialCoverage.getMaximumAltitude() != null) {
        gcmdDif.addProperty(p("hasSpatialCoverageMaximumAltitude"), l(spatialCoverage.getMaximumAltitude()));
      }
      if (spatialCoverage.getMaximumDepth() != null) {
        gcmdDif.addProperty(p("hasSpatialCoverageMaximumDepth"), l(spatialCoverage.getMaximumDepth()));
      }
      if (spatialCoverage.getMinimumAltitude() != null) {
        gcmdDif.addProperty(p("hasSpatialCoverageMinimumAltitude"), l(spatialCoverage.getMinimumAltitude()));
      }
      if (spatialCoverage.getMinimumDepth() != null) {
        gcmdDif.addProperty(p("hasSpatialCoverageMinimumDepth"), l(spatialCoverage.getMinimumDepth()));
      }
      gcmdDif.addProperty(p("hasSpatialCoverageNorthernmostLatitude"), l(spatialCoverage.getNorthernmostLatitude()));
      gcmdDif.addProperty(p("hasSpatialCoverageSouthernmostLatitude"), l(spatialCoverage.getSouthernmostLatitude()));
      gcmdDif.addProperty(p("hasSpatialCoverageWesternmostLongitude"), l(spatialCoverage.getWesternmostLongitude()));
    }

    //Location
    for (Location location : dif.getLocation()) {
      if (location.getDetailedLocation() != null) {
        gcmdDif.addProperty(p("hasLocationDetailedLocation"), l(location.getDetailedLocation()));
      }
      gcmdDif.addProperty(p("hasLocationLocationCategory"), l(location.getLocationCategory()));
      if (location.getLocationSubregion1() != null) {
        gcmdDif.addProperty(p("hasLocationLocationSubregion1"), l(location.getLocationSubregion1()));
      }
      if (location.getLocationSubregion2() != null) {
        gcmdDif.addProperty(p("hasLocationLocationSubregion2"), l(location.getLocationSubregion2()));
      }
      if (location.getLocationSubregion3() != null) {
        gcmdDif.addProperty(p("hasLocationLocationSubregion3"), l(location.getLocationSubregion3()));
      }
      gcmdDif.addProperty(p("hasLocationLocationType"), l(location.getLocationType()));
    }

    //Data_Resolution
    for (DataResolution dataResolution : dif.getDataResolution()) {
      if (dataResolution.getHorizontalResolutionRange() != null) {
        gcmdDif.addProperty(p("hasDataResolutionHorizontalResolutionRange"), l(dataResolution.getHorizontalResolutionRange()));
      }
      if (dataResolution.getLatitudeResolution() != null) {
        gcmdDif.addProperty(p("hasDataResolutionLatitudeResolution"), l(dataResolution.getLatitudeResolution()));
      }
      if (dataResolution.getLongitudeResolution() != null) {
        gcmdDif.addProperty(p("hasDataResolutionLongitudeResolution"), l(dataResolution.getLongitudeResolution()));
      }
      if (dataResolution.getTemporalResolution() != null) {
        gcmdDif.addProperty(p("hasDataResolutionTemporalResolution"), l(dataResolution.getTemporalResolution()));
      }
      if (dataResolution.getTemporalResolutionRange() != null) {
        gcmdDif.addProperty(p("hasDataResolutionTemporalResolutionRange"), l(dataResolution.getTemporalResolutionRange()));
      }
      if (dataResolution.getVerticalResolution() != null) {
        gcmdDif.addProperty(p("hasDataResolutionVerticalResolution"), l(dataResolution.getVerticalResolution()));
      }
      if (dataResolution.getVerticalResolutionRange() != null) {
        gcmdDif.addProperty(p("hasDataResolutionVerticalResolutionRange"), l(dataResolution.getVerticalResolutionRange()));
      }
    }

    //Project
    for (Project project : dif.getProject()) {
      gcmdDif.addProperty(p("hasProjectLongName"), l(project.getLongName()));
      gcmdDif.addProperty(p("hasProjectShortName"), l(project.getShortName()));
    }

    //Data_Center
    for (DataCenter dataCenter : dif.getDataCenter()) {
      DataCenterName dataCenterName = dataCenter.getDataCenterName();
      gcmdDif.addProperty(p("hasDataCenterDataCenterNameLongName"), l(dataCenterName.getLongName()));
      gcmdDif.addProperty(p("hasDataCenterDataCenterNameShortName"), l(dataCenterName.getShortName()));
      gcmdDif.addProperty(p("hasDataCenterDataCenterURL"), l(dataCenter.getDataCenterURL()));
      List<Personnel> personnelList = dataCenter.getPersonnel();
      for (Personnel personnel : personnelList) {
        gcmdDif.addProperty(p("hasDataCenterPersonnelFirstName"), l(personnel.getFirstName()));
        gcmdDif.addProperty(p("hasDataCenterPersonnelLastName"), l(personnel.getLastName()));
        if (personnel.getMiddleName() != null) {
          gcmdDif.addProperty(p("hasDataCenterPersonnelMiddleName"), l(personnel.getMiddleName()));
        }
        if (personnel.getContactAddress() != null) {
          gcmdDif.addProperty(p("hasDataCenterPersonnelContactAddress"), l(personnel.getContactAddress()));
        }
        if (personnel.getEmail() != null) {
          gcmdDif.addProperty(p("hasDataCenterPersonnelEmail"), l(personnel.getEmail()));
        }
        if (personnel.getFax() != null) {
          gcmdDif.addProperty(p("hasDataCenterPersonnelFax"), l(personnel.getFax()));
        }
        if (personnel.getPhone() != null) {
          gcmdDif.addProperty(p("hasDataCenterPersonnelPhone"), l(personnel.getPhone()));
        }
        if (personnel.getRole() != null) {
          gcmdDif.addProperty(p("hasDataCenterPersonnelRole"), l(personnel.getRole()));
        }
      }
    }

    //Reference
    for (Reference reference : dif.getReference()) {
      gcmdDif.addProperty(p("hasReference"), l(reference.toString()));
    }

    //Summary
    Summary summary = dif.getSummary();
    if (summary != null) {
      for (Iterator iterator = summary.getContent().iterator(); iterator.hasNext();) {
        gcmdDif.addProperty(p("hasSummary"), l(iterator.next()));
      }

    }

    //IDN_Node
    for (IDNNode idnNode : dif.getIDNNode()) {
      if (idnNode.getLongName() != null) {
        gcmdDif.addProperty(p("hasIDNNodeLongName"), l(idnNode.getLongName()));
      }
      if (idnNode.getShortName() != null) {
        gcmdDif.addProperty(p("hasIDNNodeShortName"), l(idnNode.getShortName()));
      }
    }

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
