package genact.temporal.data.generator;

import java.io.*;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;

public class RDFStreamerExample {

    public static void main(String[] args) {
        if (args.length < 5) {
            System.out.println("Usage: RDFStreamer <directory> <start_time> <rate> <duration(ms)> <port>");
            return;
        }

        String directoryPath = args[0];
        String startTimeStr = args[1];
        final double rate = Double.parseDouble(args[2]);
        final long duration = Long.parseLong(args[3]);
        final int startPort = Integer.parseInt(args[4]);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        final Date startTime;
        try {
            startTime = sdf.parse(startTimeStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        File directory = new File(directoryPath);
        final File[] subdirs = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        if (subdirs != null && subdirs.length > 0) {
            System.out.println("Directories found: " + subdirs.length);

            ExecutorService executor = Executors.newFixedThreadPool(subdirs.length);

            for (int i = 0; i < subdirs.length; i++) {
                final int port = startPort + i;
                final File subdir = subdirs[i];

                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String path = subdir.getAbsolutePath();
                            System.out.println("Started " + path + " Instance");
                            List<File> sortedFiles = readAndSortFiles(path);
                            streamFiles(sortedFiles, startTime, rate, port, subdir.getName(), duration);
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            try {
                List<File> sortedFiles = readAndSortFiles(directoryPath);
                streamFiles(sortedFiles, startTime, rate, startPort, directory.getName(), duration);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<File> readAndSortFiles(String directoryPath) throws IOException {
        List<File> sortedFiles = new ArrayList<File>();
        File directory = new File(directoryPath);

        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".ttl");
            }
        });

        if (files != null) {
            for (File file : files) {
                sortedFiles.add(file);
            }

            Collections.sort(sortedFiles, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return extractTimestamp(f1).compareTo(extractTimestamp(f2));
                }
            });
        }
        return sortedFiles;
    }

    private static Long extractTimestamp(File file) {
        String fileName = file.getName();
        String timestampStr = fileName.split("\\.")[0]; // Remove file extension
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            Date date = sdf.parse(timestampStr);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0L; // Handle parse exception gracefully
        }
    }

    private static void streamFiles(List<File> files, Date startTime, double rate, int port, String directoryName, long duration)
            throws IOException, InterruptedException {
        long startTimeMillis = startTime.getTime();
        Model mergedModel = ModelFactory.createDefaultModel();
        List<File> currentBatch = new ArrayList<File>();
        long currentTimestamp = -1;

        System.out.println("Files in directory " + directoryName + " sorted by timestamp:");

        for (File file : files) {
            long timestamp = extractTimestamp(file);
            System.out.println(file.getName() + " - Timestamp: " + timestamp);

            if (timestamp >= startTimeMillis) {
                long sleepTime = (long) ((timestamp - startTimeMillis) / rate);
                System.out.println("Sleeping for " + sleepTime + " milliseconds");
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            }

            startTimeMillis = timestamp;

            if (currentBatch.isEmpty()) {
                currentTimestamp = timestamp;
            }

            if (timestamp == currentTimestamp) {
                currentBatch.add(file);
            } else {
                processBatch(currentBatch, mergedModel, port);
                currentBatch.clear();
                currentBatch.add(file);
                currentTimestamp = timestamp;
            }

            if (startTimeMillis - startTime.getTime() > duration) {
                break;
            }
        }

        if (!currentBatch.isEmpty()) {
            processBatch(currentBatch, mergedModel, port);
        }
    }

    private static void processBatch(List<File> files, Model mergedModel, int port) throws IOException {
        for (File file : files) {
            Model model = ModelFactory.createDefaultModel();
            try {
                RDFDataMgr.read(model, new FileInputStream(file), Lang.TURTLE);
                mergedModel.add(model);
            } catch (RiotException e) {
                System.err.println("Error parsing file " + file.getName() + ": " + e.getMessage());
                continue; // Skip the file and continue processing the next one
            }

            Socket socket = null;
            OutputStream outputStream = null;
            try {
                socket = new Socket("localhost", port);
                outputStream = socket.getOutputStream();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                RDFDataMgr.write(baos, mergedModel, Lang.TURTLE);

                byte[] data = baos.toByteArray();
                outputStream.write(data);
                outputStream.flush();
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (socket != null) {
                    socket.close();
                }
            }
        }
    }
}
