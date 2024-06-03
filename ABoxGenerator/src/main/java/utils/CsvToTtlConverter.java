package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

// This file was used to convert actual location data from csv files to location.ttl file,
// so that it can be used as static background knowledge. Every city has a latitude,
// longitude, and is part of some country. This file can be used in conjunction with Location.owl file.
public class CsvToTtlConverter {
    public static void main(String[] args) throws CsvValidationException {
        String csvFilePath = "C:/GitHub/temporal-data-generator/CSVFiles/worldcities.csv";
        String ttlFilePath = "C:/GitHub/temporal-data-generator/Location.owl";
        String cityCsvFilePath = "C:/GitHub/temporal-data-generator/location.csv";

        Model model = ModelFactory.createDefaultModel();
        try (CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
             CSVWriter csvWriter = new CSVWriter(new FileWriter(cityCsvFilePath, true))) { // Open in append mode

            String[] header = csvReader.readNext(); // Skip header row
            String[] line;

            // Write header to city.csv if it's empty
          //  csvWriter.writeNext(new String[]{"City", "Latitude", "Longitude", "Country"});

            while ((line = csvReader.readNext()) != null) {
                if (isValidRow(line)) {
                    // Process each line of the CSV file
                    String city = line[0];
                    if (isValidFirstColumn(city)) {
                        String lat = line[1];
                        String lng = line[2];
                        String country = line[3];

                        // Convert data to RDF triples or perform desired processing
                        addTriple(model, "https://kracr.iiitd.edu.in/Location#" + city, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "https://kracr.iiitd.edu.in/Location#City");
                        addTriple(model, "https://kracr.iiitd.edu.in/Location#" + city, "https://kracr.iiitd.edu.in/Location#hasLatitude", lat);
                        addTriple(model, "https://kracr.iiitd.edu.in/Location#" + city, "https://kracr.iiitd.edu.in/Location#hasLongitude", lng);
                        addTriple(model, "https://kracr.iiitd.edu.in/Location#" + city, "https://kracr.iiitd.edu.in/Location#isPartOf", country);
                        addTriple(model, "https://kracr.iiitd.edu.in/Location#" + country, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "https://kracr.iiitd.edu.in/Location#Country");

                        // Write city data to city.csv
                        writeCityToCsv(csvWriter,  "https://kracr.iiitd.edu.in/Location#" + city, lat, lng, "https://kracr.iiitd.edu.in/Location#" + country);
                    }
                } else {
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
            System.out.println("TTL file writing completed successfully.");
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

    private static void writeCityToCsv(CSVWriter csvWriter, String city, String lat, String lng, String country) {
        String[] cityData = {city, lat, lng, country};
        csvWriter.writeNext(cityData);
    }
}