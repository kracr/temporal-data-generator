package tboxaware.generator;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * CLI entry point. Run from the temporal-data-generator repo root so the default relative paths
 * to Ontology/ and CSVFiles/ resolve, e.g.:
 *   java -jar TBoxAwareGenerator/target/tbox-aware-generator.jar ICWE2027 3 out/ICWE2027 42
 *
 * The edition number is a separate argument from the conference name deliberately: to stream a
 * recurring conference series indefinitely, invoke this once per occurrence with the same
 * conferenceLocalName and an incrementing edition (e.g. ICWE2027/1, ICWE2028/2, ...) - each
 * invocation gets its own seed-derived draw sequence (GenerationContext mixes the conference name
 * into the seed) so successive editions never collide on generated paper/user identities.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: Main <conferenceLocalName> <edition> <outputDir> [seed]");
            System.exit(1);
        }
        String conferenceName = args[0];
        int edition = Integer.parseInt(args[1]);
        String outputDir = args[2];
        long seed = args.length > 3 ? Long.parseLong(args[3]) : 42L;

        Files.createDirectories(Path.of(outputDir));

        System.out.println("Loading real ACE + Tweet TBoxes...");
        TBoxVocabulary vocab = TBoxVocabulary.loadFrom(
                new File("Ontology/ACE/Academic-Conference-Event-DL.owl"),
                new File("Ontology/Tweet.owl"));

        System.out.println("Loading declarative templates...");
        String templatesPath = extractTemplatesResourceIfNeeded();
        List<TemplateSpec> templates = TemplateLoader.load(templatesPath);
        System.out.println("Loaded " + templates.size() + " templates. Validating against TBox vocabulary...");
        for (TemplateSpec t : templates) {
            vocab.validate(t);
        }
        System.out.println("All templates validated OK.");

        System.out.println("Loading CSV reference data...");
        DataSources dataSources = DataSources.loadFrom("CSVFiles");

        GenerationContext ctx = new GenerationContext(conferenceName, edition, outputDir, seed, dataSources);
        TemplateEngine engine = new TemplateEngine(ctx);
        System.out.println("Conference window: before=" + fmt(ctx.timeline.phaseWindow("before"))
                + " during=" + fmt(ctx.timeline.phaseWindow("during")) + " after=" + fmt(ctx.timeline.phaseWindow("after")));

        int written = 0;
        for (String phase : new String[]{"before", "during", "after"}) {
            for (TemplateSpec t : templates) {
                if (!phase.equals(t.phase)) {
                    continue;
                }
                for (int i = 0; i < t.frequency; i++) {
                    engine.instantiate(t);
                    written++;
                }
            }
        }
        System.out.println("Wrote " + written + " tweets (" + (written * 2) + " files) to " + outputDir);
    }

    private static String fmt(long[] window) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(window[0])) + ".."
                + new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(window[1]));
    }

    /** templates.yaml ships as a classpath resource; TemplateLoader reads from a real file path. */
    private static String extractTemplatesResourceIfNeeded() throws Exception {
        File direct = new File("TBoxAwareGenerator/src/main/resources/templates.yaml");
        if (direct.exists()) {
            return direct.getPath();
        }
        try (InputStream in = Main.class.getClassLoader().getResourceAsStream("templates.yaml")) {
            if (in == null) {
                throw new IllegalStateException("templates.yaml not found on classpath or at " + direct.getPath());
            }
            Path tmp = Files.createTempFile("templates", ".yaml");
            Files.copy(in, tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return tmp.toString();
        }
    }
}
