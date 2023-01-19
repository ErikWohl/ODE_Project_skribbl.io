package Controller;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SkribblServer implements Runnable, ServerObserver{
    private Logger logger = LogManager.getLogger(SkribblServer.class);
    private int port = 8080;
    private ServerSocket serverSocket;
    private Map<String, SkribblClient> clientMap = new HashMap<>();
    private ThreadPoolExecutor executor;
    public SkribblServer() throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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
                logger.info("Client accepting...");
                lock.writeLock().lock();
                try {
                    SkribblClient skribblClient = new SkribblClient(client);
                    skribblClient.setUUID(UUID.randomUUID().toString());
                    PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
                    skribblClient.setPrintWriter(printWriter);
                    // verbindung mit Server für crash
                    skribblClient.setServerObserver(this);
                    clientMap.put(skribblClient.getuUID(), skribblClient);
                    executor.submit(() -> skribblClient.run());
                    printWriter.println("Hello Client");
                } finally {
                    lock.writeLock().unlock();
                }
                logger.info("Client accepted: " + client.getRemoteSocketAddress().toString());
            } while (true);
        }catch (Exception e) {
            logger.error("Error log message", e);
        }
    }

    @Override
    public void onCrash(String UUID) {
        logger.info("Server: Removing client (" + UUID + ") from list.");
        ReadWriteLock lock = new ReentrantReadWriteLock();
        lock.writeLock().lock();
        try {
            clientMap.remove(UUID);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void echo(String UUID, String msg) {
        logger.info("Echo: " + msg);
        for(var client : clientMap.entrySet()) {
            if(client.getKey() != UUID) {
                client.getValue().getPrintWriter().println("Client (" + UUID + ") sent: " + msg);
            }
        }
    }
}
