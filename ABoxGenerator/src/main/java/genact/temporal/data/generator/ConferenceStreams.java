
package genact.temporal.data.generator;

import java.util.ArrayList;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.jena.vocabulary.RDF;
import java.io.PrintWriter;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
	String confId, confName, confInstance, confURL, confAccount;
	String profile;
	int acceptedPaperCount;
	int otherPeopleInvolved;
	int seed;
	int tweetCount;
	Random random = new Random();
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
	String ACE_URL = "https://anonymous.com/AcademicConferenceEvent#";
	String OWL2Bench_URL = "https://kracr.iiitd.edu.in/OWL2Bench#";
	String Location_URL = "https://anonymous.com/Location#";
	String Twitter_URL = "https://anonymous.com/Tweet#";
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
	int nonAcadOrganizationCount_min;
	int nonAcadOrganizationCount_max;
	int nonAcadOrganizationCount;
	int researchGroupCount_min;
	int researchGroupCount_max;
	int researchGroupCount;
	int cityCount_min;
	int cityCount_max;
	int cityCount;
	int random_tweets_min;
	int random_tweets_max;
	int usersListCount;
	RDFWriter tweetModelWriter;
	long randomOffsetMillis;
	long conferenceStartMillis;
	int conferenceDuration_min_months;
	int conferenceDuration_max_months;
	int conferenceDuration_months;
	long startTimestampMillis;
	long monthsInMillis;
	int early_announcement_peak;
	int notification_peak;
	int during_conference_peak;
	int after_conference_peak;
	List<String> colleges = new ArrayList<>();
	List<String> academicOrganizations = new ArrayList<>();
	List<String> nonAcademicOrganizations = new ArrayList<>();
	List<String> peopleDirectlyInvolvedList = new ArrayList<>();
	List<String> otherPeopleInvolvedList = new ArrayList<>();
	List<String> papersList = new ArrayList<>();
	List<String> cityList = new ArrayList<>();
	CountDownLatch latch;
	Object usersList;
	Map<String, Map<String, Object>> conferencePaperList = new HashMap<>();;
	Map<String, List<String>> volunteerAndStudentGrantList;
	Map<String, List<String>> organizingCommitteeList;
	Map<String, List<String>> speakerList;
	Property rdfSubject, rdfPredicate, rdfObject, rdfType;
	Property hasGeneralChair, getsStudentGrantFor, hasAuthor, isAuthorOf, hasLocalChair, hasResearchTrackChair,
			hasResourceTrackChair, posts, hasTweetID, hasUserID, hasDisplayName, volunteersFor, hasHashtag,
			isAbout,hasEventPhase, mentionsPerson, mentionsOrganization, mentionsConference, hasInformation,
			hasDateTimestamp, hasUserName, hasAffiliation, hasDesignation, hasId, hasConferenceName,
			hasEventMode, hasWebsiteURL, hasLocation, hasEdition, hasPaperTrack, hasTrackChair, hasTitle,
			hasPaperDomain, isPresentedBy, hasRole, attends, isAcceptedAt, givesTalk, givesTalkOn, hasPaper;
	File tweetMetaData_n3;
	File eventData_n3;
	RDFWriter tweetMetaDataWriter;
	RDFWriter tweetEventDataWriter;
	Model tweetMetaProperties = ModelFactory.createDefaultModel();
	Model eventDataProperties = ModelFactory.createDefaultModel();
	String[] TOKEN_EventMode;
	String[] TOKEN_ConferenceEventTrack;
	LocalDateTime nextCycleStart;
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
	// Resource Volunteer = eventDataProperties.createResource(ACE_URL +
	// "Volunteer");
	Resource StudentGrant = eventDataProperties.createResource(ACE_URL + "StudentGrant");
	Resource Speaker = eventDataProperties.createResource(ACE_URL + "Speaker");
	Resource Award = eventDataProperties.createResource(ACE_URL + "Award");
	Resource ResearchGroup = eventDataProperties.createResource(OWL2Bench_URL + "ResearchGroup");
	Resource PaperTrack = eventDataProperties.createResource(ACE_URL + "PaperTrack");
	Resource Organization = eventDataProperties.createResource("http://xmlns.com/foaf/0.1/" + "Organization");
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
	Resource MainConferenceAnnouncementPhase = eventDataProperties
			.createResource(ACE_URL + "MainConferenceAnnouncementPhase");
	Resource CallForPapersAnnouncementPhase = eventDataProperties
			.createResource(ACE_URL + "CallForPapersAnnouncementPhase");
	Resource AcceptedPapersNotificationPhase = eventDataProperties
			.createResource(ACE_URL + "AcceptedPapersNotificationPhase");
	Resource PaperSubmissionReminderPhase = eventDataProperties
			.createResource(ACE_URL + "PaperSubmissionReminderPhase");
	Resource RegistrationReminderPhase = eventDataProperties.createResource(ACE_URL + "RegistrationReminderPhase");
	List<String> cities = new ArrayList<>();
	Map<String, Map<String, String>> userData;
	Map<String, Map<String, Object>> paperData;
	List<String> researchGroups = new ArrayList<>();
	DateTimeFormatter fileNameFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

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
		this.isAbout = tweetMetaProperties.createProperty(Twitter_URL + "isAbout");
		this.hasDateTimestamp = tweetMetaProperties.createProperty(Twitter_URL + "hasDateTimestamp");
		this.mentionsPerson = tweetMetaProperties.createProperty(Twitter_URL + "mentionsPerson");
		this.mentionsConference = tweetMetaProperties.createProperty(Twitter_URL + "mentionsConference");
		this.mentionsOrganization = tweetMetaProperties.createProperty(Twitter_URL + "mentionsOrganization");
		this.hasUserName = tweetMetaProperties.createProperty(Twitter_URL + "hasUserName");
		this.hasAffiliation = tweetMetaProperties.createProperty(Twitter_URL + "hasAffiliation");
		this.hasDesignation = tweetMetaProperties.createProperty(Twitter_URL + "hasDesignation");
		this.hasId = tweetMetaProperties.createProperty(Twitter_URL + "hasId");
		this.hasConferenceName = eventDataProperties.createProperty(ACE_URL + "hasConferenceName");
		this.hasEventMode = eventDataProperties.createProperty(ACE_URL + "hasEventMode");
		this.hasEventPhase = eventDataProperties.createProperty(ACE_URL + "hasEventPhase");
		this.hasWebsiteURL = eventDataProperties.createProperty(ACE_URL + "hasWebsiteURL");
		this.hasLocation = eventDataProperties.createProperty(Location_URL + "hasLocation");
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
		this.hasGeneralChair = eventDataProperties.createProperty(ACE_URL + "hasGeneralChair");
		this.hasLocalChair = eventDataProperties.createProperty(ACE_URL + "hasLocalChair");
		this.hasResearchTrackChair = eventDataProperties.createProperty(ACE_URL + "hasResearchTrackChair");
		this.hasResourceTrackChair = eventDataProperties.createProperty(ACE_URL + "hasResourceTrackChair");
		this.hasAuthor = eventDataProperties.createProperty(ACE_URL + "hasAuthor");
		this.isAuthorOf = eventDataProperties.createProperty(ACE_URL + "isAuthorOf");
		this.TOKEN_EventMode = gen.TOKEN_EventMode;
		this.getsStudentGrantFor = eventDataProperties.createProperty(ACE_URL + "getsStudentGrantFor");
		this.TOKEN_ConferenceEventTrack = gen.TOKEN_ConferenceEventTrack;
		this.gen = gen;
		this.confCycle = gen.confCycle;
		this.confIndex = confIndex;
		this.year = Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date(startTimestampMillis)));
		this.userData = gen.userData;
		this.usersList = gen.usersList;
		this.researchGroups = gen.researchGroups;
		this.startTimestampMillis=gen.startTimestampMillis;
		//System.out.println(this.startTimestampMillis);
		long maxRandomMillis = gen.random.nextInt(4, 10) * 30L * 24 * 60 * 60 * 1000;
 // 3 months in milliseconds
//		this.startTimestampMillis = ThreadLocalRandom.current().nextLong(this.startTimestampMillis, (this.startTimestampMillis + maxRandomMillis));
		this.startTimestampMillis = this.startTimestampMillis + maxRandomMillis;
		//        LocalDateTime randomStartTime = LocalDateTime.ofEpochSecond((System.currentTimeMillis() / 1000) + (randomMillis / 1000), 0, ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now()));
//
//        this.startTimestampMillis = randomStartTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		//System.out.println(this.startTimestampMillis);
		defineDatastructures();

		distributeTimestamps();

	}

	public void defineDatastructures() {
		// every user has a research interest
		// every paper has a domain and track
		// every tweet has a mention tag--organization (his own affiliaation and the
		// affiliation of the people involved),
		// co-author, speaker etc and conferenceaccount
//	CONFERENCE PAPER LIST

		this.conferencePaperList = generateConferencePaperList(gen.paperData); // remember to add awards

		this.researchGroups = gen.researchGroups; // already full iris
		this.cities = gen.cities; // already full iris
//	VOLUNTEER and STUDENT GRANT LIST
//  this hashmap links with only students , some of them from the authors and some random just attendees		
		this.volunteerAndStudentGrantList = generateVolunteerAndStudentGrantList(gen.userData);

//	ORGANIZING COMMITTEE LIST
//		again another hashmap from userlist that links different organizer roles with 1 or 2 users role, list		
		this.organizingCommitteeList = generateOrganizingCommitteeList(gen.userData);

//	SPEAKER LIST
//	hashmap that links users with KeyNote, InvitedTalk, or just Speaker		
		this.speakerList = generateSpeakerList(gen.userData);

	}

	void distributeTimestamps() {
		int i = 1;
		this.nextCycleStart = LocalDateTime.ofInstant(new Date(startTimestampMillis).toInstant(),
				ZoneId.systemDefault());
		// how to make sure that the years are increasing
		this.streamsDirectory = gen.streamsDirectory;

		while (i <= this.confCycle) {
			this.random.setSeed((long) this.seed);
			this.tweetCount = 0;
			this.acceptedPaperCount = gen.random.nextInt(gen.acceptedPaperCount_max - gen.acceptedPaperCount_min + 1)
					+ gen.acceptedPaperCount_min;
			this.usersListCount = this.random.nextInt(gen.usersInvolved_max - gen.usersInvolved_min + 1)
					+ gen.usersInvolved_min;

			this.otherPeopleInvolved = this.random.nextInt(
					gen.otherPeopleInvolved_max - gen.otherPeopleInvolved_min + 1) + gen.otherPeopleInvolved_min;
			this.early_announcement_peak = this.random
					.nextInt(gen.early_announcement_peak_max - gen.early_announcement_peak_min + 1)
					+ gen.early_announcement_peak_min;
			this.notification_peak = this.random.nextInt(gen.notification_peak_max - gen.notification_peak_min + 1)
					+ gen.notification_peak_min;
			this.during_conference_peak = this.random
					.nextInt(gen.during_conference_peak_max - gen.during_conference_peak_min + 1)
					+ gen.during_conference_peak_min;
			this.after_conference_peak = this.random.nextInt(
					gen.after_conference_peak_max - gen.after_conference_peak_min + 1) + gen.after_conference_peak_min;
			this.random_tweets_min = gen.random_tweets_min;
			this.random_tweets_max = gen.random_tweets_max;
			// Generate overall conference duration between 6 to 9 months
			int overallDurationMonths = this.random
					.nextInt((gen.conferenceDuration_max_months - gen.conferenceDuration_min_months) + 1)
					+ gen.conferenceDuration_min_months; // Range: 6 to 9 months

			// Calculate durations for each phase
			int duringConferenceDays = this.random
					.nextInt((gen.during_conference_days_max - gen.during_conference_days_min) + 1)
					+ gen.during_conference_days_min; // Range: 3 to 5 days
			int afterConferenceDays = this.random
					.nextInt((gen.after_conference_days_max - gen.after_conference_days_min) + 1)
					+ gen.after_conference_days_min; // Range: 2 to 3 days
			int beforeConferenceDays = overallDurationMonths * 30 - duringConferenceDays - afterConferenceDays; // Remaining
																												// days
			//System.out.println("overall"+overallDurationMonths);
			//System.out.println("beforeConferenceDays"+beforeConferenceDays);
			//System.out.println("duringConferenceDays"+duringConferenceDays);
			//System.out.println("afterConferenceDays"+afterConferenceDays);
			// Calculate start timestamps for each phase
			long beforeConferenceStartMillis = this.startTimestampMillis;
			long beforeConferenceEndMillis = beforeConferenceStartMillis + beforeConferenceDays * 24 * 60 * 60 * 1000L;
			long duringConferenceEndMillis = beforeConferenceEndMillis + duringConferenceDays * 24 * 60 * 60 * 1000L;
			long afterConferenceEndMillis = duringConferenceEndMillis + afterConferenceDays * 24 * 60 * 60 * 1000L;

			System.out.println("beforeConferenceStartMillis"+ LocalDateTime.ofInstant(new Date(beforeConferenceStartMillis).toInstant(),
					ZoneId.systemDefault()));
			
			System.out.println("beforeConferenceEndMillis"+LocalDateTime.ofInstant(new Date(beforeConferenceEndMillis).toInstant(),
					ZoneId.systemDefault()));
			
			System.out.println("duringConferenceEndMillis"+LocalDateTime.ofInstant(new Date(duringConferenceEndMillis).toInstant(),
					ZoneId.systemDefault()));
			System.out.println("afterConferenceEndMillis"+LocalDateTime.ofInstant(new Date(afterConferenceEndMillis).toInstant(),
					ZoneId.systemDefault()));
			this.confAccount = "conf" + this.confIndex; 
			this.confInstance = "conf" + this.confIndex + "_" + this.year; // year will be a variable
			System.out.println("Started " + this.confAccount + "for the year " + this.year);
			this.confName = this.confInstance;
			this.confId = this.confInstance;
			this.seed = confIndex + 10000 * this.confCycle;
			this.confURL = "https://anonymous.com/" + this.confInstance + ".com";
			this.confDirectory = new File(this.streamsDirectory + "/" + this.confInstance + "/");
			if (!this.confDirectory.exists()) {
				this.confDirectory.mkdirs();
			}
			this.bc = new BeforeConference(this, beforeConferenceStartMillis, beforeConferenceEndMillis);
			this.dc = new DuringConference(this, beforeConferenceEndMillis, duringConferenceEndMillis);
			this.ac = new AfterConference(this, duringConferenceEndMillis, afterConferenceEndMillis);

			// Calculate time until the next conference cycle
			this.nextCycleStart = this.nextCycleStart.plusYears(1);
			// Adjust the start timestamp to a date close to the previous start but within
			// the same month
			this.nextCycleStart = this.nextCycleStart.plusDays(this.random.nextInt(30) - 30); // Randomly add or
																								// subtract up to 30
			// days
			System.out.println("Next Cycle Start: " + nextCycleStart);
			// Update start timestamp for the next cycle
			startTimestampMillis = nextCycleStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
			this.year += 1;
			i++;

		}
	}

	public Map<String, Map<String, Object>> generateConferencePaperList(Map<String, Map<String, Object>> paperData) {

		this.conferencePaperList.clear();
		List<String> paperIds = new ArrayList<>(paperData.keySet());

		// Map to track the count of papers per conference track in the list
		Map<String, Integer> trackCounts = new HashMap<>();
		for (String track : new String[] { "applicationsTrack", "demoTrack", "doctoralConsortiumTrack", "posterTrack",
				"researchTrack", "resourcesTrack", "tutorialTrack", "workshopTrack" }) {
			trackCounts.put(track, 0);
		}

		// Ensure at least one paper from each track
		for (String track : trackCounts.keySet()) {
			boolean found = false;
			for (Iterator<String> iterator = paperIds.iterator(); iterator.hasNext();) {
				String paperId = iterator.next();
				String conferenceTrack = (String) paperData.get(paperId).get("ConferenceTrack");
				if (track.equals(conferenceTrack)) {
					this.conferencePaperList.put(paperId, paperData.remove(paperId));
					trackCounts.put(track, trackCounts.get(track) + 1);
					iterator.remove();
					found = true;
					break;
				}
			}
			if (!found) {
				System.err.println("No paper found for track: " + track);
			}
		}

		// Fill the remaining slots with random papers
		while (this.conferencePaperList.size() < this.acceptedPaperCount && !paperIds.isEmpty()) {
			String selectedKey = paperIds.remove(this.random.nextInt(paperIds.size()));
			Map<String, Object> selectedEntry = paperData.remove(selectedKey);
			String conferenceTrack = (String) selectedEntry.get("ConferenceTrack");
			this.conferencePaperList.put(selectedKey, selectedEntry);
			trackCounts.put(conferenceTrack, trackCounts.get(conferenceTrack) + 1);
		}

		return this.conferencePaperList;
	}

	public  Map<String, List<String>> generateVolunteerAndStudentGrantList(
			Map<String, Map<String, String>> userData) {
		Map<String, List<String>> volunteerAndStudentGrantList = new HashMap<>();
		List<String> eligibleUsers = new ArrayList<>();

		for (Map.Entry<String, Map<String, String>> entry : userData.entrySet()) {
			String designation = entry.getValue().get("designation");
			if ("Student".equals(designation) || "PhD Student".equals(designation)) {
				eligibleUsers.add(entry.getKey());
			}
		}

		
		while (!eligibleUsers.isEmpty()) {
			int index =gen.random.nextInt(eligibleUsers.size());
			String userId = eligibleUsers.get(index);
			String type =gen.random.nextBoolean() ? "Volunteer" : "StudentGrant";
			if (!volunteerAndStudentGrantList.containsKey(type)) {
				volunteerAndStudentGrantList.put(type, new ArrayList<String>());
			}
			volunteerAndStudentGrantList.get(type).add(userId);
			eligibleUsers.remove(index); // Remove user to avoid duplication
		}

		return volunteerAndStudentGrantList;
	}

public Map<String, List<String>> generateOrganizingCommitteeList(
			Map<String, Map<String, String>> userData) {
		Map<String, List<String>> organizingCommitteeList = new HashMap<>();
		String[] chairRoles = { "generalChair", "localChair", "researchTrackChair", "resourcesTrackChair", "trackChair",
				"tutorialTrackChair", "workshopTrackChair" };
		List<String> eligibleUsers = new ArrayList<>();

		for (Map.Entry<String, Map<String, String>> entry : userData.entrySet()) {
			String designation = entry.getValue().get("designation");
			if ("Professor".equals(designation) || "Faculty".equals(designation) || "Researcher".equals(designation)) {
				eligibleUsers.add(entry.getKey());
			}
		}

		
		for (String role : chairRoles) {
			int numberOfUsers =gen.random.nextInt(2) + 1; // Assign 1 or 2 users to each role
			for (int i = 0; i < numberOfUsers && !eligibleUsers.isEmpty(); i++) {
				int index =gen.random.nextInt(eligibleUsers.size());
				String userId = eligibleUsers.get(index);
				if (!organizingCommitteeList.containsKey(role)) {
					organizingCommitteeList.put(role, new ArrayList<String>());
				}
				organizingCommitteeList.get(role).add(userId);
				eligibleUsers.remove(index); // Remove user to avoid duplication
			}
		}

		return organizingCommitteeList;
	}

public  Map<String, List<String>> generateSpeakerList(Map<String, Map<String, String>> userData) {
		Map<String, List<String>> speakerList = new HashMap<>();
		List<String> eligibleUsers = new ArrayList<>();

		for (Map.Entry<String, Map<String, String>> entry : userData.entrySet()) {
			String designation = entry.getValue().get("designation");
			if ("Professor".equals(designation) || "Faculty".equals(designation)) {
				eligibleUsers.add(entry.getKey());
			}
		}

	
		// Assign Keynote speakers
		int keynoteCount = Math.min(3,gen.random.nextInt(2) + 2); // 2-3 keynote speakers
		for (int i = 0; i < keynoteCount && !eligibleUsers.isEmpty(); i++) {
			int index =gen.random.nextInt(eligibleUsers.size());
			String userId = eligibleUsers.get(index);
			if (!speakerList.containsKey("Keynote")) {
				speakerList.put("Keynote", new ArrayList<String>());
			}
			speakerList.get("Keynote").add(userId);
			eligibleUsers.remove(index); // Remove user to avoid duplication
		}

		// Assign Invited Talk speakers
		int invitedTalkCount = Math.min(3,gen.random.nextInt(2) + 2); // 2-3 invited talk speakers
		for (int i = 0; i < invitedTalkCount && !eligibleUsers.isEmpty(); i++) {
			int index =gen.random.nextInt(eligibleUsers.size());
			String userId = eligibleUsers.get(index);
			if (!speakerList.containsKey("InvitedTalk")) {
				speakerList.put("InvitedTalk", new ArrayList<String>());
			}
			speakerList.get("InvitedTalk").add(userId);
			eligibleUsers.remove(index); // Remove user to avoid duplication
		}

//		// Assign other speakers
//		while (!eligibleUsers.isEmpty()) {
//			int index =gen.random.nextInt(eligibleUsers.size());
//			String userId = eligibleUsers.get(index);
//			if (!speakerList.containsKey("Speaker")) {
//				speakerList.put("Speaker", new ArrayList<String>());
//			}
//			speakerList.get("Speaker").add(userId);
//			eligibleUsers.remove(index); // Remove user to avoid duplication
//		}

		return speakerList;
	}

}
