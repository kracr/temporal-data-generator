package genact.temporal.data.generator;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import java.io.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
public class OrganizationsAndLocations {
	public static void main(String[] args) throws CsvValidationException, OWLOntologyCreationException, OWLOntologyStorageException {
	        String csvFilePath = "C:/GitHub/temporal-data-generator/StaticData/worldcities.csv";
	        String owlFilePath = "C:/GitHub/temporal-data-generator/Ontology/Organization.owl";
	        //Model model = ModelFactory.createDefaultModel();
	        Set<String> citySet = new HashSet<>();
	        // Load the existing ontology
	        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
	        OWLOntology ontology = ontologyManager.createOntology();
	        OWLDataFactory dataFactory = ontologyManager.getOWLDataFactory();
	        // Load the ontology from the file
	        try (InputStream inputStream = new FileInputStream(new File(owlFilePath))) {
	            // Load the ontology from the input stream
	            ontology = ontologyManager.loadOntologyFromOntologyDocument(inputStream);
	            System.out.println("Ontology loaded successfully!");
	        } catch (IOException | OWLOntologyCreationException e) {
	            System.err.println("Error loading ontology: " + e.getMessage());
	            e.printStackTrace();
	        }
	        
	        try (CSVReader csvReader = new CSVReader(new FileReader(csvFilePath))) {
	            String[] header = csvReader.readNext(); // Skip header row
	            String[] line;
	            while ((line = csvReader.readNext()) != null) {
	                if (isValidFirstColumn(line[0]) && isValidFirstColumn(line[3])) {
	                    String city = line[0];
	                    citySet.add(city); // Add city to the citySet
	                } else {
	                    System.err.println("Invalid line format: " + Arrays.toString(line));
	                }
	            }
	            System.out.println("Cities loaded successfully.");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        List<String> cityList = new ArrayList<>(citySet);
	        OWLClass concept;
	        OWLNamedIndividual instance0,instance1,instance2,instance3,instance4 ; 
	        OWLObjectProperty isPartOf = dataFactory.getOWLObjectProperty(IRI.create("https://kracr.iiitd.edu.in/OWL2Bench#isPartOf"));
	        OWLObjectProperty hasLocation = dataFactory.getOWLObjectProperty(IRI.create("https://kracr.iiitd.edu.in/OWL2Bench#hasLocation"));
	        OWLNamedIndividual cityy;
	        String city;
	        for(int i = 1;i<=100;i++)
	    	{
	    		
	    		instance0 = dataFactory.getOWLNamedIndividual(IRI.create("https://kracr.iiitd.edu.in/AcademicConferenceEvent#NAO" + i));
	    		concept = dataFactory.getOWLClass(IRI.create("https://kracr.iiitd.edu.in/AcademicConferenceEvent#NonAcademicOrganization"));
	    		OWLAxiom ax1 = dataFactory.getOWLClassAssertionAxiom(concept, instance0);
	    		ontologyManager.addAxiom(ontology, ax1);
	    		
	    		for(int k = 1;k<=10;k++)
		    	{
	    			city = getRandomCity(cityList);
	    			cityy= dataFactory.getOWLNamedIndividual(IRI.create("https://kracr.iiitd.edu.in/Location#" + city));
	    			//hasLocation= dataFactory.getOWLObjectProperty(IRI.create("https://kracr.iiitd.edu.in/AcademicConferenceEvent#hasLocation"));
	    		      
	    			concept = dataFactory.getOWLClass(IRI.create("https://kracr.iiitd.edu.in/AcademicConferenceEvent#NonAcademicOrganization"));
			    	
		    		instance4 = dataFactory.getOWLNamedIndividual(IRI.create("https://kracr.iiitd.edu.in/AcademicConferenceEvent#NAO" + i+"RG"+k));
		    		concept = dataFactory.getOWLClass(IRI.create("https://kracr.iiitd.edu.in/OWL2Bench#ResearchGroup"));
		    		OWLAxiom ax3 = dataFactory.getOWLClassAssertionAxiom(concept, instance4);
		    		ontologyManager.addAxiom(ontology, ax3);
		    		ontologyManager.addAxiom(ontology, dataFactory.getOWLObjectPropertyAssertionAxiom(isPartOf, instance0, instance4));
		    		ontologyManager.addAxiom(ontology, dataFactory.getOWLObjectPropertyAssertionAxiom(hasLocation, instance4, cityy));
			    	System.out.println("debugi"+i);
		    	}	
	    	}
	    	
	    	for(int i = 1;i<=100;i++)
	    	{
	    		
	    		instance1 = dataFactory.getOWLNamedIndividual(IRI.create("https://kracr.iiitd.edu.in/OWL2Bench#U" + i));
	    		concept = dataFactory.getOWLClass(IRI.create("https://kracr.iiitd.edu.in/OWL2Bench#University"));
	    		OWLAxiom ax1 = dataFactory.getOWLClassAssertionAxiom(concept, instance1);
	    		ontologyManager.addAxiom(ontology, ax1);
		    	for(int j = 1;j<=10;j++)
		    	{
		    		
		    		instance2 = dataFactory.getOWLNamedIndividual(IRI.create("https://kracr.iiitd.edu.in/OWL2Bench#U" + i + "C" +j));
		    		concept = dataFactory.getOWLClass(IRI.create("https://kracr.iiitd.edu.in/OWL2Bench#College"));
		    		OWLAxiom ax2 = dataFactory.getOWLClassAssertionAxiom(concept, instance2);
		    		ontologyManager.addAxiom(ontology, ax2);
		    		ontologyManager.addAxiom(ontology, dataFactory.getOWLObjectPropertyAssertionAxiom(isPartOf, instance1, instance2));
		   
		    		for(int k = 1;k<=10;k++)
			    	{
		    			city = getRandomCity(cityList);
		    			cityy= dataFactory.getOWLNamedIndividual(IRI.create("https://kracr.iiitd.edu.in/Location#" + city));
			    		instance3 = dataFactory.getOWLNamedIndividual(IRI.create("https://kracr.iiitd.edu.in/OWL2Bench#U" + i + "C" +j+"RG"+k));
			    		concept = dataFactory.getOWLClass(IRI.create("https://kracr.iiitd.edu.in/OWL2Bench#ResearchGroup"));
			    		OWLAxiom ax3 = dataFactory.getOWLClassAssertionAxiom(concept, instance3);
			    		ontologyManager.addAxiom(ontology, ax3);
			    		ontologyManager.addAxiom(ontology, dataFactory.getOWLObjectPropertyAssertionAxiom(isPartOf, instance2, instance3));
			    		ontologyManager.addAxiom(ontology, dataFactory.getOWLObjectPropertyAssertionAxiom(hasLocation, instance3, cityy));
			    		System.out.println("debugi2_"+i);
			    	}	    		
		    	}   		
	    	}
	
	        try (OutputStream outputStream = new FileOutputStream(new File(owlFilePath))) {
	            // Save the ontology with the correct file path protocol
	        	ontologyManager.saveOntology(ontology, outputStream);
	            System.out.println("Ontology saved successfully!");
	        } catch (IOException | OWLOntologyStorageException e) {
	            System.err.println("Error saving ontology: " + e.getMessage());
	            e.printStackTrace();
	        }
	    
	}
	       
	private static String getRandomCity(List<String> cityList) {
        // Get a random city from the citySet
        
            int randomIndex = ThreadLocalRandom.current().nextInt(cityList.size());
          //  List<String> cityList = new ArrayList<>(citySet);
            return cityList.get(randomIndex);
        
    }
    private static boolean isValidRow(String[] row) {
    	
        // Adjust the expected number of elements based on your CSV file structure
        return row.length >= 4;
    }
    
    private static boolean isValidFirstColumn(String value) {
        // Customize this method to define your criteria for valid characters
        // For example, check if the value contains only letters and/or digits
        return value.matches("[a-zA-Z0-9]+");
    }
}