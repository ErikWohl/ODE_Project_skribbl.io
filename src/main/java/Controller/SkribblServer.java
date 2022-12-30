package Controller;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SkribblServer implements Runnable{
    private Logger logger = LogManager.getLogger(SkribblServer.class);
    private int port = 8080;
    private ServerSocket serverSocket;
    private Map<String, SkribblClient> clientMap = new HashMap<>();

    public SkribblServer() throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        port = port;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        ReadWriteLock lock = new ReentrantReadWriteLock();

        try {
            do {
                logger.info("Server accepting clients.");
                Socket client = serverSocket.accept();

                lock.writeLock().lock();
                try {
                    SkribblClient skribblClient = new SkribblClient(client);
                    clientMap.put(UUID.randomUUID().toString(), skribblClient);
                    Thread thread = new Thread(() -> skribblClient.run());
                    thread.start();
                } finally {
                    lock.writeLock().unlock();
                }

                logger.info("Client accepted: " + client.getInetAddress());

                PrintWriter printWriter = new PrintWriter(client.getOutputStream(), true);
                printWriter.println("Hello Client");
            } while (true);
        }catch (Exception e) {
            logger.error("Error log message", e);
        }
    }
}
