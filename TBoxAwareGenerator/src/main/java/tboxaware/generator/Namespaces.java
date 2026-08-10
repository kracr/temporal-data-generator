package tboxaware.generator;

/**
 * Real namespaces as used by the live GenACT pipeline (ABoxGenerator/ConferenceStreams.java),
 * not the namespaces declared inside the TBox OWL files themselves - Tweet.owl declares its own
 * ontology IRI as "http:/anonymous.com/Tweet#" (malformed, missing a slash), but every instance
 * the real pipeline ever writes uses "https://anonymous.com/Tweet#". TBoxVocabulary strips the
 * namespace and matches on local name only, so this mismatch does not affect validation.
 */
public final class Namespaces {
    public static final String ACE = "https://anonymous.com/AcademicConferenceEvent#";
    public static final String TWEET = "https://anonymous.com/Tweet#";
    public static final String OWL2BENCH = "https://kracr.iiitd.edu.in/OWL2Bench#";
    public static final String LOCATION = "https://anonymous.com/Location#";

    public static String uri(String ns, String localName) {
        return ns + localName;
    }

    private Namespaces() {
    }
}
