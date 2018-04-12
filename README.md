MMKG Relation extractor
===================

**(A part of the picturing knowledge project at the ANU Computational Media Lab)**

**Latest version**: v2.4.0

**Author**: Jae Hee Lee

The program extracts relations from multimedia sources (e.g. tweets, images, articles), which are then used for labels of the entity graph.


Dependencies
-------------

**Java (triple extraction and gexf. export)**

> - DBpedia Spotlight
> - Stanford CoreNLP
> - Gexf4j
> - ElasticSearch
> - Semafor

For a further details, refer to **pom.xml**.

**Python (web-based knowledge graph)**

> - Django
> - Python 3
> - NetworkX

(better to create virtual environment when running manage.py in app folder)

Installation Instructions
-------------------------

### Java application

**Make sure to examine the config files (e.g. DBpediaSpotlightConfig.java as API_URL constant will need to be changed) before running the application!**

> - Create a Maven project
> - Maven build - compile
> - Run the application with the following VM arguments: (**-Xms8g -Xmx9g**)
> - Before running the application, a local server that returns a frame needs to be run. Please refer to 'Running SemaforSocketServer to extract frames' section.

### Python application

> - Run local server to run the application locally.
> - Go to app folder for more detailed instruction.

### When exporting in jar file

> - make sure to install 3rd party JARs if they are not in the Maven central repository.

```sh
mvn install:install-file -Dfile=matlabcontrol-4.1.0.jar -DgroupId=org.matlabcontrol \
    -DartifactId=matlabcontrol -Dversion=4.1.0 -Dpackaging=jar
```

```sh
 <dependency>
        <groupId>org.matlabcontrol</groupId>
        <artifactId>matlabcontrol</artifactId>
        <version>4.1.0</version>
 </dependency>
```

Therefore, make sure to go through this step for the following:

```sh
mvn install:install-file -Dfile=external/stanford-corenlp-3.8.0-models.jar -DgroupId=edu.stanford.nlp -DartifactId=stanford-corenlp-models -Dversion=3.8.0 -Dpackaging=jar
```

In Maven, you need to specify goal as "package." Any dependencies using 'system path' would not work.

WHen running jar:

```sh
 java -Xms6g -Xmx6g -cp MmkgRelationExtractor-0.0.1-SNAPSHOT-jar-with-dependencies.jar extractor.main.Application "beef_ban" "2017-07-07" "2017-07-07"
```
(depending on the heap memory your computer has - change Xms and Xmx variables)

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

Running SemaforSocketServer to extract frames
---------------------

Make sure to fork the repository from https://github.com/Noahs-ARK/semafor

### Step 1. Modify files

When downloaded and cloned the repository, please modify **bin/config.sh** file as follows:

```sh
# assumes this script (config.sh) lives in "${BASE_DIR}/semafor/bin/"
export BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/../.." > /dev/null && pwd )"
# path to the absolute path
# where you decompressed SEMAFOR.
export SEMAFOR_HOME="${BASE_DIR}/semafor"

export CLASSPATH=".:${SEMAFOR_HOME}/target/Semafor-3.0-alpha-04.jar"

# Change the following to the bin directory of your $JAVA_HOME
export JAVA_HOME_BIN="/usr/bin"

# Change the following to the directory where you decompressed 
# the models for SEMAFOR 2.0.
export MALT_MODEL_DIR="../../models/semafor_malt_model_20121129"
export MALT_ABS_MODEL_DIR="models/semafor_malt_model_20121129"
export TURBO_MODEL_DIR="{BASE_DIR}/models/turbo_20130606"

```

Before running the java application you should fix the **SemaforConfig.java** file as the value is based on my local machine.

```java
//Fix the following constants
MALT_PARSER_SHELL_DIR = "/home/admin-u4722839/Desktop/semafor/bin/runMalt.sh";
INPUT_FILE_DIR = "/home/admin-u4722839/Documents/workspace/MmkgRelationExtractor/src/extractor/lib/files/";
OUTPUT_DIR = "/home/admin-u4722839/Desktop/semafor/output";
```

### Step 2. Running SemaforSocketServer

The basic command of running the SmeaforSocketServer local server is:

```sh
java -Xms4g -Xmx4g -cp target/Semafor-3.0-alpha-04.jar edu.cmu.cs.lti.ark.fn.SemaforSocketServer model-dir:models port:8888
```

> **! I created models folder in the main folder (the main repository folder) and created 'semafor_malt_model_20121129' folder under models folder.**

> **Also make sure to have Semafor-3.0-alpha-0.4.jar inside the 'target' folder.**

### Step 3. Running Maltparser

When running the java application this step is automatically involved. The step is:

1. The preprocessed sentences per article will be stored in a text file.

2. This file (i.e. document_id-completed.txt) is then given as an input of the following command

```sh
MALT_PARSER_SHELL_DIR  SemaforConfig.INPUT_FILE_DIR (with filename) SemaforConfig.OUTPUT_DIR
```

It is possible to get an error during this stage. You will need to debug it. The parser first generates .conll file from text file and use that to extract the canonical form of a relation. The conll file will be in **/output** folder and then the following command will be executed.

### Step 4. Let the server process conll file to fetch the frame for a relation.

```sh
/bin/sh -c cat /home/admin-u4722839/Desktop/semafor/output/conll | nc localhost 8888
```
The output will be the json files of the result for each sentence. From this result, we get the frame for each relation.

**Make sure to change the directory (it is in AppWorker.java file)**

Now the application should be run successfully.

History
-------

v 2.4

> - Improvements in graph visualization (e.g. color palette for changing node color, displaing node statistics)

v 2.3

> - Addition of useful examples and added the functionality to see connnected components of a graph

v 2.2

> - Allow graph generation of multiple documents (e.g articles or tweets in a certain topic)

v 2.1

> - Improvements in gexf file processing speed (i.e. without calling to API, process it locally).

v 2.0

> - A web-based knowledge graph added

v 1.1

> - Gexf4j added for exporting extraction results into .gexf file.

v 1.0

> - Semafor API added for extracting the canonical form of relations.
> - ElasticSearch API added for retrieving documents.
> - DBpedia Spotlight API added for extracting the canonical form of entities.
> - Initial files added.
