// Variant of runWindowed.js that never loads the ACE-RL TBox, so RDFox answers plain
// SPARQL over the raw asserted RDF stream only -- no OWL 2 RL materialization at all.
// This isolates stream-engine performance from reasoning overhead, giving a comparison
// against C-SPARQL2 (which also never reasons) that is apples-to-apples on workload, not
// just on engine. Everything else (window tiers, replay-rate scaling, incremental
// add/delete per window, N genuinely concurrent RDFox processes) is identical to
// runWindowed.js. Static background data (e.g. Organization.owl for CQ7/CQ11) is still
// loaded, since C-SPARQL2's own real runs also load it as plain RDF -- that's not an
// ontology-reasoning step, just background facts needed for the join to be possible at all.

const fs = require("fs");
const path = require("path");
const { spawn } = require("child_process");
const { TIER, CQS } = require("./queries.js");

const RATE = 200000;
const RDFOX_EXE = "D:/RDFox-win64-x86_64-7.6b/RDFox.exe";
const TMP_DIR = "D:/temporal-data-generator/RDFoxWindowedEval/tmp_noreason";
// queries.js's `staticFrom` points at the OWL-functional-syntax conversion of
// Organization.owl (needed for the with-ontology run's RDFox import). RDFox's
// OWL-functional-syntax import path files some of that content under fact-domain=all
// rather than =explicit even with zero rules loaded (confirmed empirically -- no TBox
// axioms exist in the source file at all, so this isn't reasoning, just an RDFox
// import-path quirk). To keep this run honestly "explicit-only", substitute a plain
// Turtle re-serialization of the same 55,934 triples, which RDFox files as ordinary
// explicit data like every other .ttl import in this pipeline.
const STATIC_ORG_FUNC = "D:/temporal-data-generator/RDFoxWindowedEval/tbox/organization-func.owl";
const STATIC_ORG_PLAIN = "D:/temporal-data-generator/RDFoxWindowedEval/tbox/organization-plain.ttl";

function parseTs(filename) {
  const m = filename.match(/^(\d{8})_(\d{6})/);
  if (!m) return null;
  const y = m[1].slice(0, 4), mo = m[1].slice(4, 6), d = m[1].slice(6, 8);
  const h = m[2].slice(0, 2), mi = m[2].slice(2, 4), s = m[2].slice(4, 6);
  return Date.UTC(+y, +mo - 1, +d, +h, +mi, +s);
}

function loadConferenceFiles(dir) {
  const files = fs.readdirSync(dir).filter(f => f.endsWith(".ttl"));
  const withTs = files.map(f => ({ f, t: parseTs(f) })).filter(x => x.t !== null);
  withTs.sort((a, b) => a.t - b.t);
  return withTs.map(x => ({ path: path.join(dir, x.f), t: x.t }));
}

function computeWindows(sortedFiles, rangeMs, stepMs) {
  const rangeSim = rangeMs * RATE;
  const stepSim = stepMs * RATE;
  const minT = sortedFiles[0].t;
  const maxT = sortedFiles[sortedFiles.length - 1].t;
  const windows = [];
  for (let t = minT + stepSim; t <= maxT + stepSim; t += stepSim) {
    const windowStart = t - rangeSim;
    const inWindow = sortedFiles.filter(x => x.t > windowStart && x.t <= t);
    windows.push({ t, files: inWindow });
  }
  return windows;
}

function buildQueryLine(select, body) {
  const singleLineBody = body.replace(/\n/g, " ").trim();
  return `select ${select} where { ${singleLineBody} }`;
}

function prepareWindowedCQ(cqId, confDir, opts = {}) {
  const def = CQS[cqId];
  const [rangeMs, stepMs] = TIER[def.tier];
  const sortedFiles = loadConferenceFiles(confDir);
  let windows = computeWindows(sortedFiles, rangeMs, stepMs);
  if (opts.limit) windows = windows.slice(0, opts.limit);

  if (!fs.existsSync(TMP_DIR)) fs.mkdirSync(TMP_DIR, { recursive: true });
  const cqTmpDir = path.join(TMP_DIR, opts.label || cqId);
  if (!fs.existsSync(cqTmpDir)) fs.mkdirSync(cqTmpDir, { recursive: true });

  const dsName = cqId;
  const scriptLines = ["set output out", "set reason.monitor summary", `dstore create ${dsName}`, `active ${dsName}`];
  if (def.staticFrom) {
    const staticImport = def.staticFrom === STATIC_ORG_FUNC ? STATIC_ORG_PLAIN : def.staticFrom;
    scriptLines.push(`import ${staticImport}`);
  }
  // Deliberately no `import ${TBOX}` here -- this is the only difference from
  // runWindowed.js's prepareWindowedCQ. No TBox means no rules, so `explicit` and `all`
  // fact-domain counts below should come out identical, which doubles as a sanity check.

  const queryLine = buildQueryLine(def.select, def.body);
  let prevSet = new Set();

  windows.forEach((w, k) => {
    const curSet = new Set(w.files.map(f => f.path));
    const added = w.files.filter(f => !prevSet.has(f.path));
    const removed = [...prevSet].filter(p => !curSet.has(p));

    if (added.length) {
      const addFile = path.join(cqTmpDir, `add${k}.ttl`);
      fs.writeFileSync(addFile, added.map(f => fs.readFileSync(f.path, "utf8")).join("\n"));
      scriptLines.push(`import ${addFile.replace(/\\/g, "/")}`);
    }
    if (removed.length) {
      const delFile = path.join(cqTmpDir, `del${k}.ttl`);
      fs.writeFileSync(delFile, removed.map(p => fs.readFileSync(p, "utf8")).join("\n"));
      scriptLines.push(`import - ${delFile.replace(/\\/g, "/")}`);
    }
    // Order matters for both correctness and for parseOutNoOntology.js's block splitting
    // (it splits on each `set query.fact-domain explicit` echo, so that command must appear
    // exactly once per window). The real query runs second, still under fact-domain=explicit:
    // with no TBox imported, fact-domain=all can still include RDFox's own OWL-functional-
    // syntax-import bookkeeping triples (confirmed via the asserted/materialised sanity check
    // on cq7/cq11, the only two CQs that load the static Organization.owl background file) --
    // explicit keeps this run honestly reasoning-free. materialised count moves last, purely
    // for its own sanity-check value, after it's no longer in the way of the real query.
    scriptLines.push(`set query.fact-domain explicit`);
    scriptLines.push(`select (COUNT(*) as ?asserted) where { ?s ?p ?o }`);
    scriptLines.push(queryLine);
    scriptLines.push(`set query.fact-domain all`);
    scriptLines.push(`select (COUNT(*) as ?materialised) where { ?s ?p ?o }`);

    prevSet = curSet;
  });

  const scriptPath = path.join(cqTmpDir, "run.rdfox");
  fs.writeFileSync(scriptPath, scriptLines.join("\n") + "\n");
  const outPath = path.join(cqTmpDir, "out.txt");

  return { cqId, windows: windows.length, outPath, scriptPath };
}

function execRdfoxScript(scriptPath, outPath, opts = {}) {
  return new Promise((resolve) => {
    const rdfoxDir = path.dirname(RDFOX_EXE);
    const scriptAbs = path.resolve(scriptPath);
    const child = spawn(RDFOX_EXE, ["sandbox", ".", scriptAbs], { cwd: rdfoxDir });
    child.stdin.end();
    let stdout = "", stderr = "";
    const timer = setTimeout(() => {
      child.kill();
      stdout += "\n--TIMEOUT--\n";
    }, opts.timeoutMs || 300000);
    child.stdout.on("data", d => { stdout += d; });
    child.stderr.on("data", d => { stderr += d; });
    child.on("close", () => {
      clearTimeout(timer);
      fs.writeFileSync(outPath, stdout + (stderr ? "\n--STDERR--\n" + stderr : ""));
      resolve({ outPath });
    });
  });
}

async function runConcurrentCQ(cqId, confDirs, opts = {}) {
  const prepared = confDirs.map((confDir, ci) =>
    prepareWindowedCQ(cqId, confDir, { ...opts, label: `${cqId}_N${confDirs.length}_c${ci}` })
  );
  await Promise.all(prepared.map(p => execRdfoxScript(p.scriptPath, p.outPath, opts)));
  return prepared;
}

module.exports = {
  loadConferenceFiles, computeWindows, buildQueryLine,
  prepareWindowedCQ, execRdfoxScript, runConcurrentCQ, RATE,
};
