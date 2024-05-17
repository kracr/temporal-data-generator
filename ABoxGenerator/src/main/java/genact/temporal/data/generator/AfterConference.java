package genact.temporal.data.generator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class AfterConference {
	ConferenceStreams conf;
	long startTime;
	long endTime;
	public AfterConference(ConferenceStreams conf, long startTime, long endTime) {
		// Convert timestamps to LocalDateTime
		this.startTime=startTime;
		this.endTime=endTime;
        LocalDateTime duringConferenceEnd = LocalDateTime.ofInstant(new Date(this.startTime).toInstant(), ZoneId.systemDefault());
        LocalDateTime afterConferenceEnd = LocalDateTime.ofInstant(new Date(this.endTime).toInstant(), ZoneId.systemDefault());

        // Display timestamps for each phase
        System.out.println("Cycle " + conf.confCycle);
        System.out.println("Before Conference Start: " + duringConferenceEnd);
        BestPaperTrackAwardAnnouncement(this.startTime,this.endTime);
        NextConferenceAnnouncement(this.startTime,this.endTime);
        SuccessfulEventCongratulations(this.startTime,this.endTime);
        MemorableConferenceExperience(this.startTime,this.endTime);
        ConferenceSuccessThankYou(this.startTime,this.endTime);
//        BestPaperTrackAwardAnnouncement, NextConferenceAnnouncement, SuccessfulEventCongratulations, 
//        MemorableConferenceExperience, ConferenceSuccessThankYou
//        for each tweet assign timestamp randomly getRandomTimestamp(startTime, endTime); (but change this time to long?)
        System.out.println("After Conference End: " + afterConferenceEnd);
	}
	public static LocalDateTime getRandomTimestamp(LocalDateTime start, LocalDateTime end) {
	    long days = start.until(end, ChronoUnit.DAYS);
	    long randomDays = ThreadLocalRandom.current().nextLong(days + 1);
	    long randomHours = ThreadLocalRandom.current().nextLong(24);
	    long randomMinutes = ThreadLocalRandom.current().nextLong(60);
	    long randomSeconds = ThreadLocalRandom.current().nextLong(60);
	    
	    return start
	        .plusDays(randomDays)
	        .plusHours(randomHours)
	        .plusMinutes(randomMinutes)
	        .plusSeconds(randomSeconds);
	}
}

