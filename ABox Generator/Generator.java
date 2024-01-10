package OWL2StreamBench.ABox.Generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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

import au.com.bytecode.opencsv.CSVReader;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.formats.*;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
//tracks, authorids and affiliations are assigned randomly 
//currently we have data for 27 conferences starting from the year 2000 to 2022
public class Generator {
    String requiredOtologyFormat;
    String requiredABoxFormat;
    int acceptedPaperCount_min;
    int acceptedPaperCount_max;
	int otherPeopleInvolved_min;
	int otherPeopleInvolved_max;
	int organizationCount_min;
	int acadOrganizationCount_min;
	int	acadOrganizationCount_max;
	int acadOrganizationCount;
	int	nonAcadOrganizationCount_min;
	int	nonAcadOrganizationCount_max;
	int nonAcadOrganizationCount;
	int	researchGroupCount_min;
	int	researchGroupCount_max;
	int researchGroupCount;
	int	collegeCount_min;
	int	collegeCount_max;
	int collegeCount;
	Map<String, Map<String, Object>> papers;
    String[] TOKEN_ConferenceEventTrack= new String[]{"applicationsTrack", "demoTrack", "doctoralConsortiumTrack", "posterTrack", "researchTrack", "resourcesTrack", "tutorialTrack", "workshopTrack"};
    String[] TOKEN_EventMode = new String[]{"online", "offline", "hybrid"};
    String[] TOKEN_ChairRole = new String[]{"generalChair", "localChair", "researchTrackChair", "resourcesTrackChair", "trackChair", "tutorialTrackChair", "workshopTrackChair"};
    //String[] TOKEN_ConferenceList=new String[] {"MOBICOM", "ASPLOS", "ISCA", "AAAI", "ISWC", "ESWC", "IJCAI", "CIKM", "ACL", "KDD", "ECCV", "ICCV", "CVPR", "NeurIPS", "WWW", "UbiComp", "SIGIR", "SIGCOMM", "IJCAR", "ICDE", "AAMAS", "ACMMM", "CAV", "CRYPTO", "HPCA", "FOGA", "ICDM"};
    //publish streams on a url
    String[] TOKEN_Domain=new String[] {"ai", "ml", "nlp", "aiForSocialGood", "artificialIntelligence", "bigData", "blockchain",
    		"cloudComputing", "computerVision", "dataScience", "deepLearning","internetOfThings", "knowledgeGraph", "linkedData","machineLearning","ontology",
    		"naturalLanguageProcessing","quantumComputing", "semanticWeb"};
    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    PrefixManager pm = new DefaultPrefixManager("https://kracr.iiitd.edu.in/OWL2StreamBench");
    IRI ontologyIRI = IRI.create(pm.getDefaultPrefix());
    OWLOntology o = createOWLOntology(pm);
   // OWLOntology o2 = createOWLOntology(pm);
    File file2;
    OWLDataFactory factory = manager.getOWLDataFactory();
    OWLOntology ontology1;
    int confNum;
    String profile;
    String confInstance;
    String directoryPath;
    Conference[] conferences;
    Random random = new Random();
    HashMap<Integer,String> map1 = new HashMap<>();
    HashMap<Integer,String> map2 = new HashMap<>();
    HashMap<Integer,String> map3 = new HashMap<>();


    public Generator() {
    }

    public static void main(String[] args) {
    	//input 
        int confNum=5;
        int seed=1;	      
        String profile="EL";  
        String directoryPath;
        if(args.length==4)
        {
        	confNum=Integer.parseInt(args[0]);
            profile= args[1];
            directoryPath=args[2];
            seed=Integer.parseInt(args[3]);
        }
        else if(args.length==3)
        {
        	confNum=Integer.parseInt(args[0]);
            profile= args[1];
            directoryPath=args[2];
            System.out.println("Default Seed value is 1");
        }
        else
        {
     
                System.out.println("Please give arguments in the following order: No. of conferences (int), OWL 2 Profile (EL/QL/RL/DL), DirectoryPath, Seed (optional) ");
                System.out.println("For example: 1 DL C:/Users/OWL2StreamBench 1 or 1 DL C:/Users/OWL2StreamBench(where default seed value is 1");
        }
        //System.out.println(profile);
        new Generator().start(confNum, seed, profile);
    }

//TBox
    
    public void start(int confNum, int seed, String profile) {
    	//System.out.println(profile);
    	this.profile=profile;
    	System.out.println(".." + profile);
    //	use TBox corresponding to the user input
    	 if(profile.matches("EL")){
    		 System.out.println("Loading TBox Academic-Conference-Event-OWL2EL.owl");
    		 file2 = new File(this.directoryPath+"/Ontology/Academic-Conference-Event-OWL2EL.owl");
    	 }
    	 else if (profile.matches("QL")) {

    		 System.out.println("Loading TBox Academic-Conference-Event-OWL2QL.owl");
    		 file2 = new File(this.directoryPath+"/Ontology/Academic-Conference-Event-OWL2QL.owl");
    	 }
    	 else if (profile.matches("RL")) {

    		 System.out.println("Loading TBox Academic-Conference-Event-OWL2RL.owl");
    		 file2 = new File(this.directoryPath+"/Ontology/Academic-Conference-Event-OWL2RL.owl");
    	 }
    	 else if (profile.matches("DL"))
    	 {
    		 System.out.println("Loading TBox Academic-Conference-Event-OWL2DL.owl");
    		 file2 = new File(this.directoryPath+"/Ontology/Academic-Conference-Event-OWL2DL.owl");
    	 }
    	//OWLOntology o = loadOWLOntology(file2);
    	System.out.println("here");
    	//load TBox and also load the background data depending on the number of conferences user wanted
        //OWLDocumentFormat format = manager.getOntologyFormat(o);
        //System.out.println("Academic-Conference-Event Ontology format is " + format);
        this.confNum = confNum;
        random.setSeed((long) seed);

        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream("config.properties");
            // load range of different parameters from config.properties file
            prop.load(input);
            this.requiredABoxFormat=prop.getProperty("requiredABoxFormat");
            this.acceptedPaperCount_min=Integer.parseInt(prop.getProperty("acceptedPaperCount_min"));
            this.acceptedPaperCount_max=Integer.parseInt(prop.getProperty("acceptedPaperCount_max"));
            this.acceptedPaperCount_min=Integer.parseInt(prop.getProperty("acceptedPaperCount_min"));
            this.acceptedPaperCount_max=Integer.parseInt(prop.getProperty("acceptedPaperCount_max"));
            this.otherPeopleInvolved_min=Integer.parseInt(prop.getProperty("otherPeopleInvolved_min"));
            this.otherPeopleInvolved_max=Integer.parseInt(prop.getProperty("otherPeopleInvolved_max"));            
            this.acadOrganizationCount_min=Integer.parseInt(prop.getProperty("acadOrganizationCount_min"));
            this.acadOrganizationCount_max=Integer.parseInt(prop.getProperty("acadOrganizationCount_max"));
            this.researchGroupCount_min=Integer.parseInt(prop.getProperty("researchGroupCount_min"));
            this.researchGroupCount_max=Integer.parseInt(prop.getProperty("researchGroupCount_max"));
            this.collegeCount_min=Integer.parseInt(prop.getProperty("collegeCount_min"));
            this.collegeCount_max=Integer.parseInt(prop.getProperty("collegeCount_max"));
            this.nonAcadOrganizationCount_min=Integer.parseInt(prop.getProperty("nonAcadOrganizationCount_min"));
            this.nonAcadOrganizationCount_max=Integer.parseInt(prop.getProperty("nonAcadOrganizationCount_min"));	
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
        this.generate();
    }

    private void generate() {
    	//code for creating organization mappings 
    	this.researchGroupCount= random.nextInt(researchGroupCount_max - researchGroupCount_min + 1) + researchGroupCount_min; 
    	this.collegeCount= random.nextInt(collegeCount_max - collegeCount_min + 1) + collegeCount_min; 
    	this.acadOrganizationCount= random.nextInt(acadOrganizationCount_max - acadOrganizationCount_min + 1) + acadOrganizationCount_min; 
    	this.nonAcadOrganizationCount= random.nextInt(nonAcadOrganizationCount_max - nonAcadOrganizationCount_min + 1) + nonAcadOrganizationCount_min; 
        
    	// Create lists to store research groups, colleges, academic organizations, and non-academic organizations
        List<String> researchGroups = new ArrayList<>();
        List<String> colleges = new ArrayList<>();
        List<String> academicOrganizations = new ArrayList<>();
        List<String> nonAcademicOrganizations = new ArrayList<>();
        String instance;
        // Generate random names for research groups, colleges, academic organizations, and non-academic organizations
        for (int i = 1; i <= researchGroupCount; i++) {
            //researchGroups.add("researchGroup" + i);
        	instance="researchGroup" + i;
            classAssertion(getOWL2BenchClass("ResearchGroup"),getOWL2BenchNamedIndividual(instance) );
        }

        for (int i = 1; i <= collegeCount; i++) {
            //colleges.add("college" + i);
            instance="college" + i;
            classAssertion(getOWL2BenchClass("College"),getOWL2BenchNamedIndividual(instance));
        }

        for (int i = 1; i <= acadOrganizationCount; i++) {
            //academicOrganizations.add("academicOrganization" + i);
            instance="academicOrganization" + i;
            classAssertion(getOWL2BenchClass("AcademicOrganization"),getOWL2BenchNamedIndividual(instance));
        }

        for (int i = 1; i <= nonAcadOrganizationCount; i++) {
            //nonAcademicOrganizations.add("nonAcademicOrganization " + i);
            instance="noncAcademicOrganization" + i;
            classAssertion(getOWL2BenchClass("NonAcademicOrganization"),getOWL2BenchNamedIndividual(instance));
        }

        // Map research groups to colleges
        //Map<String, String> researchGroupToCollegeMap = new HashMap<>();
        for (String researchGroup : researchGroups) {
            // Check if the research group should be mapped with a college
            if (random.nextBoolean()) {
                String college = getRandomElement(colleges, random);
                objectPropertyAssertion(getOWL2BenchObjectProperty("isPartOf"),getOWL2BenchNamedIndividual(researchGroup),getOWL2BenchNamedIndividual(college));

               

                //researchGroupToCollegeMap.put(researchGroup, college);
            } 
//            else {
//                researchGroupToCollegeMap.put(researchGroup, null); // Set college attribute as null
//            }
        }

        // Map research groups to non-academic organizations
       // Map<String, String> researchGroupToNonAcadOrganizationMap = new HashMap<>();
        for (String researchGroup : researchGroups) {
            // Check if the research group should be mapped with a non-academic organization
            if (random.nextBoolean()) {
                String nonAcadOrganization = getRandomElement(nonAcademicOrganizations, random);
                objectPropertyAssertion(getOWL2BenchObjectProperty("isPartOf"),getOWL2BenchNamedIndividual(researchGroup),getOWL2BenchNamedIndividual(nonAcadOrganization));
                
                //researchGroupToNonAcadOrganizationMap.put(researchGroup, nonAcadOrganization);
            }
        }

        // Map colleges to academic organizations
        //Map<String, String> collegeToAcadOrganizationMap = new HashMap<>();
        for (String college : colleges) {
            String acadOrganization = getRandomElement(academicOrganizations, random);
            objectPropertyAssertion(getOWL2BenchObjectProperty("isPartOf"),getOWL2BenchNamedIndividual(college),getOWL2BenchNamedIndividual(acadOrganization));
            
            //collegeToAcadOrganizationMap.put(college, acadOrganization);
        }

    	//code for finding paperinstances from papers.csv file and store them in availableConferences hash set
		String papersFile = "papers.csv"; // path to the papers.csv file
		String authorsFile = "authors.csv"; // path to the authors.csv file
		papers = readCSVFiles(papersFile, authorsFile);

        this.conferences = new Conference[this.confNum];

        // Generates conference instances 
        ExecutorService executor = Executors.newFixedThreadPool(confNum);
        for (int i = 0; i < this.confNum; ++i) {
            final int confIndex = i;
            Runnable task = () -> {
            	// Introduce a random delay of up to 1 second
                try {
                    Thread.sleep(random.nextInt(2000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            	this.conferences[confIndex] = new Conference(this, confIndex, "conf"+confIndex, papers);
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
        
        
        OWLXMLDocumentFormat owx = new OWLXMLDocumentFormat();
        TurtleDocumentFormat ttl=new TurtleDocumentFormat();
        //RDfXMLSyntaxDocumentFormat rdf= new  RDfXMLDocumentFormat();
        FunctionalSyntaxDocumentFormat ofn = new FunctionalSyntaxDocumentFormat();
        ManchesterSyntaxDocumentFormat omn = new ManchesterSyntaxDocumentFormat();
        RDFXMLDocumentFormat rdf = new RDFXMLDocumentFormat();
        

        try  {
            File file = new File(System.getProperty("user.dir")+ "/" + "OWL2"+this.profile + "-" + confNum + ".owl");
try {
            if (!file.exists()) {
                
            	file.createNewFile();
            } }catch (IOException e) {
   		System.out.println("Exception Occurred:");
	        e.printStackTrace();
	  }
			
            
            System.out.println("Total Axiom Count ="+ o.getAxiomCount());
            System.out.println("Total Logical Axiom Count ="+ o.getLogicalAxiomCount());
            //System.out.println("Total Axiom Count in triple file="+ o2.getAxiomCount());
            //System.out.println("Total Logical Axiom Count in triple file="+ o2.getLogicalAxiomCount());
            if(this.requiredABoxFormat.matches("owx")) {
            	manager.saveOntology(o,owx,IRI.create(file.toURI()));
            	//manager.saveOntology(o2,owx,IRI.create(file.toURI()));
            	System.out.println("Saved Ontology Format is OWL/XML");
            }
            else if(this.requiredABoxFormat.matches("ttl")) {
            	manager.saveOntology(o,ttl,IRI.create(file.toURI()));
            	//manager.saveOntology(o2,owx,IRI.create(file.toURI()));
            	System.out.println("Saved Ontology Format is Turtle");
            }
            else if(this.requiredABoxFormat.matches("ofn")) {
            	manager.saveOntology(o,ofn,IRI.create(file.toURI()));
            	//manager.saveOntology(o2,owx,IRI.create(file.toURI()));
            	System.out.println("Saved Ontology Format is OWL Functional");
            }
            else if(this.requiredABoxFormat.matches("omn")) {
            	manager.saveOntology(o,omn,IRI.create(file.toURI()));
            	//manager.saveOntology(o2,owx,IRI.create(file.toURI()));
            	System.out.println("Saved Ontology Format is Manchester");
            }
            else if(this.requiredABoxFormat.matches("rdf")) {
            	
            	 try  {
                     File file2 = new File(System.getProperty("user.dir")+ "/" + "OWL2"+this.profile + "-" + confNum + ".rdf");
         manager.saveOntology(o, rdf, IRI.create(file2.toURI()));
         System.out.println("Ontology saved as RDF: " + file2);
            } catch (OWLOntologyStorageException e) {
                e.printStackTrace();
            }
            }
            else {
            OWLDocumentFormat format = manager.getOntologyFormat(o);
            manager.saveOntology(o,format,IRI.create(file.toURI()));
            //manager.saveOntology(o2,owx,IRI.create(file.toURI()));
            String f=format.toString();
            System.out.println("Saved Ontology Format is "+ f);
            }
          //  System.out.println(System.getProperty("user.dir"));
            
            String savedOWLFile=System.getProperty("user.dir")+ "/" + "OWL2"+this.profile + "-" + confNum + ".owl";
            //String savedRDFFile=System.getProperty("user.dir")+ "/" + "OWL2"+this.profile + "-" + confNum + ".rdf";
            //String savedN3File=System.getProperty("user.dir")+ "/" + "OWL2"+this.profile + "-" + confNum + ".n3";
            //OWLDocumentFormat format = manager.getOntologyFormat(o);
            //System.out.println("Ontology Format="+ format);
           // turtleConverter(savedOWLFile,savedRDFFile,savedN3File);
            System.out.println("Finished Writing to file "+ savedOWLFile);
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        }
        
    }
    private static <T> T getRandomElement(List<T> list, Random random) {
        int index = random.nextInt(list.size());
        return list.get(index);}
    
    public Map<String, Map<String, Object>> readCSVFiles(String papersFile, String authorsFile)
    {
    	Map<String, Map<String, Object>> paperDetails = new HashMap<>();
    	 // Read the papers.csv file and store the paper titles in a HashMap
        try (CSVReader reader = new CSVReader(new FileReader(papersFile))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                String paperId = line[0];
                String title = line[1];
                //String venueName = line[2];

                // Check if the paper belongs to the desired venue
                //if (venueName.equals(venue)) {
                    Map<String, Object> paperInfo = new HashMap<>();
                    paperInfo.put("title", title);
                    paperInfo.put("authors", new ArrayList<String>());
                    paperDetails.put(paperId, paperInfo);
//                    System.out.println("here"+ paperDetails);
               // }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

		// Read the authors.csv file and store the author names in a list for each paper
		try (CSVReader reader = new CSVReader(new FileReader(authorsFile))) {
			String[] line;
			while ((line = reader.readNext()) != null) {
				String paperId = line[0];
				String authorName = line[1];

				// Check if the paper belongs to the desired venue and is present in the
				// paperDetails map
				if (paperDetails.containsKey(paperId)) {
					List<String> authorsList = (List<String>) paperDetails.get(paperId).get("authors");
					authorsList.add(authorName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return paperDetails;
    }
    

public void turtleConverter(String inputFile, String outputRdfFile, String outputN3File) {
    // Load the Turtle file into a Jena model
    Model model = ModelFactory.createDefaultModel();
    RDFDataMgr.read(model, inputFile, Lang.TURTLE);

    // Save the model as RDF
    File rdfFile = new File(outputRdfFile);
    if (!rdfFile.exists()) {
        try {
            rdfFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
    try (FileOutputStream rdfFos = new FileOutputStream(rdfFile)) {
        RDFDataMgr.write(rdfFos, model, RDFLanguages.RDFXML);
        System.out.println("Conversion to RDF complete. Saved as: " + rdfFile.getAbsolutePath());
    } catch (IOException e) {
        e.printStackTrace();
    }

    // Save the model as N3
    File n3File = new File(outputN3File);
    if (!n3File.exists()) {
        try {
            n3File.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
    try (FileOutputStream n3Fos = new FileOutputStream(n3File)) {
        RDFDataMgr.write(n3Fos, model, RDFLanguages.TURTLE);
        System.out.println("Conversion to N3 complete. Saved as: " + n3File.getAbsolutePath());
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    public OWLOntology createOWLOntology(PrefixManager pm) {
        if (pm.getDefaultPrefix() == null) {
            throw new IllegalStateException("Default ontology prefix must not be null.");
        }
        
        try  {
            ontology1= manager.createOntology(IRI.create(pm.getDefaultPrefix()));
            // use the inputStream to read a file
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        // Cast to a mutable ontology to pass OWLApi's strange checks
        return ontology1;
    }

    public OWLOntology loadOWLOntology(File file2)
    {
        try{
            o = manager.loadOntologyFromOntologyDocument(file2);
        }catch (OWLOntologyCreationException e)
        {
            e.printStackTrace();
        }

        return o;

    }
    public OWLIndividual getNamedIndividual(String name) {

        OWLIndividual individual = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + name));
        return individual;
    }
    public OWLIndividual getOWL2BenchNamedIndividual(String name) {

        OWLIndividual individual = factory.getOWLNamedIndividual(IRI.create("https://kracr.iiitd.edu.in/OWL2Bench#" + name));
        return individual;
    }
    public OWLObjectProperty getObjectProperty(String name) {

        OWLObjectProperty property = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#" + name));
        return property;

    }
    public OWLObjectProperty getOWL2BenchObjectProperty(String name) {

        OWLObjectProperty property = factory.getOWLObjectProperty(IRI.create("https://kracr.iiitd.edu.in/OWL2Bench#" + name));
        return property;

    }
    public OWLObjectProperty getOWL2BenchDataProperty(String name) {

        OWLObjectProperty property = factory.getOWLObjectProperty(IRI.create("https://kracr.iiitd.edu.in/OWL2Bench#" + name));
        return property;

    }
    public OWLDataProperty getDataProperty(String name) {

        OWLDataProperty property = factory.getOWLDataProperty(IRI.create(ontologyIRI + "#" + name));
        return property;

    }

    public OWLClass getClass(String name) {

        OWLClass classname = factory.getOWLClass(IRI.create(ontologyIRI + "#" + name));
        return classname;

    }
    public OWLClass getOWL2BenchClass(String name) {

        OWLClass classname = factory.getOWLClass(IRI.create("https://kracr.iiitd.edu.in/OWL2Bench#" + name));
        return classname;

    }
    public OWLLiteral getLiteral(String name) {

        OWLLiteral literal = factory.getOWLLiteral(name);
        return literal;

    }

    public void addAxiomToOntology(OWLAxiom axiom) {
//       System.out.println(axiom);
       o.getOWLOntologyManager().addAxiom(o, axiom);
       //we keep appending all the assertion axioms to TBox file.
    }

    public void classAssertion(OWLClassExpression classExpression, OWLIndividual individual) {

        addAxiomToOntology(factory.getOWLClassAssertionAxiom(classExpression, individual));
    }

    public void dataPropertyAssertion(OWLDataProperty property, OWLIndividual subject, OWLLiteral object) {
        addAxiomToOntology(factory.getOWLDataPropertyAssertionAxiom(property,subject,  object));
    }

    public void objectPropertyAssertion(OWLObjectProperty property, OWLIndividual subject, OWLIndividual object) {
        addAxiomToOntology(factory.getOWLObjectPropertyAssertionAxiom( property, subject,  object));
    }

}



