/** For each confersity instance, College instances (Both Women and Co-Ed) and Research Groups are generated. 
 * And Basic hasName, hasCode data property assertion axioms are generated
* In order to modify the min-max range,that is, to modify the density of each node, user can make changes in the config.properties file */

package genact.temporal.data.generator;

import org.apache.jena.query.ARQ;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import org.yaml.snakeyaml.Yaml;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFWriterBuilder;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.vocabulary.RDF;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.jena.query.DatasetFactory;
import java.io.OutputStream;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.RDFDataMgr;
import java.util.concurrent.CountDownLatch;

/*
randomly picked a paper to map the last authors in the author list as the keynote, trackchairs, organizers
*/
//for each conference, we can have a start time that's within a range. For the dataset we provide we chose 6 months, users
//can change it
public class ConferenceStreams {
	
	DataGenerator gen;
	BeforeConference bc;
	DuringConference dc;
	AfterConference ac;
	int confIndex, confCycle;
	String confId, confName, confInstance, confURL;
	String profile;
	int acceptedPaperCount;
	int otherPeopleInvolved;
	int seed;
	Random random = new Random();
	Dataset dataset = DatasetFactory.create();
	int graphCounter = 1;
	HashSet<String> attendees = new HashSet<>();
	HashSet<String> nonAttendees = new HashSet<>();
	HashSet<String> organizers = new HashSet<>();
	HashSet<String> chairs = new HashSet<>();
	List<String[]> confTweets = new ArrayList<>();
	Map<String, List<String[]>> userTweets = new HashMap<>();
	File streamFile;
	int year;
	Map<String, Map<String, Object>> papers;
	PrintWriter writer;
	String[] categories = { "Conference Announcement", "Call for Papers", "Submission Reminder", "Notification",
			"Registration Reminder", "Before Conference", "During Conference", "After Conference" };
	long currentTimeMillis;
	String ACE_URL = "https://kracr.iiitd.edu.in/AcademicConferenceEvent#";
	String OWL2Bench_URL = "https://kracr.iiitd.edu.in/OWL2Bench#";
	String Location_URL = "https://kracr.iiitd.edu.in/Location#";
	String Twitter_URL = "https://kracr.iiitd.edu.in/Twitter#";
	String directoryPath;
	File streamsDirectory;
	File confDirectory;
	int acceptedPaperCount_min;
	int acceptedPaperCount_max;
	int peopleDirectlyInvolved_min;
	int peopleDirectlyInvolved_max;
	int peopleDirectlyInvolved;
	int usersInvolved_min;
	int usersInvolved_max;
	int usersInvolved;
	int otherPeopleInvolved_min;
	int otherPeopleInvolved_max;
	int organizationCount_min;
	int acadOrganizationCount_min;
	int acadOrganizationCount_max;
	int acadOrganizationCount;
	int nonAcadOrganizationCount_min;
	int nonAcadOrganizationCount_max;
	int nonAcadOrganizationCount;
	int researchGroupCount_min;
	int researchGroupCount_max;
	int researchGroupCount;
	int collegeCount_min;
	int collegeCount_max;
	int collegeCount;
	int nextConferenceCycleStartsIn_min;
	int nextConferenceCycleStartsIn_max;
	int nextConferenceCycleStartsIn;
	int cityCount_min;
	int cityCount_max;
	int cityCount;
	int usersListCount;
	RDFWriter tweetModelWriter;
	long randomOffsetMillis;
	long conferenceStartMillis;
	int conferenceDuration_min_months;
	int conferenceDuration_max_months;
	int conferenceDuration_months;
	long startTimestampMillis;
	long monthsInMillis;
	List<String> researchGroups = new ArrayList<>();
	List<String> colleges = new ArrayList<>();
	List<String> academicOrganizations = new ArrayList<>();
	List<String> nonAcademicOrganizations = new ArrayList<>();
	List<String> peopleDirectlyInvolvedList = new ArrayList<>();
	List<String> otherPeopleInvolvedList = new ArrayList<>();
	List<String> papersList = new ArrayList<>();
	List<String> cityList = new ArrayList<>();
	CountDownLatch latch;
	Object usersList;
	Map<String, Map<String, Object>> conferencePaperList;
	Map<String, List<String>> volunteerAndStudentGrantList;
	Map<String, List<String>> organizingCommitteeList;
	Map<String, List<String>> speakerList;
	Property rdfSubject,rdfPredicate,rdfObject,rdfType;
	Property hasGeneralChair,getsStudentGrantFor, hasAuthor, isAuthorOf, hasLocalChair, hasResearchTrackChair, hasResourceTrackChair, posts, hasTweetID, hasUserID, hasDisplayName, volunteersFor, hasHashtag, isAboutEventPhase, mentionsPerson, mentionsOrganization, mentionsConference, hasInformation, hasDateTimestamp, isAboutEvent, hasUserName, hasAffiliation, hasDesignation, hasId, hasConferenceName, hasEventMode, hasWebsiteURL, hasLocation, hasEdition, hasPaperTrack, hasTrackChair, hasTitle, hasPaperDomain, isPresentedBy, hasRole, attends, isAcceptedAt, givesTalk, givesTalkOn, hasPaper;
	File tweetMetaData_n3;
	File eventData_n3;
	RDFWriter tweetMetaDataWriter;
	RDFWriter tweetEventDataWriter;
	Model tweetMetaProperties = ModelFactory.createDefaultModel();
	Model eventDataProperties = ModelFactory.createDefaultModel();
	String[] TOKEN_EventMode;
	String[] TOKEN_ConferenceEventTrack;
    Resource PersonAccount = tweetMetaProperties.createResource(Twitter_URL + "PersonAccount");
    Resource ConferenceAccount = tweetMetaProperties.createResource(Twitter_URL + "ConferenceAccount");
    Resource OrganizationAccount = tweetMetaProperties.createResource(Twitter_URL + "OrganizationAccount");  
    Resource Tweet = tweetMetaProperties.createResource(Twitter_URL + "Tweet");
    Resource City = eventDataProperties.createResource(Location_URL + "City");
    Resource Conference = eventDataProperties.createResource(ACE_URL + "Conference");
    Resource ConferenceTracks = eventDataProperties.createResource(ACE_URL + "ConferenceEventTrack");
    Resource ConferencePaper = eventDataProperties.createResource(ACE_URL + "ConferencePaper");
    Resource Person = eventDataProperties.createResource("http://xmlns.com/foaf/0.1/" + "Person");
    Resource Organizers = eventDataProperties.createResource(ACE_URL + "Organizers");
    Resource Attending = eventDataProperties.createResource(ACE_URL + "Attendee");
    //Resource Volunteer = eventDataProperties.createResource(ACE_URL + "Volunteer");
    Resource StudentGrant = eventDataProperties.createResource(ACE_URL + "StudentGrant");
    Resource Speaker = eventDataProperties.createResource(ACE_URL + "Speaker");
    Resource Award = eventDataProperties.createResource(ACE_URL + "Award");
    Resource ResearchGroup = eventDataProperties.createResource(OWL2Bench_URL + "ResearchGroup");
    Resource PaperTrack = eventDataProperties.createResource(ACE_URL + "PaperTrack");
    Resource Organization = eventDataProperties.createResource("http://xmlns.com/foaf/0.1/"  + "Organization");
    Resource Author = eventDataProperties.createResource(ACE_URL + "Author");
    Resource Student = eventDataProperties.createResource(ACE_URL + "Student");
    Resource PhDStudent = eventDataProperties.createResource(ACE_URL + "PhDStudent");
    Resource InvitedTalks = eventDataProperties.createResource(ACE_URL + "InvitedTalks");
    Resource KeynoteTalks = eventDataProperties.createResource(ACE_URL + "KeynoteTalks");
    Resource LightningTalks = eventDataProperties.createResource(ACE_URL + "LightningTalks");
    Resource Presentations = eventDataProperties.createResource(ACE_URL + "Presentations");
    Resource ComputerScienceDomain = eventDataProperties.createResource(ACE_URL + "ComputerScienceDomain");
    Resource InvitedTalkSpeakerRole = eventDataProperties.createResource(ACE_URL + "InvitedTalkSpeakerRole");
    Resource KeynoteSpeakerRole = eventDataProperties.createResource(ACE_URL + "KeynoteTalkSpeakerRole");
    Resource SpeakerRole = eventDataProperties.createResource(ACE_URL + "SpeakerRole");
    Resource MainConferenceAnnouncementPhase = eventDataProperties.createResource(ACE_URL + "MainConferenceAnnouncementPhase");
    Resource CallForPapersAnnouncementPhase = eventDataProperties.createResource(ACE_URL + "CallForPapersAnnouncementPhase");
    Resource AcceptedPapersNotificationPhase = eventDataProperties.createResource(ACE_URL + "AcceptedPapersNotificationPhase");
    Resource PaperSubmissionReminderPhase = eventDataProperties.createResource(ACE_URL + "PaperSubmissionReminderPhase");
    Resource RegistrationReminderPhase = eventDataProperties.createResource(ACE_URL + "RegistrationReminderPhase");

	Map<String, Map<String, String>> userData;
	Map<String, Map<String, Object>> paperData;
    public ConferenceStreams(DataGenerator gen, int confIndex) {
        this.rdfType = RDF.type;
        this.rdfSubject = RDF.subject;
        this.rdfPredicate = RDF.predicate;
        this.rdfObject = RDF.object;
        this.posts = tweetMetaProperties.createProperty(Twitter_URL + "posts");
        this.hasTweetID = tweetMetaProperties.createProperty(Twitter_URL + "hasTweetID");
        this.hasUserID = tweetMetaProperties.createProperty(Twitter_URL + "hasUserID");
        this.hasDisplayName = tweetMetaProperties.createProperty(Twitter_URL + "hasDisplayName");
        this.hasHashtag = tweetMetaProperties.createProperty(Twitter_URL + "hasHashtag");
        this.isAboutEventPhase = tweetMetaProperties.createProperty(Twitter_URL + "isAboutEventPhase");
        this.hasDateTimestamp = tweetMetaProperties.createProperty(Twitter_URL + "hasDateTimestamp");
        this.isAboutEvent = tweetMetaProperties.createProperty(Twitter_URL + "isAboutEvent");
        this.mentionsPerson = tweetMetaProperties.createProperty(Twitter_URL + "mentionsPerson");
        this.mentionsConference = tweetMetaProperties.createProperty(Twitter_URL + "mentionsConference");
        this.mentionsOrganization = tweetMetaProperties.createProperty(Twitter_URL + "mentionsOrganization");
        this.hasUserName = tweetMetaProperties.createProperty(Twitter_URL + "hasUserName");
        this.hasAffiliation = tweetMetaProperties.createProperty(Twitter_URL + "hasAffiliation");
        this.hasDesignation = tweetMetaProperties.createProperty(Twitter_URL + "hasDesignation");
        this.hasId = tweetMetaProperties.createProperty(Twitter_URL + "hasId");
        this.hasConferenceName = eventDataProperties.createProperty(ACE_URL + "hasConferenceName");
        this.hasEventMode = eventDataProperties.createProperty(ACE_URL + "hasEventMode");
        this.hasWebsiteURL = eventDataProperties.createProperty(ACE_URL + "hasWebsiteURL");
        this.hasLocation = eventDataProperties.createProperty(ACE_URL + "hasLocation");
        this.hasEdition = eventDataProperties.createProperty(ACE_URL + "hasEdition");
        this.hasPaperTrack = eventDataProperties.createProperty(ACE_URL + "hasPaperTrack");
        this.hasTrackChair = eventDataProperties.createProperty(ACE_URL + "hasTrackChair");
        this.hasTitle = eventDataProperties.createProperty(ACE_URL + "hasTitle");
        this.hasPaperDomain = eventDataProperties.createProperty(ACE_URL + "hasPaperDomain");
        this.isPresentedBy = eventDataProperties.createProperty(ACE_URL + "isPresentedBy");
        this.hasRole = eventDataProperties.createProperty(ACE_URL + "hasRole");
        this.attends = eventDataProperties.createProperty(ACE_URL + "attends");
        this.isAcceptedAt = eventDataProperties.createProperty(ACE_URL + "isAcceptedAt");
        this.givesTalk = eventDataProperties.createProperty(ACE_URL + "givesTalk");
        this.givesTalkOn = eventDataProperties.createProperty(ACE_URL + "givesTalkOn");
        this.hasPaper = eventDataProperties.createProperty(ACE_URL + "hasPaper");
        this.volunteersFor = eventDataProperties.createProperty(ACE_URL + "volunteersFor");
        this.hasGeneralChair= eventDataProperties.createProperty(ACE_URL + "hasGeneralChair");
        this.hasLocalChair= eventDataProperties.createProperty(ACE_URL + "hasLocalChair");
        this.hasResearchTrackChair= eventDataProperties.createProperty(ACE_URL + "hasResearchTrackChair");
        this.hasResourceTrackChair= eventDataProperties.createProperty(ACE_URL + "hasResourceTrackChair");
        this.hasAuthor= eventDataProperties.createProperty(ACE_URL + "hasAuthor");
        this.isAuthorOf= eventDataProperties.createProperty(ACE_URL + "isAuthorOf");
        this.TOKEN_EventMode=gen.TOKEN_EventMode;
        this.getsStudentGrantFor=eventDataProperties.createProperty(ACE_URL + "getsStudentGrantFor");
        this.TOKEN_ConferenceEventTrack=gen.TOKEN_ConferenceEventTrack;
		this.gen = gen;
		this.confCycle = gen.confCycle;
		this.confIndex = confIndex;
		this.startTimestampMillis = gen.startTimestampMillis;
		this.year = Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date(startTimestampMillis)));
        this.userData=gen.userData;
		this.papersList = gen.papersList;
		this.cityList = gen.cityList;
		this.usersList = gen.usersList;
		this.researchGroups = gen.researchGroups;
		this.acceptedPaperCount = gen.random.nextInt(gen.acceptedPaperCount_max - gen.acceptedPaperCount_min + 1)
				+ gen.acceptedPaperCount_min;
//		CONFERENCE PAPER LIST
		generateConferencePaperList(gen.paperData);

//		VOLUNTEER and STUDENT GRANT LIST
//      this hashmap links with only students , some of them from the authors and some random just attendees		
		this.volunteerAndStudentGrantList = generateVolunteerAndStudentGrantList(gen.userData);

//		ORGANIZING COMMITTEE LIST
// 		again another hashmap from userlist that links different organizer roles with 1 or 2 users role, list		
		this.organizingCommitteeList = generateOrganizingCommitteeList(gen.userData);

//		SPEAKER LIST
//		hashmap that links users with KeyNote, InvitedTalk, or just Speaker		
		this.speakerList = generateSpeakerList(gen.userData);

//		USERS LIST who will post
//		These will be taken from conference paper list

//      TWEET METADATA FILE
//		userid posts tweetid, mentions(users, conf, organization), hasTimeStamp, hasHashtag(domain), isAboutEventPhase, isAboutEvent, 

		this.usersListCount = this.random.nextInt(gen.usersInvolved_max - gen.usersInvolved_min + 1)
				+ gen.usersInvolved_min;

		this.otherPeopleInvolved = this.random.nextInt(gen.otherPeopleInvolved_max - gen.otherPeopleInvolved_min + 1)
				+ gen.otherPeopleInvolved_min;

		int i = 1;
		LocalDateTime nextCycleStart = LocalDateTime.ofInstant(new Date(startTimestampMillis).toInstant(),
				ZoneId.systemDefault());
		// how to make sure that the years are increasing

		while (i <= this.confCycle) {
			// System.out.println(" loop starts here" + i);
			// this.latch = new CountDownLatch(3);

			// Generate overall conference duration between 6 to 9 months
			int overallDurationMonths = this.random.nextInt(4) + 6; // Range: 6 to 9 months

			// Calculate durations for each phase
			int duringConferenceDays = this.random.nextInt(3) + 3; // Range: 3 to 5 days
			int afterConferenceDays = this.random.nextInt(2) + 2; // Range: 2 to 3 days
			int beforeConferenceDays = overallDurationMonths * 30 - duringConferenceDays - afterConferenceDays; // Remaining
																												// days

			// Calculate start timestamps for each phase
			long beforeConferenceStartMillis = startTimestampMillis;
			long beforeConferenceEndMillis = beforeConferenceStartMillis + beforeConferenceDays * 24 * 60 * 60 * 1000L;
			long duringConferenceEndMillis = beforeConferenceEndMillis + duringConferenceDays * 24 * 60 * 60 * 1000L;
			long afterConferenceEndMillis = duringConferenceEndMillis + afterConferenceDays * 24 * 60 * 60 * 1000L;

			this.confInstance = "conf" + this.confIndex + "_" + this.year; // year will be a variable
			System.out.println(this.confInstance + " year" + this.year);
			this.confName = this.confInstance;
			this.confId = this.confInstance;
			this.seed = confIndex + 10000 * this.confCycle;
			this.random.setSeed((long) this.seed);
			this.confURL = "https://www." + this.confInstance + ".com";
//			this.acceptedPaperCount = this.random.nextInt(gen.acceptedPaperCount_max - gen.acceptedPaperCount_min + 1)+ gen.acceptedPaperCount_min;
//		this.otherPeopleInvolved = this.random.nextInt(gen.otherPeopleInvolved_max - gen.otherPeopleInvolved_min + 1)
//				+ gen.otherPeopleInvolved_min;
			this.streamsDirectory = gen.streamsDirectory;
			if (!this.streamsDirectory.exists()) {
				this.streamsDirectory.mkdirs();
			}
			this.confDirectory = new File(this.streamsDirectory+"/confInstance/");
			if (!this.confDirectory.exists()) {
				this.confDirectory.mkdirs();
			}
			this.bc = new BeforeConference(this, beforeConferenceStartMillis, beforeConferenceEndMillis);
			this.dc = new DuringConference(this, beforeConferenceEndMillis, duringConferenceEndMillis);
			this.ac = new AfterConference(this, duringConferenceEndMillis, afterConferenceEndMillis);
			// Calculate time until the next conference cycle
			nextCycleStart = nextCycleStart.plusYears(1);
			// Adjust the start timestamp to a date close to the previous start but within
			// the same month
			nextCycleStart = nextCycleStart.plusDays(this.random.nextInt(30) - 30); // Randomly add or subtract up to 30
																					// days
			System.out.println("Next Cycle Start: " + nextCycleStart);
			// Update start timestamp for the next cycle
			startTimestampMillis = nextCycleStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
			this.year += 1;
			i++;
			// System.out.println(" loop ended here??" + i);
		}
	}

	public void generateConferencePaperList(Map<String, Map<String, Object>> paperData) {
		this.conferencePaperList = new HashMap<>();
		List<String> paperIds = new ArrayList<>(paperData.keySet());
		for (int j = 0; j < this.acceptedPaperCount; j++) {
			String selectedKey = paperIds.get(this.random.nextInt(paperIds.size()));
			Map<String, Object> selectedEntry = paperData.remove(selectedKey);
			this.conferencePaperList.put(selectedKey, selectedEntry);
		}

	}
	
	private static Map<String, List<String>> generateVolunteerAndStudentGrantList(Map<String, Map<String, String>> userData) {
	    Map<String, List<String>> volunteerAndStudentGrantList = new HashMap<>();
	    List<String> eligibleUsers = userData.entrySet().stream()
	            .filter(entry -> entry.getValue().get("designation").equals("Student") || entry.getValue().get("designation").equals("PhD Student"))
	            .map(Map.Entry::getKey)
	            .collect(Collectors.toList());

	    Random random = new Random();
	    for (int i = 0; i < eligibleUsers.size(); i++) {
	        String userId = eligibleUsers.get(random.nextInt(eligibleUsers.size()));
	        String type = random.nextBoolean() ? "Volunteer" : "StudentGrant";
	        volunteerAndStudentGrantList.computeIfAbsent(type, k -> new ArrayList<>()).add(userId);
	        eligibleUsers.remove(userId);  // Remove user to avoid duplication
	    }
	    return volunteerAndStudentGrantList;
	}

	private static Map<String, List<String>> generateOrganizingCommitteeList(Map<String, Map<String, String>> userData) {
	    Map<String, List<String>> organizingCommitteeList = new HashMap<>();
	    String[] chairRoles = { "generalChair", "localChair", "researchTrackChair", "resourcesTrackChair", "trackChair", "tutorialTrackChair", "workshopTrackChair" };

	    List<String> eligibleUsers = userData.entrySet().stream()
	            .filter(entry -> entry.getValue().get("designation").equals("Professor") || entry.getValue().get("designation").equals("Faculty") || entry.getValue().get("designation").equals("Researcher"))
	            .map(Map.Entry::getKey)
	            .collect(Collectors.toList());

	    Random random = new Random();
	    for (String role : chairRoles) {
	        int numberOfUsers = random.nextInt(2) + 1; // Assign 1 or 2 users to each role
	        for (int i = 0; i < numberOfUsers && !eligibleUsers.isEmpty(); i++) {
	            String userId = eligibleUsers.get(random.nextInt(eligibleUsers.size()));
	            organizingCommitteeList.computeIfAbsent(role, k -> new ArrayList<>()).add(userId);
	            eligibleUsers.remove(userId);  // Remove user to avoid duplication
	        }
	    }
	    return organizingCommitteeList;
	}

	private static Map<String, List<String>> generateSpeakerList(Map<String, Map<String, String>> userData) {
	    Map<String, List<String>> speakerList = new HashMap<>();
	    List<String> eligibleUsers = userData.entrySet().stream()
	            .filter(entry -> entry.getValue().get("designation").equals("Professor") || entry.getValue().get("designation").equals("Faculty"))
	            .map(Map.Entry::getKey)
	            .collect(Collectors.toList());

	    Random random = new Random();
	    // Assign Keynote speakers
	    int keynoteCount = Math.min(3, random.nextInt(2) + 2);  // 2-3 keynote speakers
	    for (int i = 0; i < keynoteCount && !eligibleUsers.isEmpty(); i++) {
	        String userId = eligibleUsers.get(random.nextInt(eligibleUsers.size()));
	        speakerList.computeIfAbsent("Keynote", k -> new ArrayList<>()).add(userId);
	        eligibleUsers.remove(userId);  // Remove user to avoid duplication
	    }

	    // Assign Invited Talk speakers
	    int invitedTalkCount = Math.min(3, random.nextInt(2) + 2);  // 2-3 invited talk speakers
	    for (int i = 0; i < invitedTalkCount && !eligibleUsers.isEmpty(); i++) {
	        String userId = eligibleUsers.get(random.nextInt(eligibleUsers.size()));
	        speakerList.computeIfAbsent("InvitedTalk", k -> new ArrayList<>()).add(userId);
	        eligibleUsers.remove(userId);  // Remove user to avoid duplication
	    }

	    // Assign other speakers
	    while (!eligibleUsers.isEmpty()) {
	        String userId = eligibleUsers.get(random.nextInt(eligibleUsers.size()));
	        speakerList.computeIfAbsent("Speaker", k -> new ArrayList<>()).add(userId);
	        eligibleUsers.remove(userId);  // Remove user to avoid duplication
	    }

	    return speakerList;
	}

}
