package genact.temporal.data.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;

public class AfterConference {
	ConferenceStreams conf;
	Random random;
	Property rdfPredicate = RDF.predicate;
	File tweetMetaData;
	File eventData;
	RDFWriter tweetMetaDataWriter;
	RDFWriter tweetEventDataWriter;
	Model tweetMetaDataModel;
	Model eventDataModel;

	public AfterConference(ConferenceStreams conf, long startTime, long endTime) {
		this.conf = conf;
		this.random = conf.random;
		LocalDateTime duringConferenceEnd = LocalDateTime.ofInstant(new Date(startTime).toInstant(),
				ZoneId.systemDefault());
		LocalDateTime afterConferenceEnd = LocalDateTime.ofInstant(new Date(endTime).toInstant(),
				ZoneId.systemDefault());

		LocalDateTime timestamp = getRandomTimestamp(duringConferenceEnd, afterConferenceEnd);
		for (String track : conf.TOKEN_ConferenceEventTrack) {
			BestPaperTrackAwardAnnouncement(timestamp, track);
		}
		// NextConferenceAnnouncement(timestamp);
		for (int i = 0; i <= conf.after_conference_peak; i++) {
			timestamp = getRandomTimestamp(duringConferenceEnd, afterConferenceEnd);
			SuccessfulEventCongratulations(timestamp);
			timestamp = getRandomTimestamp(duringConferenceEnd, afterConferenceEnd);
			MemorableConferenceExperience(timestamp);
			timestamp = getRandomTimestamp(duringConferenceEnd, afterConferenceEnd);
			ConferenceSuccessThankYou(timestamp);
		}

		while (timestamp.isBefore(conf.nextCycleStart)) {
			MemorableConferenceExperience(timestamp.plusDays(ThreadLocalRandom.current().nextInt(5, 7)));
		}
	}

	public void BestPaperTrackAwardAnnouncement(LocalDateTime timeStamp, String track) {
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
		tweetMetaDataModel.add(tweetId, conf.isAboutEvent, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEventPhase,
				eventDataModel.createTypedLiteral(conf.ACE_URL + "mainConferenceAnnouncementPhase"));
		eventDataModel.add(tweetId, conf.hasEventPhase,
				eventDataModel.createTypedLiteral(conf.ACE_URL + "mainConferenceAnnouncementPhase"));
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.hasHashtag, eventDataModel.createLiteral(conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.mentionsConference, conferenceAccount);
		eventDataModel.add(conferenceInstance, RDF.type, conf.Conference);
		eventDataModel.add(conferenceInstance, conf.hasConferenceName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		eventDataModel.add(conferenceInstance, conf.hasEdition, eventDataModel.createTypedLiteral(conf.confCycle));
		Resource city = eventDataModel.createResource(conf.cities.get(random.nextInt(conf.cities.size())));
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

		for (String track1 : conf.TOKEN_ConferenceEventTrack) {
			eventDataModel.add(conferenceInstance, conf.hasPaperTrack,
					eventDataModel.createResource(conf.ACE_URL + track1));
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
				System.out.println("File created: " + tweetMetaData.getName());
			} else {
				System.out.println("File already exists: " + tweetMetaData.getName());
			}

			if (!eventData.exists()) {
				eventData.createNewFile();
				System.out.println("File created: " + eventData.getName());
			} else {
				System.out.println("File already exists: " + eventData.getName());
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

	public void NextConferenceAnnouncement(LocalDateTime timeStamp) {
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
				System.out.println("File created: " + tweetMetaData.getName());
			} else {
				System.out.println("File already exists: " + tweetMetaData.getName());
			}

			if (!eventData.exists()) {
				eventData.createNewFile();
				System.out.println("File created: " + eventData.getName());
			} else {
				System.out.println("File already exists: " + eventData.getName());
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

	public void SuccessfulEventCongratulations(LocalDateTime timeStamp) {
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
		tweetMetaDataModel.add(tweetId, conf.isAboutEvent, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEventPhase,
				eventDataModel.createTypedLiteral(conf.ACE_URL + "mainConferenceAnnouncementPhase"));
		eventDataModel.add(tweetId, conf.hasEventPhase,
				eventDataModel.createTypedLiteral(conf.ACE_URL + "mainConferenceAnnouncementPhase"));
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.hasHashtag, eventDataModel.createLiteral(conf.confInstance));
		tweetMetaDataModel.add(tweetId, conf.mentionsConference, conferenceAccount);
		eventDataModel.add(conferenceInstance, RDF.type, conf.Conference);
		eventDataModel.add(conferenceInstance, conf.hasConferenceName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		eventDataModel.add(conferenceInstance, conf.hasEdition, eventDataModel.createTypedLiteral(conf.confCycle));
		Resource city = eventDataModel.createResource(conf.cities.get(random.nextInt(conf.cities.size())));
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
				System.out.println("File created: " + tweetMetaData.getName());
			} else {
				System.out.println("File already exists: " + tweetMetaData.getName());
			}

			if (!eventData.exists()) {
				eventData.createNewFile();
				System.out.println("File created: " + eventData.getName());
			} else {
				System.out.println("File already exists: " + eventData.getName());
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

	public void MemorableConferenceExperience(LocalDateTime timeStamp) {
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
			// eventDataModel.add(domain, RDF.type, conf.Person);
			eventDataModel.add(paperResource, conf.hasPaperDomain, domain);
			tweetMetaDataModel.add(tweetId, conf.hasHashtag, domain);
		}

		// Randomly select an author to tweet about the paper
//		String tweetingAuthorId = authorList.get(random.nextInt(authorList.size()));
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
		tweetMetaDataModel.add(tweetId, conf.isAboutEvent, confInstance);
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.isAboutEventPhase,
				eventDataModel.createTypedLiteral(conf.ACE_URL + "acceptedPapersNotificationPhase"));

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
				System.out.println("File created: " + tweetMetaData.getName());
			} else {
				System.out.println("File already exists: " + tweetMetaData.getName());
			}

			if (!eventData.exists()) {
				eventData.createNewFile();
				System.out.println("File created: " + eventData.getName());
			} else {
				System.out.println("File already exists: " + eventData.getName());
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

	public void ConferenceSuccessThankYou(LocalDateTime timeStamp) {
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

		// Add 'mentions' triples
		for (Map.Entry<String, List<String>> entry : conf.organizingCommitteeList.entrySet()) {
			String role = entry.getKey();
			List<String> userIds = entry.getValue();
			for (String userId : userIds) {
				Resource userResource = tweetMetaDataModel.createResource(conf.ACE_URL + userId);
				tweetMetaDataModel.add(tweetId, conf.mentionsPerson, userResource);
				eventDataModel.add(userResource, conf.hasRole, tweetMetaDataModel.createResource(conf.ACE_URL + role));
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
				System.out.println("File created: " + tweetMetaData.getName());
			} else {
				System.out.println("File already exists: " + tweetMetaData.getName());
			}

			if (!eventData.exists()) {
				eventData.createNewFile();
				System.out.println("File created: " + eventData.getName());
			} else {
				System.out.println("File already exists: " + eventData.getName());
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

	public static LocalDateTime getRandomTimestamp(LocalDateTime start, LocalDateTime end) {
		long days = start.until(end, ChronoUnit.DAYS);
		long randomDays = ThreadLocalRandom.current().nextLong(days + 1);
		long randomHours = ThreadLocalRandom.current().nextLong(24);
		long randomMinutes = ThreadLocalRandom.current().nextLong(60);
		long randomSeconds = ThreadLocalRandom.current().nextLong(60);

		return start.plusDays(randomDays).plusHours(randomHours).plusMinutes(randomMinutes).plusSeconds(randomSeconds);
	}

	public String generateTweetId() {

		conf.tweetCount = conf.tweetCount + 1;
		return "tweet" + Integer.toString(conf.tweetCount);
		// return UUID.randomUUID().toString();
	}

	public void writeModelToFile(Model model, File file) {
		try (OutputStream out = new FileOutputStream(file)) {
			RDFDataMgr.write(out, model, RDFFormat.TURTLE_PRETTY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
