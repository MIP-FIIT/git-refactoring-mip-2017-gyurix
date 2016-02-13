package gyurix.spigotlib;

import gyurix.chat.ChatTag;
import gyurix.json.JsonAPI;
import gyurix.protocol.PacketOutType;
import gyurix.protocol.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Collection;

public class ChatAPI {
    public static Method toICBC;
    public static Method fromICBC;
    public static Class icbcClass;

    public static void init() {
        try {
            icbcClass = Reflection.getNMSClass("IChatBaseComponent");
            if (Reflection.version.equals("v1_8_R1.") || Reflection.version.startsWith("v1_7")) {
                toICBC = Reflection.getNMSClass("ChatSerializer").getMethod("a", String.class);
            } else {
                for (Class c : icbcClass.getClasses()) {
                    if (!c.getName().endsWith("ChatSerializer")) continue;
                    toICBC = c.getMethod("a", String.class);
                    fromICBC = c.getMethod("a", icbcClass);
                }
            }
        } catch (Throwable e) {
            Main.errorLog(null, e);
        }
    }

    public static Object toICBC(String json) {
        try {
            if (json == null) {
                return null;
            }
            return toICBC.invoke(null, json);
        } catch (Throwable e) {
            Main.errorLog(null, e);
            throw new RuntimeException(e);
        }
    }

    public static String toJson(Object icbc) {
        try {
            if (icbc == null) {
                return null;
            }
            return (String) fromICBC.invoke(null, icbc);
        } catch (Throwable e) {
            Main.errorLog(null, e);
            throw new RuntimeException(e);
        }
    }

    public static void sendRawJson(ChatMessageType type, String json, Player... pls) {
        Object packet = PacketOutType.Chat.newPacket(ChatAPI.toICBC(json), null, Byte.valueOf((byte) type.ordinal()));
        for (Player p : pls) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void sendRawJson(ChatMessageType type, String json, Collection<? extends Player> pls) {
        Object packet = PacketOutType.Chat.newPacket(ChatAPI.toICBC(json), null, Byte.valueOf((byte) type.ordinal()));
        for (Player p : pls) {
            SU.tp.sendPacket(p, packet);
            if (Config.debug)
                System.out.println("Sent JSON " + json + " to " + p.getName());
        }
    }

    public static void sendJsonMsg(ChatMessageType type, String msg, Player... pls) {
        if (pls.length == 0) {
            ChatAPI.sendJsonMsg(type, msg, Bukkit.getOnlinePlayers());
            return;
        }
        String json = type == ChatMessageType.ACTION_BAR ? ChatAPI.quoteJson(msg) : ChatAPI.TextToJson(msg);
        ChatAPI.sendRawJson(type, json, pls);
    }

    public static void sendJsonMsg(ChatMessageType type, String msg, Collection<? extends Player> pls) {
        String json = type == ChatMessageType.ACTION_BAR ? ChatAPI.quoteJson(msg) : ChatAPI.TextToJson(msg);
        ChatAPI.sendRawJson(type, json, pls);
    }

    public static String unicodeEscape(char ch) {
        StringBuilder sb = new StringBuilder();
        sb.append("\\u");
        String hex = Integer.toHexString(ch);
        for (int i = hex.length(); i < 4; ++i) {
            sb.append('0');
        }
        sb.append(hex);
        return sb.toString();
    }

    public static String quoteJson(String value) {
        return "\"" + JsonAPI.escape(value) + "\"";
    }

    public static String TextToJson(String msg) {
        return ChatTag.fromExtraText(msg).toString();
    }

    public enum ChatMessageType {
        CHAT,
        SYSTEM,
        ACTION_BAR;
        ChatMessageType() {
        }
    }
}