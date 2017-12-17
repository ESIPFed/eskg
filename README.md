# eskg

[![license](https://img.shields.io/github/license/ESIPFed/eskg.svg?maxAge=2592000?style=plastic)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/ESIPFed/eskg.svg?branch=master)](https://travis-ci.org/ESIPFed/eskg)

<img src="http://www.esipfed.org/sites/default/files/esip-logo.png" align="right" width="300" />

# Introduction

**Earth Science Knowledge Graph** - An Automatic Approach to Building Earth Science Knowledge Graph to Improve Data Discovery.

Big Earth observation data have been produced, archived and made available online, but discovering the right data in a manner that precisely and efficiently satisfies user need presents a significant challenge to the Earth Science (ES) community. An emerging trend in information retrieval community is to utilize knowledge graph to assist user fast finding desired information. This is particularly prevalent within the fields of social media and complex multimodal information processing to name but a few. 

However, building a domain-specific knowledge graph is labour-intensive and hard to keep up-to-date. We propose an automatic approach to building a dynamic knowledge graph for ES to improve data discovery by leveraging implicit, latent existing knowledge present within the Web Pages of NASA DAACs websites. This project will strengthen ties between observations and user communities by:
 1. developing a knowledge graph derived from Web Pages via natural language processing and knowledge extraction techniques, and 
 2. allowing users to traverse, explore, query, reason and navigate ES data via knowledge graph interaction.

# Installation

The prerequisites are
 * [Java SE Development Kit 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
 * [Apache Maven 3.X](http://maven.apache.org/)
Once installed, you should be able to confirm using the following command
```
$ mvn -version

...

Apache Maven 3.3.9 (bb52d8502b132ec0a5a3f4c09453c07478323dc5; 2015-11-10T08:41:47-08:00)
Maven home: /usr/local/Cellar/maven/3.3.9/libexec
Java version: 1.8.0_131, vendor: Oracle Corporation
Java home: /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre
Default locale: en_US, platform encoding: UTF-8
OS name: "mac os x", version: "10.11.6", arch: "x86_64", family: "mac"
```
As you can see from above the output displays JDK 1.8.0_131 and Maven 3.3.9.
You should also download and install this code
```
$ git clone https://github.com/ESIPFed/apache-semtech.git && cd apache-semtech
$ mvn clean install
```
The above task will install the code locally so you can try out the examples below.

# Running ESKG
Creation of an ESKG dataset can be achieved by executing the main method of [PODAAC Web Services Client](https://github.com/ESIPFed/eskg/blob/master/src/main/java/org/esipfed/eskg/aquisition/PODAACWebServiceClient.java#L229). This will build a local Ontology model which, by default, can be found at ```target/classes/podaacDatasets.ttl```.

Conveniently, this can be executed from the command line as follows
```
$ mvn exec:java -Dexec.mainClass="org.esipfed.eskg.aquisition.PODAACWebServiceClient"
```

# ESKG Dataset
The canonical, current ESKG dataset is hosted at the [ESIP Community Ontology Repository](http://cor.esipfed.org). 

The dataset URI is http://cor.esipfed.org/ont/eskg/

# Acknowledgements

ESKG was initially conceived and funded through the [ESIP Testbed initiative](http://testbed.esipfed.org/). [ESIP](http://esipfed.org/) funding acknowledged.

# Community

[![Google Group](https://img.shields.io/badge/-Google%20Group-lightgrey.svg)](https://groups.google.com/forum/#!forum/eskg-dev)

# License

ESKG is licensed permissively under the [Apache License v2.0](https://www.apache.org/licenses/LICENSE-2.0) 
a copy of which ships with this source code.
