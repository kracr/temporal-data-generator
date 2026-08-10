package tboxaware.generator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Replicates the real cycle-timing formula from ABoxGenerator's ConferenceStreams.java (lines
 * ~300-378), instead of inventing a new one: overall cycle length is 6-9 months
 * (entitiesCount.properties: conferenceDuration_min/max_months), split into a "during" window of
 * 4-7 days and an "after" window of 4-7 days (tweetsCount.properties), with everything left over
 * assigned to "before". Recurring editions of the same series land ~1 year apart with up to +/-30
 * days of jitter, matching ConferenceStreams' own
 * {@code nextCycleStart.plusYears(1).plusDays(random.nextInt(30) - 30)}.
 *
 * One deliberate simplification versus the original: ConferenceStreams derives each edition's
 * jitter by replaying every prior cycle's random draw in sequence, so edition N's date depends on
 * editions 1..N-1 having actually been generated first. This class instead derives edition N's
 * jitter directly from a Random seeded on (series name, edition number), so any single edition can
 * be generated in isolation - needed since this module's CLI generates one edition per invocation,
 * not a whole multi-cycle run in one process. The real intent (same time of year, some drift,
 * never bit-identical across editions) is preserved; the exact jitter sequence is not.
 *
 * One deliberate correction versus a first draft of this class: the +/-30-day jitter anchors the
 * conference's actual date (the "during" window) across editions, not the start of the "before"
 * lead-up. Anchoring the lead-up start instead would let the actual event drift by however long
 * that edition's randomized 6-9 month lead-up happens to be (~90 days of swing) - i.e. "ICWE" could
 * land in April one year and August the next, which defeats the point of a recurring series. The
 * lead-up length still varies per edition (realistic - CFP timing isn't identical every year); it's
 * just computed backwards from the anchored conference date instead of forwards from a moving one.
 */
public class ConferenceTimeline {
    private static final DateTimeFormatter XSD_DATETIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final long beforeStartMillis;
    private final long beforeEndMillis;   // == conference start
    private final long duringEndMillis;   // == conference end
    private final long afterEndMillis;

    public ConferenceTimeline(String conferenceLocalName, int edition, long baseSeed) {
        Random seriesRandom = new Random(conferenceLocalName.hashCode());
        // Stable per series across editions: this conference always happens in roughly the same
        // month/day, like a real recurring venue.
        int anchorMonth = 1 + seriesRandom.nextInt(12);
        int anchorDay = 1 + seriesRandom.nextInt(28);
        int anchorYear = LocalDate.now().getYear();

        Random editionRandom = new Random(baseSeed * 1_000_003L + conferenceLocalName.hashCode() * 31L + edition);
        int jitterDays = editionRandom.nextInt(61) - 30; // -30..+30, matching the real formula
        int overallDurationMonths = 6 + editionRandom.nextInt(4); // 6..9 (conferenceDuration_min/max_months)
        int duringDays = 4 + editionRandom.nextInt(4);            // 4..7 (during_conference_days_min/max)
        int afterDays = 4 + editionRandom.nextInt(4);              // 4..7 (after_conference_days_min/max)
        int beforeDays = Math.max(1, overallDurationMonths * 30 - duringDays - afterDays);

        // anchorDate is the conference's actual date this edition - stable per series, jittered by
        // at most +/-30 days across editions, independent of how long that edition's lead-up is.
        LocalDate anchorDate = LocalDate.of(anchorYear, anchorMonth, anchorDay)
                .plusYears(edition - 1L)
                .plusDays(jitterDays);
        LocalDateTime conferenceStart = anchorDate.atStartOfDay();

        this.beforeEndMillis = toMillis(conferenceStart);
        this.beforeStartMillis = beforeEndMillis - beforeDays * 86_400_000L;
        this.duringEndMillis = beforeEndMillis + duringDays * 86_400_000L;
        this.afterEndMillis = duringEndMillis + afterDays * 86_400_000L;
    }

    private static long toMillis(LocalDateTime dt) {
        return dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /** [start, end) millis for the named phase - "before", "during", or "after". */
    public long[] phaseWindow(String phase) {
        return switch (phase) {
            case "before" -> new long[]{beforeStartMillis, beforeEndMillis};
            case "during" -> new long[]{beforeEndMillis, duringEndMillis};
            case "after" -> new long[]{duringEndMillis, afterEndMillis};
            default -> throw new IllegalArgumentException("Unknown phase: " + phase);
        };
    }

    public long conferenceStartMillis() {
        return beforeEndMillis;
    }

    public long conferenceEndMillis() {
        return duringEndMillis;
    }

    /** hasStartDate/hasEndDate's real TBox range is xsd:dateTimeStamp, which requires an explicit offset. */
    public static String toXsdDateTimeStamp(long millis) {
        return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(millis), ZoneId.systemDefault())
                .atZone(ZoneId.systemDefault())
                .format(XSD_DATETIME);
    }
}
