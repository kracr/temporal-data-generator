package genact.temporal.data.generator;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
public class AfterConference {
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
	public AfterConference(ConferenceStreams conf, long startTime, long endTime) {
		// Convert timestamps to LocalDateTime
		this.random = conf.random;
        LocalDateTime duringConferenceEnd = LocalDateTime.ofInstant(new Date(startTime).toInstant(), ZoneId.systemDefault());
        LocalDateTime afterConferenceEnd = LocalDateTime.ofInstant(new Date(endTime).toInstant(), ZoneId.systemDefault());

        // Display timestamps for each phase
        System.out.println("Cycle " + conf.confCycle);
        System.out.println("Before Conference Start: " + duringConferenceEnd);
        BestPaperTrackAwardAnnouncement(duringConferenceEnd,afterConferenceEnd );
        NextConferenceAnnouncement(duringConferenceEnd,afterConferenceEnd );
        SuccessfulEventCongratulations(duringConferenceEnd,afterConferenceEnd );
        MemorableConferenceExperience(duringConferenceEnd,afterConferenceEnd );
        ConferenceSuccessThankYou(duringConferenceEnd,afterConferenceEnd );
//        BestPaperTrackAwardAnnouncement, NextConferenceAnnouncement, SuccessfulEventCongratulations, 
//        MemorableConferenceExperience, ConferenceSuccessThankYou
//        for each tweet assign timestamp randomly getRandomTimestamp(startTime, endTime); (but change this time to long?)
        System.out.println("After Conference End: " + afterConferenceEnd);
	}
	public void BestPaperTrackAwardAnnouncement(LocalDateTime startTime, LocalDateTime endTime ) {
		
	}
	public void NextConferenceAnnouncement(LocalDateTime startTime, LocalDateTime endTime ) {
		
	}
	public void SuccessfulEventCongratulations(LocalDateTime startTime, LocalDateTime endTime ) {
		
	}
	public void MemorableConferenceExperience(LocalDateTime startTime, LocalDateTime endTime ) {
		
	}
	public void ConferenceSuccessThankYou(LocalDateTime startTime, LocalDateTime endTime ) {
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
		tweetMetaDataModel.add(tweetId, conf.hasDateTimestamp, tweetMetaDataModel.createTypedLiteral(
				timeStamp.format(DateTimeFormatter.ISO_DATE_TIME), XSDDatatype.XSDdateTime));
		Resource confInstance = tweetMetaDataModel.createResource(conf.ACE_URL + conf.confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEvent, confInstance);
		tweetMetaDataModel.add(tweetId, conf.isAboutEventPhase, eventDataModel.createTypedLiteral(conf.ACE_URL +"mainConferenceAnnouncementPhase"));
		tweetMetaDataModel.add(confInstance, RDF.type, conf.Conference);
		tweetMetaDataModel.add(tweetId, conf.hasHashtag, eventDataModel.createLiteral(conf.confInstance));

		Resource conferenceInstance=eventDataModel.createResource(conf.ACE_URL +conf.confInstance);
		eventDataModel.add(conferenceInstance, RDF.type, conf.Conference);
		eventDataModel.add(conferenceInstance, conf.hasConferenceName,
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance));
		eventDataModel.add(conferenceInstance, conf.hasEdition,eventDataModel.createTypedLiteral(conf.confCycle));
				tweetMetaDataModel.createLiteral("International Conference on " + conf.confInstance);
		//String city_name=;
		Resource city=eventDataModel.createResource("city_name");
		eventDataModel.add(city, RDF.type, conf.City);
		eventDataModel.add(conferenceInstance, conf.hasWebsiteURL, eventDataModel.createLiteral("www."+conf.confInstance+".com"));
		String selectedEventMode=conf.TOKEN_EventMode[(int) (Math.random() * conf.TOKEN_EventMode.length)];
		switch (selectedEventMode) {
        case "offline":
        	eventDataModel.add(conferenceInstance, conf.hasEventMode,eventDataModel.createResource(conf.ACE_URL +"offline"));
            eventDataModel.add(conferenceInstance, conf.hasLocation, city);
            break;
        case "online":
        	eventDataModel.add(conferenceInstance, conf.hasEventMode,eventDataModel.createResource(conf.ACE_URL +"online"));
            eventDataModel.add(conferenceInstance, conf.hasWebsiteURL, eventDataModel.createLiteral("www."+conf.confInstance+".com"));
            break;
        case "hybrid":
        	eventDataModel.add(conferenceInstance, conf.hasEventMode,eventDataModel.createResource(conf.ACE_URL +"hybrid"));
            eventDataModel.add(conferenceInstance, conf.hasLocation, city);
            eventDataModel.add(conferenceInstance, conf.hasWebsiteURL, eventDataModel.createLiteral("www."+conf.confInstance+".com"));
            break;
    }
		
		// Add 'mentions' triples
		for (Map.Entry<String, List<String>> entry : conf.organizingCommitteeList.entrySet()) {
		    String role = entry.getKey();
		    List<String> userIds = entry.getValue();
		    for (String userId : userIds) {
		        Resource userResource = tweetMetaDataModel.createResource(conf.ACE_URL + userId);
		        tweetMetaDataModel.add(tweetId, conf.mentions, userResource);
		        eventDataModel.add(userResource, conf.hasRole, tweetMetaDataModel.createResource(conf.ACE_URL + role));
		        // Add detailed triples about the user
		        Map<String, String> userDetails = conf.userData.get(userId);
		        tweetMetaDataModel.add(userResource, RDF.type, conf.Person);
		        tweetMetaDataModel.add(userResource, conf.hasUserID, tweetMetaDataModel.createLiteral(userId));
		        tweetMetaDataModel.add(userResource, conf.hasAffiliation, tweetMetaDataModel.createLiteral(userDetails.get("affiliation")));
		        tweetMetaDataModel.add(userResource, conf.hasDisplayName, tweetMetaDataModel.createLiteral(userDetails.get("displayName")));
		        tweetMetaDataModel.add(userResource, conf.hasDesignation, tweetMetaDataModel.createLiteral(userDetails.get("designation")));
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
	    
	    return start
	        .plusDays(randomDays)
	        .plusHours(randomHours)
	        .plusMinutes(randomMinutes)
	        .plusSeconds(randomSeconds);
	}
	 private String generateTweetId() {
	        return UUID.randomUUID().toString();
	    }
}
