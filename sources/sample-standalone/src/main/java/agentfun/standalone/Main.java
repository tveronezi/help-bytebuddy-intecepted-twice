package agentfun.standalone;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {

    public static void main(String[] args) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://www.google.com").openConnection();
        System.out.println(urlConnection.getRequestMethod());
    }

}
