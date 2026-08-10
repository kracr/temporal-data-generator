const TW = "https://anonymous.com/Tweet#";
const ACE = "https://anonymous.com/AcademicConferenceEvent#";
const LOC = "https://anonymous.com/Location#";
const OWB = "https://kracr.iiitd.edu.in/OWL2Bench#";
const STATIC_ORG = "D:/temporal-data-generator/RDFoxWindowedEval/tbox/organization-func.owl";

// range/step in ms, as used for the CSparql2 evaluation (thesis Table tab:genact-window-tiers).
const TIER = { 1: [60000, 2000], 2: [60000, 2000], 3: [180000, 6000] };

const CQS = {
  cq1:  { tier: 1, select: "?tweet ?hashtag ?conference", body:
`?tweet <${TW}hasHashTag> ?hashtag .
?tweet <${TW}mentionsConference> ?conference .` },
  cq2:  { tier: 2, select: "?person ?org ?tweet", body:
`?person <${TW}hasAffiliation> ?org .
?person <${TW}posts> ?tweet .` },
  cq3:  { tier: 2, select: "?paper ?author ?organization", body:
`?paper a <${ACE}ConferencePaper> .
?paper <${ACE}hasAuthor> ?author .
?author <${TW}hasAffiliation> ?organization .` },
  cq4:  { tier: 3, select: "?author ?domain ?conference", body:
`?paper a <${ACE}ConferencePaper> .
?paper <${ACE}hasAuthor> ?author .
?paper <${ACE}hasPaperDomain> ?domain .
?paper <${ACE}isAcceptedAt> ?conference .` },
  cq5:  { tier: 2, select: "?author ?domain1 ?domain2", body:
`?paper1 a <${ACE}ConferencePaper> .
?paper1 <${ACE}hasAuthor> ?author .
?paper1 <${ACE}hasPaperDomain> ?domain1 .
?paper2 a <${ACE}ConferencePaper> .
?paper2 <${ACE}hasAuthor> ?author .
?paper2 <${ACE}hasPaperDomain> ?domain2 .
FILTER(?domain1 != ?domain2)` },
  cq6:  { tier: 2, select: "?conf ?session ?paper", body:
`?conf <${ACE}hasSession> ?session .
?paper <${ACE}isPresentedAt> ?session .` },
  cq7:  { tier: 1, select: "?organization ?location", body:
`?paper a <${ACE}ConferencePaper> .
?paper <${ACE}hasAuthor> ?author .
?author <${TW}hasAffiliation> ?organization .
?organization <${OWB}hasLocation> ?location .`, staticFrom: STATIC_ORG },
  cq8:  { tier: 3, select: "?student ?organization ?conference", body:
`?student <${ACE}getsStudentGrant> ?organization .
?student <${ACE}attends> ?conference .` },
  cq9:  { tier: 2, select: "?person ?conf ?org", body:
`?person <${ACE}attends> ?conf .
?person <${TW}hasAffiliation> ?org .` },
  cq10: { tier: 2, select: "?paper ?author1 ?author2", body:
`?paper a <${ACE}ConferencePaper> .
?paper <${ACE}hasAuthor> ?author1 .
?paper <${ACE}hasAuthor> ?author2 .
FILTER(?author1 != ?author2)` },
  cq11: { tier: 3, select: "?paper ?author1 ?author2 ?parentOrg", body:
`?paper a <${ACE}ConferencePaper> .
?paper <${ACE}hasAuthor> ?author1 .
?paper <${ACE}hasAuthor> ?author2 .
?author2 <${TW}hasAffiliation> ?subOrg .
?parentOrg <${OWB}isPartOf> ?subOrg .
?parentOrg a <${ACE}NonAcademicOrganization> .
FILTER(?author1 != ?author2)`, staticFrom: STATIC_ORG },
  cq12: { tier: 2, select: "?conf ?city ?person", body:
`?conf <${LOC}hasLocation> ?city .
?person <${ACE}attends> ?conf .` },
  cq13: { tier: 1, select: "?person ?tweet ?hashtag", body:
`?person <${TW}posts> ?tweet .
?tweet <${TW}hasHashTag> ?hashtag .` },
};

module.exports = { TIER, CQS };
