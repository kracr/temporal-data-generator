package genact.temporal.data.generator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class DuringConference {
	ConferenceStreams conf;
	long startTime;
	long endTime;
	public DuringConference(ConferenceStreams conf, long startTime, long endTime) {
		this.startTime=startTime;
		this.endTime=endTime;
		// Convert timestamps to LocalDateTime
        LocalDateTime beforeConferenceEnd = LocalDateTime.ofInstant(new Date(startTime).toInstant(), ZoneId.systemDefault());
        LocalDateTime duringConferenceEnd = LocalDateTime.ofInstant(new Date(endTime).toInstant(), ZoneId.systemDefault());
       
        // Display timestamps for each phase
        System.out.println("Cycle " + conf.confCycle);
        System.out.println("During Conference Start: " + beforeConferenceEnd);
        
        SessionReminder(this.startTime,this.endTime);
        UpcomingPaperPresentation(this.startTime,this.endTime);
        StudentPresentationReminder(this.startTime,this.endTime);
        PaperPresentationReminder(this.startTime,this.endTime);
        InsightsBasedOnPresentations(this.startTime,this.endTime);
        NetworkingExperience(this.startTime,this.endTime);
//        SessionReminder, UpcomingPaperPresentation, StudentPresentationReminder, PaperPresentationReminder,
//        InsightsBasedOnPresentations, NetworkingExperience
        System.out.println("During Conference End: " + duringConferenceEnd);
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

