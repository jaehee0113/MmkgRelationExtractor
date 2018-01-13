MMKG Relation extractor
===================

**(A part of the picturing knowledge project at the ANU Computational Media Lab)**

Latest version: v1.1.1
Author: Jae Hee Lee

The program extracts relations from multimedia sources (e.g. tweets, images, articles), which are then used for labels of the entity graph.

Version History
---------------

- v2.4.0 : Improvements in graph visualization (e.g. color palette for changing node color, displaing node statistics)
- v2.3.0 : Addition of useful examples and added the functionality to see connnected components of a graph
- v2.2.0 : Allow graph generation of multiple documents (e.g articles or tweets in a certain topic)
- v2.1.0 : Improvements in gexf file processing speed (i.e. without calling to API, process it locally).
- v2.0.0 : A web-based knowledge graph added
- v1.1.0 : Added Gexf4j library thereby adding the ability to export results into gexf file for graph rendition.
- v1.0.0 : Created basic frame of a triple extraction.


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
