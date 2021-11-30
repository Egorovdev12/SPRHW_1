import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Request {

    private final String requestLine;
    private final Map<String, String> mapa;

    public Request (String requestLine ) {
        this.requestLine = requestLine;
        this.mapa = new HashMap<>();
    }

    public String getQueryParam(String name) {
        // Здесь мы берем вторую строку из запроса - это наша query string
        String[] splitRequestLine = requestLine.split(" ");
        final String path = splitRequestLine[1];
        // path = /my/ratings/top?first=one&second=two
        // Сначала отделим параметры от пути. "?" - символ начала параметров запроса
        String[] lineParts1 = path.split("\\?");
        final String path2 = lineParts1[1];
        // Получили строку, содержащую все пары ключ-значение. Делим далее, теперь делитель - &
        String[] lineParts2 = path2.split("&");
        // Теперь мы имеем массив, содержащий пары ключ-значение. Распарсим его в коллекцию

        for (String keyAndValue : lineParts2) {
            String[] temp = keyAndValue.split("=");
            mapa.put(temp[0],  temp[1]);
        }
        // Получим значение параметра по его ключу
        return mapa.get(name);
    }

    public void getQueryParams() {
        Iterator it = mapa.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pair = (Map.Entry)it.next();
            System.out.println("Ключ: " + pair.getKey() + ", значение: " + pair.getValue());
        }
    }

    public String getQueryParamUseHttpClient (String paramKey) {
        String ugly = requestLine.split(" ")[1].split("\\?")[1];
        List<NameValuePair> cumList = URLEncodedUtils.parse(ugly, Charset.forName("utf-8"));
        for (int i = 0; i < cumList.size(); i++) {
            if (cumList.get(i).getName().equals(paramKey)) {
                return cumList.get(i).getValue();
            }
        }
       return null;
    }

    public void getQueryParamsUseHttpClient() {
        String ugly = requestLine.split(" ")[1].split("\\?")[1];
        List<NameValuePair> pairsList = URLEncodedUtils.parse(ugly, Charset.forName("utf-8"));
        System.out.println(pairsList);
    }
}