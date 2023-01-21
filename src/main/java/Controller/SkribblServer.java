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

import Controller.Enums.CommandEnum;
import Controller.Service.GameService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SkribblServer implements Runnable, GameObserver {
    private Logger logger = LogManager.getLogger(SkribblServer.class);
    private int port = 8080;
    private ServerSocket serverSocket;
    private Map<String, SkribblClient> clientMap = new HashMap<>();
    private ThreadPoolExecutor executor;
    private GameService gameService;
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

    public void setGameService(GameService gameService) {
        this.gameService = gameService;
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
                    PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);

                    skribblClient.setUUID(UUID.randomUUID().toString());
                    skribblClient.setPrintWriter(printWriter);
                    skribblClient.setClientObserver(gameService);

                    clientMap.put(skribblClient.getUUID(), skribblClient);
                    executor.submit(() -> skribblClient.run());

                    printWriter.println(CommandEnum.MESSAGE.getCommand() + "Hello Client");
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
        logger.error("Removing client (" + UUID + ") from list.");
        ReadWriteLock lock = new ReentrantReadWriteLock();
        lock.writeLock().lock();
        try {
            clientMap.remove(UUID);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void unicast(String UUID, String msg) {
        logger.trace("Sending unicast to Client (" + UUID + ") sent: " + msg);
        clientMap.get(UUID).getPrintWriter().println(msg);
    }

    @Override
    public void multicast(String UUID, String msg) {
        for(var client : clientMap.entrySet()) {
            if(!client.getKey().equals(UUID)) {
                logger.trace("Sending multicast to Client (" + client.getKey() + ") sent: " + msg);
                client.getValue().getPrintWriter().println(msg);
            }
        }
    }

    @Override
    public void startGame() {

    }

    @Override
    public void startRound() {

    }

    @Override
    public void endRound() {

    }
}
