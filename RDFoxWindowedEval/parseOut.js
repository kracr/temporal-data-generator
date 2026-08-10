// Parses a run's out.txt (incremental single-datastore design) into per-window records.
const fs = require("fs");

function parseOut(outPath) {
  const text = fs.readFileSync(outPath, "utf8");
  // Each window starts with 'query.fact-domain = "explicit"' (from `set query.fact-domain explicit`).
  const blocks = text.split(/(?=query\.fact-domain = "explicit")/).filter(b => b.includes('query.fact-domain = "explicit"'));

  return blocks.map((block, idx) => {
    // Order within a block: COUNT(*) explicit -> "N ." then answers=1; then fact-domain=all;
    // COUNT(*) all -> "M ." then answers=1; then the real query -> answers=K (K may be 0).
    const countLines = [...block.matchAll(/^\s*([\d,]+)\s*\.\s*$/gm)].map(m => parseInt(m[1].replace(/,/g, ""), 10));
    const asserted = countLines[0] ?? null;
    const materialised = countLines[1] ?? null;

    const answersMatches = [...block.matchAll(/Number of query answers:\s+(\d+)/g)];
    const queryAnswers = answersMatches.length >= 3 ? parseInt(answersMatches[2][1], 10) : null;

    const latencyMatches = [...block.matchAll(/Total statement evaluation time:\s+([\d.]+) s/g)];
    const queryLatency = latencyMatches.length >= 3 ? parseFloat(latencyMatches[2][1]) : null;

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
  const avgLatency = valid.reduce((s, w) => s + (w.queryLatency || 0), 0) / valid.length;
  const totalBindings = valid.reduce((s, w) => s + (w.queryAnswers || 0), 0);
  console.log("Avg query latency (s):", avgLatency.toFixed(4));
  console.log("Total bindings across all windows:", totalBindings);
  console.log("Final asserted/materialised:", windows[windows.length - 1]);
}

module.exports = { parseOut };
