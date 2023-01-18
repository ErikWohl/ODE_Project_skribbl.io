package Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
public class SkribblClient implements Runnable {
    private Logger logger = LogManager.getLogger(SkribblClient.class);
    private Socket clientSocket;
    private String uUID;

    private ServerObserver serverObserver;
    public SkribblClient(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void setUUID(String uUID) {
        this.uUID = uUID;
    }
    public void setServerObserver(ServerObserver serverObserver) {
        this.serverObserver = serverObserver;
    }
    public String getuUID() {
        return uUID;
    }
    public Socket getClientSocket() {
        return clientSocket;
    }

    @Override
    public void run() {
        try {
            do {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String message = bufferedReader.readLine();
                logger.info("Server: Message from Client (" + uUID + "): " + message);
                serverObserver.echo(uUID, message);
            } while (true);
        }catch (IOException e) {
            logger.info("Server: Connection to client (" + uUID + ") lost.");
            serverObserver.onCrash(uUID);
            //e.printStackTrace();
        }
    }
}
