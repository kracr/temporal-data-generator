package utils;

import java.io.*;
import java.nio.file.*;
import java.util.regex.*;
import java.util.*;

public class CreateCSVs {
    private File streamsDirectory;

    public CreateCSVs(String directoryPath) {
        this.streamsDirectory = new File(directoryPath + "/Streams/");
    }

    public void processDirectories() {
        if (streamsDirectory.exists() && streamsDirectory.isDirectory()) {
            File[] subdirectories = streamsDirectory.listFiles(File::isDirectory);

            if (subdirectories != null) {
                for (File subdirectory : subdirectories) {
                    processSubdirectory(subdirectory);
                }
            } else {
                System.err.println("No subdirectories found in the streams directory.");
            }
        } else {
            System.err.println("Streams directory does not exist or is not a directory.");
        }
    }

    private void processSubdirectory(File subdirectory) {
        File[] ttlFiles = subdirectory.listFiles((dir, name) -> name.endsWith(".ttl"));

        if (ttlFiles != null) {
            List<String> timestamps = new ArrayList<>();
            Pattern pattern = Pattern.compile("^(\\d{8}_\\d{6})_tweet");

            for (File ttlFile : ttlFiles) {
                Matcher matcher = pattern.matcher(ttlFile.getName());
                if (matcher.find()) {
                    timestamps.add(matcher.group(1));
                }
            }

            saveTimestampsToCsv(subdirectory.getName(), timestamps);
        } else {
            System.err.println("No .ttl files found in subdirectory: " + subdirectory.getName());
        }
    }

    private void saveTimestampsToCsv(String subdirectoryName, List<String> timestamps) {
        File csvFile = new File(streamsDirectory, subdirectoryName + ".csv");

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
            writer.println("Timestamp");

            for (String timestamp : timestamps) {
                writer.println(timestamp);
            }

            System.out.println("CSV file created: " + csvFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + csvFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String directoryPath = "C:\\GitHub\\temporal-data-generator\\"; // Replace with your actual directory path
        CreateCSVs extractor = new CreateCSVs(directoryPath);
        extractor.processDirectories();
    }
}
