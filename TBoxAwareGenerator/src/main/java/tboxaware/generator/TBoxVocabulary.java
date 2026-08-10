package tboxaware.generator;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Loads the real ACE and Tweet TBoxes via the OWL API (ACE ships in OWL functional syntax, Tweet
 * in RDF/XML - the OWL API handles both uniformly, a plain Jena RDF parser cannot read the ACE files)
 * and exposes the declared vocabulary so template definitions can be validated against it: every
 * class/property a template references must actually be declared in one of these TBoxes. Matching is
 * on local name (the fragment after '#'), not full IRI, because the TBox files' own declared
 * ontology namespaces do not all match the namespaces the live pipeline actually asserts data under
 * (see Namespaces).
 */
public class TBoxVocabulary {
    private final Set<String> classLocalNames = new HashSet<>();
    private final Set<String> objectPropertyLocalNames = new HashSet<>();
    private final Set<String> dataPropertyLocalNames = new HashSet<>();

    public static TBoxVocabulary loadFrom(File... owlFiles) throws Exception {
        TBoxVocabulary vocab = new TBoxVocabulary();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        for (File f : owlFiles) {
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
            for (OWLClass c : ontology.getClassesInSignature()) {
                vocab.classLocalNames.add(localName(c.getIRI()));
            }
            for (OWLObjectProperty p : ontology.getObjectPropertiesInSignature()) {
                vocab.objectPropertyLocalNames.add(localName(p.getIRI()));
            }
            for (OWLDataProperty p : ontology.getDataPropertiesInSignature()) {
                vocab.dataPropertyLocalNames.add(localName(p.getIRI()));
            }
        }
        return vocab;
    }

    private static String localName(IRI iri) {
        String s = iri.toString();
        int hash = s.lastIndexOf('#');
        return hash >= 0 ? s.substring(hash + 1) : s.substring(s.lastIndexOf('/') + 1);
    }

    public boolean hasClass(String localName) {
        return classLocalNames.contains(localName);
    }

    public boolean hasObjectProperty(String localName) {
        return objectPropertyLocalNames.contains(localName);
    }

    public boolean hasDataProperty(String localName) {
        return dataPropertyLocalNames.contains(localName);
    }

    public boolean hasProperty(String localName) {
        return hasObjectProperty(localName) || hasDataProperty(localName);
    }

    /** Validates every class/property a template references. Throws with a precise message on the first miss. */
    public void validate(TemplateSpec t) {
        requireClass(t.account.className, t.id, "account.class");
        if (t.subjectResource != null) {
            requireClass(t.subjectResource.className, t.id, "subjectResource.class");
        }
        for (TemplateSpec.ResourceSpec extra : t.extraResources) {
            requireClass(extra.className, t.id, "extraResources[" + extra.id + "].class");
        }
        for (TemplateSpec.TripleSpec triple : t.tweetTriples) {
            requireProperty(triple.property, t.id, "tweetTriples[" + triple.property + "]");
        }
        for (TemplateSpec.TripleSpec triple : t.eventTriples) {
            requireProperty(triple.property, t.id, "eventTriples[" + triple.property + "]");
        }
        if (t.userInvolvement != null) {
            requireProperty(t.userInvolvement.roleProperty, t.id, "userInvolvement.roleProperty");
        }
    }

    private void requireClass(String localName, String templateId, String field) {
        if (!hasClass(localName)) {
            throw new IllegalArgumentException("Template '" + templateId + "': " + field
                    + " references class '" + localName + "' which is not declared in the loaded TBoxes.");
        }
    }

    private void requireProperty(String localName, String templateId, String field) {
        if (!hasProperty(localName)) {
            throw new IllegalArgumentException("Template '" + templateId + "': " + field
                    + " references property '" + localName + "' which is not declared in the loaded TBoxes.");
        }
    }
}
