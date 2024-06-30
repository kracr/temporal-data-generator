package genact.temporal.data.generator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class RDFServerExample {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: RDFServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            while (true) {
                Socket clientSocket = null;
                InputStream inputStream = null;
                try {
                    clientSocket = serverSocket.accept();
                    inputStream = clientSocket.getInputStream();

                    Model model = ModelFactory.createDefaultModel();
                    RDFDataMgr.read(model, inputStream, Lang.TURTLE);

                    System.out.println("Received RDF data:");
                    model.write(System.out, "TURTLE");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
