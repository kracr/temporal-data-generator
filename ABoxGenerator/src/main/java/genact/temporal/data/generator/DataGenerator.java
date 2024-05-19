package genact.temporal.data.generator;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.formats.*;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
public class DataGenerator {

	// The code makes use of: 
	// -config.properties file to adjust the size of the data generated
	// -twitter templates and rdf mappings files that are in YAML.
	// authors.csv, papers.csv, cities.csv, 
	//Twitter metadata goes to separate file, coz thats not required for answering queries.
	//this file will only be required to obtain timestamps associated with tweetids. 
	//the actual event data will be saved with tweetid.ttl
	//and streamed
	//To save time, different conferences are saved in different folders
	//At the time of generation itself, we provide 2 types of streams : Conferences, Users
	int acceptedPaperCount_min;
	int acceptedPaperCount_max;
	int acceptedPaperCount;
	int peopleDirectlyInvolved_min;
	int peopleDirectlyInvolved_max;
	int peopleDirectlyInvolved;
	int otherPeopleInvolved_min;
	int otherPeopleInvolved_max;
	int otherPeopleInvolved;
	int usersInvolved_min;
	int usersInvolved_max;
	int usersInvolved;
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
	int conferenceDuration_min_months;
	int conferenceDuration_max_months;
	int conferenceDuration_months;
	int nextConferenceCycleStartsIn_min;
	int nextConferenceCycleStartsIn_max;
	int nextConferenceCycleStartsIn;
	int cityCount_min;
	int cityCount_max;
	int cityCount;
	List<String> researchGroups = new ArrayList<>();
	List<String> colleges = new ArrayList<>();
	List<String> academicOrganizations = new ArrayList<>();
	List<String> nonAcademicOrganizations = new ArrayList<>();
	List<String> peopleDirectlyInvolvedList = new ArrayList<>();
	List<String> otherPeopleInvolvedList = new ArrayList<>();
	List<String> papersList = new ArrayList<>();
	List<String> cityList = new ArrayList<>();
	Map<String, Map<String, Object>> papers;
	File streamsDirectory;
	//Partition partition;
	Map<String, Map<String, String>> userData;
	Map<String, Map<String, Object>> paperData;
	// these are the instances that have been defined in the ontologies
	String[] TOKEN_ConferenceEventTrack = new String[] { "applicationsTrack", "demoTrack", "doctoralConsortiumTrack",
			"posterTrack", "researchTrack", "resourcesTrack", "tutorialTrack", "workshopTrack" };
	String[] TOKEN_EventMode = new String[] { "online", "offline", "hybrid" };
	String[] TOKEN_ChairRole = new String[] { "generalChair", "localChair", "researchTrackChair", "resourcesTrackChair",
			"trackChair", "tutorialTrackChair", "workshopTrackChair" };
	String[] TOKEN_Domain = new String[] { "ai", "ml", "nlp", "aiForSocialGood", "artificialIntelligence", "bigData",
			"blockchain", "cloudComputing", "computerVision", "dataScience", "deepLearning", "internetOfThings",
			"knowledgeGraph", "linkedData", "machineLearning", "ontology", "naturalLanguageProcessing",
			"quantumComputing", "semanticWeb" };
	String[] TOKEN_EventPhases= new String[] { };
	Model staticModel = ModelFactory.createDefaultModel();
	// Model streamModel = ModelFactory.createDefaultModel();
	// OWLOntology o2 = createOWLOntology(pm);
	File staticFile_n3, staticFile_rdf;
	File streamFile_n3, streamFile_rdf;
	RDFWriter staticModelWriter;
	int confNum; // user specifies the number of conferences required such as if user wants 4:
					// ESWC, ISWC, AAAI, WWW.
	int confCycle; // user specifies the number of conference cycles needed such as ESWC21. ESWC22,
					// ESWC23...
	File staticDirectory;
	String profile;
	String confInstance;
	String directoryPath;
	ConferenceStreams[] conferences;
	Random random = new Random();
	HashMap<Integer, String> map1 = new HashMap<>();
	HashMap<Integer, String> map2 = new HashMap<>();
	HashMap<Integer, String> map3 = new HashMap<>();
	long startTimestampMillis;
	List<String> usersList;

	public DataGenerator() {
	}

	public static void main(String[] args) throws IOException {
		int confNum = 5;
		int seed = 1;
		int confCycle = 3;
		String currentDirectory = System.getProperty("user.dir");
		File currentDirFile = new File(currentDirectory);
		String directoryPath = currentDirFile.getParent();
		if (args.length == 4) {
			confNum = Integer.parseInt(args[0]);
			confCycle = Integer.parseInt(args[1]);
			directoryPath = args[2];
			seed = Integer.parseInt(args[3]);
		}
		if (args.length == 3) {
			confNum = Integer.parseInt(args[0]);
			confCycle = Integer.parseInt(args[1]);
			directoryPath = args[2];
		}

		else if (args.length == 2) {
			confNum = Integer.parseInt(args[0]);
			confCycle = Integer.parseInt(args[1]);
		} else {
			System.out.println(
					"Please give arguments in the following order: No. of conferences (int)*Mandatory, No. of conference Cycles (int)*Mandatory , DirectoryPath (optional), Seed (optional) ");
			System.out.println("For example: 2 5 C:/GitHub/OWL2StreamBench 100");
		}

		long startTimestampMillis = new Date(2000 - 1900, 0, 1, 0, 0).getTime();// represents January 1, 2000, at
																				// midnight (00:00:00) in Coordinated
																				// Universal Time (UTC).
		new DataGenerator().start(confNum, confCycle, directoryPath, seed, startTimestampMillis);
	}


	// data generator starts with selecting the name of the authors, paper,
	// conferences, organizations, locations randomly
	// and storing them to a data structure and utilize them for generating the instances in the later part of the code
	//tracks, authorids and affiliations are assigned randomly 
	//currently we have data for 27 conferences starting from the year 2000 to 2022
	public void start(int confNum, int confCycle, String directoryPath, int seed, long startTimestampMillis) throws IOException {
	
		this.directoryPath = directoryPath;
		this.startTimestampMillis = startTimestampMillis;
		this.staticDirectory = new File(directoryPath + "/StaticData");
		if (!staticDirectory.exists()) {
			staticDirectory.mkdirs();
		}
		//static file consists of static information such as conferences, cities in rdf format. There will be
		// a single static file for each run of the generated data. 
		//location related details are in a separate owl file
		//static twitter data is also in a separate static file such as twitter ids associated with different users.
		//their bios and affiliation can change so they are part of dynamic data streams 
		
		this.staticFile_rdf = new File(this.staticDirectory + "/conf.n3");

		if (!this.staticFile_rdf.exists()) {
			try {
				// Create a new file
				this.staticFile_rdf.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.confNum = confNum;
		this.confCycle = confCycle;
		random.setSeed((long) seed);

		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(directoryPath+"/ABoxGenerator/config.properties");
			// load range of different parameters from config.properties file
			prop.load(input);
			// this.requiredABoxFormat=prop.getProperty("requiredABoxFormat");
			this.acceptedPaperCount_min = Integer.parseInt(prop.getProperty("acceptedPaperCount_min"));
			this.acceptedPaperCount_max = Integer.parseInt(prop.getProperty("acceptedPaperCount_max"));

			this.usersInvolved_min = Integer.parseInt(prop.getProperty("usersInvolved_min"));
			this.usersInvolved_max = Integer.parseInt(prop.getProperty("usersInvolved_max"));
			this.usersInvolved=this.random.nextInt(usersInvolved_max - usersInvolved_min + 1)
					+ usersInvolved_min;
			this.peopleDirectlyInvolved_min = Integer.parseInt(prop.getProperty("peopleDirectlyInvolved_min"));
			this.peopleDirectlyInvolved_max = Integer.parseInt(prop.getProperty("peopleDirectlyInvolved_max"));
			this.otherPeopleInvolved_min = Integer.parseInt(prop.getProperty("otherPeopleInvolved_min"));
			this.otherPeopleInvolved_max = Integer.parseInt(prop.getProperty("otherPeopleInvolved_max"));
			this.acadOrganizationCount_min = Integer.parseInt(prop.getProperty("acadOrganizationCount_min"));
			this.acadOrganizationCount_max = Integer.parseInt(prop.getProperty("acadOrganizationCount_max"));
			this.researchGroupCount_min = Integer.parseInt(prop.getProperty("researchGroupCount_min"));
			this.researchGroupCount_max = Integer.parseInt(prop.getProperty("researchGroupCount_max"));
			this.collegeCount_min = Integer.parseInt(prop.getProperty("collegeCount_min"));
			this.collegeCount_max = Integer.parseInt(prop.getProperty("collegeCount_max"));
			this.nonAcadOrganizationCount_min = Integer.parseInt(prop.getProperty("nonAcadOrganizationCount_min"));
			this.nonAcadOrganizationCount_max = Integer.parseInt(prop.getProperty("nonAcadOrganizationCount_min"));
			this.conferenceDuration_min_months = Integer.parseInt(prop.getProperty("conferenceDuration_min_months"));
			this.conferenceDuration_max_months = Integer.parseInt(prop.getProperty("conferenceDuration_max_months"));
			this.cityCount_min = Integer.parseInt(prop.getProperty("cityCount_min"));
			this.cityCount_max = Integer.parseInt(prop.getProperty("cityCount_max"));
			
//            this.publicationNum_Min=Integer.parseInt(prop.getProperty("publicationNum_Min"));
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		 int totalUserCount = this.usersInvolved_max * this.confNum * this.confCycle * 10;
		 int totalPaperCount=this.acceptedPaperCount_max * this.confNum * this.confCycle * 10;
		// Load display names from authors.csv
	        List<String> displayNames = loadDisplayNames(this.directoryPath +"/CSVFiles/authors.csv");
	        List<String> paperTitles = loadDisplayNames(this.directoryPath +"/CSVFiles/papers.csv");
	        // Load affiliations from Organization.owl
	        List<String> affiliations = loadAffiliations(this.directoryPath+"/Ontology/Organization.owl");

	        // Generate user data
	        this.userData = generateUserData(totalUserCount, displayNames, affiliations);
	        this.paperData = generatePaperData(totalPaperCount, paperTitles, userData);
	        // Print user data for verification
	        for (Map.Entry<String, Map<String, String>> entry : this.userData.entrySet()) {
	            System.out.println("UserID: " + entry.getKey() + ", Data: " + entry.getValue());
	        }

		this.generate(seed);
	}
	private static Map<String, Map<String, Object>> generatePaperData(int totalPaperCount, List<String> paperTitles, Map<String, Map<String, String>> userData) {
        Map<String, Map<String, Object>> paperData = new HashMap<>();
        String[] TOKEN_Domain = { "ai", "ml", "nlp", "aiForSocialGood", "artificialIntelligence", "bigData", "blockchain", "cloudComputing", "computerVision", "dataScience", "deepLearning", "internetOfThings", "knowledgeGraph", "linkedData", "machineLearning", "ontology", "naturalLanguageProcessing", "quantumComputing", "semanticWeb" };

        Random random = new Random();
        String[] conferenceTracks = {"applicationsTrack", "demoTrack", "doctoralConsortiumTrack", "posterTrack", "researchTrack", "resourcesTrack", "tutorialTrack", "workshopTrack"};

        List<String> students = userData.entrySet().stream()
                .filter(entry -> entry.getValue().get("designation").equals("Student") || entry.getValue().get("designation").equals("PhD Student"))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<String> professors = userData.entrySet().stream()
                .filter(entry -> entry.getValue().get("designation").equals("Professor"))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<String> otherUsers = userData.entrySet().stream()
                .filter(entry -> !(entry.getValue().get("designation").equals("Student") || entry.getValue().get("designation").equals("PhD Student") || entry.getValue().get("designation").equals("Professor")))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        for (int i = 0; i < totalPaperCount; i++) {
            String paperId = "paper" + (i + 1);
            String paperTitle = paperTitles.get(random.nextInt(paperTitles.size()));
            String conferenceTrack = conferenceTracks[random.nextInt(conferenceTracks.length)];

            List<String> authorList = new ArrayList<>();
            authorList.add(students.get(random.nextInt(students.size())));  // First author as a student

            int authorCount = 1 + random.nextInt(7);  // Total authors between 1 and 8
            for (int j = 1; j < authorCount - 1; j++) {
                String author = otherUsers.get(random.nextInt(otherUsers.size()));
                if (!authorList.contains(author)) {
                    authorList.add(author);
                }
            }
            
            authorList.add(professors.get(random.nextInt(professors.size())));  // Last author as a professor

         
            
            List<String> paperDomains = new ArrayList<>();
            int domainCount = 1 + random.nextInt(3);  // Total domains between 1 and 3
            for (int k = 0; k < domainCount; k++) {
                String domain = TOKEN_Domain[random.nextInt(TOKEN_Domain.length)];
                if (!paperDomains.contains(domain)) {
                    paperDomains.add(domain);
                }
            }

            Map<String, Object> paperMetaData = new HashMap<>();
            paperMetaData.put("PaperTitle", paperTitle);
            paperMetaData.put("ConferenceTrack", conferenceTrack);
            paperMetaData.put("AuthorList", authorList);
            paperMetaData.put("PaperDomains", paperDomains);

            paperData.put(paperId, paperMetaData);
        }

        return paperData;
    }
	private static List<String> loadDisplayNames(String filePath) throws IOException {
        List<String> displayNames = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = br.readLine()) != null) {
            displayNames.add(line.split(",")[0]); // Assuming display name is in the first column
        }
        br.close();
        return displayNames;
    }
    public static List<String> readPaperTitles(String filePath) throws IOException {
        List<String> paperTitles = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Assuming the paper title is in the first column of the CSV
                String[] values = line.split(",");
                if (values.length > 0) {
                    paperTitles.add(values[0]);
                }
            }
        }
        return paperTitles;
    }
    private static List<String> loadAffiliations(String owlFilePath) {
        List<String> affiliations = new ArrayList<>();
        String queryStr = "SELECT ?researchGroup WHERE { ?researchGroup a <https://kracr.iiitd.edu.in/OWL2Bench#ResearchGroup> }";
        Query query = QueryFactory.create(queryStr);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, ModelFactory.createDefaultModel().read(owlFilePath))) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                affiliations.add(soln.getResource("researchGroup").getURI());
            }
        }
        return affiliations;
    }

    private static Map<String, Map<String, String>> generateUserData(int totalUserCount, List<String> displayNames, List<String> affiliations) {
        Map<String, Map<String, String>> userData = new HashMap<>();
        Random random = new Random();
        List<String> designations = Arrays.asList("Student", "PhDStudent", "Professor", "Researcher", "Faculty");

        int studentCount = (int) (totalUserCount * 0.7);
        int phdStudentCount = Math.min(20, (int) (totalUserCount * 0.1));
        int remainingCount = totalUserCount - studentCount - phdStudentCount;

        Map<String, Integer> designationCounts = new HashMap<>();
        designationCounts.put("Student", studentCount);
        designationCounts.put("PhD Student", phdStudentCount);
        designationCounts.put("Professor", remainingCount / 3);
        designationCounts.put("Researcher", remainingCount / 3);
        designationCounts.put("Faculty", remainingCount - (2 * (remainingCount / 3)));

        for (int i = 0; i < totalUserCount; i++) {
            String userId = "user" + (i + 1);
            String userName = userId;
            String displayName = displayNames.get(random.nextInt(displayNames.size()));
            String affiliation = affiliations.get(random.nextInt(affiliations.size()));

            String designation = null;
            while (designation == null) {
                String potentialDesignation = designations.get(random.nextInt(designations.size()));
                if (designationCounts.get(potentialDesignation) > 0) {
                    designation = potentialDesignation;
                    designationCounts.put(potentialDesignation, designationCounts.get(potentialDesignation) - 1);
                }
            }

            Map<String, String> userMetaData = new HashMap<>();
            userMetaData.put("userName", userName);
            userMetaData.put("displayName", displayName);
            userMetaData.put("affiliation", affiliation);
            userMetaData.put("designation", designation);

            userData.put(userId, userMetaData);
        }

        return userData;
    }
	private void generate(int seed) {
		// code for creating organization mappings

		
		
		List<String> researchGroups = new ArrayList<>(); //obtained from organization.owl file using sparql query
		//List<String> cityList = new ArrayList<>(); //not needed as this info is already part of organization.owl
		List<String> usersList = new ArrayList<>(); //obtain all the user names from the csv file
		List<String> papersList = new ArrayList<>(); //obtain all the paper names from the csv file
		//the above lists are used to create random datastructures for each conference
		String authorsCsvFilePath = this.directoryPath +"/CSVFiles/authors.csv";
		String papersCsvFilePath = this.directoryPath + "/CSVFiles/papers.csv";		
 
		this.papersList = readFirstColumnRandomly(papersCsvFilePath, this.acceptedPaperCount*1000); //papers cant be repeated in any cnference cycle / across cofnerences
		//this.cityList = readFirstColumnRandomly(worldCitiesCsvFilePath, this.cityCount);
		//System.out.println(this.cityList);

		String instance, concept, objectProperty, dataProperty;
		String subject, predicate, object, city;
		
		//use owlapi to find researchgroupds from the organization.owl file
		// Generate random names for research groups, colleges, academic organizations,
		// and non-academic organizations

		this.streamsDirectory = new File(this.directoryPath + "/Streams/");
		 if (streamsDirectory.exists() && streamsDirectory.isDirectory()) {
	            File[] files = streamsDirectory.listFiles();

	            // Check if there are any files in the directory
	            if (files != null) {
	                for (File file : files) {
	                    // Delete each file
	                    if (file.isFile()) {
	                        boolean deleted = file.delete();
	                        if (deleted) {
	                            System.out.println("Deleted file: " + file.getName());
	                        } else {
	                            System.err.println("Failed to delete file: " + file.getName());
	                        }
	                    }
	                }
	            } else {
	                System.out.println("No files found in the directory.");
	            }
	        } else {
	            System.err.println("Invalid directory path or directory does not exist.");
	        }
//		this.conferences = new ConferenceStreams[this.confNum];

		// Generates conference instances
		ExecutorService executor = Executors.newFixedThreadPool(confNum);
		for (int i = 0; i < this.confNum; ++i) {
			final int confIndex = i;
			Runnable task = () -> {
				System.out.println("Started Conference Instance " + confIndex);
				this.conferences[confIndex] = new ConferenceStreams(this, confIndex);
			};
			executor.submit(task);
		}
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
     // Save the model in RDF format
     this.staticModel.write(System.out, "TTL");

     // Save the model to a file in RDF format
     this.staticModelWriter = staticModel.getWriter("TTL");
     try {
    	 staticModelWriter.write(staticModel, new FileOutputStream(staticFile_rdf), null);
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		//this.partition.executeAndSaveSparqlQueries(this.confNum);
	}
	private static List<String> readFirstColumnRandomly(String csvFilePath, int count) {
	    List<String> resultList = new ArrayList<>();

	    try (CSVReader csvReader = new CSVReader(new FileReader(csvFilePath))) {
	        // Read all records from the CSV file
	        List<String[]> records = csvReader.readAll();

	        // Check if the CSV file has at least one column
	        if (!records.isEmpty() && records.get(0).length > 0) {
	            int columnIndex = 0; // Assuming the first column
	            int totalEntries = records.size();

	            // Use a random number generator to pick 'count' random entries
	            Random random = new Random();
	            for (int i = 0; i < count; i++) {
	                int randomIndex = random.nextInt(totalEntries);
	                String entry = records.get(randomIndex)[columnIndex];

	                // Sanitize the entry to make it compliant with URL format
	                String sanitizedEntry = sanitizeUri(entry);
	                resultList.add(sanitizedEntry);
	            }
	        }
	    } catch (IOException | CsvException e) {
	        e.printStackTrace();
	    }
	    return resultList;
	}

	private static String sanitizeUri(String uri) {
	    // Replace spaces with underscores
	    String sanitizedUri = uri.replace(" ", "_");

	    // Remove characters that may cause issues in URIs
	    sanitizedUri = sanitizedUri.replaceAll("[^\\w-]", "");

	    return sanitizedUri;
	}


	private static <T> T getRandomElement(List<T> list, Random random) {
		//System.out.println("lets check the list size"+list.size());
		int index = random.nextInt(list.size());
		return list.get(index);
	}

	public void classAssertion(String concept, String instance) {
		Resource Concept = this.staticModel.createResource(concept);
		Resource Instance = this.staticModel.createResource(instance);
		staticModel.add(this.staticModel.createStatement(Concept, RDF.type, OWL.Class));
		Instance.addProperty(RDF.type, Concept);
	}

	public void objectPropertyAssertion(String subject, String predicate, String object) {
		Resource Subject = this.staticModel.createResource(subject);
		Property Predicate = this.staticModel.createProperty(predicate);
		Resource Object = this.staticModel.createResource(object);
		staticModel.add(this.staticModel.createStatement(Subject, Predicate, Object));

	}

	public void dataPropertyAssertion(String subject, String predicate, String object) {
		Resource Subject = this.staticModel.createResource(subject);
		Property Predicate = this.staticModel.createProperty(predicate);
		staticModel.add(this.staticModel.createStatement(Subject, Predicate, object));
	}

}

