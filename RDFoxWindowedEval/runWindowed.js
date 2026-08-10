// Real sliding-window evaluation of GenACT competency questions against RDFox 7.6,
// with OWL 2 RL materialization (ACE-RL TBox) applied throughout -- our own pipeline,
// built on the already-working RDFox.exe sandbox scripting (own license, own D: install),
// not the old JNI-embedded harness.
//
// Windows are computed directly from the real embedded file timestamps (not live TCP
// replay), using the same range/step tiers and the same replay-rate scaling (200,000)
// already used for the CSparql2 evaluation, so window semantics stay comparable.
//
// One datastore is used for the whole run (not one per window): the static background
// file and the RL TBox are loaded once, and each window transition applies a real
// incremental ADDITION (files newly inside the window) and DELETION (files that fell
// out of it), matching genuine incremental materialization rather than a cold reload
// every step.
//
// N parallel conference streams are executed as N genuinely concurrent RDFox.exe
// processes (see runManyConcurrently), matching how CSparql2's N per-conference engine
// instances actually run at the same time and compete for CPU/memory -- not sequentially,
// which would understate real resource contention at higher N.

const fs = require("fs");
const path = require("path");
const { spawn } = require("child_process");
const { TIER, CQS } = require("./queries.js");

const RATE = 200000;
const RDFOX_EXE = "D:/RDFox-win64-x86_64-7.6b/RDFox.exe";
const TBOX = "D:/temporal-data-generator/RDFoxWindowedEval/tbox/ace-rl-func.owl";
const TMP_DIR = "D:/temporal-data-generator/RDFoxWindowedEval/tmp";

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

// Prepares all files/scripts for one CQ x conference run, without executing RDFox.
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
  if (def.staticFrom) scriptLines.push(`import ${def.staticFrom}`);
  scriptLines.push(`import ${TBOX}`);

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
    scriptLines.push(`set query.fact-domain explicit`);
    scriptLines.push(`select (COUNT(*) as ?asserted) where { ?s ?p ?o }`);
    scriptLines.push(`set query.fact-domain all`);
    scriptLines.push(`select (COUNT(*) as ?materialised) where { ?s ?p ?o }`);
    scriptLines.push(queryLine);

    prevSet = curSet;
  });

  const scriptPath = path.join(cqTmpDir, "run.rdfox");
  fs.writeFileSync(scriptPath, scriptLines.join("\n") + "\n");
  const outPath = path.join(cqTmpDir, "out.txt");

  return { cqId, windows: windows.length, outPath, scriptPath };
}

// Runs one already-prepared RDFox script as a real child process, resolving once it exits.
// Multiple calls can be in-flight at once (see runManyConcurrently) -- each is a fully
// separate RDFox.exe process/instance, so this reproduces genuine concurrent CPU/memory
// contention between conference streams, not just independent-but-serial execution.
function execRdfoxScript(scriptPath, outPath, opts = {}) {
  return new Promise((resolve) => {
    const rdfoxDir = path.dirname(RDFOX_EXE);
    const scriptAbs = path.resolve(scriptPath);
    const child = spawn(RDFOX_EXE, ["sandbox", ".", scriptAbs], { cwd: rdfoxDir });
    child.stdin.end(); // RDFox's sandbox shell waits on stdin after the script finishes unless
                        // it sees EOF here (execFileSync closes stdin by default; spawn does not).
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

// Runs a whole CQ x N conferences run with N genuinely concurrent RDFox processes.
async function runConcurrentCQ(cqId, confDirs, opts = {}) {
  const prepared = confDirs.map((confDir, ci) =>
    prepareWindowedCQ(cqId, confDir, { ...opts, label: `${cqId}_N${confDirs.length}_c${ci}` })
  );
  await Promise.all(prepared.map(p => execRdfoxScript(p.scriptPath, p.outPath, opts)));
  return prepared;
}

// Single CQ x single conference, synchronous-style convenience wrapper for CLI/testing use.
function runWindowedCQ(cqId, confDir, opts = {}) {
  const { execFileSync } = require("child_process");
  const prepared = prepareWindowedCQ(cqId, confDir, opts);
  const rdfoxDir = path.dirname(RDFOX_EXE);
  const scriptAbs = path.resolve(prepared.scriptPath);
  let output;
  try {
    output = execFileSync(RDFOX_EXE, ["sandbox", ".", scriptAbs], {
      cwd: rdfoxDir,
      encoding: "utf8",
      maxBuffer: 1024 * 1024 * 200,
      timeout: opts.timeoutMs || 300000,
    });
  } catch (e) {
    output = (e.stdout || "") + "\n--STDERR--\n" + (e.stderr || "") + "\n--ERROR--\n" + e.message;
  }
  fs.writeFileSync(prepared.outPath, output);
  return prepared;
}

module.exports = {
  loadConferenceFiles, computeWindows, buildQueryLine,
  prepareWindowedCQ, execRdfoxScript, runConcurrentCQ, runWindowedCQ, RATE,
};

if (require.main === module) {
  const cqId = process.argv[2] || "cq3";
  const confDir = process.argv[3] || "D:/temporal-data-generator/EventData/_N1/conf0_1970";
  const limit = process.argv[4] ? parseInt(process.argv[4], 10) : undefined;
  console.log(`Running ${cqId} against ${confDir}${limit ? " (limit " + limit + " windows)" : ""}...`);
  const result = runWindowedCQ(cqId, confDir, { limit });
  console.log(`Done. ${result.windows} windows. Output: ${result.outPath}`);
}
