package utils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ModelFactoryBase;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.N3DocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class OWLToN3RDF {

   public static void main(String[] args) {

      // Path to the RDF/XML file
      File inputFile = new File("C:\\GitHub\\owl2streambench\\Ontology\\Academic-Conference-Event-RL.owl");

      // Path to the output N3 file
      File outputFile = new File("C:\\GitHub\\owl2streambench\\Ontology\\Academic-Conference-Event-RL.n3");

      // Load the ontology from the input file
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.loadOntologyFromOntologyDocument(inputFile);
      } catch (OWLOntologyCreationException e) {
         e.printStackTrace();
      }

      // Create a document target for the output file
      FileDocumentTarget target = new FileDocumentTarget(outputFile);

      // Serialize the ontology in N3 format
      N3DocumentFormat n3Format = new N3DocumentFormat();
      try {
         ontology.saveOntology(n3Format, target);
      } catch (OWLOntologyStorageException e) {
         e.printStackTrace();
      }

      System.out.println("Ontology has been converted to N3 format and saved to " + outputFile.getAbsolutePath());
   
      Model model = ModelFactory.createDefaultModel();
      RDFDataMgr.read(model, "C:\\GitHub\\owl2streambench\\Ontology\\Academic-Conference-Event-RL.n3", Lang.N3);

      // Write the RDF model to an output file with .rdf extension
      String rdfFilePath = "C:\\GitHub\\owl2streambench\\Ontology\\Academic-Conference-Event-RL.rdf";
      try (FileOutputStream fos = new FileOutputStream(rdfFilePath)) {
          RDFDataMgr.write(fos, model, RDFLanguages.RDFXML);
      } catch (IOException e) {
          System.err.println("Error writing RDF to file: " + e.getMessage());
      }
      System.out.println("Ontology has been converted to RDF format and saved" + rdfFilePath);
      
   }
}
