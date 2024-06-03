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
import java.util.UUID;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;

public class DuringConference {
	ConferenceStreams conf;
	Random random;
	Property rdfPredicate = RDF.predicate;
	File tweetMetaData;
	File eventData;
	RDFWriter tweetMetaDataWriter;
	RDFWriter tweetEventDataWriter;
	Model tweetMetaDataModel;
	Model eventDataModel;

	public DuringConference(ConferenceStreams conf, long startTime, long endTime) {
		this.conf = conf;
		this.random = conf.random;
		// Convert timestamps to LocalDateTime
		LocalDateTime beforeConferenceEnd = LocalDateTime.ofInstant(new Date(startTime).toInstant(),
				ZoneId.systemDefault());
		LocalDateTime duringConferenceEnd = LocalDateTime.ofInstant(new Date(endTime).toInstant(),
				ZoneId.systemDefault());

		// Display timestamps for each phase
		System.out.println("Cycle " + conf.confCycle);
		System.out.println("During Conference Start: " + beforeConferenceEnd);
		// calculate random timestamps
		// implement a for loop here
		LocalDateTime timestamp = getRandomTimestamp(beforeConferenceEnd, duringConferenceEnd);
		for (int i = 0; i <= this.random.nextInt(conf.speakerList.size()); i++) {
			timestamp = getRandomTimestamp(beforeConferenceEnd, duringConferenceEnd);
			SessionReminder(timestamp);
		}

		int equalPeak = conf.during_conference_peak / 3;
		int remainder = conf.during_conference_peak % 3;

		int peak1 = equalPeak;
		int peak2 = equalPeak;
		int peak3 = equalPeak + remainder; // Add the remainder to one of the peaks to ensure the total sum is correct

		// First loop for PaperPresentationReminder
		for (int i = 0; i < peak1; i++) {
			timestamp = getRandomTimestamp(beforeConferenceEnd, duringConferenceEnd);
			PaperPresentationReminder(timestamp); // also uses details from sessions in the previous loop
		}

		// Second loop for InsightsBasedOnPresentations
		for (int i = 0; i < peak2; i++) {
			timestamp = getRandomTimestamp(beforeConferenceEnd, duringConferenceEnd);
			InsightsBasedOnPresentations(timestamp); // uses details from both session and paper presentations
		}

		// Third loop for NetworkingExperience
		for (int i = 0; i < peak3; i++) {
			timestamp = getRandomTimestamp(beforeConferenceEnd, duringConferenceEnd);
			NetworkingExperience(timestamp); // general authors non authors volunteers organizers or anybody can be
												// attendee.
		}

		System.out.println("During Conference End: " + duringConferenceEnd);
	}

	public void SessionReminder(LocalDateTime timeStamp) {
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

		tweetMetaDataModel.add(tweetId, conf.isAbout, confInstance);
//		tweetMetaDataModel.add(tweetId, conf.isAbout,
//				eventDataModel.createTypedLiteral(conf.ACE_URL + "mainConferenceAnnouncementPhase"));
//		eventDataModel.add(tweetId, conf.hasEventPhase,
//				eventDataModel.createTypedLiteral(conf.ACE_URL + "mainConferenceAnnouncementPhase"));
//		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.hasHashtag, eventDataModel.createLiteral(conf.confInstance));
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
					// // // System.out.println("File already exists: " + tweetMetaData.getName());
				} else {
					//System.out.println("File already exists: " + tweetMetaData.getName());
				}

				if (!eventData.exists()) {
					eventData.createNewFile();
					//System.out.println("File created: " + eventData.getName());
				} else {
					//System.out.println("File already exists: " + eventData.getName());
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

	public void PaperPresentationReminder(LocalDateTime timeStamp) {
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
			// eventDataModel.add(domain, RDF.type, conf.Person);
			eventDataModel.add(paperResource, conf.hasPaperDomain, domain);
			tweetMetaDataModel.add(tweetId, conf.hasHashtag, domain);
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
		tweetMetaDataModel.add(tweetId, conf.isAbout, confInstance);
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.isAbout,
				eventDataModel.createTypedLiteral(conf.ACE_URL + "acceptedPapersNotificationPhase"));

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
				// // System.out.println("File created: " + tweetMetaData.getName());
			} else {
				// System.out.println("File already exists: " + tweetMetaData.getName());
			}

			if (!eventData.exists()) {
				eventData.createNewFile();
				//System.out.println("File created: " + eventData.getName());
			} else {
				//System.out.println("File already exists: " + eventData.getName());
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

	public void InsightsBasedOnPresentations(LocalDateTime timeStamp) {
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
		tweetMetaDataModel.add(tweetId, conf.isAbout, confInstance);
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.isAbout,
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
				// // // System.out.println("File created: " + tweetMetaData.getName());
			} else {
				// System.out.println("File already exists: " + tweetMetaData.getName());
			}

			if (!eventData.exists()) {
				eventData.createNewFile();
				//System.out.println("File created: " + eventData.getName());
			} else {
				//System.out.println("File already exists: " + eventData.getName());
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

	public void NetworkingExperience(LocalDateTime timeStamp) {
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
		tweetMetaDataModel.add(tweetId, conf.isAbout, confInstance);
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.isAbout,
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
				// // // System.out.println("File created: " + tweetMetaData.getName());
			} else {
				// System.out.println("File already exists: " + tweetMetaData.getName());
			}

			if (!eventData.exists()) {
				eventData.createNewFile();
				//System.out.println("File created: " + eventData.getName());
			} else {
				//System.out.println("File already exists: " + eventData.getName());
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
		long days = start.until(end, ChronoUnit.DAYS);
        long randomDays = conf.random.nextLong(days + 1);
        long randomHours = conf.random.nextLong(24);
        long randomMinutes = conf.random.nextLong(60);
        long randomSeconds = conf.random.nextLong(60);

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
