import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TCPConnection implements Runnable{

    private final List<String> validPaths = List.of("/index.html", "/picture.png");
    private final ExecutorService threadPool = Executors.newFixedThreadPool(64);
    private BufferedReader buffReader;
    private BufferedOutputStream buffWriter;

    TCPConnection(Socket socket) {
        try {
            buffReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            buffWriter = new BufferedOutputStream(socket.getOutputStream());
            threadPool.execute(this);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            threadPool.shutdown();
        }
    }

    @Override
    public void run() {
        try {
            // В эту строку придёт http-запрос, сформированный браузером
            final String requestLine = buffReader.readLine();

            // Если длина не равна 3, то запрос ситается некорректным
            final String[] lineParts = requestLine.split(" ");
            if (lineParts.length != 3) {
                return;
            }

            // Получаем путь к файлу в каталоге, отделив его от параметров
            final String path = lineParts[1].split("\\?")[0];

            if (lineParts[1].contains("?")) {
                // Парсим вручную
                Request request = new Request(requestLine);

                // Парсим с помощью библиотеки
                System.out.println("Получим значение второго параметра с помощью httpClient: " + request.getQueryParamUseHttpClient("second"));
                request.getQueryParamsUseHttpClient();
            }

            // Если в списке имен файлов такого не будет найдено, то вернется вот такой запрос
            if (!validPaths.contains(path)) {
                buffWriter.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                buffWriter.flush();
                return;
            }

            // Получение пути к файлу в корне нашего проекта
            final Path filePath = Path.of(".", "publicFiles", path);
            // Получение mimeType
            final String mimeType = Files.probeContentType(filePath);

            final long length = Files.size(filePath);
            buffWriter.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, buffWriter);
            buffWriter.flush();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}