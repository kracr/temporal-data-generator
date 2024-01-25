package genact.temporal.data.generator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class CsvToTtlConverter {
    public static void main(String[] args) throws CsvValidationException {
        String csvFilePath = "C:/GitHub/OWL2StreamBench/CSVFiles/worldcities.csv";
        String ttlFilePath = "C:/GitHub/OWL2StreamBench/staticData/cities.ttl";
        Model model = ModelFactory.createDefaultModel();
        try (CSVReader csvReader = new CSVReader(new FileReader(csvFilePath))) {
            String[] header = csvReader.readNext(); // Skip header row
            String[] line;
            while ((line = csvReader.readNext()) != null) {
            	 if (isValidRow(line)) {
                // Process each line of the CSV file
                String city = line[0];
                if (isValidFirstColumn(city)) {
                String lat = line[1];
                String lng = line[2];
                String country = line[3];

                // Convert data to RDF triples or perform desired processing
                addTriple(model, "https://kracr.iiitd.edu.in/genACT#"+city, "RDF.type", "https://kracr.iiitd.edu.in/genACT#City");
                addTriple(model, "https://kracr.iiitd.edu.in/genACT#"+city, "https://kracr.iiitd.edu.in/genACT#hasLatitude", lat);
                addTriple(model, "https://kracr.iiitd.edu.in/genACT#"+city, "https://kracr.iiitd.edu.in/genACT#hasLongitude", lng);
                addTriple(model, "https://kracr.iiitd.edu.in/genACT#"+city, "https://kracr.iiitd.edu.in/genACT#isPartOf", country);
                addTriple(model, "https://kracr.iiitd.edu.in/genACT#"+country, "RDF.type", "https://kracr.iiitd.edu.in/genACT#Country");
            	}}
            	else {
                    System.err.println("Invalid line format: " + Arrays.toString(line));
                }
            }
            
            System.out.println("Conversion completed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Write the model to the TTL file
        try (FileWriter ttlWriter = new FileWriter(ttlFilePath)) {
            model.write(ttlWriter, "TTL");
            System.out.println("Conversion completed successfully.");
        } catch (IOException e) {
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
    private static void addTriple(Model model, String subject, String predicate, String object) {
        model.add(model.createStatement(
                model.createResource(subject),
                model.createProperty(predicate),
                model.createResource(object)));
    }
}
