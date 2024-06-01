package utils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ResearchGroups {
    public static void main(String[] args) {
        // Load the ontology
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        File file = new File("C:\\GitHub\\temporal-data-generator\\Ontology\\Organization.owl");
        OWLOntology ontology;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(file);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            return;
        }

        // Get instances of ResearchGroup class directly
        Set<OWLNamedIndividual> researchGroups = getInstancesOfResearchGroup(ontology);

        // Save instances to CSV
        saveInstancesToCSV(researchGroups, "research_groups.csv");
    }

    private static Set<OWLNamedIndividual> getInstancesOfResearchGroup(OWLOntology ontology) {
        Set<OWLNamedIndividual> researchGroups = new HashSet<>();
        OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        OWLClass researchGroupClass = getResearchGroupClass(ontology);
        if (researchGroupClass != null) {
            for (OWLClassAssertionAxiom classAssertion : ontology.getAxioms(AxiomType.CLASS_ASSERTION)) {
                if (classAssertion.getClassExpression().equals(researchGroupClass)) {
                    researchGroups.add(classAssertion.getIndividual().asOWLNamedIndividual());
                }
            }
        }
        return researchGroups;
    }

    private static OWLClass getResearchGroupClass(OWLOntology ontology) {
        OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        IRI researchGroupIRI = IRI.create("https://kracr.iiitd.edu.in/OWL2Bench#ResearchGroup");
        return factory.getOWLClass(researchGroupIRI);
    }

    private static void saveInstancesToCSV(Set<OWLNamedIndividual> instances, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            for (OWLNamedIndividual instance : instances) {
                writer.append(instance.getIRI().toString()).append('\n');
            }
            System.out.println("Instances saved to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}