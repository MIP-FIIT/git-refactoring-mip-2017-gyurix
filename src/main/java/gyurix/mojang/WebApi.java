package gyurix.mojang;

import gyurix.spigotlib.Config;
import org.apache.commons.io.IOUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by GyuriX on 2016. 06. 10..
 */
public class WebApi {
    public static String get(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            return IOUtils.toString(con.getInputStream(), Charset.forName("UTF-8"));
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String post(String urlString, String req) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Content-Length", String.valueOf(req.length()));
            con.getOutputStream().write(req.getBytes(Charset.forName("UTF-8")));
            if (Config.debug)
                System.out.println("POST - " + urlString + " - " + req);
            return IOUtils.toString(con.getInputStream(), Charset.forName("UTF-8"));
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String postWithHeader(String urlString, String headerKey, String headerValue, String req) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Length", String.valueOf(req.length()));
            con.setRequestProperty(headerKey, headerValue);
            con.getOutputStream().write(req.getBytes(Charset.forName("UTF-8")));
            if (Config.debug)
                System.out.println("POST - " + urlString + " - " + headerKey + ": " + headerValue + " - " + req);
            return IOUtils.toString(con.getInputStream(), Charset.forName("UTF-8"));
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}