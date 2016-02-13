package gyurix.api;

import gyurix.json.JsonAPI;
import gyurix.protocol.utils.GameProfile;
import org.apache.commons.io.IOUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by GyuriX on 2015.12.27..
 */
public class MojangAPI {
    public static HashMap<String, MojangServerState> getServerState() {
        HashMap<String, MojangServerState> out = new HashMap<>();
        try {
            String[] d = get("https://status.mojang.com/check").split(",");
            for (String s : d) {
                String[] s2 = s.split(":");
                out.put(s2[0].substring(s2[0].indexOf("\"") + 1, s2[0].length() - 1),
                        MojangServerState.valueOf(s2[1].substring(s2[1].indexOf("\"") + 1, s2[1].lastIndexOf("\"")).toUpperCase()));
            }
            return out;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static GameProfile getProfile(String name, long time) {
        try {
            return (GameProfile) JsonAPI.deserialize(get("https://api.mojang.com/users/profiles/minecraft/" + name + "?at=" + time), GameProfile.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static GameProfile getProfile(String name) {
        try {
            return (GameProfile) JsonAPI.deserialize(get("https://api.mojang.com/users/profiles/minecraft/" + name), GameProfile.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static GameProfile getProfileWithSkin(UUID id) {
        try {
            return (GameProfile) JsonAPI.deserialize(get("https://sessionserver.mojang.com/session/minecraft/profile/" + id.toString().replace("-", "") + "?unsigned=false"), GameProfile.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<NameData> getNameHistory(UUID id) {
        try {
            return (ArrayList<NameData>) JsonAPI.deserialize(get("https://api.mojang.com/user/profiles/" + id.toString().replace("-", "") + "/names"), ArrayList.class, NameData.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<GameProfile> getProfiles(String... names) {
        try {
            return (ArrayList<GameProfile>) JsonAPI.deserialize(post("https://api.mojang.com/profiles/minecraft", JsonAPI.serialize(names)), ArrayList.class, GameProfile.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

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
            con.setRequestProperty("Content-Length", "" + req.length());
            con.getOutputStream().write(req.getBytes(Charset.forName("UTF-8")));
            System.out.println("POST " + req);
            return IOUtils.toString(con.getInputStream(), Charset.forName("UTF-8"));
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public enum MojangServerState {
        RED, GREEN, YELLOW;
    }

    public static class NameData {
        public String name;
        public long changedToAt;

        @Override
        public String toString() {
            return JsonAPI.serialize(this);
        }
    }
}
