package utils;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.QuerySolution;

public class Partition {

	// Your existing class members and methods...

	public void executeAndSaveSparqlQueries(int confNum) {
		  String directoryPath =System.getProperty("user.dir"); // Replace with your actual directory path

		    for (int i = 0; i < confNum; i++) {
		        String rdfFileName = "C:/GitHub/owl2streambench/Streams/conf" + i + ".n3";
		        Model model = loadRdfFromFile(rdfFileName);

		        // Execute and save conference streams query
		        executeAndSaveConferenceStreamsQuery(model,i);

		        // If needed, you can add more queries here using the same model
		        // executeAndSaveUserStreamsQuery(model);
		    }
	}

	private Model loadRdfFromFile(String rdfFile) {
		Model model = ModelFactory.createDefaultModel();

		// Path path = Paths.get(rdfFilePath);
		try (InputStream inputStream = new FileInputStream(rdfFile)) {
			model.read(inputStream, null, "N3"); // Assumes RDF/XML format, adjust as needed
		} catch (IOException e) {
			e.printStackTrace();
		}

		return model;
	}

	private void executeAndSaveConferenceStreamsQuery(Model model, int i) {
		// Step 1: Query for conference instances
		
				
				String conferenceStreamsQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
						+ "PREFIX tweet: https://kracr.iiitd.edu.in/OWL2StreamBench#" + // Replace with your actual
																						// namespace
						"SELECT ?tweetId ?time ?p ?o \n" + "WHERE {\n" + "  ?tweetId tweet:hasDateTimestamp ?time.\n"
						+ "?tweetId tweet:isAbout tweet:conf"+i+".\n" + "  ?tweetId tweet:hasInformation ?infoId.\n" + "  ?infoId ?p ?o.\n" + "}";
				
				try (QueryExecution qexecStreams = QueryExecutionFactory
						.create(QueryFactory.create(conferenceStreamsQuery), model)) {
					Model resultModel = qexecStreams.execConstruct();

					// Create or append to the output file
					String outputFile = System.getProperty("user.dir") + "/Streams/Conference/conf"
							+ i;
					Path outputPath = Paths.get(outputFile);
					try (OutputStream outputStream = Files.newOutputStream(outputPath,
							Files.exists(outputPath) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE)) {
						// System.out.println(outputStream);
						resultModel.write(outputStream, "RDF/XML");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		
	

	private void executeAndSaveUserStreamsQuery(Model model) {
		String userStreamsQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX tweet: <your_tweet_namespace>\n" + // Replace with your actual namespace
				"CONSTRUCT { ?tweetId ?p ?o }\n" + "WHERE {\n" + "  ?tweetId tweet:hasTimeStamp ?time.\n"
				+ "  ?tweetId tweet:hasInformation ?infoId.\n" + "  ?infoId ?p ?o.\n" + "}";

	}

}

// Rest of your class...
