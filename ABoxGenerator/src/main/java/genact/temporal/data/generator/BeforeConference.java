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
import java.io.OutputStream;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.riot.RDFDataMgr;
import java.time.temporal.ChronoUnit;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.RDFWriterF;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class BeforeConference {
	ConferenceStreams conf;
	Random random;
	Property rdfSubject = RDF.subject;
	Property rdfPredicate = RDF.predicate;
	Property rdfObject = RDF.object;
	File tweetMetaData_n3;
	File eventData_n3;
	RDFWriter tweetMetaDataWriter;
	RDFWriter tweetEventDataWriter;
	Model tweetMetaDataModel;
	Model eventDataModel;
	// Define properties

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
			PaperSubmissionReminder(paperSubmissionReminderTime);
			System.out.println("Paper Submission Reminder Tweet at: " + paperSubmissionReminderTime);
			paperSubmissionReminderTime = paperSubmissionReminderTime
					.plusDays(ThreadLocalRandom.current().nextInt(14, 22)); // Randomize
			// between
			// 2-3
			// weeks
		}

		// Timestamp for AcceptedPaperNotification (middle of conference duration)
		//		midConferenceTime = conferenceStart	.plusDays(conferenceStart.until(beforeConferenceEnd, ChronoUnit.DAYS) / 2); // Adjust conferenceDuration
		LocalDateTime acceptedPaperNotificationTime = midConferenceTime;
		LocalDateTime regReminderTime = midConferenceTime.plusWeeks(1);
		while (acceptedPaperNotificationTime.isBefore(acceptedPaperNotificationTime.plusDays(3))) { // peak about
			// acceptance

			AcceptedPaperNotification(acceptedPaperNotificationTime);
			RegistrationReminder(regReminderTime);

			acceptedPaperNotificationTime = acceptedPaperNotificationTime
					.plusDays(ThreadLocalRandom.current().nextInt(14, 22)); // Randomize
			regReminderTime = regReminderTime.plusDays(ThreadLocalRandom.current().nextInt(14, 22)); // Randomize
		}
		LocalDateTime InsightsBasedOnAcceptedPapersTime = getRandomTimestamp(callForPapersTime,
				acceptedPaperNotificationTime);
		InsightsBasedOnAcceptedPapers(InsightsBasedOnAcceptedPapersTime);

		LocalDateTime last15DaysBeforeConference = beforeConferenceEnd.minusDays(15);

		LocalDateTime excitementForAttendingTheConferenceTime = getRandomTimestamp(last15DaysBeforeConference,
				beforeConferenceEnd);
		while (excitementForAttendingTheConferenceTime.isBefore(beforeConferenceEnd.plusDays(3))) { // peak about
			// acceptance
			// starts
			// Schedule submission reminder tweet
			KeynotesAndPanelAnnouncement(excitementForAttendingTheConferenceTime);
			VolunteerandStudentGrantAnnouncement(excitementForAttendingTheConferenceTime);

			ExcitementForAttendingTheConference(excitementForAttendingTheConferenceTime);
			ScheduleAnnouncement(excitementForAttendingTheConferenceTime);

			excitementForAttendingTheConferenceTime = excitementForAttendingTheConferenceTime
					.plusDays(ThreadLocalRandom.current().nextInt(14, 22)); // Randomize
			// regReminderTime =
			// regReminderTime.plusDays(ThreadLocalRandom.current().nextInt(14, 22)); //
			// Randomize
		}

		// Timestamp for PaperSubmissionReminder (every 2-3 weeks during the Before

		System.out.println("Before Conference End: " + beforeConferenceEnd);

	}

	public void MainConferenceAnnouncement(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		Resource conferenceAccount = tweetMetaDataModel.createResource(conf.ACE_URL + "conf" + conf.confIndex);
		tweetMetaDataModel.add(conferenceAccount, RDF.type, conf.ConferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(tweetId, conf.hasTweetID, tweetId);
		tweetMetaDataModel.add(conferenceAccount, conf.hasUserID, conferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.hasDisplayName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEvent, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEventPhase,
				eventDataModel.createTypedLiteral(conf.ACE_URL + "mainConferenceAnnouncementPhase"));
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.hasHashtag, eventDataModel.createLiteral(conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.mentionsConference, confInstance);
		// mentions can be ignored here
		// tweetMetaDataModel.add(tweetId, conf.mentions, );
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
		String selectedEventMode = conf.TOKEN_EventMode[(int) (Math.random() * conf.TOKEN_EventMode.length)];
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

		tweetMetaDataModel.add(conf.tweetMetaProperties);
		eventDataModel.add(conf.eventDataProperties);
		tweetMetaData_n3 = new File(conf.confDirectory + tweetId0 + "_metadata");
		eventData_n3 = new File(conf.confDirectory + tweetId0 + "_eventdata");
		// Writing the models to the files
		writeModelToFile(tweetMetaDataModel, tweetMetaData_n3);
		writeModelToFile(eventDataModel, eventData_n3);
	}

	public void CallForPapersAnnouncement(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		Resource conferenceAccount = tweetMetaDataModel.createResource(conf.ACE_URL + "conf" + conf.confIndex);
		tweetMetaDataModel.add(conferenceAccount, RDF.type, conf.ConferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(tweetId, conf.hasTweetID, tweetId);
		tweetMetaDataModel.add(conferenceAccount, conf.hasUserID, conferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.hasDisplayName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEvent, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEventPhase,
				eventDataModel.createTypedLiteral(conf.ACE_URL + "mainConferenceAnnouncementPhase"));
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.hasHashtag, eventDataModel.createLiteral(conf.confInstance));

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
		String selectedEventMode = conf.TOKEN_EventMode[(int) (Math.random() * conf.TOKEN_EventMode.length)];
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
		tweetMetaData_n3 = new File(conf.confDirectory + tweetId0 + "_metadata");
		eventData_n3 = new File(conf.confDirectory + tweetId0 + "_eventdata");
		// Writing the models to the files
		writeModelToFile(tweetMetaDataModel, tweetMetaData_n3);
		writeModelToFile(eventDataModel, eventData_n3);
	}

	public void RegistrationReminder(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		Resource conferenceAccount = tweetMetaDataModel.createResource(conf.ACE_URL + "conf" + conf.confIndex);
		tweetMetaDataModel.add(conferenceAccount, RDF.type, conf.ConferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(tweetId, conf.hasTweetID, tweetId);
		tweetMetaDataModel.add(conferenceAccount, conf.hasUserID, conferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.hasDisplayName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEvent, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEventPhase,
				eventDataModel.createTypedLiteral(conf.ACE_URL + "mainConferenceAnnouncementPhase"));
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.hasHashtag, eventDataModel.createLiteral(conf.confInstance));

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
		String selectedEventMode = conf.TOKEN_EventMode[(int) (Math.random() * conf.TOKEN_EventMode.length)];
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
		tweetMetaData_n3 = new File(conf.confDirectory + tweetId0 + "_metadata");
		eventData_n3 = new File(conf.confDirectory + tweetId0 + "_eventdata");
		// Writing the models to the files
		writeModelToFile(tweetMetaDataModel, tweetMetaData_n3);
		writeModelToFile(eventDataModel, eventData_n3);
	}

	public void ExcitementAboutTheConferenceAnnouncement(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		Resource conferenceAccount = tweetMetaDataModel.createResource(conf.ACE_URL + "conf" + conf.confIndex);
		tweetMetaDataModel.add(conferenceAccount, RDF.type, conf.ConferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(tweetId, conf.hasTweetID, tweetId);
		tweetMetaDataModel.add(conferenceAccount, conf.hasUserID, conferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.hasDisplayName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEvent, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEventPhase,
				eventDataModel.createTypedLiteral(conf.ACE_URL + "mainConferenceAnnouncementPhase"));
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.hasHashtag, eventDataModel.createLiteral(conf.confInstance));

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
		String selectedEventMode = conf.TOKEN_EventMode[(int) (Math.random() * conf.TOKEN_EventMode.length)];
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
		tweetMetaData_n3 = new File(conf.confDirectory + tweetId0 + "_metadata");
		eventData_n3 = new File(conf.confDirectory + tweetId0 + "_eventdata");
		// Writing the models to the files
		writeModelToFile(tweetMetaDataModel, tweetMetaData_n3);
		writeModelToFile(eventDataModel, eventData_n3);
	}

	public void KeynotesAndPanelAnnouncement(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		Resource conferenceAccount = tweetMetaDataModel.createResource(conf.ACE_URL + "conf" + conf.confIndex);
		tweetMetaDataModel.add(conferenceAccount, RDF.type, conf.ConferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(tweetId, conf.hasTweetID, tweetId);
		tweetMetaDataModel.add(conferenceAccount, conf.hasUserID, conferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.hasDisplayName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEvent, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEventPhase,
				eventDataModel.createTypedLiteral(conf.ACE_URL + "mainConferenceAnnouncementPhase"));
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.hasHashtag, eventDataModel.createLiteral(conf.confInstance));

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
		String selectedEventMode = conf.TOKEN_EventMode[(int) (Math.random() * conf.TOKEN_EventMode.length)];
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
						tweetMetaDataModel.createLiteral(userDetails.get("affiliation")));
				tweetMetaDataModel.add(userResource, conf.hasDisplayName,
						tweetMetaDataModel.createLiteral(userDetails.get("displayName")));
				tweetMetaDataModel.add(userResource, conf.hasDesignation,
						tweetMetaDataModel.createLiteral(userDetails.get("designation")));
				tweetMetaDataModel.add(userResource, conf.givesTalkOn,
						tweetMetaDataModel.createLiteral(userDetails.get("keynoteTitle")));

				if (type.equals("Keynote")) {
					tweetMetaDataModel.add(userResource, conf.hasRole, conf.KeynoteSpeakerRole);
				} else if (type.equals("InvitedTalk")) {
					tweetMetaDataModel.add(userResource, conf.hasRole, conf.InvitedTalkSpeakerRole);
				} else if (type.equals("Speaker")) {
					tweetMetaDataModel.add(userResource, conf.hasRole, conf.SpeakerRole);
				}
			}
			tweetMetaDataModel.add(conf.tweetMetaProperties);
			eventDataModel.add(conf.eventDataProperties);
			tweetMetaData_n3 = new File(conf.confDirectory + tweetId0 + "_metadata");
			eventData_n3 = new File(conf.confDirectory + tweetId0 + "_eventdata");
			// Writing the models to the files
			writeModelToFile(tweetMetaDataModel, tweetMetaData_n3);
			writeModelToFile(eventDataModel, eventData_n3);
		}
	}

	public void VolunteerandStudentGrantAnnouncement(LocalDateTime timeStamp) {
		for (Map.Entry<String, List<String>> entry : conf.volunteerAndStudentGrantList.entrySet()) {
			String type = entry.getKey();
			List<String> userIds = entry.getValue();

			for (String userId : userIds) {
				Resource userResource = tweetMetaDataModel.createResource(conf.ACE_URL + userId);

				// Common user triples
				tweetMetaDataModel.add(userResource, RDF.type, conf.Person);
				tweetMetaDataModel.add(userResource, RDF.type, conf.PersonAccount);
				tweetMetaDataModel.add(userResource, conf.hasUserID, tweetMetaDataModel.createLiteral(userId));

				Map<String, String> userDetails = conf.userData.get(userId);
				tweetMetaDataModel.add(userResource, conf.hasAffiliation,
						tweetMetaDataModel.createLiteral(userDetails.get("affiliation")));
				tweetMetaDataModel.add(userResource, conf.hasDisplayName,
						tweetMetaDataModel.createLiteral(userDetails.get("displayName")));
				tweetMetaDataModel.add(userResource, conf.hasDesignation,
						tweetMetaDataModel.createLiteral(userDetails.get("designation")));

				// Generate a tweet ID and add tweet triples
				String tweetId0 = UUID.randomUUID().toString();
				Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
				tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
				tweetMetaDataModel.add(userResource, conf.posts, tweetId);

				if (type.equals("Volunteer")) {
					tweetMetaDataModel.add(userResource, conf.volunteersFor, conf.confInstance);
					tweetMetaDataModel.add(userResource, conf.attends,
							tweetMetaDataModel.createResource(conf.confInstance));
				} else if (type.equals("StudentGrant")) {
					tweetMetaDataModel.add(userResource, conf.getsStudentGrantFor, conf.confInstance);
				}
			}
		}
	}

	public void ExcitementForAttendingTheConference(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		//has to be changed

		Resource conferenceAccount = tweetMetaDataModel.createResource(conf.ACE_URL + "conf" + conf.confIndex);
		tweetMetaDataModel.add(conferenceAccount, RDF.type, conf.Person);
		tweetMetaDataModel.add(conferenceAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(tweetId, conf.hasTweetID, tweetId);


		change to person
		tweetMetaDataModel.add(conferenceAccount, conf.hasUserID, conferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.hasDisplayName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEvent, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEventPhase,
				eventDataModel.createTypedLiteral(conf.ACE_URL + "mainConferenceAnnouncementPhase"));
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.hasHashtag, eventDataModel.createLiteral(conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.mentionsConference, confInstance);
		// mentions can be ignored here
		// tweetMetaDataModel.add(tweetId, conf.mentions, );
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
		String selectedEventMode = conf.TOKEN_EventMode[(int) (Math.random() * conf.TOKEN_EventMode.length)];
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

		tweetMetaDataModel.add(conf.tweetMetaProperties);
		eventDataModel.add(conf.eventDataProperties);
		tweetMetaData_n3 = new File(conf.confDirectory + tweetId0 + "_metadata");
		eventData_n3 = new File(conf.confDirectory + tweetId0 + "_eventdata");
		// Writing the models to the files
		writeModelToFile(tweetMetaDataModel, tweetMetaData_n3);
		writeModelToFile(eventDataModel, eventData_n3);
	}

	public void AcceptedPaperNotification(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		// get a random person from paper data, mentions will be the list of additional
		// authors in the paper list
		List<String> paperIds = new ArrayList<>(conf.conferencePaperList.keySet());
		String selectedPaperId = paperIds.get(conf.random.nextInt(paperIds.size()));
		Map<String, Object> paperDetails = conf.conferencePaperList.get(selectedPaperId);

		// Extract paper details
		String paperTitle = (String) paperDetails.get("PaperTitle");
		String conferenceTrack = (String) paperDetails.get("ConferenceTrack");
		List<String> authorList = (List<String>) paperDetails.get("AuthorList");
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
			// eventDataModel.add(domain, RDF.type, conf.Person);
			eventDataModel.add(paperResource, conf.hasPaperDomain, domain);
			tweetMetaDataModel.add(tweetId, conf.hasHashtag, domain);
		}

		// Randomly select an author to tweet about the paper
		String tweetingAuthorId = authorList.get(random.nextInt(authorList.size()));
		// Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL +
		// generateTweetId());
		Resource personAccount = tweetMetaDataModel.createResource(conf.ACE_URL + tweetingAuthorId);
		tweetMetaDataModel.add(personAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(personAccount, RDF.type, conf.Person);
		tweetMetaDataModel.add(personAccount, RDF.type, conf.PersonAccount);
		tweetMetaDataModel.add(personAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(personAccount, conf.hasUserID, personAccount);
		// Add additional tweet metadata
		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		eventDataModel.add(paperResource, conf.isAcceptedAt, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEvent, confInstance);
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.isAboutEventPhase,
				eventDataModel.createTypedLiteral(conf.ACE_URL + "acceptedPapersNotificationPhase"));

		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));

		// Add hashtags for paper domain

	}

	public void ScheduleAnnouncement(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		Resource conferenceAccount = tweetMetaDataModel.createResource(conf.ACE_URL + "conf" + conf.confIndex);
		tweetMetaDataModel.add(conferenceAccount, RDF.type, conf.ConferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(tweetId, conf.hasTweetID, tweetId);
		tweetMetaDataModel.add(conferenceAccount, conf.hasUserID, conferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.hasDisplayName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEvent, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEventPhase,
				eventDataModel.createTypedLiteral(conf.ACE_URL + "mainConferenceAnnouncementPhase"));
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.hasHashtag, eventDataModel.createLiteral(conf.confInstance));

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
		String selectedEventMode = conf.TOKEN_EventMode[(int) (Math.random() * conf.TOKEN_EventMode.length)];
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

		//		// Add 'mentions' triples
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
		tweetMetaData_n3 = new File(conf.confDirectory + tweetId0 + "_metadata");
		eventData_n3 = new File(conf.confDirectory + tweetId0 + "_eventdata");
		// Writing the models to the files
		writeModelToFile(tweetMetaDataModel, tweetMetaData_n3);
		writeModelToFile(eventDataModel, eventData_n3);
	}

	public void InsightsBasedOnAcceptedPapers(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		// get a random person from paper data, mentions will be the list of additional
		// authors in the paper list
		List<String> paperIds = new ArrayList<>(conf.conferencePaperList.keySet());
		String selectedPaperId = paperIds.get(conf.random.nextInt(paperIds.size()));
		Map<String, Object> paperDetails = conf.conferencePaperList.get(selectedPaperId);

		// Extract paper details
		String paperTitle = (String) paperDetails.get("PaperTitle");
		String conferenceTrack = (String) paperDetails.get("ConferenceTrack");
		List<String> authorList = (List<String>) paperDetails.get("AuthorList");
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
			// eventDataModel.add(domain, RDF.type, conf.Person);
			eventDataModel.add(paperResource, conf.hasPaperDomain, domain);
			tweetMetaDataModel.add(tweetId, conf.hasHashtag, domain);
		}

		// Randomly select an author to tweet about the paper
		String tweetingAuthorId = authorList.get(random.nextInt(authorList.size()));
		// Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL +
		// generateTweetId());
		Resource personAccount = tweetMetaDataModel.createResource(conf.ACE_URL + tweetingAuthorId);
		tweetMetaDataModel.add(personAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(personAccount, RDF.type, conf.Person);
		tweetMetaDataModel.add(personAccount, RDF.type, conf.PersonAccount);
		tweetMetaDataModel.add(personAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(personAccount, conf.hasUserID, personAccount);
		// Add additional tweet metadata
		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		eventDataModel.add(paperResource, conf.isAcceptedAt, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEvent, confInstance);
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.isAboutEventPhase,
				eventDataModel.createTypedLiteral(conf.ACE_URL + "acceptedPapersNotificationPhase"));

		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
	}

	public void PaperSubmissionReminder(LocalDateTime timeStamp) {
		tweetMetaDataModel = ModelFactory.createDefaultModel();
		eventDataModel = ModelFactory.createDefaultModel();
		String tweetId0 = generateTweetId();
		Resource tweetId = tweetMetaDataModel.createResource(conf.Twitter_URL + tweetId0);
		tweetMetaDataModel.add(tweetId, RDF.type, conf.Tweet);
		Resource conferenceAccount = tweetMetaDataModel.createResource(conf.ACE_URL + "conf" + conf.confIndex);
		tweetMetaDataModel.add(conferenceAccount, RDF.type, conf.ConferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.posts, tweetId);
		tweetMetaDataModel.add(tweetId, conf.hasTweetID, tweetId);
		tweetMetaDataModel.add(conferenceAccount, conf.hasUserID, conferenceAccount);
		tweetMetaDataModel.add(conferenceAccount, conf.hasDisplayName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel
				.createTypedLiteral(timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEvent, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEventPhase,
				eventDataModel.createTypedLiteral(conf.ACE_URL + "mainConferenceAnnouncementPhase"));
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.hasHashtag, eventDataModel.createLiteral(conf.confInstance));

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
		String selectedEventMode = conf.TOKEN_EventMode[(int) (Math.random() * conf.TOKEN_EventMode.length)];
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
		tweetMetaData_n3 = new File(conf.confDirectory + tweetId0 + "_metadata");
		eventData_n3 = new File(conf.confDirectory + tweetId0 + "_eventdata");
		// Writing the models to the files
		writeModelToFile(tweetMetaDataModel, tweetMetaData_n3);
		writeModelToFile(eventDataModel, eventData_n3);
	}

	public static LocalDateTime getRandomTimestamp(LocalDateTime start, LocalDateTime end) {
		long days = start.until(end, ChronoUnit.DAYS);
		long randomDays = ThreadLocalRandom.current().nextLong(days + 1);
		long randomHours = ThreadLocalRandom.current().nextLong(24);
		long randomMinutes = ThreadLocalRandom.current().nextLong(60);
		long randomSeconds = ThreadLocalRandom.current().nextLong(60);

		return start.plusDays(randomDays).plusHours(randomHours).plusMinutes(randomMinutes).plusSeconds(randomSeconds);
	}

	public String generateTweetId() {
		return UUID.randomUUID().toString();
	}

	public void writeModelToFile(Model model, File file) {
		try (OutputStream out = new FileOutputStream(file)) {
			RDFDataMgr.write(out, model, RDFFormat.TURTLE_PRETTY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
