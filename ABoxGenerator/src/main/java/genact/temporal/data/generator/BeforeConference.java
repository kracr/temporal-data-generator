package genact.temporal.data.generator;

import java.util.UUID;
import org.apache.jena.rdf.model.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.jena.vocabulary.RDF;
import org.apache.jena.riot.RDFDataMgr;
import java.time.temporal.ChronoUnit;

import org.apache.jena.riot.RDFFormat;
import org.apache.jena.datatypes.xsd.XSDDatatype;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class BeforeConference {
	ConferenceStreams conf;
	Random random;
	Property rdfPredicate = RDF.predicate;
	File tweetMetaData;
	File eventData;
	RDFWriter tweetMetaDataWriter;
	RDFWriter tweetEventDataWriter;
	Model tweetMetaDataModel;
	Model eventDataModel;

	public BeforeConference(ConferenceStreams conf, long startTime, long endTime) {
		this.conf = conf;
		this.random = conf.random;

		// Convert timestamps from long to LocalDateTime such as 2007-12-03T10:15:30.
		LocalDateTime conferenceStart = LocalDateTime.ofInstant(new Date(startTime).toInstant(),
				ZoneId.systemDefault());
		LocalDateTime beforeConferenceEnd = LocalDateTime.ofInstant(new Date(endTime).toInstant(),
				ZoneId.systemDefault());
		LocalDateTime mainConfAnnouncementTime = conferenceStart;
		LocalDateTime midConferenceTime = mainConfAnnouncementTime
				.plusDays(mainConfAnnouncementTime.until(beforeConferenceEnd, ChronoUnit.DAYS) / 2); // Calculate
																										// mid-conference
																										// time
		LocalDateTime acceptedPaperNotificationTime = midConferenceTime;

		EarlyConferenceAnnouncement(mainConfAnnouncementTime, beforeConferenceEnd);

		for (int i = 0; i < ThreadLocalRandom.current().nextInt(conf.random_tweets_min, conf.random_tweets_max); i++) {
			LocalDateTime excitementAnnouncementTime = getRandomTimestamp(
					mainConfAnnouncementTime.plusDays(ThreadLocalRandom.current().nextInt(7, 14)),
					mainConfAnnouncementTime.plusDays(ThreadLocalRandom.current().nextInt(22, 30)));
			ExcitementAboutTheConferenceAnnouncement(excitementAnnouncementTime);
		}

		// Reminders for paper submissions
		LocalDateTime paperSubmissionReminderTime = conferenceStart.plusWeeks(2);
		while (paperSubmissionReminderTime.isBefore(midConferenceTime.minusDays(45))) {
			PaperSubmissionReminder(paperSubmissionReminderTime);
			paperSubmissionReminderTime = paperSubmissionReminderTime
					.plusDays(ThreadLocalRandom.current().nextInt(14, 22)); // Randomize the interval
		}

		// Notification Peak
		// Announcements about accepted papers and insights based on them
		for (int i = 0; i < conf.notification_peak; i++) {
			AcceptedPaperNotification(acceptedPaperNotificationTime);
			acceptedPaperNotificationTime = midConferenceTime.plusDays(ThreadLocalRandom.current().nextInt(1, 4)); // Randomize
																													// the
																													// interval
		}

		for (int i = 0; i < ThreadLocalRandom.current().nextInt(conf.random_tweets_min, conf.random_tweets_max); i++) {
			LocalDateTime insightsTime = getRandomTimestamp(midConferenceTime,
					midConferenceTime.plusDays(ThreadLocalRandom.current().nextInt(1, 7)));
			InsightsBasedOnAcceptedPapers(insightsTime);
		}

		// Registration reminders
		LocalDateTime regReminderTime = midConferenceTime.plusWeeks(1);
		while (regReminderTime.isBefore(beforeConferenceEnd)) {
			RegistrationReminder(regReminderTime);
			regReminderTime = regReminderTime.plusDays(ThreadLocalRandom.current().nextInt(7, 14)); // Randomize the
																									// interval
		}

		LocalDateTime last15DaysBeforeConference = beforeConferenceEnd.minusDays(15);
		BeforeTheConference(last15DaysBeforeConference, beforeConferenceEnd);

	}

	public void EarlyConferenceAnnouncement(LocalDateTime mainConfAnnouncementTime, LocalDateTime beforeConferenceEnd) {

		// Initialize important timestamps for various announcements and reminders

		LocalDateTime callForPapersTime = mainConfAnnouncementTime.plusDays(conf.random.nextInt(2))
				.plusHours(ThreadLocalRandom.current().nextLong(24))
				.plusMinutes(ThreadLocalRandom.current().nextLong(60))
				.plusSeconds(ThreadLocalRandom.current().nextLong(60));
		// Main conference announcement

		MainConferenceAnnouncement(mainConfAnnouncementTime);
		// Call for papers announcement
		CallForPapersAnnouncement(callForPapersTime);

		// Generate excitement announcements related to the conference
		// starts after conference announcement and can be posted anytime until 2 weeks
		for (int i = 0; i < conf.early_announcement_peak; i++) {
			LocalDateTime excitementAnnouncementTime = getRandomTimestamp(mainConfAnnouncementTime,
					mainConfAnnouncementTime.plusDays(ThreadLocalRandom.current().nextInt(1, 14)));
			ExcitementAboutTheConferenceAnnouncement(excitementAnnouncementTime);
		}
	}

	public void BeforeTheConference(LocalDateTime last15DaysBeforeConference, LocalDateTime beforeConferenceEnd) {
		LocalDateTime timestamp;
		for (int i = 0; i <= conf.random.nextInt(conf.speakerList.size()); i++) {
			timestamp = getRandomTimestamp(last15DaysBeforeConference, beforeConferenceEnd);
			KeynotesAndPanelAnnouncement(timestamp);
		}

		// Additional excitement announcements for volunteers and student grants
		for (int i = 0; i <= conf.random.nextInt(conf.volunteerAndStudentGrantList.size()); i++) {
			timestamp = getRandomTimestamp(last15DaysBeforeConference, beforeConferenceEnd);
			VolunteerandStudentGrantAnnouncement(timestamp);
		}

		// General excitement announcements about attending the conference
		for (int i = 0; i <= ThreadLocalRandom.current().nextInt(conf.random_tweets_min, conf.random_tweets_max); i++) {
			timestamp = getRandomTimestamp(last15DaysBeforeConference, beforeConferenceEnd);
			ExcitementForAttendingTheConference(timestamp);
		}

		// Schedule announcements leading up to the conference
		timestamp = getRandomTimestamp(last15DaysBeforeConference, beforeConferenceEnd);
		while (timestamp.isBefore(beforeConferenceEnd)) {
			ScheduleAnnouncement(timestamp);
			timestamp = timestamp.plusDays(ThreadLocalRandom.current().nextInt(5, 7)); // Randomize the interval
		}

	}

	public void MainConferenceAnnouncement(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		String twitterAccount = conf.ACE_URL + conf.confAccount;

		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		Resource conferenceAccount = tweetMetaDataModel.createResource(twitterAccount);

		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		tweetMetaDataModel.add(conferenceAccount, RDF.type, conf.ConferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(tweetId, conf.hasTweetID, tweetId);
		tweetMetaDataModel.add(conferenceAccount, conf.hasUserID, conferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.hasDisplayName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		Resource conferenceInstance = eventDataModel.createResource(conf.ACE_URL + conf.confInstance);
		// tweetMetaDataModel.add(tweetId, conf.isAbout, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAbout, eventDataModel.createTypedLiteral(conf.ACE_URL + "announcement"));
//		eventDataModel.add(tweetId, conf.hasEventPhase,
//				eventDataModel.createTypedLiteral(conf.ACE_URL + "announcement"));
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		// tweetMetaDataModel.add(tweetId, conf.hasHashtag,
		// eventDataModel.createLiteral(conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.mentionsConference, conferenceAccount);
		eventDataModel.add(conferenceInstance, RDF.type, conf.Conference);
		eventDataModel.add(conferenceInstance, conf.hasConferenceName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		eventDataModel.add(conferenceInstance, conf.hasEdition, eventDataModel.createTypedLiteral(conf.confCycle));
		Resource city = eventDataModel.createResource(conf.cities.get(conf.random.nextInt(conf.cities.size())));
		eventDataModel.add(city, RDF.type, conf.City);
		eventDataModel.add(conferenceInstance, conf.hasWebsiteURL,
				eventDataModel.createLiteral("www." + conf.confInstance + ".com"));
		String selectedEventMode = conf.TOKEN_EventMode[conf.random.nextInt(conf.TOKEN_EventMode.length)];

		switch (selectedEventMode) {
		case "offline":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "offline"));
			eventDataModel.add(conferenceInstance, conf.hasLocation, city);
			break;
		case "online":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "online"));
			eventDataModel.add(conferenceInstance, conf.hasWebsiteURL, eventDataModel.createLiteral(conf.confURL));
			break;
		case "hybrid":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "hybrid"));
			eventDataModel.add(conferenceInstance, conf.hasLocation, city);
			eventDataModel.add(conferenceInstance, conf.hasWebsiteURL, eventDataModel.createLiteral(conf.confURL));
			break;
		}

		tweetMetaDataModel.add(conf.tweetMetaProperties);
		eventDataModel.add(conf.eventDataProperties);
		tweetMetaData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_metadata" + ".ttl");
		eventData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_eventdata" + ".ttl");

		try {
			if (!tweetMetaData.exists()) {
				tweetMetaData.createNewFile();
				// System.out.println("File created: " + tweetMetaData.getName());
			} else {
				// System.out.println("File already exists: " + tweetMetaData.getName());
			}

			if (!eventData.exists()) {
				eventData.createNewFile();
				// System.out.println("File created: " + eventData.getName());
			} else {
				// System.out.println("File already exists: " + eventData.getName());
			}

			// Configure RDF writers to write in Turtle format
		} catch (IOException e) {
			System.out.println("An error occurred: " + e.getMessage());
			e.printStackTrace();
		}
		// Writing the models to the files
		writeModelToFile(tweetMetaDataModel, tweetMetaData);
		writeModelToFile(eventDataModel, eventData);

	}

	public void CallForPapersAnnouncement(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		String twitterAccount = conf.ACE_URL + conf.confAccount;

		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		Resource conferenceAccount = tweetMetaDataModel.createResource(twitterAccount);

		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		tweetMetaDataModel.add(conferenceAccount, RDF.type, conf.ConferenceAccount);

		tweetMetaDataModel.add(conferenceAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(tweetId, conf.hasTweetID, tweetId);
		tweetMetaDataModel.add(conferenceAccount, conf.hasUserID, conferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.hasDisplayName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		Resource conferenceInstance = eventDataModel.createResource(conf.ACE_URL + conf.confInstance);
//		tweetMetaDataModel.add(tweetId, conf.isAbout, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAbout, eventDataModel.createTypedLiteral(conf.ACE_URL + "announcement"));
//		eventDataModel.add(tweetId, conf.hasEventPhase,
//				eventDataModel.createTypedLiteral(conf.ACE_URL + "announcement"));
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		// tweetMetaDataModel.add(tweetId, conf.hasHashtag,
		// eventDataModel.createLiteral(conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.mentionsConference, conferenceAccount);
		eventDataModel.add(conferenceInstance, RDF.type, conf.Conference);
		eventDataModel.add(conferenceInstance, conf.hasConferenceName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		eventDataModel.add(conferenceInstance, conf.hasEdition, eventDataModel.createTypedLiteral(conf.confCycle));
		Resource city = eventDataModel.createResource(conf.cities.get(conf.random.nextInt(conf.cities.size())));
		eventDataModel.add(city, RDF.type, conf.City);
		eventDataModel.add(conferenceInstance, conf.hasWebsiteURL,
				eventDataModel.createLiteral("www." + conf.confInstance + ".com"));
		String selectedEventMode = conf.TOKEN_EventMode[conf.random.nextInt(conf.TOKEN_EventMode.length)];

		switch (selectedEventMode) {
		case "offline":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "offline"));
			eventDataModel.add(conferenceInstance, conf.hasLocation, city);
			break;
		case "online":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "online"));
			eventDataModel.add(conferenceInstance, conf.hasWebsiteURL, eventDataModel.createLiteral(conf.confURL));
			break;
		case "hybrid":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "hybrid"));
			eventDataModel.add(conferenceInstance, conf.hasLocation, city);
			eventDataModel.add(conferenceInstance, conf.hasWebsiteURL, eventDataModel.createLiteral(conf.confURL));
			break;
		}

		for (String track : conf.TOKEN_ConferenceEventTrack) {
			eventDataModel.add(conferenceInstance, conf.hasPaperTrack,
					eventDataModel.createResource(conf.ACE_URL + track));
		}

		// Add 'mentions' triples
		for (Map.Entry<String, List<String>> entry : conf.organizingCommitteeList.entrySet()) {
			String role = entry.getKey();
			List<String> userIds = entry.getValue();
			for (String userId : userIds) {
				Resource userResource = eventDataModel.createResource(conf.ACE_URL + userId);
				tweetMetaDataModel.add(tweetId, conf.mentionsPerson, userResource);
				eventDataModel.add(userResource, conf.hasRole, tweetMetaDataModel.createResource(conf.ACE_URL + role));
				if (role == "generalChair") {
					eventDataModel.add(conferenceInstance, conf.hasGeneralChair, userResource);
				}
				if (role == "localChair") {
					eventDataModel.add(conferenceInstance, conf.hasLocalChair, userResource);
				}
				if (role == "ResearchTrackChair") {
					eventDataModel.add(conferenceInstance, conf.hasResearchTrackChair, userResource);
				}
				if (role == "ResourceTrackChair") {
					eventDataModel.add(conferenceInstance, conf.hasResourceTrackChair, userResource);
				}

				// Add detailed triples about the user
				Map<String, String> userDetails = conf.userData.get(userId);
				tweetMetaDataModel.add(userResource, RDF.type, conf.Person);
				tweetMetaDataModel.add(userResource, conf.hasUserID, tweetMetaDataModel.createLiteral(userId));
				tweetMetaDataModel.add(userResource, conf.hasAffiliation,
						tweetMetaDataModel.createLiteral(userDetails.get("affiliation")));
				tweetMetaDataModel.add(userResource, conf.hasDisplayName,
						tweetMetaDataModel.createLiteral(userDetails.get("displayName")));
				tweetMetaDataModel.add(userResource, conf.hasDesignation,
						tweetMetaDataModel.createLiteral(userDetails.get("designation")));
			}
		}

		tweetMetaDataModel.add(conf.tweetMetaProperties);
		eventDataModel.add(conf.eventDataProperties);
		tweetMetaData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_metadata" + ".ttl");
		eventData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_eventdata" + ".ttl");

		try {
			if (!tweetMetaData.exists()) {
				tweetMetaData.createNewFile();
				// System.out.println("File created: " + tweetMetaData.getName());
			} else {
				// System.out.println("File already exists: " + tweetMetaData.getName());
			}

			if (!eventData.exists()) {
				eventData.createNewFile();
				// System.out.println("File created: " + eventData.getName());
			} else {
				// System.out.println("File already exists: " + eventData.getName());
			}

			// Configure RDF writers to write in Turtle format
		} catch (IOException e) {
			System.out.println("An error occurred: " + e.getMessage());
			e.printStackTrace();
		}
		// Writing the models to the files
		writeModelToFile(tweetMetaDataModel, tweetMetaData);
		writeModelToFile(eventDataModel, eventData);
	}

	public void RegistrationReminder(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		String twitterAccount = conf.ACE_URL + conf.confAccount;

		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		Resource conferenceAccount = tweetMetaDataModel.createResource(twitterAccount);

		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		tweetMetaDataModel.add(conferenceAccount, RDF.type, conf.ConferenceAccount);

		tweetMetaDataModel.add(conferenceAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(tweetId, conf.hasTweetID, tweetId);
		tweetMetaDataModel.add(conferenceAccount, conf.hasUserID, conferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.hasDisplayName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		Resource conferenceInstance = eventDataModel.createResource(conf.ACE_URL + conf.confInstance);
//		tweetMetaDataModel.add(tweetId, conf.isAbout, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAbout, tweetMetaDataModel.createTypedLiteral(conf.ACE_URL + "reminder"));
//		eventDataModel.add(tweetId, conf.hasEventPhase,
//				eventDataModel.createTypedLiteral(conf.ACE_URL + "announcement"));
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		// tweetMetaDataModel.add(tweetId, conf.hasHashtag,
		// eventDataModel.createLiteral(conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.mentionsConference, conferenceAccount);
		eventDataModel.add(conferenceInstance, RDF.type, conf.Conference);
		eventDataModel.add(conferenceInstance, conf.hasConferenceName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		eventDataModel.add(conferenceInstance, conf.hasEdition, eventDataModel.createTypedLiteral(conf.confCycle));
		Resource city = eventDataModel.createResource(conf.cities.get(conf.random.nextInt(conf.cities.size())));
		eventDataModel.add(city, RDF.type, conf.City);
		eventDataModel.add(conferenceInstance, conf.hasWebsiteURL,
				eventDataModel.createLiteral("www." + conf.confInstance + ".com"));
		String selectedEventMode = conf.TOKEN_EventMode[conf.random.nextInt(conf.TOKEN_EventMode.length)];

		switch (selectedEventMode) {
		case "offline":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "offline"));
			eventDataModel.add(conferenceInstance, conf.hasLocation, city);
			break;
		case "online":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "online"));
			eventDataModel.add(conferenceInstance, conf.hasWebsiteURL, eventDataModel.createLiteral(conf.confURL));
			break;
		case "hybrid":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "hybrid"));
			eventDataModel.add(conferenceInstance, conf.hasLocation, city);
			eventDataModel.add(conferenceInstance, conf.hasWebsiteURL, eventDataModel.createLiteral(conf.confURL));
			break;
		}

		for (String track : conf.TOKEN_ConferenceEventTrack) {
			eventDataModel.add(conferenceInstance, conf.hasPaperTrack,
					eventDataModel.createResource(conf.ACE_URL + track));
		}

		// Add 'mentions' triples
		for (Map.Entry<String, List<String>> entry : conf.organizingCommitteeList.entrySet()) {
			String role = entry.getKey();
			List<String> userIds = entry.getValue();
			for (String userId : userIds) {
				Resource userResource = eventDataModel.createResource(conf.ACE_URL + userId);
				tweetMetaDataModel.add(tweetId, conf.mentionsPerson, userResource);
				eventDataModel.add(userResource, conf.hasRole, tweetMetaDataModel.createResource(conf.ACE_URL + role));
				if (role == "generalChair") {
					eventDataModel.add(conferenceInstance, conf.hasGeneralChair, userResource);
				}
				if (role == "localChair") {
					eventDataModel.add(conferenceInstance, conf.hasLocalChair, userResource);
				}
				if (role == "ResearchTrackChair") {
					eventDataModel.add(conferenceInstance, conf.hasResearchTrackChair, userResource);
				}
				if (role == "ResourceTrackChair") {
					eventDataModel.add(conferenceInstance, conf.hasResourceTrackChair, userResource);
				}

				// Add detailed triples about the user
				Map<String, String> userDetails = conf.userData.get(userId);
				tweetMetaDataModel.add(userResource, RDF.type, conf.Person);
				tweetMetaDataModel.add(userResource, conf.hasUserID, tweetMetaDataModel.createLiteral(userId));
				tweetMetaDataModel.add(userResource, conf.hasAffiliation,
						tweetMetaDataModel.createLiteral(userDetails.get("affiliation")));
				tweetMetaDataModel.add(userResource, conf.hasDisplayName,
						tweetMetaDataModel.createLiteral(userDetails.get("displayName")));
				tweetMetaDataModel.add(userResource, conf.hasDesignation,
						tweetMetaDataModel.createLiteral(userDetails.get("designation")));
			}
		}

		tweetMetaDataModel.add(conf.tweetMetaProperties);
		eventDataModel.add(conf.eventDataProperties);
		tweetMetaData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_metadata" + ".ttl");
		eventData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_eventdata" + ".ttl");

		try {
			if (!tweetMetaData.exists()) {
				tweetMetaData.createNewFile();
				// System.out.println("File created: " + tweetMetaData.getName());
			} else {
				// System.out.println("File already exists: " + tweetMetaData.getName());
			}

			if (!eventData.exists()) {
				eventData.createNewFile();
				// System.out.println("File created: " + eventData.getName());
			} else {
				// System.out.println("File already exists: " + eventData.getName());
			}

			// Configure RDF writers to write in Turtle format
		} catch (IOException e) {
			System.out.println("An error occurred: " + e.getMessage());
			e.printStackTrace();
		}
		// Writing the models to the files
		writeModelToFile(tweetMetaDataModel, tweetMetaData);
		writeModelToFile(eventDataModel, eventData);
	}

	public void ExcitementAboutTheConferenceAnnouncement(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		// pick a user from paper list
		List<String> paperIds = new ArrayList<>(conf.conferencePaperList.keySet());
		String selectedPaperId = paperIds.get(conf.random.nextInt(paperIds.size()));
		Map<String, Object> paperDetails = conf.conferencePaperList.get(selectedPaperId);
		List<String> authorList = (List<String>) paperDetails.get("AuthorList");
		String someUser = authorList.get(conf.random.nextInt(authorList.size()));
		String twitterAccount = conf.ACE_URL + someUser;

		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		Resource personAccount = tweetMetaDataModel.createResource(twitterAccount);

		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		tweetMetaDataModel.add(personAccount, RDF.type, conf.PersonAccount);

		tweetMetaDataModel.add(personAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(tweetId, conf.hasTweetID, tweetId);
		tweetMetaDataModel.add(personAccount, conf.hasUserID, personAccount);
		Map<String, String> userDetails = conf.userData.get(someUser);
		eventDataModel.add(personAccount, RDF.type, conf.Person);
		tweetMetaDataModel.add(personAccount, conf.hasAffiliation,
				tweetMetaDataModel.createResource(userDetails.get("affiliation")));
		eventDataModel.add(personAccount, conf.hasAffiliation,
				tweetMetaDataModel.createResource(userDetails.get("affiliation")));
		tweetMetaDataModel.add(personAccount, conf.hasDisplayName,
				tweetMetaDataModel.createLiteral(userDetails.get("displayName")));
		tweetMetaDataModel.add(personAccount, conf.hasDesignation,
				tweetMetaDataModel.createLiteral(userDetails.get("designation")));
		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
//		tweetMetaDataModel.add(tweetId, conf.isAbout, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAbout,
				tweetMetaDataModel.createTypedLiteral(conf.ACE_URL + "announcement"));
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		// tweetMetaDataModel.add(tweetId, conf.hasHashtag,
		// eventDataModel.createLiteral(conf.confInstance));

		Resource conferenceInstance = eventDataModel.createResource(conf.ACE_URL + conf.confInstance);
		eventDataModel.add(conferenceInstance, RDF.type, conf.Conference);
		eventDataModel.add(conferenceInstance, conf.hasConferenceName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		eventDataModel.add(conferenceInstance, conf.hasEdition, eventDataModel.createTypedLiteral(conf.confCycle));
		tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance);
		// String city_name=;
		Resource city = eventDataModel.createResource("city_name");
		eventDataModel.add(city, RDF.type, conf.City);
		eventDataModel.add(conferenceInstance, conf.hasWebsiteURL,
				eventDataModel.createLiteral("www." + conf.confInstance + ".com"));
		String selectedEventMode = conf.TOKEN_EventMode[conf.random.nextInt(conf.TOKEN_EventMode.length)];

		switch (selectedEventMode) {
		case "offline":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "offline"));
			eventDataModel.add(conferenceInstance, conf.hasLocation, city);
			break;
		case "online":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "online"));
			eventDataModel.add(conferenceInstance, conf.hasWebsiteURL,
					eventDataModel.createLiteral("www." + conf.confInstance + ".com"));
			break;
		case "hybrid":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "hybrid"));
			eventDataModel.add(conferenceInstance, conf.hasLocation, city);
			eventDataModel.add(conferenceInstance, conf.hasWebsiteURL,
					eventDataModel.createLiteral("www." + conf.confInstance + ".com"));
			break;
		}
		for (String track : conf.TOKEN_ConferenceEventTrack) {
			eventDataModel.add(conferenceInstance, conf.hasPaperTrack,
					eventDataModel.createResource(conf.ACE_URL + track));
		}

		// Add 'mentions' triples
		for (Map.Entry<String, List<String>> entry : conf.organizingCommitteeList.entrySet()) {
			String role = entry.getKey();
			List<String> userIds = entry.getValue();
			for (String userId : userIds) {
				Resource userResource = eventDataModel.createResource(conf.ACE_URL + userId);
				tweetMetaDataModel.add(tweetId, conf.mentionsPerson, userResource);
				eventDataModel.add(userResource, conf.hasRole, tweetMetaDataModel.createResource(conf.ACE_URL + role));
				if (role == "generalChair") {
					eventDataModel.add(conferenceInstance, conf.hasGeneralChair, userResource);
				}
				if (role == "localChair") {
					eventDataModel.add(conferenceInstance, conf.hasLocalChair, userResource);
				}
				if (role == "ResearchTrackChair") {
					eventDataModel.add(conferenceInstance, conf.hasResearchTrackChair, userResource);
				}
				if (role == "ResourceTrackChair") {
					eventDataModel.add(conferenceInstance, conf.hasResourceTrackChair, userResource);
				}

				// Add detailed triples about the user
				userDetails = conf.userData.get(userId);
				tweetMetaDataModel.add(userResource, RDF.type, conf.Person);
				tweetMetaDataModel.add(userResource, conf.hasUserID, tweetMetaDataModel.createLiteral(userId));
				tweetMetaDataModel.add(userResource, conf.hasAffiliation,
						tweetMetaDataModel.createResource(userDetails.get("affiliation")));
				tweetMetaDataModel.add(userResource, conf.hasDisplayName,
						tweetMetaDataModel.createLiteral(userDetails.get("displayName")));
				tweetMetaDataModel.add(userResource, conf.hasDesignation,
						tweetMetaDataModel.createLiteral(userDetails.get("designation")));
				eventDataModel.add(userResource, conf.attends, confInstance);
			}
		}

		tweetMetaDataModel.add(conf.tweetMetaProperties);
		eventDataModel.add(conf.eventDataProperties);
		tweetMetaData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_metadata" + ".ttl");
		eventData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_eventdata" + ".ttl");

		try {
			if (!tweetMetaData.exists()) {
				tweetMetaData.createNewFile();
				// System.out.println("File created: " + tweetMetaData.getName());
			} else {
				// System.out.println("File already exists: " + tweetMetaData.getName());
			}

			if (!eventData.exists()) {
				eventData.createNewFile();
				// System.out.println("File created: " + eventData.getName());
			} else {
				// System.out.println("File already exists: " + eventData.getName());
			}

			// Configure RDF writers to write in Turtle format
		} catch (IOException e) {
			System.out.println("An error occurred: " + e.getMessage());
			e.printStackTrace();
		}
		// Writing the models to the files
		writeModelToFile(tweetMetaDataModel, tweetMetaData);
		writeModelToFile(eventDataModel, eventData);
	}

	public void KeynotesAndPanelAnnouncement(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		String twitterAccount = conf.ACE_URL + conf.confAccount;

		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		Resource conferenceAccount = tweetMetaDataModel.createResource(twitterAccount);

		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		tweetMetaDataModel.add(conferenceAccount, RDF.type, conf.ConferenceAccount);

		tweetMetaDataModel.add(conferenceAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(tweetId, conf.hasTweetID, tweetId);
		tweetMetaDataModel.add(conferenceAccount, conf.hasUserID, conferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.hasDisplayName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		Resource conferenceInstance = eventDataModel.createResource(conf.ACE_URL + conf.confInstance);

//		tweetMetaDataModel.add(tweetId, conf.isAbout, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAbout,
				tweetMetaDataModel.createTypedLiteral(conf.ACE_URL + "announcement"));
//		eventDataModel.add(tweetId, conf.hasEventPhase,
//				eventDataModel.createTypedLiteral(conf.ACE_URL + "announcement"));
//		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		// tweetMetaDataModel.add(tweetId, conf.hasHashtag,
		// eventDataModel.createLiteral(conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.mentionsConference, conferenceAccount);
		eventDataModel.add(conferenceInstance, RDF.type, conf.Conference);
		eventDataModel.add(conferenceInstance, conf.hasConferenceName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		eventDataModel.add(conferenceInstance, conf.hasEdition, eventDataModel.createTypedLiteral(conf.confCycle));
		Resource city = eventDataModel.createResource(conf.cities.get(conf.random.nextInt(conf.cities.size())));
		eventDataModel.add(city, RDF.type, conf.City);
		eventDataModel.add(conferenceInstance, conf.hasWebsiteURL,
				eventDataModel.createLiteral("www." + conf.confInstance + ".com"));
		String selectedEventMode = conf.TOKEN_EventMode[conf.random.nextInt(conf.TOKEN_EventMode.length)];

		switch (selectedEventMode) {
		case "offline":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "offline"));
			eventDataModel.add(conferenceInstance, conf.hasLocation, city);
			break;
		case "online":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "online"));
			eventDataModel.add(conferenceInstance, conf.hasWebsiteURL, eventDataModel.createLiteral(conf.confURL));
			break;
		case "hybrid":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "hybrid"));
			eventDataModel.add(conferenceInstance, conf.hasLocation, city);
			eventDataModel.add(conferenceInstance, conf.hasWebsiteURL, eventDataModel.createLiteral(conf.confURL));
			break;
		}

		for (Map.Entry<String, List<String>> entry : conf.speakerList.entrySet()) {
			String type = entry.getKey();
			List<String> userIds = entry.getValue();
			for (String userId : userIds) {
				Resource userResource = tweetMetaDataModel.createResource(conf.ACE_URL + userId);

				// Common user triples
				tweetMetaDataModel.add(userResource, RDF.type, conf.Person);
				tweetMetaDataModel.add(tweetId, conf.mentionsPerson, userResource);
				tweetMetaDataModel.add(userResource, conf.hasUserID, tweetMetaDataModel.createLiteral(userId));

				Map<String, String> userDetails = conf.userData.get(userId);
				tweetMetaDataModel.add(userResource, conf.hasAffiliation,
						tweetMetaDataModel.createResource(userDetails.get("affiliation")));
				tweetMetaDataModel.add(userResource, conf.hasDisplayName,
						tweetMetaDataModel.createLiteral(userDetails.get("displayName")));
				tweetMetaDataModel.add(userResource, conf.hasDesignation,
						tweetMetaDataModel.createLiteral(userDetails.get("designation")));
				tweetMetaDataModel.add(userResource, conf.givesTalkOn,
						tweetMetaDataModel.createLiteral("keynoteTitle"));

				if (type.equals("Keynote")) {
					eventDataModel.add(userResource, conf.hasRole, conf.KeynoteSpeakerRole);
					eventDataModel.add(userResource, conf.givesTalkOn, "someKeynoteTalkTitle");
				} else if (type.equals("InvitedTalk")) {
					eventDataModel.add(userResource, conf.givesTalkOn, "someInvitedTalkTitle");
				} else if (type.equals("Speaker")) {
					eventDataModel.add(userResource, conf.givesTalkOn, "someTalkTitle");
				}
				eventDataModel.add(userResource, conf.attends, confInstance);
			}
			tweetMetaDataModel.add(conf.tweetMetaProperties);
			eventDataModel.add(conf.eventDataProperties);
			tweetMetaData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_"
					+ tweetId0 + "_metadata" + ".ttl");
			eventData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
					+ "_eventdata" + ".ttl");

			try {
				if (!tweetMetaData.exists()) {
					tweetMetaData.createNewFile();
					// System.out.println("File created: " + tweetMetaData.getName());
				} else {
					// System.out.println("File already exists: " + tweetMetaData.getName());
				}

				if (!eventData.exists()) {
					eventData.createNewFile();
					// System.out.println("File created: " + eventData.getName());
				} else {
					// System.out.println("File already exists: " + eventData.getName());
				}

				// Configure RDF writers to write in Turtle format
			} catch (IOException e) {
				System.out.println("An error occurred: " + e.getMessage());
				e.printStackTrace();
			}
			// Writing the models to the files
			writeModelToFile(tweetMetaDataModel, tweetMetaData);
			writeModelToFile(eventDataModel, eventData);
		}
	}

	public void VolunteerandStudentGrantAnnouncement(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();

		// Combine all user IDs into a single list
		List<String> allUsers = new ArrayList<>();
		for (List<String> users : conf.volunteerAndStudentGrantList.values()) {
			allUsers.addAll(users);
		}

		// Select a random user ID from the combined list
		String someUser = allUsers.get(conf.random.nextInt(allUsers.size()));

//		System.out.println(someUser);
		String twitterAccount = conf.ACE_URL + someUser;
		Map<String, String> userDetails = conf.userData.get(someUser);
//		System.out.println(userDetails);
		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		Resource personAccount = tweetMetaDataModel.createResource(twitterAccount);

		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		tweetMetaDataModel.add(personAccount, RDF.type, conf.PersonAccount);

		tweetMetaDataModel.add(personAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(tweetId, conf.hasTweetID, tweetId);
		tweetMetaDataModel.add(personAccount, RDF.type, conf.PersonAccount);

		eventDataModel.add(personAccount, RDF.type, conf.Person);
		tweetMetaDataModel.add(personAccount, conf.hasAffiliation,
				tweetMetaDataModel.createResource(userDetails.get("affiliation")));
		eventDataModel.add(personAccount, conf.hasAffiliation,
				tweetMetaDataModel.createResource(userDetails.get("affiliation")));
		tweetMetaDataModel.add(personAccount, conf.hasDisplayName,
				tweetMetaDataModel.createLiteral(userDetails.get("displayName")));
		tweetMetaDataModel.add(personAccount, conf.hasDesignation,
				tweetMetaDataModel.createLiteral(userDetails.get("designation")));
		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		eventDataModel.add(personAccount, conf.attends, confInstance);
//		tweetMetaDataModel.add(tweetId, conf.isAbout, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAbout,
				tweetMetaDataModel.createTypedLiteral(conf.ACE_URL + "announcement"));
//		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		// tweetMetaDataModel.add(tweetId, conf.hasHashtag,
		// eventDataModel.createLiteral(conf.confInstance));

		eventDataModel.add(confInstance, RDF.type, conf.Conference);
		eventDataModel.add(confInstance, conf.hasConferenceName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		eventDataModel.add(confInstance, conf.hasEdition, eventDataModel.createTypedLiteral(conf.confCycle));
		tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance);
		// String city_name=;
		Resource city = eventDataModel.createResource("city_name");
		eventDataModel.add(city, RDF.type, conf.City);
		tweetMetaDataModel.add(personAccount, conf.hasUserID, tweetMetaDataModel.createLiteral(someUser));

		// Check if the user is a "Volunteer" or "StudentGrant"
		if (conf.volunteerAndStudentGrantList.get("Volunteer").contains(someUser)) {
			tweetMetaDataModel.add(personAccount, conf.volunteersFor, conf.confInstance);
			tweetMetaDataModel.add(personAccount, conf.attends, tweetMetaDataModel.createResource(conf.confInstance));
		} else if (conf.volunteerAndStudentGrantList.get("StudentGrant").contains(someUser)) {
			tweetMetaDataModel.add(personAccount, conf.getsStudentGrantFor, conf.confInstance);
			tweetMetaDataModel.add(personAccount, conf.attends, tweetMetaDataModel.createResource(conf.confInstance));
		}

		tweetMetaDataModel.add(conf.tweetMetaProperties);
		eventDataModel.add(conf.eventDataProperties);
		tweetMetaData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_metadata" + ".ttl");
		eventData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_eventdata" + ".ttl");

		try {
			if (!tweetMetaData.exists()) {
				tweetMetaData.createNewFile();
				// System.out.println("File created: " + tweetMetaData.getName());
			} else {
				// System.out.println("File already exists: " + tweetMetaData.getName());
			}

			if (!eventData.exists()) {
				eventData.createNewFile();
				// System.out.println("File created: " + eventData.getName());
			} else {
				// System.out.println("File already exists: " + eventData.getName());
			}

			// Configure RDF writers to write in Turtle format
		} catch (IOException e) {
			System.out.println("An error occurred: " + e.getMessage());
			e.printStackTrace();
		}
		// Writing the models to the files
		writeModelToFile(tweetMetaDataModel, tweetMetaData);
		writeModelToFile(eventDataModel, eventData);
	}

	public void ExcitementForAttendingTheConference(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		// pick a user from paper list
		List<String> paperIds = new ArrayList<>(conf.conferencePaperList.keySet());
		String selectedPaperId = paperIds.get(conf.random.nextInt(paperIds.size()));
		Map<String, Object> paperDetails = conf.conferencePaperList.get(selectedPaperId);
		List<String> authorList = (List<String>) paperDetails.get("AuthorList");
		String someUser = authorList.get(conf.random.nextInt(authorList.size()));
		String twitterAccount = conf.ACE_URL + someUser;

		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		Resource personAccount = tweetMetaDataModel.createResource(twitterAccount);

		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		tweetMetaDataModel.add(personAccount, RDF.type, conf.PersonAccount);

		tweetMetaDataModel.add(personAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(tweetId, conf.hasTweetID, tweetId);
		tweetMetaDataModel.add(personAccount, conf.hasUserID, personAccount);

		Map<String, String> userDetails = conf.userData.get(someUser);
		eventDataModel.add(personAccount, RDF.type, conf.Person);
		tweetMetaDataModel.add(personAccount, conf.hasAffiliation,
				tweetMetaDataModel.createResource(userDetails.get("affiliation")));
		eventDataModel.add(personAccount, conf.hasAffiliation,
				tweetMetaDataModel.createResource(userDetails.get("affiliation")));

		tweetMetaDataModel.add(personAccount, conf.hasDisplayName,
				tweetMetaDataModel.createLiteral(userDetails.get("displayName")));
		tweetMetaDataModel.add(personAccount, conf.hasDesignation,
				tweetMetaDataModel.createLiteral(userDetails.get("designation")));
		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAbout, confInstance);
		eventDataModel.add(personAccount, conf.attends, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAbout, eventDataModel.createTypedLiteral(conf.ACE_URL + "announcement"));
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		// tweetMetaDataModel.add(tweetId, conf.hasHashtag,
		// eventDataModel.createLiteral(conf.confInstance));

		Resource conferenceInstance = eventDataModel.createResource(conf.ACE_URL + conf.confInstance);
		eventDataModel.add(conferenceInstance, RDF.type, conf.Conference);
		eventDataModel.add(conferenceInstance, conf.hasConferenceName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		eventDataModel.add(conferenceInstance, conf.hasEdition, eventDataModel.createTypedLiteral(conf.confCycle));
		tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance);
		// String city_name=;
		Resource city = eventDataModel.createResource("city_name");
		eventDataModel.add(city, RDF.type, conf.City);
		eventDataModel.add(conferenceInstance, conf.hasWebsiteURL,
				eventDataModel.createLiteral("www." + conf.confInstance + ".com"));
		String selectedEventMode = conf.TOKEN_EventMode[conf.random.nextInt(conf.TOKEN_EventMode.length)];

		switch (selectedEventMode) {
		case "offline":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "offline"));
			eventDataModel.add(conferenceInstance, conf.hasLocation, city);
			break;
		case "online":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "online"));
			eventDataModel.add(conferenceInstance, conf.hasWebsiteURL,
					eventDataModel.createLiteral("www." + conf.confInstance + ".com"));
			break;
		case "hybrid":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "hybrid"));
			eventDataModel.add(conferenceInstance, conf.hasLocation, city);
			eventDataModel.add(conferenceInstance, conf.hasWebsiteURL,
					eventDataModel.createLiteral("www." + conf.confInstance + ".com"));
			break;
		}
		for (String track : conf.TOKEN_ConferenceEventTrack) {
			eventDataModel.add(conferenceInstance, conf.hasPaperTrack,
					eventDataModel.createResource(conf.ACE_URL + track));
		}

		tweetMetaDataModel.add(conf.tweetMetaProperties);
		eventDataModel.add(conf.eventDataProperties);
		tweetMetaData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_metadata" + ".ttl");
		eventData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_eventdata" + ".ttl");

		try {
			if (!tweetMetaData.exists()) {
				tweetMetaData.createNewFile();
				// System.out.println("File created: " + tweetMetaData.getName());
			} else {
				// System.out.println("File already exists: " + tweetMetaData.getName());
			}

			if (!eventData.exists()) {
				eventData.createNewFile();
				// System.out.println("File created: " + eventData.getName());
			} else {
				// System.out.println("File already exists: " + eventData.getName());
			}

			// Configure RDF writers to write in Turtle format
		} catch (IOException e) {
			System.out.println("An error occurred: " + e.getMessage());
			e.printStackTrace();
		}
		// Writing the models to the files
		writeModelToFile(tweetMetaDataModel, tweetMetaData);
		writeModelToFile(eventDataModel, eventData);
	}

	public void AcceptedPaperNotification(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		// pick a user from paper list
		List<String> paperIds = new ArrayList<>(conf.conferencePaperList.keySet());
		String selectedPaperId = paperIds.get(conf.random.nextInt(paperIds.size()));
		Map<String, Object> paperDetails = conf.conferencePaperList.get(selectedPaperId);
		List<String> authorList = (List<String>) paperDetails.get("AuthorList");
		String someUser = authorList.get(conf.random.nextInt(authorList.size()));
		String twitterAccount = conf.ACE_URL + someUser;

		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		Resource personAccount = tweetMetaDataModel.createResource(twitterAccount);

		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		tweetMetaDataModel.add(personAccount, RDF.type, conf.PersonAccount);

		// Extract paper details
		String paperTitle = (String) paperDetails.get("PaperTitle");
		String conferenceTrack = (String) paperDetails.get("ConferenceTrack");
		List<String> paperDomains = (List<String>) paperDetails.get("PaperDomains");

		// Create paper resource
		Resource paperResource = eventDataModel.createResource(conf.ACE_URL + selectedPaperId);
		eventDataModel.add(paperResource, RDF.type, conf.ConferencePaper);
		eventDataModel.add(paperResource, conf.hasPaperTrack, eventDataModel.createLiteral(conferenceTrack));
		eventDataModel.add(paperResource, conf.hasTitle, eventDataModel.createLiteral(paperTitle));

		// Add author triples
		for (String authorId : authorList) {
			Resource authorResource = eventDataModel.createResource(conf.ACE_URL + authorId);
			eventDataModel.add(authorResource, RDF.type, conf.Person);
			eventDataModel.add(paperResource, conf.hasAuthor, authorResource);
			Map<String, String> userDetails = conf.userData.get(authorId);
			tweetMetaDataModel.add(authorResource, conf.hasAffiliation,
					tweetMetaDataModel.createLiteral(userDetails.get("affiliation")));
			tweetMetaDataModel.add(authorResource, conf.hasDisplayName,
					tweetMetaDataModel.createLiteral(userDetails.get("displayName")));
			tweetMetaDataModel.add(authorResource, conf.hasDesignation,
					tweetMetaDataModel.createLiteral(userDetails.get("designation")));
		}
		for (String paperDomain : paperDomains) {
			Resource domain = eventDataModel.createResource(conf.ACE_URL + paperDomain);
			Resource hashtag = tweetMetaDataModel.createResource(conf.Twitter_URL + paperDomain);
			// eventDataModel.add(domain, RDF.type, conf.Person);
			eventDataModel.add(paperResource, conf.hasPaperDomain, domain);
			tweetMetaDataModel.add(tweetId, conf.hasHashtag, hashtag);

		}

		// Randomly select an author to tweet about the paper
		String tweetingAuthorId = authorList.get(conf.random.nextInt(authorList.size()));
		// Resource personAccount = tweetMetaDataModel.createResource(conf.ACE_URL +
		// tweetingAuthorId);
		tweetMetaDataModel.add(personAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(personAccount, RDF.type, conf.Person);
		tweetMetaDataModel.add(personAccount, RDF.type, conf.PersonAccount);
		tweetMetaDataModel.add(personAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(personAccount, conf.hasUserID, personAccount);
		// Add additional tweet metadata
		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		eventDataModel.add(paperResource, conf.isAcceptedAt, confInstance);
//		tweetMetaDataModel.add(tweetId, conf.isAbout, confInstance);
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.isAbout,
				tweetMetaDataModel.createTypedLiteral(conf.ACE_URL + "notification"));

		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));

		tweetMetaDataModel.add(conf.tweetMetaProperties);
		eventDataModel.add(conf.eventDataProperties);
		// Add hashtags for paper domain
		tweetMetaData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_metadata" + ".ttl");
		eventData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_eventdata" + ".ttl");

		try {
			if (!tweetMetaData.exists()) {
				tweetMetaData.createNewFile();
				// System.out.println("File created: " + tweetMetaData.getName());
			} else {
				// System.out.println("File already exists: " + tweetMetaData.getName());
			}

			if (!eventData.exists()) {
				eventData.createNewFile();
				// System.out.println("File created: " + eventData.getName());
			} else {
				// System.out.println("File already exists: " + eventData.getName());
			}

			// Configure RDF writers to write in Turtle format
		} catch (IOException e) {
			System.out.println("An error occurred: " + e.getMessage());
			e.printStackTrace();
		}
		// Writing the models to the files
		writeModelToFile(tweetMetaDataModel, tweetMetaData);
		writeModelToFile(eventDataModel, eventData);
	}

	public void ScheduleAnnouncement(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		String twitterAccount = conf.ACE_URL + conf.confAccount;

		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		Resource conferenceAccount = tweetMetaDataModel.createResource(twitterAccount);

		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		tweetMetaDataModel.add(conferenceAccount, RDF.type, conf.ConferenceAccount);

		tweetMetaDataModel.add(conferenceAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(tweetId, conf.hasTweetID, tweetId);
		tweetMetaDataModel.add(conferenceAccount, conf.hasUserID, conferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.hasDisplayName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		Resource conferenceInstance = eventDataModel.createResource(conf.ACE_URL + conf.confInstance);
		// tweetMetaDataModel.add(tweetId, conf.isAbout, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAbout,
				tweetMetaDataModel.createTypedLiteral(conf.ACE_URL + "announcement"));
//		eventDataModel.add(tweetId, conf.hasEventPhase,
//				eventDataModel.createTypedLiteral(conf.ACE_URL + "announcement"));
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		// tweetMetaDataModel.add(tweetId, conf.hasHashtag,
		// eventDataModel.createLiteral(conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.mentionsConference, conferenceAccount);
		eventDataModel.add(conferenceInstance, RDF.type, conf.Conference);
		eventDataModel.add(conferenceInstance, conf.hasConferenceName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		eventDataModel.add(conferenceInstance, conf.hasEdition, eventDataModel.createTypedLiteral(conf.confCycle));
		Resource city = eventDataModel.createResource(conf.cities.get(conf.random.nextInt(conf.cities.size())));
		eventDataModel.add(city, RDF.type, conf.City);
		eventDataModel.add(conferenceInstance, conf.hasWebsiteURL,
				eventDataModel.createLiteral("www." + conf.confInstance + ".com"));
		String selectedEventMode = conf.TOKEN_EventMode[conf.random.nextInt(conf.TOKEN_EventMode.length)];

		switch (selectedEventMode) {
		case "offline":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "offline"));
			eventDataModel.add(conferenceInstance, conf.hasLocation, city);
			break;
		case "online":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "online"));
			eventDataModel.add(conferenceInstance, conf.hasWebsiteURL, eventDataModel.createLiteral(conf.confURL));
			break;
		case "hybrid":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "hybrid"));
			eventDataModel.add(conferenceInstance, conf.hasLocation, city);
			eventDataModel.add(conferenceInstance, conf.hasWebsiteURL, eventDataModel.createLiteral(conf.confURL));
			break;
		}

		for (String track : conf.TOKEN_ConferenceEventTrack) {
			eventDataModel.add(conferenceInstance, conf.hasPaperTrack,
					eventDataModel.createResource(conf.ACE_URL + track));
		}

		// Add 'mentions' triples
		for (Map.Entry<String, List<String>> entry : conf.organizingCommitteeList.entrySet()) {
			String role = entry.getKey();
			List<String> userIds = entry.getValue();
			for (String userId : userIds) {
				Resource userResource = eventDataModel.createResource(conf.ACE_URL + userId);
				tweetMetaDataModel.add(tweetId, conf.mentionsPerson, userResource);

				eventDataModel.add(userResource, conf.hasRole, tweetMetaDataModel.createResource(conf.ACE_URL + role));
				if (role == "generalChair") {
					eventDataModel.add(conferenceInstance, conf.hasGeneralChair, userResource);
				}
				if (role == "localChair") {
					eventDataModel.add(conferenceInstance, conf.hasLocalChair, userResource);
				}
				if (role == "ResearchTrackChair") {
					eventDataModel.add(conferenceInstance, conf.hasResearchTrackChair, userResource);
				}
				if (role == "ResourceTrackChair") {
					eventDataModel.add(conferenceInstance, conf.hasResourceTrackChair, userResource);
				}

				// Add detailed triples about the user
				Map<String, String> userDetails = conf.userData.get(userId);
				tweetMetaDataModel.add(userResource, RDF.type, conf.Person);
				tweetMetaDataModel.add(userResource, conf.hasUserID, tweetMetaDataModel.createLiteral(userId));
				tweetMetaDataModel.add(userResource, conf.hasAffiliation,
						tweetMetaDataModel.createLiteral(userDetails.get("affiliation")));
				tweetMetaDataModel.add(userResource, conf.hasDisplayName,
						tweetMetaDataModel.createLiteral(userDetails.get("displayName")));
				tweetMetaDataModel.add(userResource, conf.hasDesignation,
						tweetMetaDataModel.createLiteral(userDetails.get("designation")));
			}
		}

		tweetMetaDataModel.add(conf.tweetMetaProperties);
		eventDataModel.add(conf.eventDataProperties);
		tweetMetaData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_metadata" + ".ttl");
		eventData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_eventdata" + ".ttl");

		try {
			if (!tweetMetaData.exists()) {
				tweetMetaData.createNewFile();
				// System.out.println("File created: " + tweetMetaData.getName());
			} else {
				// System.out.println("File already exists: " + tweetMetaData.getName());
			}

			if (!eventData.exists()) {
				eventData.createNewFile();
				// System.out.println("File created: " + eventData.getName());
			} else {
				// System.out.println("File already exists: " + eventData.getName());
			}

			// Configure RDF writers to write in Turtle format
		} catch (IOException e) {
			System.out.println("An error occurred: " + e.getMessage());
			e.printStackTrace();
		}
		// Writing the models to the files
		writeModelToFile(tweetMetaDataModel, tweetMetaData);
		writeModelToFile(eventDataModel, eventData);
	}

	public void InsightsBasedOnAcceptedPapers(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		// pick a user from paper list
		List<String> paperIds = new ArrayList<>(conf.conferencePaperList.keySet());
		String selectedPaperId = paperIds.get(conf.random.nextInt(paperIds.size()));
		Map<String, Object> paperDetails = conf.conferencePaperList.get(selectedPaperId);
		List<String> authorList = (List<String>) paperDetails.get("AuthorList");
		String someUser = authorList.get(conf.random.nextInt(authorList.size()));
		String twitterAccount = conf.ACE_URL + someUser;

		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		Resource personAccount = tweetMetaDataModel.createResource(twitterAccount);

		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		tweetMetaDataModel.add(personAccount, RDF.type, conf.PersonAccount);
		// get a random person from paper data, mentions will be the list of additional
		// authors in the paper list

		// Extract paper details
		String paperTitle = (String) paperDetails.get("PaperTitle");
		String conferenceTrack = (String) paperDetails.get("ConferenceTrack");
		List<String> paperDomains = (List<String>) paperDetails.get("PaperDomains");

		// Create paper resource
		Resource paperResource = eventDataModel.createResource(conf.ACE_URL + selectedPaperId);
		eventDataModel.add(paperResource, RDF.type, conf.ConferencePaper);
		eventDataModel.add(paperResource, conf.hasPaperTrack, eventDataModel.createLiteral(conferenceTrack));
		eventDataModel.add(paperResource, conf.hasTitle, eventDataModel.createLiteral(paperTitle));

		// Add author triples
		for (String authorId : authorList) {
			Resource authorResource = eventDataModel.createResource(conf.ACE_URL + authorId);
			eventDataModel.add(authorResource, RDF.type, conf.Person);
			eventDataModel.add(paperResource, conf.hasAuthor, authorResource);
			Map<String, String> userDetails = conf.userData.get(authorId);
			tweetMetaDataModel.add(authorResource, conf.mentionsPerson, authorResource);
			tweetMetaDataModel.add(authorResource, conf.hasAffiliation,
					tweetMetaDataModel.createLiteral(userDetails.get("affiliation")));
			tweetMetaDataModel.add(authorResource, conf.hasDisplayName,
					tweetMetaDataModel.createLiteral(userDetails.get("displayName")));
			tweetMetaDataModel.add(authorResource, conf.hasDesignation,
					tweetMetaDataModel.createLiteral(userDetails.get("designation")));
		}
		for (String paperDomain : paperDomains) {
			Resource domain = eventDataModel.createResource(conf.ACE_URL + paperDomain);
			Resource hashtag = tweetMetaDataModel.createResource(conf.Twitter_URL + paperDomain);
			// eventDataModel.add(domain, RDF.type, conf.Person);
			eventDataModel.add(paperResource, conf.hasPaperDomain, domain);
			tweetMetaDataModel.add(tweetId, conf.hasHashtag, hashtag);
		}

		// Randomly select an author to tweet about the paper
//		String tweetingAuthorId = authorList.get(conf.random.nextInt(authorList.size()));
		// Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL +
		// generateTweetId());
		// personAccount = tweetMetaDataModel.createResource(conf.ACE_URL +
		// tweetingAuthorId);
		tweetMetaDataModel.add(personAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(personAccount, RDF.type, conf.Person);
		tweetMetaDataModel.add(personAccount, RDF.type, conf.PersonAccount);
		tweetMetaDataModel.add(personAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(personAccount, conf.hasUserID, personAccount);
		// Add additional tweet metadata
		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		eventDataModel.add(paperResource, conf.isAcceptedAt, confInstance);
//		tweetMetaDataModel.add(tweetId, conf.isAbout, confInstance);
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.isAbout, tweetMetaDataModel.createTypedLiteral(conf.ACE_URL + "insights"));

		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));

		tweetMetaDataModel.add(conf.tweetMetaProperties);
		eventDataModel.add(conf.eventDataProperties);
		tweetMetaData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_metadata" + ".ttl");
		eventData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_eventdata" + ".ttl");

		try {
			if (!tweetMetaData.exists()) {
				tweetMetaData.createNewFile();
				// System.out.println("File created: " + tweetMetaData.getName());
			} else {
				// System.out.println("File already exists: " + tweetMetaData.getName());
			}

			if (!eventData.exists()) {
				eventData.createNewFile();
				// System.out.println("File created: " + eventData.getName());
			} else {
				// System.out.println("File already exists: " + eventData.getName());
			}

			// Configure RDF writers to write in Turtle format
		} catch (IOException e) {
			System.out.println("An error occurred: " + e.getMessage());
			e.printStackTrace();
		}
		// Writing the models to the files
		writeModelToFile(tweetMetaDataModel, tweetMetaData);
		writeModelToFile(eventDataModel, eventData);
	}

	public void PaperSubmissionReminder(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		String twitterAccount = conf.ACE_URL + conf.confAccount;

		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		Resource conferenceAccount = tweetMetaDataModel.createResource(twitterAccount);

		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		tweetMetaDataModel.add(conferenceAccount, RDF.type, conf.ConferenceAccount);

		tweetMetaDataModel.add(conferenceAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(tweetId, conf.hasTweetID, tweetId);
		tweetMetaDataModel.add(conferenceAccount, conf.hasUserID, conferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.hasDisplayName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		Resource conferenceInstance = eventDataModel.createResource(conf.ACE_URL + conf.confInstance);
//		tweetMetaDataModel.add(tweetId, conf.isAbout, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAbout, eventDataModel.createTypedLiteral(conf.ACE_URL + "reminder"));
//		eventDataModel.add(tweetId, conf.hasEventPhase,
//				eventDataModel.createTypedLiteral(conf.ACE_URL + "announcement"));
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		// tweetMetaDataModel.add(tweetId, conf.hasHashtag,
		// eventDataModel.createLiteral(conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.mentionsConference, conferenceAccount);
		eventDataModel.add(conferenceInstance, RDF.type, conf.Conference);
		eventDataModel.add(conferenceInstance, conf.hasConferenceName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		eventDataModel.add(conferenceInstance, conf.hasEdition, eventDataModel.createTypedLiteral(conf.confCycle));
		Resource city = eventDataModel.createResource(conf.cities.get(conf.random.nextInt(conf.cities.size())));
		eventDataModel.add(city, RDF.type, conf.City);
		eventDataModel.add(conferenceInstance, conf.hasWebsiteURL,
				eventDataModel.createLiteral("www." + conf.confInstance + ".com"));

		String selectedEventMode = conf.TOKEN_EventMode[conf.random.nextInt(conf.TOKEN_EventMode.length)];

		switch (selectedEventMode) {
		case "offline":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "offline"));
			eventDataModel.add(conferenceInstance, conf.hasLocation, city);
			break;
		case "online":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "online"));
			eventDataModel.add(conferenceInstance, conf.hasWebsiteURL, eventDataModel.createLiteral(conf.confURL));
			break;
		case "hybrid":
			eventDataModel.add(conferenceInstance, conf.hasEventMode,
					eventDataModel.createResource(conf.ACE_URL + "hybrid"));
			eventDataModel.add(conferenceInstance, conf.hasLocation, city);
			eventDataModel.add(conferenceInstance, conf.hasWebsiteURL, eventDataModel.createLiteral(conf.confURL));
			break;
		}

		for (String track : conf.TOKEN_ConferenceEventTrack) {
			eventDataModel.add(conferenceInstance, conf.hasPaperTrack,
					eventDataModel.createResource(conf.ACE_URL + track));
		}

		// Add 'mentions' triples
		for (Map.Entry<String, List<String>> entry : conf.organizingCommitteeList.entrySet()) {
			String role = entry.getKey();
			List<String> userIds = entry.getValue();
			for (String userId : userIds) {
				Resource userResource = eventDataModel.createResource(conf.ACE_URL + userId);
				tweetMetaDataModel.add(tweetId, conf.mentionsPerson, userResource);
				eventDataModel.add(userResource, conf.hasRole, tweetMetaDataModel.createResource(conf.ACE_URL + role));
				if (role == "generalChair") {
					eventDataModel.add(conferenceInstance, conf.hasGeneralChair, userResource);
				}
				if (role == "localChair") {
					eventDataModel.add(conferenceInstance, conf.hasLocalChair, userResource);
				}
				if (role == "ResearchTrackChair") {
					eventDataModel.add(conferenceInstance, conf.hasResearchTrackChair, userResource);
				}
				if (role == "ResourceTrackChair") {
					eventDataModel.add(conferenceInstance, conf.hasResourceTrackChair, userResource);
				}

				// Add detailed triples about the user
				Map<String, String> userDetails = conf.userData.get(userId);
				tweetMetaDataModel.add(userResource, RDF.type, conf.Person);
				tweetMetaDataModel.add(userResource, conf.hasUserID, tweetMetaDataModel.createLiteral(userId));
				tweetMetaDataModel.add(userResource, conf.hasAffiliation,
						tweetMetaDataModel.createResource(userDetails.get("affiliation")));
				tweetMetaDataModel.add(userResource, conf.hasDisplayName,
						tweetMetaDataModel.createLiteral(userDetails.get("displayName")));
				tweetMetaDataModel.add(userResource, conf.hasDesignation,
						tweetMetaDataModel.createLiteral(userDetails.get("designation")));
			}
		}

		tweetMetaDataModel.add(conf.tweetMetaProperties);
		eventDataModel.add(conf.eventDataProperties);
		tweetMetaData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_metadata" + ".ttl");
		eventData = new File(conf.confDirectory + "/" + timeStamp.format(conf.fileNameFormatter) + "_" + tweetId0
				+ "_eventdata" + ".ttl");

		try {
			if (!tweetMetaData.exists()) {
				tweetMetaData.createNewFile();
				//// System.out.println("File created: " + tweetMetaData.getName());
			} else {
				// System.out.println("File already exists: " + tweetMetaData.getName());
			}

			if (!eventData.exists()) {
				eventData.createNewFile();
				// System.out.println("File created: " + eventData.getName());
			} else {
				// System.out.println("File already exists: " + eventData.getName());
			}

			// Configure RDF writers to write in Turtle format
		} catch (IOException e) {
			System.out.println("An error occurred: " + e.getMessage());
			e.printStackTrace();
		}
		// Writing the models to the files
		writeModelToFile(tweetMetaDataModel, tweetMetaData);
		writeModelToFile(eventDataModel, eventData);
	}

	public LocalDateTime getRandomTimestamp(LocalDateTime start, LocalDateTime end) {
	    long days = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
	    long randomDays = (long) (conf.random.nextDouble() * (days + 1));
	    long randomHours = (long) (conf.random.nextDouble() * 24);
	    long randomMinutes = (long) (conf.random.nextDouble() * 60);
	    long randomSeconds = (long) (conf.random.nextDouble() * 60);

	    return start.plusDays(randomDays).plusHours(randomHours).plusMinutes(randomMinutes).plusSeconds(randomSeconds);
	}

	public String generateTweetId() {

		conf.tweetCount = conf.tweetCount + 1;
		return "tweet" + Integer.toString(conf.tweetCount);
		// return UUID.randomUUID().toString();
	}

	public void writeModelToFile(Model model, File file) {
		// System.out.println("Writing model to file: " + file.getAbsolutePath());
		try (OutputStream out = new FileOutputStream(file)) {
			RDFDataMgr.write(out, model, RDFFormat.TURTLE_PRETTY);
			// System.out.println("Model successfully written to file.");
		} catch (IOException e) {
			System.err.println("An I/O error occurred while writing to file: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("An unexpected error occurred while writing to file: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
