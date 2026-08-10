package genact.temporal.data.generator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Server-mode counterpart to RDFStreamerExample: instead of connecting out to a receiver, this opens a
 * ServerSocket per partition directory and waits for a stream-reasoner runner (RDFoxStreamReceiver /
 * CSPARQLStreamReceiver) to connect in, then replays that partition's .ttl files over the socket, paced
 * by the timestamps already encoded in their filenames. Required because both runners connect out to the
 * port named in their query's stream URI rather than listening themselves.
 */
public class GenActStreamServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 2) {
            System.out.println("Usage: GenActStreamServer <directory> <basePort> [rate]");
            System.out.println("  If <directory> contains subdirectories, one server is started per subdirectory,");
            System.out.println("  on consecutive ports starting at <basePort>, mirroring RDFStreamerExample's parallel-stream layout.");
            return;
        }

        String directoryPath = args[0];
        final int basePort = Integer.parseInt(args[1]);
        final double rate = args.length >= 3 ? Double.parseDouble(args[2]) : 1.0;

        File directory = new File(directoryPath);
        File[] subdirs = directory.listFiles(File::isDirectory);
        // File.listFiles() order is filesystem/OS-dependent, not guaranteed alphabetical. Since
        // configuration.json hardcodes specific ports to specific query semantics (e.g. conf0 on
        // basePort, conf1 on basePort+1), an unsorted assignment would make that mapping
        // nondeterministic across runs.
        if (subdirs != null) {
            Arrays.sort(subdirs, Comparator.comparing(File::getName));
        }

        if (subdirs != null && subdirs.length > 0) {
            System.out.println("Directories found: " + subdirs.length);
            ExecutorService executor = Executors.newFixedThreadPool(subdirs.length);
            for (int i = 0; i < subdirs.length; i++) {
                final int port = basePort + i;
                final File subdir = subdirs[i];
                executor.submit(() -> serveDirectory(subdir, port, rate));
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } else {
            serveDirectory(directory, basePort, rate);
        }
    }

    private static void serveDirectory(File directory, int port, double rate) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Listening on port " + port + " for directory " + directory.getAbsolutePath());
            Socket client = serverSocket.accept();
            System.out.println("Client connected on port " + port + ", starting replay.");
            // A stream-reasoner client connects its socket (CSPARQLStreamReceiver/RDFoxStreamReceiver)
            // before it has finished registering the query that consumes this stream - registering
            // the query requires the stream to already be known to the engine, so the two can't be
            // reordered on the client side. Give the client a moment to finish registering (an
            // in-memory operation, fast) before sending the first byte, so the earliest triples
            // aren't pushed into a stream nothing is listening to yet.
            Thread.sleep(2000);
            List<File> sortedFiles = readAndSortFiles(directory);
            streamFiles(sortedFiles, rate, client.getOutputStream());
            System.out.println("Finished replay on port " + port + "; idling so the receiver's inactivity timeout can end the stream cleanly.");
            Thread.sleep(20000);
            client.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static List<File> readAndSortFiles(File directory) {
        List<File> sortedFiles = new ArrayList<>();
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".ttl"));
        if (files != null) {
            Collections.addAll(sortedFiles, files);
            sortedFiles.sort(Comparator.comparingLong(GenActStreamServer::extractTimestamp));
        }
        return sortedFiles;
    }

    private static long extractTimestamp(File file) {
        String timestampStr = file.getName().split("_")[0] + "_" + file.getName().split("_")[1];
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            return sdf.parse(timestampStr).getTime();
        } catch (ParseException e) {
            return 0L;
        }
    }

    private static void streamFiles(List<File> files, double rate, OutputStream out) throws IOException, InterruptedException {
        long previousTimestamp = -1;
        for (File file : files) {
            long timestamp = extractTimestamp(file);
            if (previousTimestamp >= 0 && timestamp > previousTimestamp) {
                long sleepTime = (long) ((timestamp - previousTimestamp) / rate);
                Thread.sleep(Math.min(sleepTime, 5000));
            }
            previousTimestamp = timestamp;

            byte[] content = readAllBytes(file);
            out.write(content);
            if (content.length == 0 || content[content.length - 1] != '\n') {
                out.write('\n');
            }
            out.flush();
        }
    }

    private static byte[] readAllBytes(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int read;
            while ((read = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return buffer.toByteArray();
        }
    }
}
