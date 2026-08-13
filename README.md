# GenACT Documentation
GenACT is an ongoing project that is designed to address challenges in obtaining realistic temporal web data. 
Inspired by real-world data, GenACT is based on academic conference tweets tailored to provision temporal and static data in a streaming fashion, 
which is suitable for studying the Knowledge Graph Evolution as well as evaluating temporal and stream reasoning applications.

Data files generated using GenACT are associated with timestamps which allows users to stream the data at different rates as per their requirements. 

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
GenACT models data after the Academic Conference Tweet (ACT) domain, chosen for its ability to represent various application types with realistic workload 
scenarios. The synthetic data for studying KG evolution should be diverse, well-annotated, dynamically changing over time, include temporal 
constraints, historical data, and temporal dependencies, and consider scalability. GenACT meets these requirements by incorporating temporality, timeliness and dynamicity.


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

[RunnableJars](https://drive.google.com/drive/folders/1xWfHi9lOZ_OhOmD_VVQuDXW976R4c2HQ?usp=sharing) (#usage) for usage instructions. 

[EventData](https://github.com/kracr/temporal-data-generator/tree/main/EventData) Consists of event data generated in separate directories for each conference and each conference cycle: such as ESWC_2023, ESWC_2024. Inside each directory two files tweetMetadata and eventData are created for each tweet. Each file is named as timestamp_tweetid_metadata.ttl and timestamp_tweetid_eventdata.ttl

[SequenceData](https://github.com/kracr/temporal-data-generator/tree/main/SequenceData) The segments generated after partitioning goes to this directory. The files generated are the RDF triple files with associated timestamps hence these files
 can be streamed as per user requirements. 

[SparqlQueriesForPartition](https://github.com/kracr/temporal-data-generator/tree/main/SparqlQueriesForPartition) This directory consists of two subdirectories: ByAttribute and ByShape. Each of these consists of sparql queries that are
required for segmenting the event data. Users can add more sparql queries to 'ByShape' folder and generate the partitions as per their requirements.

<a name="usage"></a>
## 3. Instructions to generate the data

Requirements: The user must have *java 1.7 and maven* installed in the system. 

<a name="edgexe"></a>
## 3.1. Event Data Generation (Direct execution using executable jar)

In order to generate the event data for the required number of conferences, users can directly run the executable jar **[GenACT.jar](https://drive.google.com/file/d/1xxiU2j5swBRF8SCk8WZ43LMhfSd9YqMB/view?usp=sharing)** that generates the datasets using the default configurations. In order to execute this Jar file, user need to give the inputs (in the same order):  

No. of conferences (int)*Mandatory, No. of conference Cycles (int)*Mandatory , DirectoryPath (*Mandatory), Seed (optional) .  DirectoryPath is the path where all the folders (ontologies, queries, streams, csv files, etc) can be found. So, the user needs to provide the correct directory path. 

For eg. : java -jar genact.jar 1 5 C:\GitHub\temporal-data-generator 100

(where the number of conferences--> 1, number of cycles--> 5, files_directory_path--> C:\GitHub\temporal-data-generator, seed --> 100)

<a name="sdgexe"></a>
## 3.2. Sequence Data Generation (Direct execution using executable jar)

In order to generate different sequences from the event data generated in the previous step, users can directly run the executable jar **[GenACT_partition.jar](https://drive.google.com/file/d/1IXnHdioTIB-vLDYAdT9gecQfyyChEdym/view?usp=sharing)** that generates the datasets using the default configurationsd. 

In order to create partitions based on attributes--> java -jar partition.jar --attribute conference/user/domain/tweet_type/object DirectoryPath. 
Example: java -jar partition.jar --attribute conference C:\GitHub\temporal-data-generator

(conference: creates sequences corresponding to different conference instances, user: creates sequences corresponding to different users involved throughout the conference,
domain: creates segments corresponding to different research domains such as AI, tweetType creates segments based on tweet categories defined in the 
paper such as Announcement, Insight etc, object: creates partition based on different objects in each [s p o] triple). 

In order to create partitions based on shape--> java -jar partition.jar --shape star/chain/tree/other DirectoryPath. user can specify the shape they want for their segments
by writing the query in other.txt file. Example: java -jar partition.jar --shape star C:\GitHub\temporal-data-generator

<a name="code"></a>
### 3.3. Using Source Code :
In order to run the source code, user need to download the project repository. Extract it and save it in a folder. There is a maven project [ABoxGenerator](https://github.com/kracr/temporal-data-generator). Open command line and change to the directory that contains the pom.xml of this project. Execute the maven command:

mvn compile

mvn install

For event data generation: mvn exec:java -Dexec.mainClass=genact.temporal.data.generator.DataGenerator -Dexec.args="2 3 C:\GitHub\temporal-data-generator"

For sequence data generation: mvn exec:java -Dexec.mainClass=genact.temporal.data.generator.CreatePartitions -Dexec.args="--attribute conference"


# 4 Instructions for using the data:

Each file generated inside the SequenceData directory is named in the format timestamp_tweetid_eventdata.ttl, where timestamp is in yymmdd_hhmmss format. The conference tweets start at
 January 1, 1970, 00:00:00 IST. For example: 20700101_000000. The reason  for using this format is the file naming convention. The timestamp can be changed in the appropriate format as per the user requirments, 
 such as 2021-01-01 00:00:00 (local date time) is 1609459200000 in long format.  yymmdd_hhmmss format allows to sort the generated files  them by timetamps, which arranges them in a sequential order. 
 Once sorted, each file can be streamed at a defined rate.

In order to stream data, the Code repository consists of two java classes- RDFStreamerExample.java and RDFServerExample.java. 
Users can use them to stream the sequence data generated and use them as per their requirements. 

Users can also modify the property files present in the code repository and generate datasets of varying sizes.

### Other Details

The instantiation process involves populating the RDF structure with data
from external sources, such as the Digital Bibliography and Library Project, providing
information about authors and papers. We synthetically generate complementary
information to complete the instantiation, ensuring a diverse and
realistic dataset for subsequent analysis. For instance, we synthetically generate
Research Groups and organizations, but we map them with real locations. The csv files are present in the repository. 

# 5. Reproducing the Thesis Experiments

This section documents the real, verified commands used to (re)generate the
thesis's experimental results: the C-SPARQL2 evaluation (all 13 competency
questions, $N \in \{1,2,5\}$ parallel conference streams) and the RDFox
evaluation (bulk OWL~2~RL materialisation, and a windowed evaluation directly
comparable to the C-SPARQL2 one). Everything below assumes Windows with
Git Bash, Java 17, Maven, and Node.js available.

## 5.1 Regenerating Event Data

```
cd D:\temporal-data-generator\ABoxGenerator
mvn exec:java -Dexec.mainClass=genact.temporal.data.generator.DataGenerator -Dexec.args="2 3 D:/temporal-data-generator 2026"
```
(2 conferences, 3 cycles each, seed 2026 -- adjust as needed.) This
**wipes and regenerates** `EventData/`, which deletes the `_N1`/`_N2`/`_N5`
junction directories described below -- recreate them afterward.

### Recreating the `_N1`/`_N2`/`_N5` multi-conference views

`GenActStreamServer` and the RDFox windowed pipeline both expect a directory
containing one subdirectory per conference to stream/window in parallel.
Since `EventData/` only contains the individual conference directories
(`conf0_1970`, `conf1_1970`, ...), junctions provide the N-conference views
without duplicating data on disk:

```powershell
New-Item -ItemType Junction -Path "D:\temporal-data-generator\EventData\_N1\conf0_1970" -Target "D:\temporal-data-generator\EventData\conf0_1970"

New-Item -ItemType Junction -Path "D:\temporal-data-generator\EventData\_N2\conf0_1970" -Target "D:\temporal-data-generator\EventData\conf0_1970"
New-Item -ItemType Junction -Path "D:\temporal-data-generator\EventData\_N2\conf1_1970" -Target "D:\temporal-data-generator\EventData\conf1_1970"

New-Item -ItemType Junction -Path "D:\temporal-data-generator\EventData\_N5\conf0_1970" -Target "D:\temporal-data-generator\EventData\conf0_1970"
New-Item -ItemType Junction -Path "D:\temporal-data-generator\EventData\_N5\conf0_1971" -Target "D:\temporal-data-generator\EventData\conf0_1971"
New-Item -ItemType Junction -Path "D:\temporal-data-generator\EventData\_N5\conf0_1972" -Target "D:\temporal-data-generator\EventData\conf0_1972"
New-Item -ItemType Junction -Path "D:\temporal-data-generator\EventData\_N5\conf1_1970" -Target "D:\temporal-data-generator\EventData\conf1_1970"
New-Item -ItemType Junction -Path "D:\temporal-data-generator\EventData\_N5\conf1_1971" -Target "D:\temporal-data-generator\EventData\conf1_1971"
```

These are **fragile**: `DataGenerator` recursively deletes `EventData/`
before regenerating it, which destroys any junctions living inside it. Recreate
them after every regeneration, before running either evaluation below.

## 5.2 C-SPARQL2 Evaluation

Location: `D:\Github\CSPARQL-Running-Example-For-Unifying-Interface` (the
runner/engine, built with Maven) and `D:\temporal-data-generator\CSparqlEval`
(the orchestration scripts that drive it for this thesis's evaluation).

One-time build of the runner:
```
cd D:\Github\CSPARQL-Running-Example-For-Unifying-Interface
mvn compile
```

Run all 13 CQs for $N=1,2,5$ (one C-SPARQL2 engine instance per CQ, since
registering all 13 in one shared engine measurably starved each query's
evaluation frequency):
```
cd D:\temporal-data-generator\CSparqlEval
bash run_all.sh
```
This writes per-run logs and `answers.json` snapshots to
`D:\temporal-data-generator\CSparqlEval\results\`. `gen_config_percq.js`
(also in this directory) generates each CQ's `configuration.json` -- the real
SPARQL bodies and per-tier window (range/step) definitions for all 13
competency questions live there and are the source of truth for the queries
also used by the RDFox windowed evaluation (`RDFoxWindowedEval/queries.js`).

Aggregate results into per-CQ latency/throughput/memory/non-empty-rate
summaries:
```
node aggregate.js
```
(Writes `aggregated2.json` alongside `results/`; this is what the thesis's
Table~\ref{tab:genact-csparql-results} and the artifact's CSparql2 table are
built from.)

## 5.3 RDFox Evaluation

Location: `D:\RDFox-win64-x86_64-7.6b` (the real RDFox 7.6 installation and
license used throughout) and `D:\temporal-data-generator\RDFoxWindowedEval`
(the evaluation pipeline, built from scratch for this thesis -- see below for
why the original paper's JNI-based RDFox harness in
`D:\Github\RDFox-Running-Example-For-Unifying-Interface` is not used for
reproduction, even though it was repaired and is left in place for reference).

### 5.3.1 Bulk materialisation (single import + materialise per scale)

Real ad hoc `RDFox.exe sandbox` shell scripts (see the thesis's
Section~\ref{subsec:genact-rdfox} for the exact commands) import a scale's
full ABox into a rule-less store, then import the ACE-RL TBox
(`RDFoxWindowedEval/tbox/ace-rl-func.owl`, converted from
`Ontology/ACE/Academic-Conference-Event-RL.owl` to OWL Functional-Style
Syntax since RDFox's shell `import` does not auto-detect RDF/XML) to trigger
materialisation. This produces Table~\ref{tab:genact-rdfox-results}.

### 5.3.2 Windowed evaluation (all 13 CQs, real OWL 2 RL materialisation)

```
cd D:\temporal-data-generator\RDFoxWindowedEval
node runAll.js 1        # N=1
node runAll.js 2        # N=2
node runAll.js 5        # N=5
node aggregate.js       # combines all three into results/aggregated.json
```
`runAll.js` reads the `_N1`/`_N2`/`_N5` conference directories directly (no
live streaming: windows are computed from each file's embedded timestamp,
scaled by the same 200,000 replay rate used to pace C-SPARQL2's real TCP
replay, so window semantics stay comparable). For each CQ, one persistent
RDFox datastore is used for the whole run: the ACE-RL TBox and, where
relevant, the static organisation background data
(`RDFoxWindowedEval/tbox/organization-func.owl`, converted the same way as
the ACE-RL TBox) are loaded once, and each window transition applies a real
incremental RDFox `import`/`import -` (addition/deletion) rather than a cold
reload -- this produces Table~\ref{tab:genact-rdfox-windowed}, the direct
counterpart to the C-SPARQL2 table.

Individual pieces, useful when iterating on one CQ:
```
node runWindowed.js cq3 D:/temporal-data-generator/EventData/_N1/conf0_1970       # one CQ, one conference
node parseOut.js tmp/cq3_N1_c0/out.txt                                            # inspect one run's parsed results
```

**Why not the original paper's RDFox harness:** an embedded, JNI-based
streaming harness from the original GenACT paper exists at
`D:\Github\RDFox-Running-Example-For-Unifying-Interface`, mirroring the
C-SPARQL2 runner. It was repaired during this thesis (its JRDFox client API
calls, connection/license setup, and a stranded-final-flush bug that silently
dropped data received after a stream's last scheduled flush were all fixed to
run against the RDFox~7.6 install), and does run, but the shared TCP
replay/receiver design it depends on has real, unresolved data-timing
sensitivity. The windowed pipeline in `RDFoxWindowedEval` was built instead:
it needs no live streaming, is far faster (single-digit seconds per CQ rather
than 5-minute timeouts), and is fully reproducible from the files already on
disk.

## 5.4 New Repository Contents (added for this thesis)

- **`VerifyRunRDFox/`** -- real replayed `.ttl` streams used to cross-validate
  the RDFox bulk-load pipeline against an earlier, already-published result.
- **`TBoxAwareGenerator/`** -- a second, declarative data generator (separate
  from `ABoxGenerator`) that validates every template's asserted classes and
  properties against the real ACE/Tweet TBoxes via the OWL API before
  generating anything, and includes a template that produces genuine
  tree-shaped ABoxes (structurally unreachable by `ABoxGenerator`).
- **`CSparqlEval/`** -- orchestration for the C-SPARQL2 evaluation (Section 5.2
  above): `run_all.sh`, `gen_config_percq.js` (the 13 real CQ definitions and
  window tiers), `aggregate.js`.
- **`RDFoxWindowedEval/`** -- the from-scratch RDFox windowed evaluation
  pipeline (Section 5.3.2 above): `runWindowed.js`, `queries.js` (same 13 CQ
  definitions as `CSparqlEval`), `runAll.js`, `parseOut.js`, `aggregate.js`,
  and `tbox/` (the converted ACE-RL TBox and static organisation data RDFox
  can actually import).
- **`thesis/`** -- `genact-section.tex`, the thesis chapter these experiments
  support.