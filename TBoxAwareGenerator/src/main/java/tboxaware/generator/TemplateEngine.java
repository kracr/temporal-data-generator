package tboxaware.generator;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Instantiates one TemplateSpec into a real (metadata.ttl, eventdata.ttl) file pair, following
 * the same two-model split and file-naming convention as the existing hardcoded pipeline
 * (BeforeConference/DuringConference/AfterConference) so GenActStreamServer and the runner
 * harnesses can consume this module's output unchanged.
 */
public class TemplateEngine {
    private static final SimpleDateFormat FILE_TIMESTAMP = new SimpleDateFormat("yyyyMMdd_HHmmss");

    private final GenerationContext ctx;

    public TemplateEngine(GenerationContext ctx) {
        this.ctx = ctx;
    }

    public void instantiate(TemplateSpec t) throws Exception {
        Map<String, String> ph = ctx.drawPlaceholders();
        String tweetId = ctx.nextTweetId();
        long timestampMillis = ctx.drawTimestampMillis(t);
        String fileStamp = FILE_TIMESTAMP.format(new Date(timestampMillis));

        // Edition-qualified: recurring editions of the same series ("ICWE_ed1", "ICWE_ed2", ...) are
        // distinct individuals, not the same resource reasserted with conflicting hasStartDate/
        // hasEdition facts each time.
        String conferenceIri = Namespaces.uri(Namespaces.ACE, ctx.conferenceInstanceLocalName);
        String userIri = Namespaces.uri(Namespaces.ACE, ph.get("userLocalId"));
        String subjectIri = t.subjectResource == null
                ? conferenceIri
                : Namespaces.uri(nsFor(t.subjectResource.ns), resolve(t.subjectResource.localName, ph));
        String accountIri = resolveAccountIri(t.account.className, ctx.conferenceLocalName, userIri, ph);

        // Every node a triple's subjectRef/resource can address by name: "self"/"conference"/"user"
        // plus any of this template's extraResources/extraUsers (e.g. "session1", "speakerB") -
        // extras exist so one tweet's eventdata.ttl can contain multiple distinct sessions/people,
        // which is what makes a genuine tree shape possible within a single file.
        Map<String, String> namedNodes = new HashMap<>();
        namedNodes.put("self", subjectIri);
        namedNodes.put("subject", subjectIri);
        namedNodes.put("conference", conferenceIri);
        namedNodes.put("user", userIri);
        for (TemplateSpec.ResourceSpec extra : t.extraResources) {
            namedNodes.put(extra.id, Namespaces.uri(nsFor(extra.ns), resolve(extra.localName, ph)));
        }
        for (String extraUserId : t.extraUsers) {
            String[] drawn = ctx.drawAuthor();
            namedNodes.put(extraUserId, Namespaces.uri(Namespaces.ACE, drawn[0]));
        }

        Model tweetModel = ModelFactory.createDefaultModel();
        Resource tweetRes = tweetModel.createResource(Namespaces.uri(Namespaces.TWEET, tweetId));
        Resource accountRes = tweetModel.createResource(accountIri);
        tweetModel.add(tweetRes, RDF_TYPE(tweetModel), tweetModel.createResource(Namespaces.uri(Namespaces.TWEET, "Tweet")));
        tweetModel.add(accountRes, RDF_TYPE(tweetModel), tweetModel.createResource(Namespaces.uri(Namespaces.TWEET, t.account.className)));
        tweetModel.add(accountRes, tweetModel.createProperty(Namespaces.uri(Namespaces.TWEET, "posts")), tweetRes);
        tweetModel.add(tweetRes, tweetModel.createProperty(Namespaces.uri(Namespaces.TWEET, "hasTweetID")), tweetModel.createLiteral(tweetId));
        tweetModel.add(tweetRes, tweetModel.createProperty(Namespaces.uri(Namespaces.TWEET, "hasDateTimestamp")),
                tweetModel.createLiteral(new Date(timestampMillis).toInstant().toString()));

        if ("PersonAccount".equals(t.account.className)) {
            tweetModel.add(accountRes, tweetModel.createProperty(Namespaces.uri(Namespaces.TWEET, "hasUserID")), tweetModel.createLiteral(ph.get("userLocalId")));
            tweetModel.add(accountRes, tweetModel.createProperty(Namespaces.uri(Namespaces.TWEET, "hasDisplayName")), tweetModel.createLiteral(ph.get("userName")));
            tweetModel.add(accountRes, tweetModel.createProperty(Namespaces.uri(Namespaces.TWEET, "hasAffiliation")), tweetModel.createResource(ph.get("org")));
            tweetModel.add(accountRes, tweetModel.createProperty(Namespaces.uri(Namespaces.TWEET, "hasDesignation")), tweetModel.createLiteral(ph.get("designation")));
        } else if ("ConferenceAccount".equals(t.account.className)) {
            tweetModel.add(accountRes, tweetModel.createProperty(Namespaces.uri(Namespaces.TWEET, "hasUserID")), tweetModel.createLiteral(ctx.conferenceLocalName + "Handle"));
            tweetModel.add(accountRes, tweetModel.createProperty(Namespaces.uri(Namespaces.TWEET, "hasDisplayName")), tweetModel.createLiteral("International Conference on " + ctx.conferenceLocalName));
        }

        for (TemplateSpec.TripleSpec triple : t.tweetTriples) {
            applyTriple(tweetModel, triple, namedNodes, ph, "tweet");
        }

        Model eventModel = ModelFactory.createDefaultModel();
        Resource conferenceRes = eventModel.createResource(conferenceIri);
        Resource subjectRes = eventModel.createResource(subjectIri);
        eventModel.add(conferenceRes, RDF_TYPE(eventModel), eventModel.createResource(Namespaces.uri(Namespaces.ACE, "Conference")));
        if (t.subjectResource != null) {
            eventModel.add(subjectRes, RDF_TYPE(eventModel), eventModel.createResource(Namespaces.uri(nsFor(t.subjectResource.ns), t.subjectResource.className)));
        }
        for (TemplateSpec.ResourceSpec extra : t.extraResources) {
            eventModel.add(eventModel.createResource(namedNodes.get(extra.id)), RDF_TYPE(eventModel),
                    eventModel.createResource(Namespaces.uri(nsFor(extra.ns), extra.className)));
        }

        for (TemplateSpec.TripleSpec triple : t.eventTriples) {
            applyTriple(eventModel, triple, namedNodes, ph, "ace");
        }

        if (t.userInvolvement != null) {
            Resource userRes = eventModel.createResource(userIri);
            Resource target = "conference".equals(t.userInvolvement.linkTarget) ? conferenceRes : subjectRes;
            Property roleProp = eventModel.createProperty(Namespaces.uri(Namespaces.ACE, t.userInvolvement.roleProperty));
            eventModel.add(userRes, roleProp, target);
            String roleClass = roleClassFor(t.userInvolvement.role);
            if (roleClass != null) {
                eventModel.add(userRes, RDF_TYPE(eventModel), eventModel.createResource(Namespaces.uri(Namespaces.ACE, roleClass)));
            }
        }

        String base = ctx.outputDir + "/" + fileStamp + "_" + tweetId;
        writeModel(tweetModel, base + "_metadata.ttl");
        writeModel(eventModel, base + "_eventdata.ttl");
    }

    private void applyTriple(Model model, TemplateSpec.TripleSpec triple, Map<String, String> namedNodes,
                              Map<String, String> ph, String defaultPropertyNs) {
        String subjectKey = triple.subjectRef == null ? "self" : triple.subjectRef;
        String subjectIriForTriple = namedNodes.getOrDefault(subjectKey, namedNodes.get("self"));
        Resource subject = model.createResource(subjectIriForTriple);
        String propNs = nsFor(triple.propertyNs != null ? triple.propertyNs : defaultPropertyNs);
        Property property = model.createProperty(Namespaces.uri(propNs, triple.property));
        if (triple.literal != null) {
            model.add(subject, property, model.createLiteral(resolve(triple.literal, ph)));
        } else if (triple.resource != null) {
            String objectIri = resolveResourceRef(triple.resource, triple.resourceNs, namedNodes, ph);
            model.add(subject, property, model.createResource(objectIri));
        }
    }

    private String resolveResourceRef(String raw, String ns, Map<String, String> namedNodes, Map<String, String> ph) {
        if (raw.startsWith("@")) {
            String key = raw.substring(1);
            String resolved = namedNodes.get(key);
            if (resolved == null) {
                throw new IllegalArgumentException("Template references unknown node '" + raw
                        + "' - not self/conference/user and not declared in extraResources/extraUsers.");
            }
            return resolved;
        }
        String resolved = resolve(raw, ph);
        if (resolved.startsWith("http")) {
            return resolved;
        }
        return Namespaces.uri(nsFor(ns), resolved);
    }

    private String resolveAccountIri(String accountClass, String conference, String userIri, Map<String, String> ph) {
        return switch (accountClass) {
            case "ConferenceAccount" -> Namespaces.uri(Namespaces.TWEET, conference + "Account");
            case "OrganizationAccount" -> ph.get("org");
            default -> userIri; // PersonAccount
        };
    }

    private static String roleClassFor(String role) {
        return switch (role) {
            case "author" -> "Author";
            case "attendee" -> "Attendee";
            case "organizer" -> "Organizer";
            case "volunteer" -> "Volunteer";
            default -> null; // e.g. "speaker" - the ACE ontology's Speaker class has an inconsistent namespace, skip typing
        };
    }

    private static String nsFor(String ns) {
        if (ns == null) {
            return Namespaces.ACE;
        }
        return switch (ns) {
            case "tweet" -> Namespaces.TWEET;
            case "owl2bench" -> Namespaces.OWL2BENCH;
            case "location" -> Namespaces.LOCATION;
            default -> Namespaces.ACE;
        };
    }

    private static String resolve(String template, Map<String, String> placeholders) {
        String result = template;
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            result = result.replace("{" + e.getKey() + "}", e.getValue());
        }
        return result;
    }

    private static Property RDF_TYPE(Model m) {
        return m.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    }

    private static void writeModel(Model model, String path) throws Exception {
        try (OutputStream out = new FileOutputStream(path)) {
            RDFDataMgr.write(out, model, RDFFormat.TURTLE_PRETTY);
        }
    }
}
