MMKG Relation extractor
===================

**(A part of the picturing knowledge project at the ANU Computational Media Lab)**

Latest version: v1.1.1
Author: Jae Hee Lee

The program extracts relations from multimedia sources (e.g. tweets, images, articles), which are then used for labels of the entity graph.

Dependencies
-------------

> - DBpedia Spotlight
> - Stanford CoreNLP
> - Gexf4j
> - ElasticSearch
> - Semafor

For a further details, refer to **pom.xml**.

Installation Instructions
-------------------------

> - Create a Maven project
> - Maven build - compile
> - Run the application with the following VM arguments: (**-Xms6g -Xmx7g**)

Application Structure
---------------------

#### Models

Under this package, core object models are defined including Article, Tweet and MMKGRelationTriple.

#### Semafor

Under this package, files necessary for Semafor API are defined.

#### Elastic

Under this package, files necessary for ElasticSearch are defined. Note that the client connects to the ANU internal server so there may be some problem accessing the server outside the ANU. Indexes are defined in **elastic.lib.TopicDict**.

#### Export

Under this package, files necessary for export are defined. At the moment, the available export file is **.gexf**, which will be used to render a graph.

#### Lib

Under this package, files necessary for processing files or texts are defined.

#### Diff

Under this package, files necessary for measuring differences or similarities bewteen tokens are defined.

#### Main

This is the package where the application runs.

Important Information
---------------------

dfasfdssdfaasdf

History
-------

v 1.1

> - Gexf4j added for exporting extraction results into .gexf file.

v 1.0

> - Semafor API added for extracting the canonical form of relations.
> - ElasticSearch API added for retrieving documents.
> - DBpedia Spotlight API added for extracting the canonical form of entities.
> - Initial files added.
