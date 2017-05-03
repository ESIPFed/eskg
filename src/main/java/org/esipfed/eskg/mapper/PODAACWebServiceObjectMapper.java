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

import org.esipfed.eskg.structures.DIF;
import org.esipfed.eskg.structures.DataCenter;
import org.esipfed.eskg.structures.DataCenterName;
import org.esipfed.eskg.structures.DataResolution;
import org.esipfed.eskg.structures.DataSetCitation;
import org.esipfed.eskg.structures.IDNNode;
import org.esipfed.eskg.structures.Location;
import org.esipfed.eskg.structures.ObjectFactory;
import org.esipfed.eskg.structures.Parameters;
import org.esipfed.eskg.structures.Personnel;
import org.esipfed.eskg.structures.Project;
import org.esipfed.eskg.structures.SensorName;
import org.esipfed.eskg.structures.SourceName;
import org.esipfed.eskg.structures.SpatialCoverage;
import org.esipfed.eskg.structures.Summary;
import org.esipfed.eskg.structures.TemporalCoverage;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class contains functionality for mapping all {@link ByteArrayInputStream}'s
 * generated via {@link org.esipfed.eskg.aquisition.PODAACWebServiceClient}
 * to POJO's. The structre for all POJO's is contained within the 
 * <b>org.esipfed.eskg.structures</b> package.
 */
public class PODAACWebServiceObjectMapper implements ObjectMapper {

  private static final Logger LOG = LoggerFactory.getLogger(PODAACWebServiceObjectMapper.class);

  /**
   * Default constructor
   */
  public PODAACWebServiceObjectMapper() {
    //default constructor
  }

  /**
   * @see org.esipfed.eskg.mapper.ObjectMapper#map(java.lang.String, java.io.ByteArrayInputStream)
   */
  @Override
  public Object map(String mapperId, ByteArrayInputStream inputStream) {
    Object mappedPOJO = null;
    if (mapperId.equals(MapperID.PODAAC_GCMD.name())) {
      mappedPOJO = mapGCMDXMLToPOJO(inputStream);
    } else {
      LOG.error("No object mapper id found for: {}.", mapperId);
    }
    return mappedPOJO;
  }

  private DIF mapGCMDXMLToPOJO(ByteArrayInputStream gcmdByteArrayInputStream) {
    //create DIF
    DIF dif = new DIF();
    try {
      SAXBuilder jdomBuilder = new SAXBuilder();

      // jdomDocument is the JDOM2 Object
      Document jdomDocument = jdomBuilder.build(gcmdByteArrayInputStream);
      Element difElement = jdomDocument.getRootElement();
      Namespace ns = difElement.getNamespace();
      //populate immediate children
      dif.setEntryID(difElement.getChild("Entry_ID", ns).getTextTrim());
      dif.setEntryTitle(difElement.getChild("Entry_Title", ns).getTextTrim());
      dif.setAccessConstraints(difElement.getChild("Access_Constraints", ns).getTextTrim());
      dif.setUseConstraints(difElement.getChild("Use_Constraints", ns).getTextTrim());
      dif.getDataSetLanguage().add(difElement.getChild("Data_Set_Language", ns).getTextTrim());
      if (difElement.getChild("Originating_Center", ns).getTextTrim() != null) {
        dif.setOriginatingCenter(difElement.getChild("Originating_Center", ns).getTextTrim());
      }
      dif.setMetadataName(difElement.getChild("Metadata_Name", ns).getTextTrim());
      dif.setMetadataVersion(difElement.getChild("Metadata_Version", ns).getTextTrim());
      dif.setDIFCreationDate(difElement.getChild("DIF_Creation_Date", ns).getTextTrim());
      dif.setLastDIFRevisionDate(difElement.getChild("Last_DIF_Revision_Date", ns).getTextTrim());
      dif.setDIFRevisionHistory(difElement.getChild("DIF_Revision_History", ns).getTextTrim());

      //create ISO_Topic_Catgeory(s)
      List<Element> iSOTopicCategoryList = difElement.getChildren("ISO_Topic_Category", ns);
      for (Element iSOTopicCategory : iSOTopicCategoryList) {
        dif.getISOTopicCategory().add(iSOTopicCategory.getTextTrim());
      }

      //create Data_Set_Citation(s)
      List<Element> dataSetCitationList = difElement.getChildren("Data_Set_Citation", ns);
      for (Element dataSetCitationElement : dataSetCitationList) {
        List<Element> dataSetCitationElementChildren = dataSetCitationElement.getChildren();
        DataSetCitation dataSetCitation = new DataSetCitation();
        for (Element element : dataSetCitationElementChildren) {
          if ("Dataset_Creator".equals(element.getName())) {
            dataSetCitation.setDatasetCreator(element.getTextTrim());
          } else if ("Dataset_Title".equals(element.getName())) {
            dataSetCitation.setDatasetTitle(element.getTextTrim());
          } else if ("Dataset_Series_Name".equals(element.getName())) {
            dataSetCitation.setDatasetSeriesName(element.getTextTrim());
          } else if ("Dataset_Release_Date".equals(element.getName())) {
            dataSetCitation.setDatasetReleaseDate(element.getTextTrim());
          } else if ("Dataset_Release_Place".equals(element.getName())) {
            dataSetCitation.setDatasetReleasePlace(element.getTextTrim());
          } else if ("Dataset_Publisher".equals(element.getName())) {
            dataSetCitation.setDatasetPublisher(element.getTextTrim());
          } else if ("Version".equals(element.getName())) {
            dataSetCitation.setVersion(element.getTextTrim());
          } else if ("Online_Resource".equals(element.getName())) {
            dataSetCitation.setOnlineResource(element.getTextTrim());
          }
        }
        dif.getDataSetCitation().add(dataSetCitation);
      }

      //create Personnel(s)
      List<Element> personnelList = difElement.getChildren("Personnel", ns);
      for (Element personnelElement : personnelList) {
        List<Element> personnelElementChildren = personnelElement.getChildren();
        Personnel personnel = new Personnel();
        for (Element element : personnelElementChildren) {
          if ("Role".equals(element.getName())) {
            personnel.getRole().add(element.getTextTrim());
          } else if ("First_Name".equals(element.getName())) {
            personnel.setFirstName(element.getTextTrim());
          } else if ("Last_Name".equals(element.getName())) {
            personnel.setLastName(element.getTextTrim());
          } else if ("Email".equals(element.getName())) {
            personnel.getEmail().add(element.getTextTrim());
          }
        }
        dif.getPersonnel().add(personnel);
      }

      //create Parameters(s)
      List<Element> parameterList = difElement.getChildren("Parameters", ns);
      for (Element parameterElement : parameterList) {
        List<Element> parameterElementChildren = parameterElement.getChildren();
        Parameters parameters = new Parameters();
        for (Element element : parameterElementChildren) {
          if ("Category".equals(element.getName())) {
            parameters.setCategory(element.getTextTrim());
          } else if ("Topic".equals(element.getName())) {
            parameters.setTopic(element.getTextTrim());
          } else if ("Detailed_Variable".equals(element.getName())) {
            parameters.setDetailedVariable(element.getTextTrim());
          } else if ("Term".equals(element.getName())) {
            parameters.setTerm(element.getTextTrim());
          } else if ("Variable_Level_1".equals(element.getName())) {
            parameters.setVariableLevel1(element.getTextTrim());
          } else if ("Variable_Level_2".equals(element.getName())) {
            parameters.setVariableLevel2(element.getTextTrim());
          } else if ("Variable_Level_3".equals(element.getName())) {
            parameters.setVariableLevel3(element.getTextTrim());
          }
        }
        dif.getParameters().add(parameters);
      }

      //create Sensor_Name(s)
      List<Element> sensorNameList = difElement.getChildren("Sensor_Name", ns);
      for (Element sensorNameElement : sensorNameList) {
        List<Element> sensorNameElementChildren = sensorNameElement.getChildren();
        SensorName sensorName = new SensorName();
        for (Element element : sensorNameElementChildren) {
          if ("Short_Name".equals(element.getName())) {
            sensorName.setShortName(element.getTextTrim());
          } else if ("Long_Name".equals(element.getName())) {
            sensorName.setLongName(element.getTextTrim());
          }
        }
        dif.getSensorName().add(sensorName);
      }

      //create Source_Name(s)
      List<Element> sourceNameList = difElement.getChildren("Source_Name", ns);
      for (Element sourceNameElement : sourceNameList) {
        List<Element> sourceNameElementChildren = sourceNameElement.getChildren();
        SourceName sourceName = new SourceName();
        for (Element element : sourceNameElementChildren) {
          if ("Short_Name".equals(element.getName())) {
            sourceName.setShortName(element.getTextTrim());
          } else if ("Long_Name".equals(element.getName())) {
            sourceName.setLongName(element.getTextTrim());
          }
        }
        dif.getSourceName().add(sourceName);
      }

      //create Temporal_Coverage(s)
      List<Element> temporalCoverageList = difElement.getChildren("Temporal_Coverage", ns);
      for (Element temporalCoverageElement : temporalCoverageList) {
        List<Element> temporalCoverageElementChildren = temporalCoverageElement.getChildren();
        TemporalCoverage temporalCoverage = new TemporalCoverage();
        for (Element element : temporalCoverageElementChildren) {
          if ("Start_Date".equals(element.getName())) {
            temporalCoverage.setStartDate(element.getTextTrim());
          } else if ("Stop_Date".equals(element.getName())){
            temporalCoverage.setStopDate(element.getTextTrim());
          }
        }
        dif.getTemporalCoverage().add(temporalCoverage);
      }

      //create Spatial_Coverage(s)
      List<Element> spatialCoverageList = difElement.getChildren("Spatial_Coverage", ns);
      for (Element spatiaCoverageElement : spatialCoverageList) {
        List<Element> spatiallCoverageElementChildren = spatiaCoverageElement.getChildren();
        SpatialCoverage spatialCoverage = new SpatialCoverage();
        for (Element element : spatiallCoverageElementChildren) {
          if ("Easternmost_Longitude".equals(element.getName())) {
            spatialCoverage.setEasternmostLongitude(element.getTextTrim());
          } else if ("Maximum_Altitude".equals(element.getName())) {
            spatialCoverage.setMaximumAltitude(element.getTextTrim());
          } else if ("Maximum_Depth".equals(element.getName())) {
            spatialCoverage.setMaximumDepth(element.getTextTrim());
          } else if ("Minimum_Altitude".equals(element.getName())) {
            spatialCoverage.setMinimumAltitude(element.getTextTrim());
          } else if ("Minimum_Depth".equals(element.getName())) {
            spatialCoverage.setMinimumDepth(element.getTextTrim());
          } else if ("Northernmost_Latitude".equals(element.getName())) {
            spatialCoverage.setNorthernmostLatitude(element.getTextTrim());
          } else if ("Southernmost_Latitude".equals(element.getName())) {
            spatialCoverage.setSouthernmostLatitude(element.getTextTrim());
          } else if ("Westernmost_Longitude".equals(element.getName())) {
            spatialCoverage.setWesternmostLongitude(element.getTextTrim());
          }
        }
        dif.getSpatialCoverage().add(spatialCoverage);
      }

      //create Location(s)
      List<Element> locationList = difElement.getChildren("Location", ns);
      for (Element locationElement : locationList) {
        List<Element> locationElementChildren = locationElement.getChildren();
        Location location = new Location();
        for (Element element : locationElementChildren) {
          if ("Detailed_Location".equals(element.getName())) {
            location.setDetailedLocation(element.getTextTrim());
          } else if ("Location_Category".equals(element.getName())) {
            location.setLocationCategory(element.getTextTrim());
          } else if ("Location_Subregion_1".equals(element.getName())) {
            location.setLocationSubregion1(element.getTextTrim());
          } else if ("Location_Subregion_2".equals(element.getName())) {
            location.setLocationSubregion2(element.getTextTrim());
          } else if ("Location_Subregion_3".equals(element.getName())) {
            location.setLocationSubregion3(element.getTextTrim());
          } else if ("Location_Type".equals(element.getName())) {
            location.setLocationType(element.getTextTrim());
          }
        }
        dif.getLocation().add(location);
      }

      //create Data_Resolution(s)
      List<Element> dataResolutionList = difElement.getChildren("Data_Resolution", ns);
      for (Element dataResolutionElement : dataResolutionList) {
        List<Element> dataResolutionElementChildren = dataResolutionElement.getChildren();
        DataResolution dataResolution = new DataResolution();
        for (Element element : dataResolutionElementChildren) {
          if ("Horizontal_Resolution_Range".equals(element.getName())) {
            dataResolution.setHorizontalResolutionRange(element.getTextTrim());
          } else if ("Latitude_Resolution".equals(element.getName())) {
            dataResolution.setLatitudeResolution(element.getTextTrim());
          } else if ("Longitude_Resolution".equals(element.getName())) {
            dataResolution.setLongitudeResolution(element.getTextTrim());
          } else if ("Temporal_Resolution".equals(element.getName())) {
            dataResolution.setTemporalResolution(element.getTextTrim());
          } else if ("Temporal_Resolution_Range".equals(element.getName())) {
            dataResolution.setTemporalResolutionRange(element.getTextTrim());
          } else if ("Vertical_Resolution".equals(element.getName())) {
            dataResolution.setVerticalResolution(element.getTextTrim());
          } else if ("Vertical_Resolution_Range".equals(element.getName())) {
            dataResolution.setVerticalResolutionRange(element.getTextTrim());
          }
        }
        dif.getDataResolution().add(dataResolution);
      }

      //create Project(s)
      List<Element> projectList = difElement.getChildren("Project", ns);
      for (Element projectElement : projectList) {
        List<Element> projectElementChildren = projectElement.getChildren();
        Project project = new Project();
        for (Element element : projectElementChildren) {
          if ("Long_Name".equals(element.getName())) {
            project.setLongName(element.getTextTrim());
          } else if ("Short_Name".equals(element.getName())){
            project.setShortName(element.getTextTrim());
          }
        }
        dif.getProject().add(project);
      }

      //create Data_Center(s)
      List<Element> dataCenterList = difElement.getChildren("Data_Center", ns);
      for (Element dataCenterElement : dataCenterList) {
        List<Element> dataCenterElementChildren = dataCenterElement.getChildren();
        DataCenter dataCenter = new DataCenter();
        for (Element dataCenterElementChild : dataCenterElementChildren) {
          if ("Data_Center_Name".equals(dataCenterElementChild.getName())) {
            //create Data_Center_Name
            DataCenterName dataCenterName = new DataCenterName();
            List<Element> dataCenterNameChildren = dataCenterElementChild.getChildren();
            for (Element dataCenterNameChild : dataCenterNameChildren) {
              if ("Long_Name".equals(dataCenterNameChild.getName())) {
                dataCenterName.setLongName(dataCenterNameChild.getTextTrim());
              } else if ("Short_Name".equals(dataCenterNameChild.getName())){
                dataCenterName.setShortName(dataCenterNameChild.getTextTrim());
              }
            }
            dataCenter.setDataCenterName(dataCenterName);
          } else if ("Data_Center_URL".equals(dataCenterElementChild.getName())) {
            dataCenter.setDataCenterURL(dataCenterElementChild.getTextTrim());
          } else if ("Personnel".equals(dataCenterElementChild.getName())) {
            //create data center Personnel
            Personnel personnel = new Personnel();
            List<Element> dataCenterPersonnelChildren = dataCenterElementChild.getChildren();
            for (Element element : dataCenterPersonnelChildren) {
              if ("Role".equals(element.getName())) {
                personnel.getRole().add(element.getTextTrim());
              } else if ("First_Name".equals(element.getName())) {
                personnel.setFirstName(element.getTextTrim());
              } else if ("Last_Name".equals(element.getName())) {
                personnel.setLastName(element.getTextTrim());
              } else if ("Email".equals(element.getName())) {
                personnel.getEmail().add(element.getTextTrim());
              }
            }
            dataCenter.getPersonnel().add(personnel);
          }
        }
        dif.getDataCenter().add(dataCenter);
      }

      //create Summary(s)
      List<Element> summaryList = difElement.getChildren("Summary", ns);
      for (Element summaryListElement : summaryList) {
        Summary summary = new Summary();
        List<Element> summaryListElementChildren = summaryListElement.getChildren();
        for (Element summaryListElementChild : summaryListElementChildren) {
          if ("Abstract".equals(summaryListElementChild.getName())) {
            ObjectFactory factory = new ObjectFactory();
            summary.getContent().add(factory.createAbstract(summaryListElementChild.getTextTrim()));
          }
        }
        dif.setSummary(summary);
      }

      //create IDN_Node(s)
      List<Element> idnNodeList = difElement.getChildren("IDN_Node", ns);
      for (Element idnNodeListElement : idnNodeList) {
        IDNNode idnNode = new IDNNode();
        List<Element> idnNodeListElementChildren = idnNodeListElement.getChildren();
        for (Element element : idnNodeListElementChildren) {
          if ("Short_Name".equals(element.getName())) {
            idnNode.setShortName(element.getTextTrim());
          } else if ("Long_Name".equals(element.getName())) {
            idnNode.setLongName(element.getTextTrim());
          }
        }
        dif.getIDNNode().add(idnNode);
      }

    } catch (Exception e) {
      LOG.error("Error whilst parsing Atom XML response from Dataset Search: ", e);
    }
    return dif;

  }

  @Override
  public void map(List<DIF> pojoList) {
    // TODO Auto-generated method stub

  }

}
