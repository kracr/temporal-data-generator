package tboxaware.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/** Per-conference/cycle generation state: what a single call to TemplateEngine.instantiate() needs. */
public class GenerationContext {
    public final String conferenceLocalName;        // plain series name, e.g. "ICWE" - stable across editions
    public final String conferenceInstanceLocalName; // edition-qualified, e.g. "ICWE_ed3" - the actual individual's IRI
    public final String outputDir;
    public final Random random;
    public final DataSources dataSources;
    public final ConferenceTimeline timeline;
    private final String editionOrdinal;

    private int tweetCounter = 0;

    // Authors and papers are drawn without replacement: each generated user/paper individual gets
    // a distinct source row, and its ID is derived from that row rather than an independent random
    // draw. Two consequences that matter for the TBox this data sits on top of: no two tweets in
    // this run can end up asserting different hasDisplayName literals for the same account IRI
    // (would happen if userLocalId were drawn independently of userName), and no two papers across
    // a run can collide on isAcceptedAt's ObjectMaxCardinality(1) restriction. Organization/city
    // picks stay random-with-replacement below since sharing an affiliation or city across
    // different people is realistic, not an identity bug.
    private final List<Integer> authorPool;
    private final List<Integer> paperPool;
    private int authorCursor = 0;
    private int paperCursor = 0;

    public GenerationContext(String conferenceLocalName, int edition, String outputDir,
                              long seed, DataSources dataSources) {
        this.conferenceLocalName = conferenceLocalName;
        this.conferenceInstanceLocalName = conferenceLocalName + "_ed" + edition;
        this.editionOrdinal = ordinal(edition) + " Edition";
        this.outputDir = outputDir;
        // Mixes both the conference name AND the edition into the seed. Name alone isn't enough:
        // the whole point of the edition parameter is to regenerate the SAME series ("ICWE") year
        // after year, so two editions sharing a base seed is the normal case, not an edge case -
        // without mixing in edition too, every edition of a series would draw the identical
        // author/paper shuffle and collide on isAcceptedAt's ObjectMaxCardinality(1) restriction
        // (confirmed by reproducing it: editions 1 and 2 of the same series picked the same papers).
        this.random = new Random(seed * 1_000_003L + conferenceLocalName.hashCode() * 31L + edition);
        this.dataSources = dataSources;
        this.timeline = new ConferenceTimeline(conferenceLocalName, edition, seed);
        this.authorPool = shuffledIndices(dataSources.authorNames.size(), random);
        this.paperPool = shuffledIndices(dataSources.paperTitles.size(), random);
    }

    /** hasEdition's real TBox range is xsd:string with the example comment "3rd or third", not a bare integer. */
    private static String ordinal(int n) {
        if (n % 100 >= 11 && n % 100 <= 13) {
            return n + "th";
        }
        return switch (n % 10) {
            case 1 -> n + "st";
            case 2 -> n + "nd";
            case 3 -> n + "rd";
            default -> n + "th";
        };
    }

    private static List<Integer> shuffledIndices(int size, Random random) {
        List<Integer> indices = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices, random);
        return indices;
    }

    /** Draws one fresh set of placeholder values for a single tweet instantiation. */
    public Map<String, String> drawPlaceholders() {
        Map<String, String> p = new HashMap<>();
        p.put("conference", conferenceInstanceLocalName);
        p.put("seriesName", conferenceLocalName);
        p.put("edition", editionOrdinal);
        p.put("startDate", ConferenceTimeline.toXsdDateTimeStamp(timeline.conferenceStartMillis()));
        p.put("endDate", ConferenceTimeline.toXsdDateTimeStamp(timeline.conferenceEndMillis()));
        p.put("eventMode", DataSources.randomEventMode(random));
        p.put("track", DataSources.randomTrack(random));
        p.put("domain", DataSources.randomDomain(random));
        p.put("designation", DataSources.randomDesignation(random));

        String[] user = drawAuthor();
        p.put("userLocalId", user[0]);
        p.put("userName", user[1]);

        String[] paper = drawPaper();
        p.put("paperLocalId", paper[0]);
        p.put("paperTitle", paper[1]);

        p.put("org", dataSources.randomOrganizationIri(random));
        p.put("city", dataSources.randomCityIri(random));
        return p;
    }

    /** Draws one more distinct (id, displayName) pair from the same without-replacement author pool,
     *  for templates that need more than one distinct person in a single tweet (e.g. two speakers
     *  under one session, for a genuine tree-shaped eventdata.ttl). */
    public String[] drawAuthor() {
        int authorIndex = nextIndex(authorPool, true);
        return new String[]{"U" + Integer.toHexString(authorIndex), dataSources.authorNames.get(authorIndex)};
    }

    public String[] drawPaper() {
        int paperIndex = nextIndex(paperPool, false);
        return new String[]{"P" + Integer.toHexString(paperIndex), dataSources.paperTitles.get(paperIndex)};
    }

    /** Uniformly draws a timestamp within the fraction [t.timingStart, t.timingEnd] of t.phase's real window. */
    public long drawTimestampMillis(TemplateSpec t) {
        long[] window = timeline.phaseWindow(t.phase);
        long span = window[1] - window[0];
        long lo = window[0] + (long) (span * t.timingStart);
        long hi = window[0] + (long) (span * t.timingEnd);
        if (hi <= lo) {
            return lo;
        }
        return lo + (long) (random.nextDouble() * (hi - lo));
    }

    private int nextIndex(List<Integer> pool, boolean isAuthor) {
        int cursor = isAuthor ? authorCursor : paperCursor;
        if (cursor >= pool.size()) {
            // Only reachable if a single run needs more distinct authors/papers than the real CSV
            // pools contain (647k authors, 181k papers) - reshuffle and start over rather than crash.
            Collections.shuffle(pool, random);
            cursor = 0;
        }
        int value = pool.get(cursor);
        cursor++;
        if (isAuthor) {
            authorCursor = cursor;
        } else {
            paperCursor = cursor;
        }
        return value;
    }

    public String nextTweetId() {
        tweetCounter++;
        return "tweet" + UUID.randomUUID().toString().substring(0, 8) + tweetCounter;
    }
}
