import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.Lang;

public class RDFStreamer {

    public static void main(String[] args) {
        String directoryPath = "SequenceData";
        double rate = 365.0; // Adjust the rate as needed
        
        try {
            List<File> sortedFiles = readAndSortFiles(directoryPath);
            streamFiles(sortedFiles, rate);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static List<File> readAndSortFiles(String directoryPath) throws IOException {
        List<File> sortedFiles = new ArrayList<>();
        File directory = new File(directoryPath);
        
        for (File subdir : directory.listFiles(File::isDirectory)) {
            for (File file : subdir.listFiles((dir, name) -> name.endsWith(".ttl"))) {
                sortedFiles.add(file);
            }
        }
        
        sortedFiles.sort(Comparator.comparing(File::getName));
        return sortedFiles;
    }

    private static long extractTimestamp(File file) {
        String fileName = file.getName();
        String timestampStr = fileName.split("_")[0];
        return Long.parseLong(timestampStr);
    }

    private static void streamFiles(List<File> files, double rate) throws IOException, InterruptedException {
        Long previousTimestamp = null;
        
        for (File file : files) {
            long timestamp = extractTimestamp(file);
            
            if (previousTimestamp != null) {
                long sleepTime = (long) ((timestamp - previousTimestamp) / rate);
                TimeUnit.MILLISECONDS.sleep(Math.max(0, sleepTime));
            }
            
            previousTimestamp = timestamp;
            
            Model model = ModelFactory.createDefaultModel();
            try (FileInputStream in = new FileInputStream(file)) {
                RDFDataMgr.read(model, in, Lang.TURTLE);
            }
            
            System.out.println("Streaming file: " + file.getName());
            // Perform streaming operation with the model
            // For example, send the model to a server or process it further
            // model.write(System.out, "TURTLE");
        }
    }
}
