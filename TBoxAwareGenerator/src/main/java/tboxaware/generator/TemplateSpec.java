package tboxaware.generator;

import java.util.ArrayList;
import java.util.List;

/** One declarative tweet-template definition, built by TemplateLoader from templates.yaml. */
public class TemplateSpec {
    public String id;
    public String category;   // Announcements | Reminders | Notifications | Insights | Others
    public String phase;      // before | during | after
    public int frequency = 1; // relative weight within its phase
    // Where within the phase's real time window (see ConferenceTimeline) this template's tweets
    // land, as a fraction of the phase's duration: 0.0 = phase start, 1.0 = phase end. E.g. a
    // "call for papers" announcement belongs early in "before" (0.0-0.1); a "registration closes"
    // reminder belongs late (0.7-0.95). Defaults to spanning the whole phase.
    public double timingStart = 0.0;
    public double timingEnd = 1.0;

    public AccountSpec account;         // which kind of Twitter account posts this tweet
    public String tweetText;            // human-readable text, placeholders resolved, not machine-read downstream
    public List<TripleSpec> tweetTriples = new ArrayList<>();   // written to *_metadata.ttl

    public ResourceSpec subjectResource;                        // the ACE-side individual this tweet is "about"
    public List<TripleSpec> eventTriples = new ArrayList<>();    // written to *_eventdata.ttl

    public UserInvolvement userInvolvement; // optional: how the tweet's posting user relates to subjectResource

    // Extra named individuals beyond subjectResource/conference/user, minted fresh per instantiation.
    // Exists so a single tweet's eventdata.ttl can contain a genuine tree shape (a root branching to
    // two children via the same predicate, one of which itself branches to two grandchildren) -
    // CreatePartitions runs the ByShape/tree.txt CONSTRUCT query against one eventdata.ttl file at a
    // time, so the whole pattern must live inside one template's output, not spread across tweets.
    public List<ResourceSpec> extraResources = new ArrayList<>(); // id (ResourceSpec.id) -> individual
    public List<String> extraUsers = new ArrayList<>();           // ids of extra distinct person individuals

    public static class AccountSpec {
        public String className; // Tweet-local class name, e.g. "ConferenceAccount", "PersonAccount"
    }

    public static class ResourceSpec {
        public String id;         // only used for extraResources: the name triples reference it by ("session1")
        public String ns;         // one of: ace, tweet, owl2bench, location
        public String localName;  // may contain {placeholder}s
        public String className;  // ACE/Tweet-local class name this individual instantiates
    }

    public static class TripleSpec {
        public String property;     // local name, namespace resolved by propertyNs
        public String propertyNs;   // one of: ace, tweet, owl2bench, location (default depends on context)
        public String subjectRef;   // "self" | "conference" | "user" | an extraResources/extraUsers id; default "self"
        public String literal;      // set if this asserts a literal value (placeholders resolved)
        public String resourceNs;   // namespace of the resource object, if this asserts a resource
        public String resource;     // "@subject"/"@conference"/"@user"/"@<extra id>", or a local name/full IRI
    }

    public static class UserInvolvement {
        public String role;         // author | attendee | speaker | organizer
        public String roleProperty; // ACE-local object property linking user -> subjectResource (or conference)
        public String linkTarget;   // "subjectResource" | "conference"; default "subjectResource"
    }
}
