import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
            final String[] lineParts = requestLine.split(" ");

            if (lineParts.length != 3) {
                return;
            }

            // Здесь мы берем вторую строку из запроса - это наша query string
            final String path = lineParts[1];
            // path = /my/ratings/top?first=one&second=two
            // Сначала отделим параметры от пути. "?" - символ начала параметров запроса
            String[] lineParts2 = path.split("\\?");
            final String path2 = lineParts2[1];
            // Получили строку, содержащую все пары ключ-значение. Делим далее, теперь делитель - &
            String[] lineParts3 = path2.split("&");
            // Теперь мы имеем массив, содержащий пары ключ-значение. Распарсим его в коллекцию
            Map<String, String> mapa = new HashMap<>();
            for (String keyAndValue : lineParts3) {
                String[] temp = keyAndValue.split("=");
                mapa.put(temp[0],  temp[1]);
            }

            Iterator it = mapa.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> pair = (Map.Entry)it.next();
                System.out.println("Ключ: " + pair.getKey() + ", значение: " + pair.getValue());
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