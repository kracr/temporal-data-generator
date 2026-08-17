// Parser matched to runWindowedNoOntology.js's per-window query order:
// set explicit -> asserted count -> REAL QUERY -> set all -> materialised count.
// (parseOut.js assumes the opposite order -- real query last -- and would silently
// misattribute the materialised-count query's answer-row-count [always 1] as the real
// query's result if reused here, since fact-domain=explicit now only appears once per
// window instead of twice.)
const fs = require("fs");

function parseOut(outPath) {
  const text = fs.readFileSync(outPath, "utf8");
  const blocks = text.split(/(?=query\.fact-domain = "explicit")/).filter(b => b.includes('query.fact-domain = "explicit"'));

  return blocks.map((block, idx) => {
    const countLines = [...block.matchAll(/^\s*([\d,]+)\s*\.\s*$/gm)].map(m => parseInt(m[1].replace(/,/g, ""), 10));
    const asserted = countLines[0] ?? null;
    const materialised = countLines[1] ?? null;

    // Order: [0] = asserted-count query (answers always 1), [1] = REAL query, [2] = materialised-count query (answers always 1).
    const answersMatches = [...block.matchAll(/Number of query answers:\s+(\d+)/g)];
    const queryAnswers = answersMatches.length >= 2 ? parseInt(answersMatches[1][1], 10) : null;

    const latencyMatches = [...block.matchAll(/Total statement evaluation time:\s+([\d.]+) s/g)];
    const queryLatency = latencyMatches.length >= 2 ? parseFloat(latencyMatches[1][1]) : null;

    const importTimeMatches = [...block.matchAll(/Import operation took ([\d.]+) s\./g)];
    const importTime = importTimeMatches.length ? importTimeMatches.reduce((s, m) => s + parseFloat(m[1]), 0) : 0;

    return { window: idx, asserted, materialised, queryAnswers, queryLatency, importTime };
  });
}

if (require.main === module) {
  const outPath = process.argv[2];
  const windows = parseOut(outPath);
  const valid = windows.filter(w => w.queryAnswers !== null);
  const nonEmpty = valid.filter(w => w.queryAnswers > 0);
  console.log(`Total windows: ${windows.length} (parsed: ${valid.length})`);
  console.log(`Non-empty windows: ${nonEmpty.length} (${(100 * nonEmpty.length / valid.length).toFixed(1)}%)`);
  console.log("Sample window (middle):", windows[Math.floor(windows.length / 2)]);
  console.log("First non-empty window:", nonEmpty[0]);
}

module.exports = { parseOut };
