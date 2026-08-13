#!/bin/bash
# Runs all 13 GenACT competency questions against CSparql2 for N = 1, 2, 5 parallel
# conference streams. One CQ per C-SPARQL2 engine instance (running several inside one
# shared engine measurably starved each query's evaluation frequency).
#
# Prerequisites:
#   - EventData/_N1, _N2, _N5 junction directories must exist under D:\temporal-data-generator
#     (recreate with `New-Item -ItemType Junction` if a DataGenerator run wiped them -- see README).
#   - D:\Github\CSPARQL-Running-Example-For-Unifying-Interface must be built (mvn compile),
#     its cp.txt must exist.
set -uo pipefail
MVN="/d/mvnwrapper/wrapper/dists/apache-maven-3.9.9/977a63e90f436cd6ade95b4c0e10c20c/bin/mvn"
NODE="/c/Program Files/nodejs/node"
JAVA="/c/Program Files/Eclipse Adoptium/jdk-17.0.19.10-hotspot/bin/java.exe"
RUNNER="/d/Github/CSPARQL-Running-Example-For-Unifying-Interface"
ABOX="/d/temporal-data-generator/ABoxGenerator"
SCR="/d/temporal-data-generator/CSparqlEval"
RESULTS="$SCR/results"
mkdir -p "$RESULTS"
CP=$(cat "$RUNNER/cp.txt")

declare -A CQPORT=( [cq1]=9010 [cq2]=9020 [cq3]=9030 [cq4]=9040 [cq5]=9050 [cq6]=9060 [cq7]=9070 [cq8]=9081 [cq9]=9090 [cq10]=9100 [cq11]=9110 [cq12]=9120 [cq13]=9130 )

for N in 1 2 5; do
  echo "=========== N=$N: regenerating per-CQ configs ==========="
  "$NODE" "$SCR/gen_config_percq.js" "$N" "$SCR/cqruns"

  for cq in cq1 cq2 cq3 cq4 cq5 cq6 cq7 cq8 cq9 cq10 cq11 cq12 cq13; do
    echo "----------- N=$N $cq -----------"
    port=${CQPORT[$cq]}

    # copy this CQ's config into the runner root (proven-working CWD)
    cp "$SCR/cqruns/$cq/configuration.json" "$RUNNER/configuration.json"

    # launch this CQ's N stream servers
    ( cd "$ABOX" && nohup "$MVN" -q exec:java \
        -Dexec.mainClass="genact.temporal.data.generator.GenActStreamServer" \
        -Dexec.args="D:/temporal-data-generator/EventData/_N$N $port 200000" \
        > "$RESULTS/${cq}_n${N}_server.log" 2>&1 & )
    sleep 3

    # run the CSparql2 process from the runner root, blocking
    ( cd "$RUNNER" && "$JAVA" \
        --add-opens java.base/java.lang=ALL-UNNAMED \
        --add-opens java.base/java.util=ALL-UNNAMED \
        --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
        -cp "target/classes;$CP" CSPARQLRunningExample \
        > "$RESULTS/${cq}_n${N}_runner.log" 2>&1 )

    cp "$RUNNER/answers.json" "$RESULTS/${cq}_n${N}_answers.json" 2>/dev/null
    n_windows=$(grep -c '"queryId"' "$RESULTS/${cq}_n${N}_answers.json" 2>/dev/null || echo "0")
    echo "N=$N $cq done: $n_windows windows"
  done
done

echo "=========== ALL DONE ==========="
