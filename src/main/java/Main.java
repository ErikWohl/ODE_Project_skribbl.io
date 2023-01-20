import Controller.SkribblServer;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//todo: @ewohlrab:
// https://www.baeldung.com/java-logging-intro
// https://mkyong.com/logging/apache-log4j-2-tutorials/
// sout,fout mit logging ersetzen, asynchronous logging anschauen

//todo: @ewohlrab: Umändern auf UDP multicast sockets?
// https://docs.oracle.com/javase/7/docs/api/java/net/MulticastSocket.html

public class Main {
    public static void main(String[] args) throws IOException {
        SkribblServer skribblServer = new SkribblServer();

        System.out.println("Server reachable at: " + skribblServer.getServerSocket().getInetAddress());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> skribblServer.run());
    }
}