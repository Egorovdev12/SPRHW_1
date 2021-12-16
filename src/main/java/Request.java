import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {

    private final String requestLine;
    private String uglyString;
    private List<NameValuePair> coupleList;

    public Request (String requestLine ) {
        this.requestLine = requestLine;
        uglyString = requestLine.split(" ")[1].split("\\?")[1];
        coupleList = URLEncodedUtils.parse(uglyString, Charset.forName("utf-8"));
    }

    public String getQueryParamUseHttpClient (String paramKey) {
        for (int i = 0; i < coupleList.size(); i++) {
            if (coupleList.get(i).getName().equals(paramKey)) {
                return coupleList.get(i).getValue();
            }
        }
       return null;
    }

    public void getQueryParamsUseHttpClient() {
        System.out.println(coupleList);
    }
}