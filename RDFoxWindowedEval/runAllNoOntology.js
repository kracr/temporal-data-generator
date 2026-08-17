const { runConcurrentCQ } = require("./runWindowedNoOntology.js");
const { CQS } = require("./queries.js");
const fs = require("fs");
const path = require("path");

function confDirsFor(n) {
  const base = `D:/temporal-data-generator/EventData/_N${n}`;
  return fs.readdirSync(base).map(d => path.join(base, d));
}
const CONF_DIRS = { 1: confDirsFor(1), 2: confDirsFor(2), 5: confDirsFor(5) };

const N = parseInt(process.argv[2] || "1", 10);
const cqFilter = process.argv[3];

const confDirs = CONF_DIRS[N];
console.log(`[no-ontology] N=${N}: conferences =`, confDirs);

const cqIds = cqFilter ? [cqFilter] : Object.keys(CQS);

async function main() {
  const summary = [];
  for (const cqId of cqIds) {
    console.log(`Running ${cqId} (N=${N}, ${confDirs.length} concurrent conferences, no TBox) ...`);
    const start = Date.now();
    const results = await runConcurrentCQ(cqId, confDirs, { timeoutMs: 300000 });
    const elapsed = (Date.now() - start) / 1000;
    console.log(`  done in ${elapsed.toFixed(1)}s (all ${confDirs.length} conferences concurrent)`);
    results.forEach((result, ci) => {
      summary.push({ cqId, N, conf: ci, confDir: confDirs[ci], outPath: result.outPath, windows: result.windows, elapsed });
    });
  }

  fs.writeFileSync(
    path.join("D:/temporal-data-generator/RDFoxWindowedEval/results", `run_manifest_N${N}${cqFilter ? "_" + cqFilter : ""}_noreason.json`),
    JSON.stringify(summary, null, 2)
  );
  console.log("All done. Manifest written.");
}

main();
