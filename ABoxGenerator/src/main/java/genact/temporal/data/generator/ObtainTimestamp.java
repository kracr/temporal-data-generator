package genact.temporal.data.generator;

		import java.io.File;
		import java.time.Instant;
		import java.time.LocalDateTime;
		import java.time.ZoneOffset;
		import java.time.format.DateTimeFormatter;

		public class ObtainTimestamp {

		    public static void main(String[] args) {
		        String directoryPath = "/path/to/your/directory"; // Replace with your actual directory path
		        File directory = new File(directoryPath);

		        if (directory.isDirectory()) {
		            File[] files = directory.listFiles();

		            if (files != null) {
		                for (File file : files) {
		                    if (file.isFile()) {
		                        String formattedDateTime = extractAndFormatTimestamp(file.getName());
		                        System.out.println("File: " + file.getName() + ", Formatted DateTime: " + formattedDateTime);
		                    }
		                }
		            } else {
		                System.err.println("Error listing files in the directory.");
		            }
		        } else {
		            System.err.println("Provided path is not a directory.");
		        }
		    }

		    private static String extractAndFormatTimestamp(String filename) {
		        // Assuming the timestamp is in the format "yyyyMMdd_HHmmss"
		        String timestampPart = filename.substring(filename.lastIndexOf('_') + 1, filename.lastIndexOf('_') + 15);

		        // Parse timestamp string to LocalDateTime
		        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
		        LocalDateTime timestampDateTime = LocalDateTime.parse(timestampPart, formatter);

		        // Format LocalDateTime to a different format if needed
		        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		        return timestampDateTime.format(outputFormatter);
		    }
		}

