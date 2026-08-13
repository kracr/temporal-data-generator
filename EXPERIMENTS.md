# Reproducing the Thesis Experiments

This document documents the real, verified commands used to (re)generate the
thesis's experimental results: the C-SPARQL2 evaluation (all 13 competency
questions, $N \in \{1,2,5\}$ parallel conference streams) and the RDFox
evaluation (bulk OWL~2~RL materialisation, and a windowed evaluation directly
comparable to the C-SPARQL2 one). Everything below assumes Windows with
Git Bash, Java 17, Maven, and Node.js available.

## 1. Regenerating Event Data

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

## 2. C-SPARQL2 Evaluation

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

## 3. RDFox Evaluation

Location: `D:\RDFox-win64-x86_64-7.6b` (the real RDFox 7.6 installation and
license used throughout) and `D:\temporal-data-generator\RDFoxWindowedEval`
(the evaluation pipeline, built from scratch for this thesis -- see below for
why the original paper's JNI-based RDFox harness in
`D:\Github\RDFox-Running-Example-For-Unifying-Interface` is not used for
reproduction, even though it was repaired and is left in place for reference).

### 3.1 Bulk materialisation (single import + materialise per scale)

Real ad hoc `RDFox.exe sandbox` shell scripts (see the thesis's
Section~\ref{subsec:genact-rdfox} for the exact commands) import a scale's
full ABox into a rule-less store, then import the ACE-RL TBox
(`RDFoxWindowedEval/tbox/ace-rl-func.owl`, converted from
`Ontology/ACE/Academic-Conference-Event-RL.owl` to OWL Functional-Style
Syntax since RDFox's shell `import` does not auto-detect RDF/XML) to trigger
materialisation. This produces Table~\ref{tab:genact-rdfox-results}.

### 3.2 Windowed evaluation (all 13 CQs, real OWL 2 RL materialisation)

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

## 4. New Repository Contents (added for this thesis)

- **`VerifyRunRDFox/`** -- real replayed `.ttl` streams used to cross-validate
  the RDFox bulk-load pipeline against an earlier, already-published result.
- **`TBoxAwareGenerator/`** -- a second, declarative data generator (separate
  from `ABoxGenerator`) that validates every template's asserted classes and
  properties against the real ACE/Tweet TBoxes via the OWL API before
  generating anything, and includes a template that produces genuine
  tree-shaped ABoxes (structurally unreachable by `ABoxGenerator`).
- **`CSparqlEval/`** -- orchestration for the C-SPARQL2 evaluation (Section 2
  above): `run_all.sh`, `gen_config_percq.js` (the 13 real CQ definitions and
  window tiers), `aggregate.js`.
- **`RDFoxWindowedEval/`** -- the from-scratch RDFox windowed evaluation
  pipeline (Section 3.2 above): `runWindowed.js`, `queries.js` (same 13 CQ
  definitions as `CSparqlEval`), `runAll.js`, `parseOut.js`, `aggregate.js`,
  and `tbox/` (the converted ACE-RL TBox and static organisation data RDFox
  can actually import).
- **`thesis/`** -- `genact-section.tex`, the thesis chapter these experiments
  support.
