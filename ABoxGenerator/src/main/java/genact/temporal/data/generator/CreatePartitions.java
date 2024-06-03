package genact.temporal.data.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.FileManager;

public class CreatePartitions {

    public static void main(String[] args) {
        // Parse command line arguments
        if (args.length < 4) {
            System.err.println("Usage: java -jar CreatePartitions.jar --attribute <attribute_type> --shape <shape_type>");
            return;
        }

        String attribute = null;
        String shape = null;

        for (int i = 0; i < args.length; i += 2) {
            if (args[i].equals("--attribute")) {
                attribute = args[i + 1];
            } else if (args[i].equals("--shape")) {
                shape = args[i + 1];
            }
        }

        if (attribute == null || shape == null) {
            System.err.println("Both --attribute and --shape options are required.");
            return;
        }

        // Directory containing metadata files
        String metadataDirectory = "C:\\GitHub\\temporal-data-generator\\EventData";

        // Create sequences directory if it does not exist
        String sequencesDirectory = "C:\\GitHub\\temporal-data-generator\\SequenceData";
        File sequencesDir = new File(sequencesDirectory);
        if (!sequencesDir.exists()) {
            sequencesDir.mkdirs();
        }

        // Iterate through metadata files
        File folder = new File(metadataDirectory);
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith("metadata.ttl")) {
                String metadataFilePath = file.getAbsolutePath();
                String timestamp = file.getName().split("_")[0];

                // Execute SPARQL query based on attribute and shape
                int queryOutputCount = executeSPARQLQuery(metadataFilePath, attribute, shape);

                // Create new directory in sequences directory
                String outputDirectory = sequencesDirectory + File.separator + timestamp;
                File directory = new File(outputDirectory);
                if (!directory.exists()) {
                    directory.mkdir();
                }

                // Copy corresponding timestamp_tweetid_eventdata.ttl files to the directory
                String eventDataFile = timestamp + "_tweetid_eventdata.ttl";
                File sourceFile = new File(metadataDirectory, eventDataFile);
                File destinationFile = new File(outputDirectory, eventDataFile);
                try {
                    Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.err.println("Error copying file: " + e.getMessage());
                }
            }
        }
    }

    // Method to execute SPARQL query
    private static int executeSPARQLQuery(String metadataFile, String attribute, String shape) {
        // Load RDF model from metadata file
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, metadataFile);

        // Construct SPARQL query
        String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "SELECT ?s WHERE { ?s rdf:type ?p }";

        Query query = QueryFactory.create(queryString);

        // Execute query and count results
        int resultCount = 0;
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                results.nextSolution();
                resultCount++;
            }
        }
        return resultCount;
    }
}
