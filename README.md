# GenACT Documentation
GenACT is a data generator designed to address challenges in obtaining realistic temporal web data. Inspired by real-world data, GenACT is based on academic conference tweets tailored to provision temporal and static data in a streaming fashion, which is suitable for temporal and stream reasoning applications.

# Table of Contents

1. [ Introduction ](#intro)

   1.1 [ Ontologies based on Academic Conference Twitter (ACT) ](#tbox)
 
   1.2 [ Data Generation ](#abox)

2. [ About the Repository ](#repo)

3. [ Usage Instructions ](#usage)

   3.1 [Event Data Generation: Direct execution using executable jar (with default configurations)](#edgexe)
	   
   3.2 [Sequence Data Generation: Direct execution using executable jar (with default configurations)](#sdgexe)

   3.3 [Using Source Code (with or without default configurations) ](#code)
	   
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

As shown in [Figure](https://github.com/kracr/temporal-data-generator/blob/main/images/generator_pipeline.png), the data generation pipeline consists of two steps: Event Data Generation and Sequence Data Generation. The first step generates data for the specified number of conference instances. The second step allows users to create segments of the generated data to simulate different scenarios.


<a name="repo"></a>
## 2. About the Repository
The project repository consists of the following directories:

[Generator](https://github.com/kracr/temporal-data-generator/tree/main/ABox%20Generator): Java source code directory of our GenACT that generates the data  (see section [ 3 ](#code) for source-code usage instructions). 

[Ontology](https://github.com/kracr/temporal-data-generator/tree/main/Ontology): Consists of four Academic Conference Event Ontologies (describing an Academic conference event) one for each OWL 2 profile, 4 University Ontologies from exisitng OWL2Bench benchmark for OWL 2 reasoners, 1 Tweet Ontology (consisting axioms describing Tweet metadata). Tweet ontology is kept separately from Academic Conference Ontology because this allows to expand the generator to other social media platforms in future. 

[Mappings](https://github.com/kracr/temporal-data-generator/tree/main/Mappings): Consists of template.yaml and mapping.yaml files that serve as the starting point for our data generator. Mapping files is used to generate RDF triples according to the placeholders in each template file. 

[StaticData](https://github.com/kracr/temporal-data-generator/tree/main/StaticData): Ontologies Location.owl (real data for cities mapped with latitude, longitude and Country information) and Organization.owl (synthetically generated research groups mapped with instances (cities) from Location ontology). 

[RunnableJars] (#usage) for usage instructions. 

[EventData] Consists of event data generated in separate directories for each conference and each conference cycle: such as ESWC_2023, ESWC_2024. Inside each directory two files tweetMetadata and eventData are created for each tweet. Each file is named as timestamp_tweetid_metadata.ttl and timestamp_tweetid_eventdata.ttl

[SequenceData] 

[SparqlQueriesForPartition]

<a name="usage"></a>
## 3. Usage Instructions

Requirements: The user must have *java 1.7 and maven* installed in the system. 

<a name="edgexe"></a>
## 3.1. Event Data Generation (Direct execution using executable jar)

In order to generate the event data for the required number of conferences, users can directly run the executable jar **[genact.jar](to be updated)** that generates the datasets using the default configurations. In order to execute this Jar file, user need to give the inputs (in the same order):  

No. of conferences (int)*Mandatory, No. of conference Cycles (int)*Mandatory , DirectoryPath (optional), Seed (optional) .  DirectoryPath is the path where all the folders (ontologies, queries, streams, csv files, etc) can be found. So, the user needs to provide the correct directory path. 

For eg. : java -jar genact.jar 1 5 C:\GitHub\temporal-data-generator 100

(where the number of conferences--> 1, number of cycles--> 5, files_directory_path--> C:\GitHub\temporal-data-generator, seed --> 100)

<a name="sdgexe"></a>
## 3.2. Sequence Data Generation (Direct execution using executable jar)

In order to generate different sequences from the event data generated in the previous step, users can directly run the executable jar **[partition.jar](to be updated)** that generates the datasets using the default configurationsd. 

In order to create partitions based on attributes--> java -jar partition.jar --attribute conference/user/domain/tweet_type/object.

(conference: creates sequences corresponding to different conference instances, user: creates sequences corresponding to different users involved throughout the conference,
domain: creates segments corresponding to different research domains such as AI, tweetType creates segments based on tweet categories defined in the 
paper such as Announcement, Insight etc, object: creates partition based on different objects in each [s p o] triple). 

In order to create partitions based on shape--> java -jar partition.jar --shape star/chain/tree/other. user can specify the shape they want for their segments
by writing the query in other.txt file. 

<a name="code"></a>
### 3.3. Using Source Code :
In order to run the source code, user need to download the project repository. Extract it and save it in a folder. There is a maven project [ABoxGenerator](https://github.com/kracr/temporal-data-generator). Open command line and change to the directory that contains the pom.xml of this project. Execute the maven command:

mvn compile

mvn install

For event data generation: mvn exec:java -Dexec.mainClass=genact.temporal.data.generator.DataGenerator -Dexec.args="2 3 C:\GitHub\temporal-data-generator"

For sequence data generation: mvn exec:java -Dexec.mainClass=genact.temporal.data.generator.CreatePartitions -Dexec.args="--attribute conference"
