/** For each confersity instance, College instances (Both Women and Co-Ed) and Research Groups are generated. 
 * And Basic hasName, hasCode data property assertion axioms are generated
* In order to modify the min-max range,that is, to modify the density of each node, user can make changes in the config.properties file */

package OWL2StreamBench.ABox.Generator;

import java.io.FileReader;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/*
 *         	//randomly picked a paper to map the last authors in the author list as the keynote, trackchairs, organizers
 */
public class Conference {
	Generator gen;
	int confIndex;
	String confId, confName, confInstance, confURL;
	String profile;
	int acceptedPaperCount;
	int otherPeopleInvolved;
	HashSet<String> attendees = new HashSet<>();
	HashSet<String> nonAttendees = new HashSet<>();
	HashSet<String> organizers = new HashSet<>();
	HashSet<String> chairs = new HashSet<>();

	public Conference(Generator gen, int confIndex, String confInstance, Map<String, Map<String, Object>> papers) {
		this.gen = gen;
		this.profile = gen.profile;
		this.confIndex = confIndex;
		this.confName = confInstance;// how to make sure that the years are increasing
		this.confInstance = confInstance; // year will be a variable
		this.confId = confInstance;
		this.confURL = "https://www." + confInstance + ".com";
		this.acceptedPaperCount = gen.random.nextInt(gen.acceptedPaperCount_max - gen.acceptedPaperCount_min + 1)
				+ gen.acceptedPaperCount_min;
		this.otherPeopleInvolved = gen.random.nextInt(gen.otherPeopleInvolved_max - gen.otherPeopleInvolved_min + 1)
				+ gen.otherPeopleInvolved_min;
		/*
		 * Code for mapping papers and authors with the conference name
		 */
		// also include some triples about Non-Academic Organizations, non-author

		Map<String, Map<String, Object>> papersAccepted = getRandomPapers(papers, this.acceptedPaperCount);
		Map<String, Map<String, Object>> extraUsers = getRandomPapers(papers, this.otherPeopleInvolved);
//		Map<String, Map<String, Object>> papersAccepted = new HashMap<>();
//		Map<String, Map<String, Object>> extraUsers = new HashMap<>();
//
//		int totalEntries = papersAccepted.size();
//		int map1Size = (int) Math.ceil(totalEntries * 0.85);
//		int currentEntry = 0;
//
//		for (Map.Entry<String, Map<String, Object>> entry : papersAccepted.entrySet()) {
//		    if (currentEntry < map1Size) {
//		    	papersAccepted.put(entry.getKey(), entry.getValue());
//		    } else {
//		    	extraUsers.put(entry.getKey(), entry.getValue());
//		    }
//		    currentEntry++;
//		}

		/*
		 * Tweets about a certain conference are always posted in a certain order It
		 * starts with announcement tweets. Conference announcement first by
		 * generalChair, followed by track announcements by track chairs
		 */
		conferenceAnnouncement(this.gen, extraUsers);
		trackAnnouncements(this.gen, extraUsers);
		sleep();
		reminders(this.gen); // deadlines approaching
		sleep();
		reviewNotificationsAndAcceptance(this.gen, papersAccepted, extraUsers);
		sleep();
		studentGrantsAndVolunteers(this.gen, papersAccepted);
		sleep();

		beforeConferenceEvent(this.gen, papersAccepted, extraUsers, this.attendees);
		duringConferenceEvent(this.gen, papersAccepted, extraUsers, this.attendees);
		afterConferenceEvent(this.gen, papersAccepted, extraUsers, this.attendees);
	}

	

	
    // Helper method to get a random element from a set
    private static <T> T getRandomElement(Set<T> set, Random random) {
        //Random random = new Random();
        int index = random.nextInt(set.size());
        int i = 0;
        for (T element : set) {
            if (i == index) {
                return element;
            }
            i++;
        }
        throw new IllegalArgumentException("Set is empty or index is out of bounds");
    }

	public Map<String, Map<String, Object>> getRandomPapers(Map<String, Map<String, Object>> papersAccepted, int n) {
		Map<String, Map<String, Object>> randomPapers = new HashMap<>();

		List<String> paperIds = new ArrayList<>(papersAccepted.keySet());
		int totalPapers = paperIds.size();

		// Shuffle the paperIds list to randomize the selection
		Collections.shuffle(paperIds);

		// Select 'n' random papers and add them to the new map
		for (int i = 0; i < n && i < totalPapers; i++) {
			String paperId = paperIds.get(i);
			randomPapers.put(paperId, papersAccepted.get(paperId));
		}

		return randomPapers;
	}

	public void sleep() {
		try {
			Thread.sleep(gen.random.nextInt(1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void conferenceAnnouncement(Generator gen, Map<String, Map<String, Object>> papersAccepted) {
		/*
		 * (Announcement Template) We're excited to announce the [n-th] edition of
		 * [@conf]! The deadlines for submissions, registrations, and other important
		 * dates have been announced. Visit our website [@ConferenceWebsiteURL]for more
		 * details and stay tuned for updates.
		 * 
		 */
		gen.classAssertion(gen.getClass("Conference"), gen.getNamedIndividual(confInstance));
		gen.dataPropertyAssertion(gen.getDataProperty("hasConferenceName"), gen.getNamedIndividual(confInstance),
				gen.getLiteral(confName));
		gen.dataPropertyAssertion(gen.getDataProperty("hasId"), gen.getNamedIndividual(confInstance),
				gen.getLiteral(confId));
		gen.dataPropertyAssertion(gen.getDataProperty("hasWebsiteURL"), gen.getNamedIndividual(confInstance),
				gen.getLiteral(confId));
		gen.dataPropertyAssertion(gen.getDataProperty("hasStatus"), gen.getNamedIndividual(confInstance),
				gen.getLiteral("announced"));
		Set<String> keys = papersAccepted.keySet();
		String randomKey = keys.toArray(new String[keys.size()])[gen.random.nextInt(keys.size())];
		Map<String, Object> paperInfo = papersAccepted.get(randomKey);
		List<String> authorNames = (List<String>) paperInfo.get("authors");
		String lastAuthor = authorNames.get(authorNames.size() - 1);
		// System.out.println("here"+ lastAuthor);
		gen.objectPropertyAssertion(gen.getObjectProperty("hasGeneralChairRoleAt"), gen.getNamedIndividual(lastAuthor),
				gen.getNamedIndividual(confInstance));
		gen.objectPropertyAssertion(gen.getObjectProperty("hasRole"), gen.getNamedIndividual(lastAuthor),
				gen.getNamedIndividual("GeneralChair"));
		organizers.add(lastAuthor);
	}

	public void trackAnnouncements(Generator gen, Map<String, Map<String, Object>> papersAccepted) {
		/*
		 * (Track chairs announcements) Excited to announce that I will be chairing the
		 * [@ConferenceTrack] track at [Conference Name]!
		 */

		/*
		 * We are proud to announce our esteemed group of track chairs for
		 * #Conference2023 (Track name and tag). With their vast knowledge and
		 * expertise, they will be leading our conference tracks
		 */

		//int randomNum = gen.random.nextInt(gen.TOKEN_ConferenceEventTrack.length) + 1;
		int randomNum = gen.TOKEN_ConferenceEventTrack.length;
		HashSet<Integer> set = new HashSet<>();
		// random track announcements
		while (set.size() < randomNum) {
			int random = gen.random.nextInt(gen.TOKEN_ConferenceEventTrack.length);
			set.add(random);
		}
		// pick random last authors from the extraUsers as different trackc chairs
		for (int value : set) {
			int length = papersAccepted.size();
			String track = gen.TOKEN_ConferenceEventTrack[value];
			// randomly picking a paper to map the last author in the author list as the
			// track chair
			Set<String> keys = papersAccepted.keySet();
			String randomKey = keys.toArray(new String[keys.size()])[gen.random.nextInt(keys.size())];
			Map<String, Object> paperInfo = papersAccepted.get(randomKey);
			List<String> authorNames = (List<String>) paperInfo.get("authors");
			String lastAuthor = authorNames.get(authorNames.size() - 1);
			gen.classAssertion(gen.getClass("Conference"), gen.getNamedIndividual(confInstance));
			gen.objectPropertyAssertion(gen.getObjectProperty("has" + track + "ChairRoleAt"),
					gen.getNamedIndividual(lastAuthor), gen.getNamedIndividual(confInstance));
			gen.objectPropertyAssertion(gen.getObjectProperty("hasRole"), gen.getNamedIndividual(lastAuthor),
					gen.getNamedIndividual(track + "Chair"));
			gen.objectPropertyAssertion(gen.getObjectProperty("hasPaperTrack"), gen.getNamedIndividual(confInstance),
					gen.getNamedIndividual(track));
			organizers.add(lastAuthor);
		}
	}

	public void reminders(Generator gen) {

		/*
		 * We are excited to announce that ⟨Conf 1⟩ will be ⟨Conf M ode⟩. Please join us
		 * ⟨Date⟩ February 7-14, 2023 at the ⟨Location⟩. The registration deadline is
		 * ⟨Date⟩.
		 * 
		 */
		int random = gen.random.nextInt(gen.TOKEN_EventMode.length);
		String eventMode = gen.TOKEN_EventMode[random];
		gen.dataPropertyAssertion(gen.getDataProperty("hasMode"), gen.getNamedIndividual(confInstance),
				gen.getLiteral(eventMode));
		if (eventMode.matches("offline") || eventMode.matches("hybrid")) {
			gen.dataPropertyAssertion(gen.getDataProperty("hasLocation"), gen.getNamedIndividual(confInstance),
					gen.getLiteral("location"));
		}
		// instead of location can actual locations be used? similar to citybench query
		// gen.dataPropertyAssertion(gen.getDataProperty("hasStartDate"),
		// gen.getNamedIndividual(confInstance), gen.getLiteral(date));
		// if mode is offline then below template
		/*
		 * 
		 */
		/*
		 * Only [insert number] days left to submit your papers for #Conference2023!
		 * Don't miss out on this opportunity to showcase your research. Submit your
		 * papers before [insert date].
		 */
		// gen.dataPropertyAssertion(gen.getDataProperty("haspapertrackSubmissionDeadline"),gen.getNamedIndividual(confInstance),
		// gen.getLiteral(date));
//		gen.dataPropertyAssertion(gen.getDataProperty("hasStatus"), gen.getNamedIndividual(confInstance),
//				gen.getLiteral("submissionDeadlineApproaching"));
	}

	public void reviewNotificationsAndAcceptance(Generator gen, Map<String, Map<String, Object>> papersAccepted,
			Map<String, Map<String, Object>> extraUsers) {
		// pick some random first authors from the papermap 2 as doctoral consortium
		// students
		for (int i = 0; i < extraUsers.size() * 0.3; i++) {
			Set<String> keys = extraUsers.keySet();
			String paperId = keys.toArray(new String[keys.size()])[gen.random.nextInt(keys.size())];
			Map<String, Object> paperInfo = extraUsers.get(paperId);
			String firstAuthor, paperTitle;

			if (extraUsers.containsKey(paperId)) {
				List<String> authorsList = (List<String>) extraUsers.get(paperId).get("authors");
				firstAuthor = authorsList.get(0);
				paperTitle = (String) extraUsers.get(paperId).get("title");
				attendees.add(firstAuthor);
				gen.objectPropertyAssertion(gen.getObjectProperty("attends"), gen.getNamedIndividual(firstAuthor),
						gen.getNamedIndividual(confInstance));
				gen.objectPropertyAssertion(gen.getObjectProperty("presents"), gen.getNamedIndividual(firstAuthor),
						gen.getNamedIndividual(paperId));
				gen.objectPropertyAssertion(gen.getObjectProperty("hasAcceptedPaper"),
						gen.getNamedIndividual(firstAuthor), gen.getNamedIndividual(paperId));
				gen.objectPropertyAssertion(gen.getObjectProperty("hasPaperTrack"), gen.getNamedIndividual(paperId),
						gen.getNamedIndividual("doctoralConsortiumTrack"));
				gen.objectPropertyAssertion(gen.getObjectProperty("isPresentedAt"), gen.getNamedIndividual(paperId),
						gen.getNamedIndividual(confInstance));
				gen.dataPropertyAssertion(gen.getDataProperty("hasTitle"), gen.getNamedIndividual(paperId),
						gen.getLiteral(paperTitle));
				gen.dataPropertyAssertion(gen.getDataProperty("hasPaperDomain"), gen.getNamedIndividual(paperId),
						gen.getLiteral(gen.TOKEN_Domain[gen.random.nextInt(gen.TOKEN_Domain.length)]));
				gen.classAssertion(gen.getClass("Student"), gen.getNamedIndividual(firstAuthor));
		
			}

		}

		/*
		 * Excited to share the news that our paper [@PaperTitle] has been accepted in
		 * the [@PaperTrack]at [@Conference]. Congrats to the co-authors
		 * 〈@Coauthor1, @Coauthor2, …〉
		 */
		// coauthors also included
		// pick random papers from paperMap 1 for the acceptance notifications (not all
		// the authors would post)
		String paperId2, postingAuthor;
		String paperTitle;
		List<String> authorsList;
		HashSet<String> postingAuthors = new HashSet<>();
		for (int i = 0; i < papersAccepted.size() * 0.7; i++) {
			Set<String> keys = extraUsers.keySet();
			String paperId = keys.toArray(new String[keys.size()])[gen.random.nextInt(keys.size())];
			Map<String, Object> paperInfo = extraUsers.get(paperId);
			if (!postingAuthors.contains(paperId)) {
				postingAuthors.add(paperId);
			}
		}
		for (String paperId : postingAuthors) {
			authorsList = (List<String>) extraUsers.get(paperId).get("authors");
			postingAuthor = authorsList.get(gen.random.nextInt(authorsList.size()));
			paperTitle = (String) extraUsers.get(paperId).get("title");

			gen.objectPropertyAssertion(gen.getObjectProperty("hasAcceptedPaper"),
					gen.getNamedIndividual(postingAuthor), gen.getNamedIndividual(paperId));
			gen.objectPropertyAssertion(gen.getObjectProperty("isAcceptedAt"), gen.getNamedIndividual(paperId),
					gen.getNamedIndividual(confInstance));
			gen.objectPropertyAssertion(gen.getObjectProperty("hasAuthor"), gen.getNamedIndividual(paperId),
					gen.getNamedIndividual(postingAuthor));
			gen.dataPropertyAssertion(gen.getDataProperty("hasTitle"), gen.getNamedIndividual(paperId),
					gen.getLiteral(paperTitle));
			gen.dataPropertyAssertion(gen.getDataProperty("hasPaperDomain"), gen.getNamedIndividual(paperId),
					gen.getLiteral(gen.TOKEN_Domain[gen.random.nextInt(gen.TOKEN_Domain.length)]));

			// now for some of these paperIds, generate co-authors

			if (gen.random.nextInt(1) == 1) {
				// authorsList = (List<String>) extraUsers.get(paperId2).get("authors");
				for (String author : authorsList) {
					// do something with the author
					if (author.matches(postingAuthor)) {
					} else
						gen.objectPropertyAssertion(gen.getObjectProperty("hasAuthor"), gen.getNamedIndividual(paperId),
								gen.getNamedIndividual(author));
					gen.objectPropertyAssertion(gen.getObjectProperty("hasCoAuthor"),
							gen.getNamedIndividual(postingAuthor), gen.getNamedIndividual(author));
				}

			}
		}

		/*
		 * Attention all attendees! Early bird registration for #Conference2023 is now
		 * open! Register before [insert date] to receive [insert discount amount].
		 * Don't miss out on this opportunity to save money and secure your spot.
		 * Regular registration will remain open until [insert date], so register now to
		 * guarantee your spot at the conference.
		 */

		/*
		 * Attention all attendees! The #Conference2023 schedule is now available. Plan
		 * your sessions ahead of time and don't miss out on any of the amazing
		 * presentations. Regular registration is still open until [insert date], so
		 * hurry and register now to secure your spot!
		 */
//		gen.dataPropertyAssertion(gen.getDataProperty("hasStatus"), gen.getNamedIndividual(confInstance),
//				gen.getLiteral("earlyRegistrationDeadlineApproaching"));

	}

	public void studentGrantsAndVolunteers(Generator gen, Map<String, Map<String, Object>> papersAccepted) {

		/*
		 * Thank you to the 〈SponsorName〉 for supporting my attendance at 〈Conf 6〉.
		 * Looking forward to representing 〈InstituteName〉 at the conference. Glad to
		 * share the news that I got the student grant to attend ⟨Conf 1⟩. Can’t wait to
		 * learn from all the amazing talks and meet so many incredible researchers at
		 * ⟨Conf Location⟩. Thank you to the 〈Organizers〉 for making this opportunity
		 * possible.
		 */
		Set<String> keys = papersAccepted.keySet();
		String paperId = keys.toArray(new String[keys.size()])[gen.random.nextInt(keys.size())];
		Map<String, Object> paperInfo = papersAccepted.get(paperId);
		String firstAuthor, paperTitle;
		String organization;
		// int length=extraUsers.size();
		// randomly pick first authors
		if (papersAccepted.containsKey(paperId)) {
			List<String> authorsList = (List<String>) papersAccepted.get(paperId).get("authors");
			firstAuthor = authorsList.get(0);
			paperTitle = (String) papersAccepted.get(paperId).get("title");

			if (gen.random.nextInt(1) == 1) {
				organization = "AcadOrg" + gen.random.nextInt(100);
			} else
				organization = "NonAcadOrg" + gen.random.nextInt(100);
			gen.objectPropertyAssertion(gen.getObjectProperty("hasAffiliation"), gen.getNamedIndividual(firstAuthor),
					gen.getNamedIndividual(organization));
			gen.objectPropertyAssertion(gen.getObjectProperty("attends"), gen.getNamedIndividual(firstAuthor),
					gen.getNamedIndividual(confInstance));
			gen.classAssertion(gen.getClass("Student"), gen.getNamedIndividual(firstAuthor));
			attendees.add(firstAuthor);

			/*
			 * Excited to be a volunteer at 〈Conf 1〉 this year! Looking forward to meeting
			 * all the attendees and helping out in any way I can.
			 */
			if (gen.random.nextInt(1) == 1) {
				gen.objectPropertyAssertion(gen.getObjectProperty("volunteersFor"), gen.getNamedIndividual(firstAuthor),
						gen.getNamedIndividual(confInstance));
				attendees.add(firstAuthor);
			}
		}

	}

	public void beforeConferenceEvent(Generator gen, Map<String, Map<String, Object>> papersAccepted,
			Map<String, Map<String, Object>> extraUsers, HashSet<String> attendees) {
		/*
		 * We’re thrilled to announce our keynote speakers for 〈Conf 1〉: 〈Speaker1〉,
		 * 〈Speaker2〉, and 〈Speaker3〉! Be sure to catch their talks during the
		 * conference.
		 */

		int randomNum = gen.random.nextInt(3) + 1;
		for (int value = 0; value < randomNum; value++) {
			int length = extraUsers.size();
			Set<String> keys = extraUsers.keySet();
			String paperId = keys.toArray(new String[keys.size()])[gen.random.nextInt(keys.size())];
			Map<String, Object> paperInfo = extraUsers.get(paperId);
			List<String> authorNames = (List<String>) paperInfo.get("authors");
			String lastAuthor = authorNames.get(authorNames.size() - 1);
			gen.objectPropertyAssertion(gen.getObjectProperty("attends"), gen.getNamedIndividual(lastAuthor),
					gen.getNamedIndividual(confInstance));
			gen.objectPropertyAssertion(gen.getObjectProperty("hasRole"), gen.getNamedIndividual(lastAuthor),
					gen.getNamedIndividual("keynoteSpeakerRole"));
			gen.objectPropertyAssertion(gen.getObjectProperty("givesTalk"), gen.getNamedIndividual(lastAuthor),
					gen.getNamedIndividual(confInstance+"keynoteTalk"+value));
			gen.objectPropertyAssertion(gen.getObjectProperty("givenAt"), gen.getNamedIndividual(confInstance+"keynoteTalk"+value),
					gen.getNamedIndividual(confInstance));
			gen.dataPropertyAssertion(gen.getDataProperty("givesTalkOn"), gen.getNamedIndividual(lastAuthor),
					gen.getLiteral(confInstance + "_keynoteTalk_" + value));
			gen.classAssertion(gen.getClass("KeynoteTalks"), gen.getNamedIndividual(confInstance+"keynoteTalk"+value));
			gen.classAssertion(gen.getClass("KeynoteSpeakerRole"), gen.getNamedIndividual("keynoteSpeakerRole"));
			attendees.add(lastAuthor);
			gen.dataPropertyAssertion(gen.getDataProperty("hasDomain"), gen.getNamedIndividual(confInstance+"keynoteTalk"+value),
					gen.getLiteral(gen.TOKEN_Domain[gen.random.nextInt(gen.TOKEN_Domain.length)]));
		}

		if (gen.random.nextInt(1) == 0) {
			randomNum = gen.random.nextInt(2) + 1;
			for (int value = 0; value < randomNum; value++) {
				int length = extraUsers.size();
				Set<String> keys = extraUsers.keySet();
				String paperId = keys.toArray(new String[keys.size()])[gen.random.nextInt(keys.size())];
				Map<String, Object> paperInfo = extraUsers.get(paperId);
				List<String> authorNames = (List<String>) paperInfo.get("authors");
				String lastAuthor = authorNames.get(authorNames.size() - 1);
				gen.objectPropertyAssertion(gen.getObjectProperty("attends"), gen.getNamedIndividual(lastAuthor),
						gen.getNamedIndividual(confInstance));
				gen.objectPropertyAssertion(gen.getObjectProperty("hasRole"), gen.getNamedIndividual(lastAuthor),
						gen.getNamedIndividual("invitedTalkSpeakerRole"));
				gen.objectPropertyAssertion(gen.getObjectProperty("givesTalk"), gen.getNamedIndividual(lastAuthor),
						gen.getNamedIndividual(confInstance+"invitedTalk"+value));
				gen.objectPropertyAssertion(gen.getObjectProperty("givenAt"), gen.getNamedIndividual(confInstance+"invitedTalk"+value),
						gen.getNamedIndividual(confInstance));
				gen.dataPropertyAssertion(gen.getDataProperty("givesTalkOn"), gen.getNamedIndividual(lastAuthor),
						gen.getLiteral(confInstance + "_invitedTalk_" + value));
				gen.classAssertion(gen.getClass("InvitedTalks"), gen.getNamedIndividual(confInstance+"invitedTalk"+value));
				gen.classAssertion(gen.getClass("InvitedTalkSpeakerRole"),
						gen.getNamedIndividual("invitedTalkSpeakerRole"));
				gen.dataPropertyAssertion(gen.getDataProperty("hasDomain"), gen.getNamedIndividual(confInstance+"invitedTalk"+value),
						gen.getLiteral(gen.TOKEN_Domain[gen.random.nextInt(gen.TOKEN_Domain.length)]));
				attendees.add(lastAuthor);
			}
		}

		/*
		 * Our research group 〈@Affiliation〉 is presenting <n> papers titled
		 * 〈@Paper1, @Paper2, …〉 at 〈Conf 4〉 this year. Looking forward to hearing about
		 * other great works as well.
		 */
		String organization, researchGroup, college;
		randomNum = gen.random.nextInt(papersAccepted.size()) + 1;
		for (int value = 0; value < randomNum * 0.4; value++) {
			int length = papersAccepted.size();
			// String track=gen.TOKEN_ConferenceEventTrack[value];
			// randomly picking a paper to map the last author in the author list as the
			// track chair
			Set<String> keys = papersAccepted.keySet();
			researchGroup = "researchGroup" + gen.random.nextInt(gen.researchGroupCount);
			int count = 0;
			while (gen.random.nextInt(5) != 0 || count < 6) {
				String paperId = keys.toArray(new String[keys.size()])[gen.random.nextInt(keys.size())];
				Map<String, Object> paperInfo2 = papersAccepted.get(paperId);
				List<String> authorNames = (List<String>) paperInfo2.get("authors");
				String lastAuthor = authorNames.get(authorNames.size() - 1);
				randomNum = gen.random.nextInt(gen.TOKEN_ConferenceEventTrack.length) + 1;
				HashSet<Integer> set = new HashSet<>();
				// random track announcements
				while (set.size() < randomNum) {
					int random = gen.random.nextInt(gen.TOKEN_ConferenceEventTrack.length);
					set.add(random);
				}
				// pick random last authors from the extraUsers as different trackc chairs
				for (int t : set) {
					// int length=extraUsers.size();
					String track = gen.TOKEN_ConferenceEventTrack[t];
					// researchGroup = "researchGroup" + gen.random.nextInt(gen.researchGroupCount);
					gen.objectPropertyAssertion(gen.getObjectProperty("hasAffiliation"),
							gen.getNamedIndividual(lastAuthor), gen.getNamedIndividual(researchGroup));
					gen.objectPropertyAssertion(gen.getObjectProperty("hasAcceptedPaper"),
							gen.getNamedIndividual(lastAuthor), gen.getNamedIndividual(paperId));
					gen.objectPropertyAssertion(gen.getObjectProperty("isAcceptedAt"), gen.getNamedIndividual(paperId),
							gen.getNamedIndividual(confInstance));
					gen.objectPropertyAssertion(gen.getObjectProperty("hasPaperTrack"), gen.getNamedIndividual(paperId),
							gen.getNamedIndividual(track));
					gen.dataPropertyAssertion(gen.getDataProperty("hasPaperDomain"), gen.getNamedIndividual(paperId),
							gen.getLiteral(gen.TOKEN_Domain[gen.random.nextInt(gen.TOKEN_Domain.length)]));
				}
				count++;
			}
		}
		/*
		 * My student 〈@Person〉 will be presenting our work titled 〈@PaperTitle〉 at
		 * 〈@Conference〉 〈@Coauthor1, @Coauthor2, …〉.
		 * 
		 */
		randomNum = gen.random.nextInt(papersAccepted.size()) + 1;
		for (int value = 0; value < randomNum * 0.3; value++) {
			int length = papersAccepted.size();
			Set<String> keys = papersAccepted.keySet();
			String paperId = keys.toArray(new String[keys.size()])[gen.random.nextInt(keys.size())];
			Map<String, Object> paperInfo2 = papersAccepted.get(paperId);
			List<String> authorNames = (List<String>) paperInfo2.get("authors");
			String lastAuthor = authorNames.get(authorNames.size() - 1);
			String firstAuthor = authorNames.get(0);
			researchGroup = "researchGroup" + gen.random.nextInt(gen.researchGroupCount);
			gen.objectPropertyAssertion(gen.getObjectProperty("hasAffiliation"), gen.getNamedIndividual(lastAuthor),
					gen.getNamedIndividual(researchGroup));
			gen.objectPropertyAssertion(gen.getObjectProperty("hasAffiliation"), gen.getNamedIndividual(firstAuthor),
					gen.getNamedIndividual(researchGroup));
			gen.objectPropertyAssertion(gen.getObjectProperty("presents"), gen.getNamedIndividual(firstAuthor),
					gen.getNamedIndividual(paperId));
			gen.objectPropertyAssertion(gen.getObjectProperty("isPresentedAt"), gen.getNamedIndividual(paperId),
					gen.getNamedIndividual(confInstance));
			gen.classAssertion(gen.getClass("Student"), gen.getNamedIndividual(firstAuthor));
			gen.objectPropertyAssertion(gen.getObjectProperty("hasAdvisor"), gen.getNamedIndividual(firstAuthor),
					gen.getNamedIndividual(lastAuthor));
			gen.objectPropertyAssertion(gen.getObjectProperty("hasAuthor"), gen.getNamedIndividual(paperId),
					gen.getNamedIndividual(firstAuthor));
			gen.objectPropertyAssertion(gen.getObjectProperty("hasAuthor"), gen.getNamedIndividual(paperId),
					gen.getNamedIndividual(lastAuthor));
			gen.dataPropertyAssertion(gen.getDataProperty("hasPaperDomain"), gen.getNamedIndividual(paperId),
					gen.getLiteral(gen.TOKEN_Domain[gen.random.nextInt(gen.TOKEN_Domain.length)]));
			attendees.add(firstAuthor);

			// authorsList = (List<String>) extraUsers.get(paperId2).get("authors");
			for (String author : authorNames) {
				// do something with the author
				if (author.matches(lastAuthor)) {
				} else
					gen.objectPropertyAssertion(gen.getObjectProperty("hasAuthor"), gen.getNamedIndividual(paperId),
							gen.getNamedIndividual(author));
				gen.objectPropertyAssertion(gen.getObjectProperty("hasCoAuthor"), gen.getNamedIndividual(lastAuthor),
						gen.getNamedIndividual(author));

			}

		}
	}

	public void duringConferenceEvent(Generator gen, Map<String, Map<String, Object>> papersAccepted,
			Map<String, Map<String, Object>> extraUsers, HashSet<String> attendees) {

		/*
		 * [@Attendee] Having a great time networking and meeting new people at
		 * [@Conference]. So many interesting conversations! or [@Attendee] Just got
		 * back from a poster session at [@Conference]. So many innovative ideas on
		 * display!
		 */
		/*
		 * [@Attendee] Just attended an amazing keynote speech at [@Conference] by
		 * [@KeynoteSpeaker]. Learned so much about 〈Topic〉!
		 */

		int randomNum = gen.random.nextInt(papersAccepted.size()) + 1;
		for (int value = 0; value < randomNum * 0.6; value++) {
			Set<String> keys = papersAccepted.keySet();
			String paperId = keys.toArray(new String[keys.size()])[gen.random.nextInt(keys.size())];
			Map<String, Object> paperInfo2 = papersAccepted.get(paperId);
			List<String> authorNames = (List<String>) paperInfo2.get("authors");
			String lastAuthor = authorNames.get(authorNames.size() - 1);
			// String firstAuthor = authorNames.get(0);
			// authorsList = (List<String>) extraUsers.get(paperId2).get("authors");
			for (String author : authorNames) {
				// do something with the author
				if (author.matches(lastAuthor)) {
				} else {
					if (gen.random.nextInt(2) == 1) {
						String researchGroup = "researchGroup" + gen.random.nextInt(gen.researchGroupCount);
						gen.objectPropertyAssertion(gen.getObjectProperty("hasAffiliation"),
								gen.getNamedIndividual(author), gen.getNamedIndividual(researchGroup));
						gen.objectPropertyAssertion(gen.getObjectProperty("attends"), gen.getNamedIndividual(author),
								gen.getNamedIndividual(confInstance));
						attendees.add(author);
					}
				}

			}
		}

		randomNum = gen.random.nextInt(extraUsers.size()) + 1;
		for (int value = 0; value < randomNum * 0.3; value++) {
			Set<String> keys = extraUsers.keySet();
			String paperId = keys.toArray(new String[keys.size()])[gen.random.nextInt(keys.size())];
			Map<String, Object> paperInfo2 = papersAccepted.get(paperId);
			List<String> authorNames = (List<String>) paperInfo2.get("authors");
			String lastAuthor = authorNames.get(authorNames.size() - 1);
			// String firstAuthor = authorNames.get(0);
			// authorsList = (List<String>) extraUsers.get(paperId2).get("authors");
			for (String author : authorNames) {
				// do something with the author
				if (author.matches(lastAuthor)) {
				} else {
					if (gen.random.nextInt(2) == 1) {
						gen.objectPropertyAssertion(gen.getObjectProperty("attends"), gen.getNamedIndividual(author),
								gen.getNamedIndividual(confInstance));
						attendees.add(author);
						if (gen.random.nextInt(3) == 1) {
							String researchGroup = "researchGroup" + gen.random.nextInt(gen.researchGroupCount);
							gen.objectPropertyAssertion(gen.getObjectProperty("hasAffiliation"),
									gen.getNamedIndividual(author), gen.getNamedIndividual(researchGroup));
						}
					}

				}
			}
		}
		/*
		 * I 〈@Person〉 will be presenting our work titled 〈@PaperTitle〉 at 〈@Conference〉
		 * 〈@Coauthor1, @Coauthor2, …〉.
		 * 
		 */

		randomNum = gen.random.nextInt(papersAccepted.size()) + 1;
		for (int value = 0; value < randomNum * 0.3; value++) {
			int length = papersAccepted.size();
			Set<String> keys = papersAccepted.keySet();
			String paperId = keys.toArray(new String[keys.size()])[gen.random.nextInt(keys.size())];
			Map<String, Object> paperInfo2 = papersAccepted.get(paperId);
			List<String> authorNames = (List<String>) paperInfo2.get("authors");
			String lastAuthor = authorNames.get(authorNames.size() - 1);
			String firstAuthor = authorNames.get(0);
			gen.objectPropertyAssertion(gen.getObjectProperty("presents"), gen.getNamedIndividual(firstAuthor),
					gen.getNamedIndividual(paperId));
			attendees.add(firstAuthor);
			gen.objectPropertyAssertion(gen.getObjectProperty("isPresentedAt"), gen.getNamedIndividual(paperId),
					gen.getNamedIndividual(confInstance));
			String researchGroup = "researchGroup" + gen.random.nextInt(gen.researchGroupCount);
			gen.objectPropertyAssertion(gen.getObjectProperty("hasAffiliation"), gen.getNamedIndividual(firstAuthor),
					gen.getNamedIndividual(researchGroup));
			gen.dataPropertyAssertion(gen.getDataProperty("hasPaperDomain"), gen.getNamedIndividual(paperId),
					gen.getLiteral(gen.TOKEN_Domain[gen.random.nextInt(gen.TOKEN_Domain.length)]));
			// authorsList = (List<String>) extraUsers.get(paperId2).get("authors");
			for (String author : authorNames) {
				// do something with the author

				gen.objectPropertyAssertion(gen.getObjectProperty("hasAuthor"), gen.getNamedIndividual(paperId),
						gen.getNamedIndividual(author));
				gen.objectPropertyAssertion(gen.getObjectProperty("hasCoAuthor"), gen.getNamedIndividual(firstAuthor),
						gen.getNamedIndividual(author));
			}

			/*
			 * We're excited to start the [@Day] day [@Conference]! Here's a quick rundown
			 * of what's happening today: [Insert schedule of events]
			 */

			// gen.dataPropertyAssertion(gen.getDataProperty("hasStatus"),gen.getNamedIndividual(confInstance),gen.getLiteral("onGoing"));

		}
		
	}
	public void afterConferenceEvent(Generator gen, Map<String, Map<String, Object>> papersAccepted,
			Map<String, Map<String, Object>> extraUsers, HashSet<String> attendees) {

		/*
		 * Had a great time at 〈Conf 1〉! The keynote speeches, paper presentations, and
		 * networking events were all top-notch. Thanks to everyone who made it a
		 * memorable experience!
		 * 
		 */
		/*
		 * Congratulations to the organizing committee <O1, O2…>of 〈Conf 1〉 for putting
		 * together a successful event. Kudos to all the volunteers and sponsors who
		 * made it possible! See you all next year at 〈Future ConfLocation 〉
		 * 
		 */
		  //organizers.add("David");

	        // Retrieve random values from the attendees and organizers sets
	        

			for (int value = 0; value < this.organizers.size() * 0.2; value++) {
		        String randomOrganizer = getRandomElement(this.organizers, gen.random);
				gen.objectPropertyAssertion(gen.getObjectProperty("attends"), gen.getNamedIndividual(randomOrganizer ),	gen.getNamedIndividual(confInstance));

			}
		for (int value = 0; value < this.attendees.size() * 0.7; value++) {
			String randomAttendee = getRandomElement(this.attendees, gen.random);
			gen.objectPropertyAssertion(gen.getObjectProperty("attends"), gen.getNamedIndividual(randomAttendee),	gen.getNamedIndividual(confInstance));

		}

		/*
		 * Thrilled to announce that we got the best 〈PaperTrack-n〉 award at 〈Conf-n〉
		 * for our paper 〈PaperTitle-n〉. 〈Institute and Author Tags〉.
		 * 
		 */
		int randomNum = gen.random.nextInt(papersAccepted.size()) + 1;
		randomNum = gen.random.nextInt(gen.TOKEN_ConferenceEventTrack.length) + 1;
		HashSet<Integer> set = new HashSet<>();
		// random track announcements
		while (set.size() < randomNum) {
			int random = gen.random.nextInt(gen.TOKEN_ConferenceEventTrack.length);
			set.add(random);
		}
		// pick random last authors from the extraUsers as different trackc chairs
		for (int value : set) {
			int length = extraUsers.size();
			String track = gen.TOKEN_ConferenceEventTrack[value];
			// randomly picking a paper to map the last author in the author list as the
			// track chair
			Set<String> keys = extraUsers.keySet();
			String randomKey = keys.toArray(new String[keys.size()])[gen.random.nextInt(keys.size())];
			Map<String, Object> paperInfo = extraUsers.get(randomKey);
			List<String> authorNames = (List<String>) paperInfo.get("authors");
			String firstAuthor = authorNames.get(0);
			String paperTitle = (String) paperInfo.get("title");
			for (String author : authorNames) {
				// do something with the author

				gen.objectPropertyAssertion(gen.getObjectProperty("hasAuthor"), gen.getNamedIndividual(randomKey),
						gen.getNamedIndividual(author));
				gen.objectPropertyAssertion(gen.getObjectProperty("hasCoAuthor"), gen.getNamedIndividual(firstAuthor),
						gen.getNamedIndividual(author));
			}
			gen.objectPropertyAssertion(gen.getObjectProperty("hasAcceptedPaper"), gen.getNamedIndividual(firstAuthor),
					gen.getNamedIndividual(randomKey));
			gen.objectPropertyAssertion(gen.getObjectProperty("isAcceptedAt"), gen.getNamedIndividual(randomKey),
					gen.getNamedIndividual(confInstance));
			gen.objectPropertyAssertion(gen.getObjectProperty("hasPaperTrack"), gen.getNamedIndividual(randomKey),
					gen.getNamedIndividual(track));
			gen.objectPropertyAssertion(gen.getObjectProperty("hasAuthor"), gen.getNamedIndividual(randomKey),
					gen.getNamedIndividual(firstAuthor));
			gen.dataPropertyAssertion(gen.getDataProperty("hasPaperDomain"), gen.getNamedIndividual(randomKey),
					gen.getLiteral(gen.TOKEN_Domain[gen.random.nextInt(gen.TOKEN_Domain.length)]));
			// gen.objectPropertyAssertion(gen.getObjectProperty("isCoAuthorOf"),gen.getNamedIndividual(person),gen.getNamedIndividual(person));
			// gen.objectPropertyAssertion(gen.getObjectProperty("isCoAuthorOf"),gen.getNamedIndividual(person),gen.getNamedIndividual(person));
			// gen.objectPropertyAssertion(gen.getObjectProperty("hasAffiliation"),gen.getNamedIndividual(personid),gen.getNamedIndividual(organizationId));
		}
		/*
		 * Thank you to all the attendees, speakers, and sponsors who made [@Conference]
		 * a huge success! We hope you had a great time and learned a lot. See you at
		 * the next conference!
		 */
//		gen.dataPropertyAssertion(gen.getDataProperty("hasStatus"), gen.getNamedIndividual(confInstance),
//				gen.getLiteral("eventOver"));
//
	}
}
