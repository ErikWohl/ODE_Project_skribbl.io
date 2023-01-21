package Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import Controller.Service.ClientObserver;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
public class SkribblClient implements Runnable {
    private Logger logger = LogManager.getLogger(SkribblClient.class);
    private Socket clientSocket;
    private String UUID;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private ClientObserver clientObserver;


    public SkribblClient(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    public String getUUID() {
        return UUID;
    }
    public void setUUID(String UUID) {
        this.UUID = UUID;
    }
    public void setClientObserver(ClientObserver clientObserver) {
        this.clientObserver = clientObserver;
    }
    public PrintWriter getPrintWriter() {
        return printWriter;
    }
    public void setPrintWriter(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }
    public Socket getClientSocket() {
        return clientSocket;
    }
    @Override
    public void run() {
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            clientObserver.onStart(UUID);
            do {
                String message = bufferedReader.readLine();
                logger.trace("Message from Client (" + UUID + "): " + message);
                message = message.replace("\uFEFF", "");
                clientObserver.processMessage(UUID, message);
            } while (true);
        }catch (IOException e) {
            logger.error("Connection to client (" + UUID + ") lost.");
            clientObserver.onCrash(UUID);
        }
    }

}
