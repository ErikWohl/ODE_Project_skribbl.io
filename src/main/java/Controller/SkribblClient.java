package Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
public class SkribblClient implements Runnable {
    private Logger logger = LogManager.getLogger(SkribblClient.class);
    private Socket clientSocket;
    private String uUID;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    private ServerObserver serverObserver;
    public SkribblClient(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    public String getuUID() {
        return uUID;
    }
    public void setUUID(String uUID) {
        this.uUID = uUID;
    }
    public void setServerObserver(ServerObserver serverObserver) {
        this.serverObserver = serverObserver;
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
            do {
                String message = bufferedReader.readLine();
                logger.trace("Message from Client (" + uUID + "): " + message);

                // Abarbeitung der frontend commands
                // Commands sind immer 3 Zeichen lang.
                String command = message.substring(0, 3);
                CommandEnum commandEnum = CommandEnum.fromString(command);
                switch (commandEnum) {
                    case MESSAGE: {
                        serverObserver.multicast(uUID, message);
                        break;
                    }
                    case DRAWING: {
                        //todo: Nur vor Rundenbeginn oder wenn man Zeichner ist
                        serverObserver.multicast(uUID, message);
                        break;
                    }
                }











                //serverObserver.onIncomingMessage(uUID, message);
            } while (true);
        }catch (IOException e) {
            logger.info("Connection to client (" + uUID + ") lost.");
            serverObserver.onCrash(uUID);
        }
    }
}
