package genact.temporal.data.generator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class TemplateProcessor {
    private Map<String, List<TemplateEntry>> templatesByCategory;

    public TemplateProcessor(List<TemplateEntry> Templates, Map<String, List<Triple>> Mappings) {
        templatesByCategory = new HashMap<>();
        processCategory("BeforeConference", Templates, Mappings);
        processCategory("DuringConference", Templates, Mappings);
        processCategory("AfterConference", Templates, Mappings);
    }

    private void processCategory(String category, List<TemplateEntry> Templates, Map<String, List<Triple>> Mappings) {
        List<TemplateEntry> categoryTemplates = Templates.stream()
                .filter(template -> template.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
        templatesByCategory.put(category, categoryTemplates);

        List<TemplateEntry> sequentialTemplates = categoryTemplates.stream()
                .filter(template -> template.getOrder().equalsIgnoreCase("Sequential"))
                .sorted(Comparator.comparingInt(TemplateEntry::getSequence))
                .collect(Collectors.toList());

        List<TemplateEntry> randomTemplates = categoryTemplates.stream()
                .filter(template -> template.getOrder().equalsIgnoreCase("Random"))
                .collect(Collectors.toList());

        processSequentialTemplates(sequentialTemplates, Mappings);
        processRandomTemplates(randomTemplates, Mappings);
    }

    private void processSequentialTemplates(List<TemplateEntry> templates, Map<String, List<Triple>> mappings) {
        for (TemplateEntry template : templates) {
            List<String> triples = template.getTriples();
            List<Triple> mappedTriples = getTriplesForTemplate(triples, mappings);
            // Instantiate the template with the fetched triples and process
            System.out.println("Processing Sequential Template: " + template.getTemplateNumber());
            System.out.println("Triples: " + mappedTriples);
            instantiate(mappedTriples);
            //call for instantiation here
        }
    }

	private void processRandomTemplates(List<TemplateEntry> templates, Map<String, List<Triple>> mappings) {
        Random rand = new Random();
        for (TemplateEntry template : templates) {
            if (rand.nextDouble() <= template.getFrequency()) {
                List<String> triples = template.getTriples();
                List<Triple> mappedTriples = getTriplesForTemplate(triples, mappings);
                // Instantiate the template with the fetched triples and process
                System.out.println("Processing Random Template: " + template.getTemplateNumber());
                System.out.println("Triples: " + mappedTriples);
                
              //call for instantiation here
                instantiate(mappedTriples);
            }
        }
    }

    private List<Triple> getTriplesForTemplate(List<String> templateTriples, Map<String, List<Triple>> mappings) {
        List<Triple> triples = new ArrayList<>();
        for (String triple : templateTriples) {
            List<Triple> mappedTriples = mappings.getOrDefault(triple, List.of());
            triples.addAll(mappedTriples); // Add all Triples to the result list
        }
        return triples;
    }
    private void instantiate(List<Triple> mappedTriples) {
		// TODO Auto-generated method stub
		
	}

}
