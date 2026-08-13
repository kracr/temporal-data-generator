const fs = require('fs');
const path = require('path');

const RESULTS = process.argv[2] || "D:/temporal-data-generator/CSparqlEval/results";

const CQ_NAMES = {
  cq1: "Trending Topics", cq2: "Active Research Groups", cq3: "Publication Activities",
  cq4: "Conference Match", cq5: "Interdisciplinary Authors", cq6: "Session Popularity",
  cq7: "Global Research Focus", cq8: "Funding Organizations", cq9: "Networking Opportunities",
  cq10: "Collaboration Networks", cq11: "Non-academic Collaborators", cq12: "Geographical Distribution",
  cq13: "Platform Impact",
};

const out = {};
for (let i = 1; i <= 13; i++) {
  const cq = `cq${i}`;
  out[cq] = { name: CQ_NAMES[cq] };
  for (const N of [1, 2, 5]) {
    const ansFile = path.join(RESULTS, `${cq}_n${N}_answers.json`);
    const logFile = path.join(RESULTS, `${cq}_n${N}_runner.log`);
    const data = JSON.parse(fs.readFileSync(ansFile));
    const windows = data.length;
    const nonEmpty = data.filter(d => d.variableBindings && d.variableBindings.length > 0).length;
    const bindings = data.reduce((s, d) => s + (d.variableBindings ? d.variableBindings.length : 0), 0);
    const mems = data.map(d => d.memoryConsumptionInMB);
    const memMin = Math.min(...mems), memMax = Math.max(...mems);
    const memAvg = mems.reduce((a, b) => a + b, 0) / mems.length;
    const ts = data.map(d => d.timestamp).sort((a, b) => a - b);
    const spanS = (ts[ts.length - 1] - ts[0]) / 1000;

    // latency from runner log: "results obtained in <ns> nanoseconds"
    const log = fs.readFileSync(logFile, 'utf8');
    const latMatches = [...log.matchAll(/results obtained in (\d+) nanoseconds/g)].map(m => Number(m[1]) / 1e6); // ms
    const latMin = latMatches.length ? Math.min(...latMatches) : null;
    const latMax = latMatches.length ? Math.max(...latMatches) : null;
    const latAvg = latMatches.length ? latMatches.reduce((a, b) => a + b, 0) / latMatches.length : null;

    const throughput = spanS > 0 ? windows / spanS : 0; // windows/sec

    out[cq][`n${N}`] = {
      windows, nonEmpty, pct: (100 * nonEmpty / windows).toFixed(1),
      bindings, memMin: memMin.toFixed(1), memMax: memMax.toFixed(1), memAvg: memAvg.toFixed(1),
      spanS: spanS.toFixed(1),
      latN: latMatches.length,
      latMin: latMin !== null ? latMin.toFixed(1) : 'n/a',
      latMax: latMax !== null ? latMax.toFixed(1) : 'n/a',
      latAvg: latAvg !== null ? latAvg.toFixed(1) : 'n/a',
      throughput: throughput.toFixed(2),
    };
  }
}

for (const [cq, row] of Object.entries(out)) {
  console.log(`${cq} (${row.name}):`);
  for (const N of [1, 2, 5]) {
    const r = row[`n${N}`];
    console.log(`  N=${N}: windows=${r.windows} nonEmpty=${r.nonEmpty}(${r.pct}%) bindings=${r.bindings} mem=${r.memMin}-${r.memMax}(${r.memAvg}) lat_ms=${r.latMin}-${r.latMax}(${r.latAvg},n=${r.latN}) throughput=${r.throughput}win/s span=${r.spanS}s`);
  }
}

fs.writeFileSync(path.join(path.dirname(RESULTS), "aggregated2.json"), JSON.stringify(out, null, 2));
console.log("\nWrote aggregated2.json");
