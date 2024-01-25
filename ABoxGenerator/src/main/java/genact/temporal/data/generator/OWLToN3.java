package genact.temporal.data.generator;

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

public class OWLToN3 {

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
   }
}
