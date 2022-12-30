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
    public SkribblClient(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            do {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String message = bufferedReader.readLine();
                logger.info("Server: Message from Client " + message);
            } while (true);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
