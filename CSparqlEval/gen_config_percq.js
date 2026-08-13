const fs = require('fs');
const path = require('path');

const TW = "https://anonymous.com/Tweet#";
const ACE = "https://anonymous.com/AcademicConferenceEvent#";
const LOC = "https://anonymous.com/Location#";
const OWB = "https://kracr.iiitd.edu.in/OWL2Bench#";
const STATIC_ORG = "file:///D:/temporal-data-generator/StaticData/Organization.owl";

const TIER = { 1: [60000, 2], 2: [60000, 2], 3: [180000, 6] };

const CQS = {
  cq1:  { n: 1, tier: 1, select: "?tweet ?hashtag ?conference", body:
`?tweet <${TW}hasHashTag> ?hashtag .
?tweet <${TW}mentionsConference> ?conference .` },
  cq2:  { n: 2, tier: 2, select: "?person ?org ?tweet", body:
`?person <${TW}hasAffiliation> ?org .
?person <${TW}posts> ?tweet .` },
  cq3:  { n: 3, tier: 2, select: "?paper ?author ?organization", body:
`?paper a <${ACE}ConferencePaper> .
?paper <${ACE}hasAuthor> ?author .
?author <${TW}hasAffiliation> ?organization .` },
  cq4:  { n: 4, tier: 3, select: "?author ?domain ?conference", body:
`?paper a <${ACE}ConferencePaper> .
?paper <${ACE}hasAuthor> ?author .
?paper <${ACE}hasPaperDomain> ?domain .
?paper <${ACE}isAcceptedAt> ?conference .` },
  cq5:  { n: 5, tier: 2, select: "?author ?domain1 ?domain2", body:
`?paper1 a <${ACE}ConferencePaper> .
?paper1 <${ACE}hasAuthor> ?author .
?paper1 <${ACE}hasPaperDomain> ?domain1 .
?paper2 a <${ACE}ConferencePaper> .
?paper2 <${ACE}hasAuthor> ?author .
?paper2 <${ACE}hasPaperDomain> ?domain2 .
FILTER(?domain1 != ?domain2)` },
  cq6:  { n: 6, tier: 2, select: "?conf ?session ?paper", body:
`?conf <${ACE}hasSession> ?session .
?paper <${ACE}isPresentedAt> ?session .` },
  cq7:  { n: 7, tier: 1, select: "?organization ?location", body:
`?paper a <${ACE}ConferencePaper> .
?paper <${ACE}hasAuthor> ?author .
?author <${TW}hasAffiliation> ?organization .
?organization <${OWB}hasLocation> ?location .`, staticFrom: STATIC_ORG },
  cq8:  { n: 8, tier: 3, select: "?student ?organization ?conference", body:
`?student <${ACE}getsStudentGrant> ?organization .
?student <${ACE}attends> ?conference .` },
  cq9:  { n: 9, tier: 2, select: "?person ?conf ?org", body:
`?person <${ACE}attends> ?conf .
?person <${TW}hasAffiliation> ?org .` },
  cq10: { n: 10, tier: 2, select: "?paper ?author1 ?author2", body:
`?paper a <${ACE}ConferencePaper> .
?paper <${ACE}hasAuthor> ?author1 .
?paper <${ACE}hasAuthor> ?author2 .
FILTER(?author1 != ?author2)` },
  cq11: { n: 11, tier: 3, select: "?paper ?author1 ?author2 ?parentOrg", body:
`?paper a <${ACE}ConferencePaper> .
?paper <${ACE}hasAuthor> ?author1 .
?paper <${ACE}hasAuthor> ?author2 .
?author2 <${TW}hasAffiliation> ?subOrg .
?parentOrg <${OWB}isPartOf> ?subOrg .
?parentOrg a <${ACE}NonAcademicOrganization> .
FILTER(?author1 != ?author2)`, staticFrom: STATIC_ORG },
  cq12: { n: 12, tier: 2, select: "?conf ?city ?person", body:
`?conf <${LOC}hasLocation> ?city .
?person <${ACE}attends> ?conf .` },
  cq13: { n: 13, tier: 1, select: "?person ?tweet ?hashtag", body:
`?person <${TW}posts> ?tweet .
?tweet <${TW}hasHashTag> ?hashtag .` },
};

const BASEPORT = {
  cq1: 9010, cq2: 9020, cq3: 9030, cq4: 9040, cq5: 9050, cq6: 9060,
  cq7: 9070, cq8: 9081, cq9: 9090, cq10: 9100, cq11: 9110, cq12: 9120, cq13: 9130,
};

function buildQueryText(cqid, def, port, suffix) {
  const [range, step] = TIER[def.tier];
  const name = `${cqid}${suffix}`;
  let lines = [`REGISTER QUERY ${name} AS`, `select ${def.select}`,
    ` from stream <http://localhost:${port}/conference> [range ${range}ms step ${step}s]`];
  if (def.staticFrom) lines.push(` FROM <${def.staticFrom}>`);
  lines.push(`where {`, def.body, `}`);
  return lines.join("\n");
}

const N = parseInt(process.argv[2], 10);
const outDir = process.argv[3]; // e.g. D:/temporal-data-generator/CSparqlEval/cqruns

const suffixLetters = "abcdefghij";
for (const [cqid, def] of Object.entries(CQS)) {
  const basePort = BASEPORT[cqid];
  const queries = {};
  for (let i = 0; i < N; i++) {
    const port = basePort + i;
    const key = `${cqid}_n${N}_${suffixLetters[i]}`;
    queries[key] = buildQueryText(cqid, def, port, `n${N}${suffixLetters[i]}`);
  }
  const config = { queries, queryDuplicates: {}, queriesUnsupported: [] };
  const dir = path.join(outDir, cqid);
  fs.mkdirSync(dir, { recursive: true });
  fs.writeFileSync(path.join(dir, "configuration.json"), JSON.stringify(config, null, 2));
}
console.log(`Wrote per-CQ configs for N=${N} under ${outDir}`);
