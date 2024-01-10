# GenACT Documentation
GenACT, a novel data generator based on academic conference tweets. GenACT consists of a Twitter-based data generator inspired by real-world data, tailored to provision temporal and static data in a streaming fashion, which is suitable for stream reasoning applications.

# Table of Contents

1. [ About the Repository ](#repo)

2. [ Usage Instructions ](#usage)

   2.1 [ Direct execution using executable jar (with default configurations) ](#exe)
   
   2.2 [ Using Source Code (with or without default configurations) ](#code)

<a name="repo"></a>
## 2. About the Repository
The project repository consists of the following directories:

[Generator](https://github.com/kracr/temporal-data-generator/tree/main/ABox%20Generator): Java source code directory of our GenACT that generates the data  (see section [ 2.2 ](#code) for source-code usage instructions). 

[Ontology](https://github.com/kracr/temporal-data-generator/tree/main/Ontology): Consists of 4 Academic Conference Event Ontologies (describing an Academic conference event) one for each OWL 2 profile, 4 University Ontologies from exisitng OWL2Bench benchmark for OWL 2 reasoners, 1 Twitter Ontology (consisting axioms describing Twitter metadata) and a Location Ontology. Twitter ontology is kept separately from Academic Conference Ontology because this allows to expand the generator to other social media platforms in future. 

[Mappings](https://github.com/kracr/temporal-data-generator/tree/main/Mappings): Consists of different twitter template yaml files (Before Conference, During Conference and After Conference) that serve as the starting point for our data generator. The directory also consists of yaml mapping files to generate RDF triples according to the placeholders in each twitter template. 

[StaticData](https://github.com/kracr/temporal-data-generator/tree/main/StaticData): RDF files consisting of static data information such as conference names, organization names etc.

[RunnableJars] (#exe) for usage instructions. 

[Streams] A few files consisting of the data generated using GenACT.



<a name="usage"></a>
## 2. Usage Instructions

Requirements: The user must have *java and maven* installed in the system. Operating System-Ubuntu

The user needs to provide two mandatory inputs, *the number of conferences* and *the number of conference cycles*. 

<a name="exe"></a>
## 3.1. Direct execution using executable jar :

We have provided a java executable jar **[genact.jar](https://drive.google.com/file/d/17fQTmytgVHJqrFjTpUIXrv_SsLFmTmjB/view?usp=drive_link)** that generates the datasets using the default configurations that were used for the experiments reported in the paper. In order to execute this Jar file, user need to give the inputs (in the same order):  

For eg. : 

java -jar genact.jar 1 5 C:\GitHub\owl2streambench (where 1 is the number of conferences,  5 is the number of cycles, files_directory_path)
         


<a name="code"></a>
## 3.2. Using Source Code :
In order to run the source code, user need download the project repository owl2bench-master. Extract it and save it in a folder. There is a maven project [ABoxGenerator](https://github.com/kracr/temporal-data-generator) inside the temporal-data-generator-master directory. Open command line and change to the directory that contains the pom.xml of this OWL2Bench project. Execute the maven command:

mvn compile

mvn install

Now, using maven's exec plugin, run the main class *Generator* and pass the list of arguments *the number of conferences* and *the number of conference cycles* (same as above) using exec.args. For example-

mvn exec:java -Dexec.mainClass=ABoxGen.InstanceGenerator.Generator -Dexec.args="2 3 C:\GitHub\owl2streambench"



