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


public class DataGenerator {

	// The code makes use of: 
	// -config.properties file to adjust the size of the data generated
	// -twitter templates and rdf mappings files that are in YAML.
	// authors.csv, papers.csv, cities.csv, 
	
	int acceptedPaperCount_min;
	int acceptedPaperCount_max;
	int acceptedPaperCount;
	int peopleDirectlyInvolved_min;
	int peopleDirectlyInvolved_max;
	int peopleDirectlyInvolved;
	int otherPeopleInvolved_min;
	int otherPeopleInvolved_max;
	int otherPeopleInvolved;
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
	Partition partition;

	// these are the instances that have been defined in the ontologies
	String[] TOKEN_ConferenceEventTrack = new String[] { "applicationsTrack", "demoTrack", "doctoralConsortiumTrack",
			"posterTrack", "researchTrack", "resourcesTrack", "tutorialTrack", "workshopTrack" };
	String[] TOKEN_EventMode = new String[] { "online", "offline", "hybrid" };
	String[] TOKEN_ChairRole = new String[] { "generalChair", "localChair", "researchTrackChair", "resourcesTrackChair",
			"trackChair", "tutorialTrackChair", "workshopTrackChair" };
	// String[] TOKEN_ConferenceList=new String[] {"MOBICOM", "ASPLOS", "ISCA",
	// "AAAI", "ISWC", "ESWC", "IJCAI", "CIKM", "ACL", "KDD", "ECCV", "ICCV",
	// "CVPR", "NeurIPS", "WWW", "UbiComp", "SIGIR", "SIGCOMM", "IJCAR", "ICDE",
	// "AAMAS", "ACMMM", "CAV", "CRYPTO", "HPCA", "FOGA", "ICDM"};
	// publish streams on a url
	String[] TOKEN_Domain = new String[] { "ai", "ml", "nlp", "aiForSocialGood", "artificialIntelligence", "bigData",
			"blockchain", "cloudComputing", "computerVision", "dataScience", "deepLearning", "internetOfThings",
			"knowledgeGraph", "linkedData", "machineLearning", "ontology", "naturalLanguageProcessing",
			"quantumComputing", "semanticWeb" };
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

	public DataGenerator() {
	}

	public static void main(String[] args) {
		int confNum = 5;
		int seed = 1;
		int confCycle = 1;
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
	// and storing them to a data structure and utilize them for generating the instances
	//tracks, authorids and affiliations are assigned randomly 
	//currently we have data for 27 conferences starting from the year 2000 to 2022
	public void start(int confNum, int confCycle, String directoryPath, int seed, long startTimestampMillis) {
	
		this.directoryPath = directoryPath;
		this.startTimestampMillis = startTimestampMillis;
		this.staticDirectory = new File(directoryPath + "/StaticData");
		if (!staticDirectory.exists()) {
			staticDirectory.mkdirs();
		}
		//static file consists of static information such as conferences, cities in rdf format. 
		
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
		this.generate(seed);
	}

	private void generate(int seed) {
		this.partition = new Partition();
		// code for creating organization mappings
		this.researchGroupCount = random.nextInt(researchGroupCount_max - researchGroupCount_min + 1)
				+ researchGroupCount_min;
		this.collegeCount = random.nextInt(collegeCount_max - collegeCount_min + 1) + collegeCount_min;
		this.acadOrganizationCount = random.nextInt(acadOrganizationCount_max - acadOrganizationCount_min + 1)
				+ acadOrganizationCount_min;
		this.nonAcadOrganizationCount = random.nextInt(nonAcadOrganizationCount_max - nonAcadOrganizationCount_min + 1)
				+ nonAcadOrganizationCount_min;
		this.peopleDirectlyInvolved = random.nextInt(peopleDirectlyInvolved_max - peopleDirectlyInvolved_min + 1)
				+ peopleDirectlyInvolved_min;
		this.otherPeopleInvolved = random.nextInt(otherPeopleInvolved_max - otherPeopleInvolved_min + 1)
				+ otherPeopleInvolved_min;
		this.cityCount = random.nextInt(cityCount_max - cityCount_min + 1) + cityCount_min;
		this.acceptedPaperCount = random.nextInt(acceptedPaperCount_max - acceptedPaperCount_min + 1)
				+ acceptedPaperCount_min;
		// also include details for tweet metadata, userid, username, affiliation
		// then another static data file consisting of locations of the organizations
		// and different conferences held so far
		// Create lists to store research groups, colleges, academic organizations, and
		// non-academic organizations
		// hence different static files for location related details (for the
		// conferences held and organizations) and tweetmetadata
		// also another static file, created from csv, that consists of city names
		// mapped with countries, their latitude and longitudes
		List<String> researchGroups = new ArrayList<>();
		List<String> colleges = new ArrayList<>();
		List<String> academicOrganizations = new ArrayList<>();
		List<String> nonAcademicOrganizations = new ArrayList<>();
		List<String> peopleDirectlyInvolvedList = new ArrayList<>();
		List<String> otherPeopleInvolvedList = new ArrayList<>();
		List<String> papersList = new ArrayList<>();
		List<String> cityList = new ArrayList<>();

		String authorsCsvFilePath = directoryPath +"/CSVFiles/authors.csv";
		String papersCsvFilePath = directoryPath + "/CSVFiles/papers.csv";
		String worldCitiesCsvFilePath = directoryPath +"/CSVFiles/worldcities.csv";

		// Read first column of authors CSV file
		this.peopleDirectlyInvolvedList = readFirstColumnRandomly(authorsCsvFilePath, this.peopleDirectlyInvolved);
		this.otherPeopleInvolvedList = readFirstColumnRandomly(authorsCsvFilePath, this.otherPeopleInvolved);
		this.papersList = readFirstColumnRandomly(papersCsvFilePath, this.acceptedPaperCount*1000);
		this.cityList = readFirstColumnRandomly(worldCitiesCsvFilePath, this.cityCount);
		System.out.println(this.cityList);

		String instance, concept, objectProperty, dataProperty;
		String subject, predicate, object, city;
		// Generate random names for research groups, colleges, academic organizations,
		// and non-academic organizations
		for (int i = 1; i <= researchGroupCount; i++) {
			researchGroups.add("researchGroup" + i);
			instance = "https://kracr.iiitd.edu.in/OWL2Bench#researchGroup" + i;
			concept = "https://kracr.iiitd.edu.in/OWL2Bench#ResearchGroup";
			classAssertion(concept, instance);
		}

		for (int i = 1; i <= collegeCount; i++) {
			colleges.add("college" + i);
			instance = "https://kracr.iiitd.edu.in/OWL2Bench#college" + i;
			concept = "https://kracr.iiitd.edu.in/OWL2Bench#College";
			classAssertion(concept, instance);
		}

		for (int i = 1; i <= acadOrganizationCount; i++) {
			academicOrganizations.add("academicOrganization" + i);
			instance = "https://kracr.iiitd.edu.in/OWL2Bench#academicOrganization" + i;
			concept = "https://kracr.iiitd.edu.in/OWL2Bench#AcademicOrganization";
			classAssertion(concept, instance);

		}

		for (int i = 1; i <= nonAcadOrganizationCount; i++) {
			nonAcademicOrganizations.add("nonAcademicOrganization " + i);
			instance = "https://kracr.iiitd.edu.in/OWL2Bench#noncAcademicOrganization" + i;
			concept = "https://kracr.iiitd.edu.in/OWL2Bench#NonAcademicOrganization";
			classAssertion(concept, instance);

		}

		// Map research groups to colleges
		// Map<String, String> researchGroupToCollegeMap = new HashMap<>();
		for (String researchGroup : researchGroups) {
			// Check if the research group should be mapped with a college
			if (random.nextBoolean()) {
				String college = getRandomElement(colleges, random);
				subject = "https://kracr.iiitd.edu.in/OWL2Bench#" + researchGroup;
				predicate = "https://kracr.iiitd.edu.in/OWL2Bench#isPartOf";
				object = "https://kracr.iiitd.edu.in/OWL2Bench#" + college;
				objectPropertyAssertion(subject, predicate, object);
				// researchGroupToCollegeMap.put(researchGroup, college);
			}
		}

		// Map research groups to non-academic organizations
		// Map<String, String> researchGroupToNonAcadOrganizationMap = new HashMap<>();
		for (String researchGroup : researchGroups) {
			// Check if the research group should be mapped with a non-academic organization
			if (random.nextBoolean()) {
				String nonAcadOrganization = getRandomElement(nonAcademicOrganizations, random);
				// objectPropertyAssertion(getOWL2BenchObjectProperty("isPartOf"),getOWL2BenchNamedIndividual(researchGroup),getOWL2BenchNamedIndividual(nonAcadOrganization));
				subject = "https://kracr.iiitd.edu.in/OWL2Bench#" + researchGroup;
				predicate = "https://kracr.iiitd.edu.in/OWL2Bench#isPartOf";
				object = "https://kracr.iiitd.edu.in/OWL2Bench#" + nonAcadOrganization;
				city = getRandomElement(this.cityList, random);
				objectPropertyAssertion(subject, predicate, object);
				objectPropertyAssertion(object, "https://kracr.iiitd.edu.in/genACT#hasLocation", city);
				// researchGroupToNonAcadOrganizationMap.put(researchGroup,
				// nonAcadOrganization);
			}
		}

		// Map colleges to academic organizations
		// Map<String, String> collegeToAcadOrganizationMap = new HashMap<>();
		for (String college : colleges) {
			String acadOrganization = getRandomElement(academicOrganizations, random);
			// objectPropertyAssertion(getOWL2BenchObjectProperty("isPartOf"),getOWL2BenchNamedIndividual(college),getOWL2BenchNamedIndividual(acadOrganization));
			subject = "https://kracr.iiitd.edu.in/OWL2Bench#" + college;
			predicate = "https://kracr.iiitd.edu.in/OWL2Bench#isPartOf";
			object = "https://kracr.iiitd.edu.in/OWL2Bench#" + acadOrganization;
			city = getRandomElement(this.cityList, random);
			objectPropertyAssertion(subject, predicate, object);
			objectPropertyAssertion(object, "https://kracr.iiitd.edu.in/genACT#hasLocation", city);
			// collegeToAcadOrganizationMap.put(college, acadOrganization);
		}

		// code for finding paper instances from papers.csv file and store them in
		// availableConferences hash set
//		String papersFile = "papers.csv"; // path to the papers.csv file
//		String authorsFile = "authors.csv"; // path to the authors.csv file
		// papers = readCSVFiles(papersFile, authorsFile);
		// adjust this part here
		// papers authors to be saved somewhere
		// locations also to be saved: conferences will have different locations.
		// different organizations have a certain location
		// similarly people have a certain affiliation that also changes with some
		// probability.
		// both affiliation and location will be a map.
		// such things can be assigned in this part and reused in different
		// conferences.the map keeps updating
		File directory = new File(this.directoryPath + "/Streams/");
		 if (directory.exists() && directory.isDirectory()) {
	            File[] files = directory.listFiles();

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
		this.conferences = new ConferenceStreams[this.confNum];

		// Generates conference instances
		ExecutorService executor = Executors.newFixedThreadPool(confNum);
		for (int i = 0; i < this.confNum; ++i) {
			final int confIndex = i;
			Runnable task = () -> {
				System.out.println("Started Conference Instance " + confIndex);
				this.conferences[confIndex] = new ConferenceStreams(this, confIndex,this.directoryPath);
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

//	public Map<String, Map<String, Object>> readCSVFiles(String papersFile, String authorsFile) {
//		Map<String, Map<String, Object>> paperDetails = new HashMap<>();
//		// Read the papers.csv file and store the paper titles in a HashMap
//		try (CSVReader reader = new CSVReader(new FileReader(papersFile))) {
//			String[] line;
//			while ((line = reader.readNext()) != null) {
//				String paperId = sanitizeUri(line[0]);
//				String title = sanitizeUri(line[1]);
//
//				// Check if the paper belongs to the desired venue
//				Map<String, Object> paperInfo = new HashMap<>();
//				paperInfo.put("title", title);
//				paperInfo.put("authors", new ArrayList<String>());
//				paperDetails.put(paperId, paperInfo);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		// Read the authors.csv file and store the author names in a list for each paper
//		try (CSVReader reader = new CSVReader(new FileReader(authorsFile))) {
//			String[] line;
//			while ((line = reader.readNext()) != null) {
//				String paperId = sanitizeUri(line[0]);
//				String authorName = sanitizeUri(line[1]);
//
//				// Check if the paper belongs to the desired venue and is present in the
//				// paperDetails map
//				if (paperDetails.containsKey(paperId)) {
//					List<String> authorsList = (List<String>) paperDetails.get(paperId).get("authors");
//					authorsList.add(authorName);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return paperDetails;
//	}
//
//	private String sanitizeUri(String uri) {
//		// Replace spaces with underscores
//		String sanitizedUri = uri.replace(" ", "_");
//
//		// Remove characters that may cause issues in URIs
//		sanitizedUri = sanitizedUri.replaceAll("[^\\w-]", "");
//
//		return sanitizedUri;
//	}

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

//this.conferences = new ConferenceStreams[this.confNum];
////Random random = new Random(seed); // Set a fixed seed for reproducibility
//
//for (int i = 0; i < this.confNum; ++i) {
//    final int confIndex = i;
//
//    long randomOffsetMillis = (long) (random.nextDouble() * sixMonthsInMillis);
//    long conferenceStartMillis = startTimestampMillis + randomOffsetMillis;
//    //System.out.println(conferenceStartMillis);
//    try {
//        Thread.sleep((long) (random.nextDouble() * 2000)); // Introduce a random delay of up to 2 seconds
//    } catch (InterruptedException e) {
//        e.printStackTrace();
//    }
//
//    System.out.println("Started Conference Instance " + confIndex);
//    this.conferences[confIndex] = new ConferenceStreams(this, confIndex, "conf" + confIndex, papers, conferenceStartMillis, this.directoryPath);
//}
//public Map<String, Map<String, Object>> readCSVFiles(String papersFile, String authorsFile)
//{
//	Map<String, Map<String, Object>> paperDetails = new HashMap<>();
//	 // Read the papers.csv file and store the paper titles in a HashMap
//  try (CSVReader reader = new CSVReader(new FileReader(papersFile))) {
//      String[] line;
//      while ((line = reader.readNext()) != null) {
//          String paperId = line[0];
//          String title = line[1];
//          //String venueName = line[2];
//
//          // Check if the paper belongs to the desired venue
//          //if (venueName.equals(venue)) {
//              Map<String, Object> paperInfo = new HashMap<>();
//              paperInfo.put("title", title);
//              paperInfo.put("authors", new ArrayList<String>());
//              paperDetails.put(paperId, paperInfo);
////              System.out.println("here"+ paperDetails);
//         // }
//      }
//  } catch (Exception e) {
//      e.printStackTrace();
//  }
//
//	// Read the authors.csv file and store the author names in a list for each paper
//	try (CSVReader reader = new CSVReader(new FileReader(authorsFile))) {
//		String[] line;
//		while ((line = reader.readNext()) != null) {
//			String paperId = line[0];
//			String authorName = line[1];
//			// Check if the paper belongs to the desired venue and is present in the
//			// paperDetails map
//			if (paperDetails.containsKey(paperId)) {
//				List<String> authorsList = (List<String>) paperDetails.get(paperId).get("authors");
//				authorsList.add(authorName);
//			}
//		}
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
//	return paperDetails;
//}
//