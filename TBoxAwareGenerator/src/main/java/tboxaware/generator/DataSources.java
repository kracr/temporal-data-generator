package tboxaware.generator;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Reads the same underlying CSV reference data ABoxGenerator's pipeline uses
 * (CSVFiles/authors.csv, papers.csv, location.csv, research_groups.csv), with its own small
 * independent reader - no dependency on ABoxGenerator's code, per the isolation requirement.
 */
public class DataSources {
    public final List<String> authorNames = new ArrayList<>();
    public final List<String> paperTitles = new ArrayList<>();
    public final List<String> organizationIris = new ArrayList<>();     // full IRIs, OWL2Bench# namespace
    public final List<String> cityIris = new ArrayList<>();             // full IRIs, Location# namespace

    private static final String[] EVENT_MODES = {"offline", "online", "hybrid"};
    private static final String[] TRACKS = {
            "researchTrack", "resourcesTrack", "demoTrack", "posterTrack", "workshopTrack", "tutorialTrack"
    };
    private static final String[] DOMAINS = {
            "artificialIntelligence", "machineLearning", "deepLearning", "computerVision",
            "naturalLanguageProcessing", "dataScience", "bigData", "knowledgeGraph", "semanticWeb"
    };
    private static final String[] DESIGNATIONS = {
            "Professor", "Associate Professor", "PhD Student", "Postdoctoral Researcher", "Research Scientist"
    };

    public static DataSources loadFrom(String csvDir) throws Exception {
        DataSources ds = new DataSources();
        ds.authorNames.addAll(readPlainLines(csvDir + "/authors.csv"));
        ds.paperTitles.addAll(readPlainLines(csvDir + "/papers.csv"));
        ds.organizationIris.addAll(readPlainLines(csvDir + "/research_groups.csv"));

        try (CSVReader reader = new CSVReader(new FileReader(csvDir + "/location.csv"))) {
            List<String[]> rows = reader.readAll();
            for (int i = 1; i < rows.size(); i++) { // skip header: City,Latitude,Longitude,Country
                if (rows.get(i).length > 0 && !rows.get(i)[0].isBlank()) {
                    ds.cityIris.add(rows.get(i)[0]);
                }
            }
        }
        return ds;
    }

    private static List<String> readPlainLines(String path) throws Exception {
        List<String> lines = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            for (String[] row : reader.readAll()) {
                if (row.length > 0 && !row[0].isBlank()) {
                    lines.add(row[0]);
                }
            }
        }
        return lines;
    }

    public String randomOrganizationIri(Random r) {
        return organizationIris.get(r.nextInt(organizationIris.size()));
    }

    public String randomCityIri(Random r) {
        return cityIris.get(r.nextInt(cityIris.size()));
    }

    public static String randomEventMode(Random r) {
        return EVENT_MODES[r.nextInt(EVENT_MODES.length)];
    }

    public static String randomTrack(Random r) {
        return TRACKS[r.nextInt(TRACKS.length)];
    }

    public static String randomDomain(Random r) {
        return DOMAINS[r.nextInt(DOMAINS.length)];
    }

    public static String randomDesignation(Random r) {
        return DESIGNATIONS[r.nextInt(DESIGNATIONS.length)];
    }
}
