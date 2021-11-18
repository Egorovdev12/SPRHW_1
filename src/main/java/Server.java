import java.io.IOException;
import java.net.ServerSocket;

public class Server {

    public void start() {
        while (true) {
            try (final ServerSocket serverSocket = new ServerSocket(1221)) {

                new TCPConnection(serverSocket.accept());

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}