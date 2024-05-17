package genact.temporal.data.generator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.time.temporal.ChronoUnit;

public class BeforeConference {
	ConferenceStreams conf;
	Random random;

	public BeforeConference(ConferenceStreams conf, long startTime, long endTime) {
		this.random = conf.random;
		// Convert timestamps to LocalDateTime
		LocalDateTime conferenceStart = LocalDateTime.ofInstant(new Date(startTime).toInstant(),
				ZoneId.systemDefault());
		LocalDateTime beforeConferenceEnd = LocalDateTime.ofInstant(new Date(endTime).toInstant(),
				ZoneId.systemDefault());

		// Display timestamps for each phase
		System.out.println("Cycle " + conf.confCycle);
		System.out.println("Before Conference Start: " + conferenceStart);

		LocalDateTime mainConfAnnouncementTime = conferenceStart;
		MainConferenceAnnouncement(mainConfAnnouncementTime);

		LocalDateTime callForPapersTime = conferenceStart.plusDays(this.random.nextInt(2))
				.plusHours(ThreadLocalRandom.current().nextLong(24))
				.plusMinutes(ThreadLocalRandom.current().nextLong(60))
				.plusSeconds(ThreadLocalRandom.current().nextLong(60));
		CallForPapersAnnouncement(callForPapersTime);
		LocalDateTime midConferenceTime = conferenceStart
				.plusDays(conferenceStart.until(beforeConferenceEnd, ChronoUnit.DAYS) / 2); // Adjust conferenceDuration

		LocalDateTime excitementAnnouncementTime = getRandomTimestamp(callForPapersTime, midConferenceTime);
		ExcitementAboutTheConferenceAnnouncement(excitementAnnouncementTime);
		
		// Conference phase)
		LocalDateTime paperSubmissionReminderTime = conferenceStart.plusWeeks(2); // Initial reminder after 2 weeks

		while (paperSubmissionReminderTime.isBefore(mainConfAnnouncementTime.minusWeeks(9))) { // Stop before
																								// notificatons are out
																								// starts
			// Schedule submission reminder tweet
			PaperSubmissionReminderTime(paperSubmissionReminderTime);
			System.out.println("Paper Submission Reminder Tweet at: " + submissionReminderTime);
			submissionReminderTime = submissionReminderTime.plusDays(ThreadLocalRandom.current().nextInt(14, 22)); // Randomize
																													// between
																													// 2-3
																													// weeks
		}

		// Timestamp for AcceptedPaperNotification (middle of conference duration)
//		midConferenceTime = conferenceStart	.plusDays(conferenceStart.until(beforeConferenceEnd, ChronoUnit.DAYS) / 2); // Adjust conferenceDuration
		LocalDateTime acceptedPaperNotificationTime=midConferenceTime;
		LocalDateTime regReminderTime = midConferenceTime.plusWeeks(1);
		while (acceptedPaperNotificationTime.isBefore(acceptedPaperNotificationTime.plusDays(3))) { // peak  about acceptance
			// starts
// Schedule submission reminder tweet
			//PaperSubmissionReminderTime(paperSubmissionReminderTime);
			System.out.println("Paper Submission Reminder Tweet at: " + submissionReminderTime);
			
		    AcceptedPaperNotification(acceptedPaperNotificationTime);
			RegistrationReminder(regReminderTime);
			
			acceptedPaperNotificationTime = acceptedPaperNotificationTime.plusDays(ThreadLocalRandom.current().nextInt(14, 22)); // Randomize
			regReminderTime = regReminderTime.plusDays(ThreadLocalRandom.current().nextInt(14, 22)); // Randomize
		}
		LocalDateTime InsightsBasedOnAcceptedPapersTime = getRandomTimestamp(callForPapersTime,
				AcceptedPaperNotificationStartTime);
		InsightsBasedOnAcceptedPapers(InsightsBasedOnAcceptedPapersTime);
		// Timestamps for KeynotesAndPanelAnnouncement, VolunteerAnnouncement,
		// StudentGrantAnnouncement, ScheduleAnnouncement,
		// ExcitementForAttendingTheConference
	   
		LocalDateTime last15DaysBeforeConference = beforeConferenceEnd.minusDays(15);
		
		LocalDateTime ExcitementForAttendingTheConferenceTime = getRandomTimestamp(last15DaysBeforeConference,
				beforeConferenceEnd);
		while (acceptedPaperNotificationTime.isBefore(acceptedPaperNotificationTime.plusDays(3))) { // peak  about acceptance
			// starts
// Schedule submission reminder tweet
			KeynotesAndPanelAnnouncement();
			VolunteerAnnouncement();
			StudentGrantAnnouncement();
			ExcitementForAttendingTheConference();
			ScheduleAnnouncement();
			
			acceptedPaperNotificationTime = acceptedPaperNotificationTime.plusDays(ThreadLocalRandom.current().nextInt(14, 22)); // Randomize
			regReminderTime = regReminderTime.plusDays(ThreadLocalRandom.current().nextInt(14, 22)); // Randomize
		}
		
		// Timestamp for PaperSubmissionReminder (every 2-3 weeks during the Before

		System.out.println("Before Conference End: " + beforeConferenceEnd);

	}

	public static LocalDateTime getRandomTimestamp(LocalDateTime start, LocalDateTime end) {
		long days = start.until(end, ChronoUnit.DAYS);
		long randomDays = ThreadLocalRandom.current().nextLong(days + 1);
		long randomHours = ThreadLocalRandom.current().nextLong(24);
		long randomMinutes = ThreadLocalRandom.current().nextLong(60);
		long randomSeconds = ThreadLocalRandom.current().nextLong(60);

		return start.plusDays(randomDays).plusHours(randomHours).plusMinutes(randomMinutes).plusSeconds(randomSeconds);
	}
}
