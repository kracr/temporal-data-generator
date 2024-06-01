package utils;


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
import java.util.Arrays;

public class CreateOntology {
    public static void main(String[] args) throws CsvValidationException, OWLOntologyCreationException, OWLOntologyStorageException {
        String csvFilePath = "C:/GitHub/temporal-data-generator/StaticData/worldcities.csv";
        String owlFilePath = "C:/GitHub/temporal-data-generator/Ontology/Location.owl";
        //Model model = ModelFactory.createDefaultModel();
        
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
                if (isValidRow(line)) {
                    // Process each valid line of the CSV file
                    String city = line[0];
                    if (isValidFirstColumn(city)&&isValidFirstColumn(line[3])) {
                        String lat = line[1];
                        String lng = line[2];
                        String country = line[3];

                    
                        // Create OWL individuals and properties
                        OWLIndividual cityIndividual = dataFactory.getOWLNamedIndividual(IRI.create("https://kracr.iiitd.edu.in/Location#" + city));
                        OWLClass cityClass = dataFactory.getOWLClass(IRI.create("https://kracr.iiitd.edu.in/Location#City"));
                        OWLClass countryClass = dataFactory.getOWLClass(IRI.create("https://kracr.iiitd.edu.in/Location#Country"));
                        OWLNamedIndividual countryIndividual = dataFactory.getOWLNamedIndividual(IRI.create("https://kracr.iiitd.edu.in/Location#" + country));
                       OWLDataProperty hasLatitudeProperty = dataFactory.getOWLDataProperty(IRI.create("https://kracr.iiitd.edu.in/Location#hasLatitude"));
                        OWLDataProperty hasLongitudeProperty = dataFactory.getOWLDataProperty(IRI.create("https://kracr.iiitd.edu.in/Location#hasLongitude"));
                        OWLObjectProperty isPartOfProperty = dataFactory.getOWLObjectProperty(IRI.create("https://kracr.iiitd.edu.in/Location#isPartOf"));
                        
                        // Add assertions to the ontology
                        OWLAxiom ax1 = dataFactory.getOWLClassAssertionAxiom(cityClass, cityIndividual);
                        OWLAxiom ax5 = dataFactory.getOWLClassAssertionAxiom(countryClass, countryIndividual);
                        OWLAxiom ax2 = dataFactory.getOWLDataPropertyAssertionAxiom(hasLatitudeProperty, cityIndividual, dataFactory.getOWLLiteral(lat));
                        OWLAxiom ax3 = dataFactory.getOWLDataPropertyAssertionAxiom(hasLongitudeProperty, cityIndividual, dataFactory.getOWLLiteral(lng));
                        OWLAxiom ax4 = dataFactory.getOWLObjectPropertyAssertionAxiom(isPartOfProperty, cityIndividual, countryIndividual);
                        ontologyManager.addAxiom(ontology, ax1);
                        ontologyManager.addAxiom(ontology, ax2);
                        ontologyManager.addAxiom(ontology, ax3);
                        ontologyManager.addAxiom(ontology, ax4);
                        ontologyManager.addAxiom(ontology, ax5);
                    }
                } else {
                    System.err.println("Invalid line format: " + Arrays.toString(line));
                }
            }
            
            System.out.println("Conversion completed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
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
