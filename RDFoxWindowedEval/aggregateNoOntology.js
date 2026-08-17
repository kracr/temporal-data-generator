const fs = require("fs");
const path = require("path");
const { parseOut } = require("./parseOutNoOntology.js");
const { CQS } = require("./queries.js");

function aggregateForN(N) {
  const manifest = JSON.parse(fs.readFileSync(`D:/temporal-data-generator/RDFoxWindowedEval/results/run_manifest_N${N}_noreason.json`, "utf8"));
  const byCQ = {};
  for (const cqId of Object.keys(CQS)) byCQ[cqId] = [];
  for (const entry of manifest) {
    const windows = parseOut(entry.outPath).filter(w => w.queryAnswers !== null);
    byCQ[entry.cqId].push({ conf: entry.conf, windows });
  }

  const results = {};
  for (const cqId of Object.keys(CQS)) {
    const streams = byCQ[cqId];
    const allWindows = streams.flatMap(s => s.windows);
    const nonEmpty = allWindows.filter(w => w.queryAnswers > 0);
    const avgLatencyMs = 1000 * allWindows.reduce((s, w) => s + (w.queryLatency || 0), 0) / allWindows.length;
    const totalBindings = allWindows.reduce((s, w) => s + (w.queryAnswers || 0), 0);
    const finalAsserted = streams.reduce((s, st) => s + (st.windows[st.windows.length - 1]?.asserted || 0), 0);
    const finalMaterialised = streams.reduce((s, st) => s + (st.windows[st.windows.length - 1]?.materialised || 0), 0);

    results[cqId] = {
      windows: allWindows.length,
      nonEmptyPct: 100 * nonEmpty.length / allWindows.length,
      avgLatencyMs,
      totalBindings,
      finalAsserted,
      finalMaterialised,
    };
  }
  return results;
}

if (require.main === module) {
  const out = {};
  for (const N of [1, 2, 5]) {
    out[N] = aggregateForN(N);
  }
  fs.writeFileSync("D:/temporal-data-generator/RDFoxWindowedEval/results/aggregated_noreason.json", JSON.stringify(out, null, 2));

  const cqNames = {
    cq1: "Trending Topics", cq2: "Active Research Groups", cq3: "Publication Activities",
    cq4: "Conference Match", cq5: "Interdisciplinary Authors", cq6: "Session Popularity",
    cq7: "Global Research Focus", cq8: "Funding Organizations", cq9: "Networking Opportunities",
    cq10: "Collaboration Networks", cq11: "Non-academic Collaborators", cq12: "Geographical Distribution",
    cq13: "Platform Impact",
  };

  console.log("Sanity check (asserted should equal materialised -- no TBox loaded):");
  for (const cqId of Object.keys(CQS)) {
    const r5 = out[5][cqId];
    const ok = r5.finalAsserted === r5.finalMaterialised ? "OK" : "MISMATCH";
    console.log(`  ${cqId}: asserted=${r5.finalAsserted} materialised=${r5.finalMaterialised} [${ok}]`);
  }

  console.log("\n--- LaTeX rows (N1lat N2lat N5lat | N1ne N2ne N5ne) ---");
  let i = 1;
  for (const cqId of Object.keys(CQS)) {
    const r1 = out[1][cqId], r2 = out[2][cqId], r5 = out[5][cqId];
    const fmt = ms => ms < 1 ? ms.toFixed(3) : ms.toFixed(1);
    console.log(
      `${i++} & ${cqNames[cqId]} & ${fmt(r1.avgLatencyMs)} & ${fmt(r2.avgLatencyMs)} & ${fmt(r5.avgLatencyMs)} & ${r1.nonEmptyPct.toFixed(1)} & ${r2.nonEmptyPct.toFixed(1)} & ${r5.nonEmptyPct.toFixed(1)} \\\\`
    );
  }
}

module.exports = { aggregateForN };
