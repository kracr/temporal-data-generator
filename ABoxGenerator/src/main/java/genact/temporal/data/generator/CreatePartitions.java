package genact.temporal.data.generator;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ArrayList;
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
		System.out.println("file");

		String type = null;
		String sequence = null;
		if (args[0].equals("--attribute")) {
			sequence="ByAttribute";
			type = args[1];
			System.out.println(type);
		} else if (args[0].equals("--shape")) {
			sequence="ByShape";
			type = args[1];
			System.out.println(type);
		}

		String currentDirectory = System.getProperty("user.dir");
		File currentDirFile = new File(currentDirectory);
		String directoryPath = currentDirFile.getParent();
		// Directory containing metadata files
		File metadataDirectory = new File(directoryPath + "/EventData/");
		String queryDirectory = directoryPath + "/SparqlQueriesForPartition/"+sequence;
		File sequencesDirectory = new File(directoryPath + "/SequenceData/");

		if (sequencesDirectory.exists()) {
			deleteDirectory(sequencesDirectory);
		}
		sequencesDirectory.mkdirs();
		List<String> results;

		File[] files = metadataDirectory.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					// Recursively process subdirectories

					for (File meta : file.listFiles()) {
						if (meta.isFile() && meta.getName().endsWith("metadata.ttl")) {
							//System.out.println(meta);
							String metadataFilePath = meta.getAbsolutePath();
							//System.out.println("metadataFilePath"+metadataFilePath);
							//String timestamp = meta.getName().split("_")[0];
							String eventFileName = meta.getName().replace("_metadata.ttl", "_eventdata.ttl");
							String queryFilePath=queryDirectory+"/" + type + ".txt";
							// Execute SPARQL query based on attribute and shape
							results = executeSPARQLQuery(queryFilePath, metadataFilePath, type);
							File eventFile = new File(file.getAbsolutePath(),eventFileName);
							//System.out.println("eventFile "+eventFile);
							for (String result : results) {
								// Create new directory in sequences directory
								String outputDirectory = sequencesDirectory + "/"+"conf0";
								File directory = new File(outputDirectory);
								if (!directory.exists()) {
									directory.mkdirs();
								}
								try {
									//System.out.println("directory "+directory);
//									String eventDataFile = baseFileName + "_eventdata.ttl";
//									File sourceFile = new File(metadataDirectory, eventDataFile);
									File destinationFile = new File(directory.getAbsolutePath(), eventFileName);
									Files.copy(eventFile.toPath(), destinationFile.toPath(),
											StandardCopyOption.REPLACE_EXISTING);
								} catch (IOException e) {
									System.err.println("Error copying file: " + e.getMessage());
								}
							
							}
						}

					}
				}
			}
		}
	}
	private static String readQueryFromFile(String queryFilePath) {
        StringBuilder queryString = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(queryFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                queryString.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error reading query file: " + e.getMessage());
        }
        return queryString.toString();
    }
	public static List<String> executeSPARQLQuery(String queryDirectory, String metadataFile, String type) {
		// Load RDF model from metadata file
		Model model = ModelFactory.createDefaultModel();
		FileManager.get().readModel(model, metadataFile);

		// Construct SPARQL query
//		String queryString = "SELECT ?s WHERE { ?s ?p ?o }";
		// Read SPARQL query from file
        String queryString = readQueryFromFile(queryDirectory);
		Query query = QueryFactory.create(queryString);

		// Execute query and store results in a list
		 List<String> results = new ArrayList<String>();
	        QueryExecution qexec = null;
	        try {
	            //Query query = QueryFactory.create(queryString);
	            qexec = QueryExecutionFactory.create(query, model);
	            ResultSet resultSet = qexec.execSelect();
	            while (resultSet.hasNext()) {
	                QuerySolution solution = resultSet.nextSolution();
	                String name = solution.get("name").toString();
	                results.add(name);
	            }
	        } finally {
	            if (qexec != null) {
	                qexec.close();
	            }
	        }

		return results;
	}

	public static void deleteDirectory(File directory) {
		if (directory.isDirectory()) {
			File[] files = directory.listFiles();
			if (files != null) {
				for (File file : files) {
					deleteDirectory(file);
				}
			}
		}
		directory.delete();
	}
}
