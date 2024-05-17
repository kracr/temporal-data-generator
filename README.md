# GenACT Documentation
GenACT is a data generator designed to address challenges in obtaining realistic temporal web data. Inspired by real-world data, GenACT is based on academic conference tweets tailored to provision temporal and static data in a streaming fashion, which is suitable for temporal and stream reasoning applications.

# Table of Contents

1. [ Introduction ](#intro)

   1.1 [ Ontologies based on Academic Conference Twitter (ACT) ](#tbox)
 
   1.2 [ Data Generation ](#abox)

2. [ About the Repository ](#repo)

3. [ Usage Instructions ](#usage)

   3.1 [ Direct execution using executable jar (with default configurations) ](#exe)
 
   3.2 [ Using Source Code (with or without default configurations) ](#code)


<a name="intro"></a>
## 1. Introduction
GenACT models data after the Academic Conference Twitter (ACT) domain, chosen for its ability to represent various application types with realistic workload scenarios. The proposed synthetic data for studying KG evolution should be diverse, well-annotated, dynamically changing over time, include temporal constraints, historical data, and temporal dependencies, and consider scalability. GenACT meets these requirements by incorporating four forms of temporality: order, timestamped event data (Tweets), Versioned Data, and interval.  


<a name="tbox"></a>

## 1.1 Ontologies based on Academic Conference Twitter (ACT)

GenACT also includes four Academic Conference Ontologies based on different OWL 2 profiles, presenting diverse reasoning challenges and additional Twitter ontology to handle the twitter metadata. 


OWL 2 DL : [Academic-Conference-Event-DL.owl](https://github.com/kracr/temporal-data-generator/blob/main/Ontology/Academic-Conference-Event-DL.owl)

OWL 2 RL : [Academic-Conference-Event-RL.owl](https://github.com/kracr/temporal-data-generator/blob/main/Ontology/Academic-Conference-Event-RL.owl)

OWL 2 QL : [Academic-Conference-Event-QL.owl](https://github.com/kracr/temporal-data-generator/blob/main/Ontology/Academic-Conference-Event-QL.owl)

OWL 2 EL : [Academic-Conference-Event-EL.owl](https://github.com/kracr/temporal-data-generator/blob/main/Ontology/Academic-Conference-Event-EL.owl)

<a name="abox"></a>
## 1.2 Data Generation

GenACT generates realistic data based on two user inputs, *the number of conferences* and *the required number of conference cycles*. The instance data that is generated complies with the schema defined in the above metioned [TBox](#tbox) . The size of the instance data for each conference instance depends on several parameters defined in the property (config.properties) file present in the ABoxGenerator directory. Data for each tweet goes to a separate file (.ttl). It allows for a faster execution of sparql queries while streaming the data. also, the naming format of each tweet file includes timestamp so that in order to replay the generated data at varying speeds, the file reading isn't required. 


<a name="repo"></a>
## 2. About the Repository
The project repository consists of the following directories:

[Generator](https://github.com/kracr/temporal-data-generator/tree/main/ABox%20Generator): Java source code directory of our GenACT that generates the data  (see section [ 2.2 ](#code) for source-code usage instructions). 

[Ontology](https://github.com/kracr/temporal-data-generator/tree/main/Ontology): Consists of 4 Academic Conference Event Ontologies (describing an Academic conference event) one for each OWL 2 profile, 4 University Ontologies from exisitng OWL2Bench benchmark for OWL 2 reasoners, 1 Twitter Ontology (consisting axioms describing Twitter metadata). Twitter ontology is kept separately from Academic Conference Ontology because this allows to expand the generator to other social media platforms in future. 

[Mappings](https://github.com/kracr/temporal-data-generator/tree/main/Mappings): Consists of different twitter template yaml files (Before Conference, During Conference and After Conference) that serve as the starting point for our data generator. The directory also consists of yaml mapping files to generate RDF triples according to the placeholders in each twitter template. 

[StaticData](https://github.com/kracr/temporal-data-generator/tree/main/StaticData): Ontologies Location.owl (real data for cities mapped with latitude, longitude and Country information) and Organization.owl (synthetically generated research groups mapped with Institutes for user Affiliations). 
[RunnableJars] (#exe) for usage instructions. 

[Streams] A few files consisting of the data generated using GenACT. TweetMetadata, eventData. 



<a name="usage"></a>
## 3. Usage Instructions

Requirements: The user must have *java 1.8 and maven* installed in the system. Operating System-Ubuntu

The user needs to provide two mandatory inputs, *the number of conferences* and *the number of conference cycles*. 

<a name="exe"></a>
## 3.1. Direct execution using executable jar :

We have provided a java executable jar **[genact.jar](https://drive.google.com/file/d/1ls89Czm-MGsLlgU1BjqykheDhRZLpn4f/view?usp=sharing)** that generates the datasets using the default configurations that were used for the experiments reported in the paper. In order to execute this Jar file, user need to give the inputs (in the same order):  

For eg. : 

java -jar genact.jar 1 5 C:\GitHub\owl2streambench (where 1 is the number of conferences,  5 is the number of cycles, files_directory_path). C:\GitHub\owl2streambench is the path where all the folders can be find. So, the user needs to provide the correct directory path. 
         


<a name="code"></a>
## 3.2. Using Source Code :
In order to run the source code, user need download the project repository owl2bench-master. Extract it and save it in a folder. There is a maven project [ABoxGenerator](https://github.com/kracr/temporal-data-generator) inside the temporal-data-generator-master directory. Open command line and change to the directory that contains the pom.xml of this OWL2Bench project. Execute the maven command:

mvn compile

mvn install

Now, using maven's exec plugin, run the main class *Generator* and pass the list of arguments *the number of conferences* and *the number of conference cycles* (same as above) using exec.args. For example-

mvn exec:java -Dexec.mainClass=ABoxGen.InstanceGenerator.Generator -Dexec.args="2 3 C:\GitHub\owl2streambench"



