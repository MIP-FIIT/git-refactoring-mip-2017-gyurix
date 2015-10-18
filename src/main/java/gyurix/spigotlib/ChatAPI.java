package gyurix.spigotlib;

import gyurix.protocol.PacketOutType;
import gyurix.protocol.Reflection;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

public class ChatAPI {
    private static Method toICBC,fromICBC;

    public static void init() {
        try {
            if ((Reflection.version.equals("v1_8_R1.")) || (Reflection.version.startsWith("v1_7"))) {
                toICBC = Reflection.getNMSClass("ChatSerializer").getMethod("a", String.class);
            } else {
                Class icbc=Reflection.getNMSClass("IChatBaseComponent");
                for (Class c : icbc.getClasses()) {
                    if (c.getName().endsWith("ChatSerializer")) {
                        toICBC = c.getMethod("a", String.class);
                        fromICBC=c.getMethod("a", icbc);
                    }
                }
            }
        } catch (Throwable e) {
            Main.errorLog(null, e);
        }
    }

    public static Object toICBC(String json) {
        try {
            if (json==null)
                return null;
            return toICBC.invoke(null, json);
        } catch (Throwable e) {
            Main.errorLog(null, e);
            throw new RuntimeException(e);
        }
    }
    public static String toJson(Object icbc){
        try {
            if (icbc==null)
                return null;
            return (String) fromICBC.invoke(null, icbc);
        } catch (Throwable e) {
            Main.errorLog(null, e);
            throw new RuntimeException(e);
        }
    }

    public static void sendRawJson(ChatMessageType type, String json, Player... pls) {
        Object packet = PacketOutType.Chat.newPacket(toICBC(json), null, (byte) type.ordinal());
        for (Player p : pls)
            SU.tp.sendPacket(p, packet);
    }

    public static void sendRawJson(ChatMessageType type, String json, Collection<? extends Player> pls) {
        Object packet = PacketOutType.Chat.newPacket(toICBC(json), null, (byte) type.ordinal());
        for (Player p : pls)
            SU.tp.sendPacket(p, packet);
    }

    public static void sendJsonMsg(ChatMessageType type, String msg, Player... pls) {
        if (pls.length == 0) {
            sendJsonMsg(type, msg, org.bukkit.Bukkit.getOnlinePlayers());
            return;
        }
        String json = type == ChatMessageType.ACTION_BAR ? quoteJson(msg) : TextToJson(msg);
        sendRawJson(type, json, pls);
    }

    public static void sendJsonMsg(ChatMessageType type, String msg, Collection<? extends Player> pls) {
        String json = type == ChatMessageType.ACTION_BAR ? quoteJson(msg) : TextToJson(msg);
        sendRawJson(type, json, pls);
    }

    public static String unicodeEscape(char ch) {
        StringBuilder sb = new StringBuilder();
        sb.append("\\u");
        String hex = Integer.toHexString(ch);
        for (int i = hex.length(); i < 4; i++) {
            sb.append('0');
        }
        sb.append(hex);
        return sb.toString();
    }

    public static String quoteJson(String value) {
        StringBuilder product = new StringBuilder();
        product.append("\"");
        char[] var2 = value.toCharArray();
        int var3 = var2.length;

        for (char ch : var2) {
            switch (ch) {
                case '\b':
                    product.append("\\b");
                    break;
                case '\t':
                    product.append("\\t");
                    break;
                case '\n':
                    product.append("\\n");
                    break;
                case '\f':
                    product.append("\\f");
                    break;
                case '\r':
                    product.append("\\r");
                    break;
                case '"':
                    product.append("\\\"");
                    break;
                case '\\':
                    product.append("\\\\");
                    break;
                default:
                    if (ch < ' ') {
                        product.append(unicodeEscape(ch));
                    } else {
                        product.append(ch);
                    }
                    break;
            }
        }
        product.append("\"");
        return product.toString();
    }

    public static String TextToJson(String msg) {
        ArrayList<ChatPart> cps = new ArrayList();
        String[] mainChatParts = msg.split("\\\\\\|");
        for (String mainPart : mainChatParts) {
            String[] parts = mainPart.split("\\\\-");
            String text = simpleTextToJson(parts[0]);
            Event click = null;
            Event hover = null;
            String insertion = null;
            for (int i = 1; i < parts.length; i++) {
                if (parts[i].charAt(0) == '+') {
                    insertion = parts[i].substring(1);
                } else {
                    String part = simpleTextToJson(parts[i].substring(1));
                    switch (parts[i].charAt(0)) {
                        case 'T':
                            hover = new Event("show_text", part);
                            break;
                        case 'I':
                            hover = new Event("show_item", part);
                            break;
                        case 'A':
                            hover = new Event("show_achievement", part);
                            break;
                        case 'E':
                            hover = new Event("show_entity", part);
                            break;
                        case 'S':
                            click = new Event("suggest_command", part);
                            break;
                        case 'R':
                            click = new Event("run_command", part);
                            break;
                        case 'U':
                            click = new Event("open_url", part);
                            break;
                        case 'F':
                            click = new Event("open_file", part);
                    }
                }
            }
            cps.add(new ChatPart(text, hover, click, insertion));
        }
        if (cps.size() == 0) {
            return "\"\"";
        }
        if (cps.size() == 1) {
            return cps.get(0).toString();
        }

        StringBuilder sb = new StringBuilder("{\"extra\":[\"\"");
        for (ChatPart p : cps) {
            sb.append(',').append(p.toString());
        }
        sb.append("],\"text\":\"\"}");
        return sb.toString();
    }

    private static String simpleTextToJson(String text) {
        ArrayList<FormattedText> fts = new ArrayList();
        FormattedText ft = new FormattedText();
        String[] out = text.split("§");
        ft.text = out[0];
        ChatColor lastc = ChatColor.WHITE;
        boolean notEmptyText;
        for (int i = 1; i < out.length; i++) {
            ChatColor color = ChatColor.getByChar(out[i].charAt(0));
            if (color != null) {
                if (color == ChatColor.RESET)
                    color = ChatColor.WHITE;
                notEmptyText = !ft.text.isEmpty();
                if (color.isFormat()) {
                    if (color == ChatColor.BOLD) {
                        if (!ft.bold) {
                            if (notEmptyText) {
                                fts.add(ft);
                                ft = new FormattedText(ft);
                            }
                            ft.bold = true;
                        }
                    } else if (color == ChatColor.ITALIC) {
                        if (!ft.bold) {
                            if (notEmptyText) {
                                fts.add(ft);
                                ft = new FormattedText(ft);
                            }
                            ft.bold = true;
                        }
                    } else if (color == ChatColor.UNDERLINE) {
                        if (!ft.underlined) {
                            if (notEmptyText) {
                                fts.add(ft);
                                ft = new FormattedText(ft);
                            }
                            ft.underlined = true;
                        }
                    } else if (color == ChatColor.STRIKETHROUGH) {
                        if (!ft.strikethrought) {
                            if (notEmptyText) {
                                fts.add(ft);
                                ft = new FormattedText(ft);
                            }
                            ft.strikethrought = true;
                        }
                    } else if (!ft.obfuscated) {
                        if (notEmptyText) {
                            fts.add(ft);
                            ft = new FormattedText(ft);
                        }
                        ft.obfuscated = true;
                    }

                } else if (lastc != color) {
                    if (notEmptyText) {
                        fts.add(ft);
                        ft = new FormattedText();
                    } else {
                        ft.removeFormat();
                    }
                    ft.color = color.name().toLowerCase();
                    lastc = color;
                } else if (notEmptyText) {
                    fts.add(ft);
                    ft = new FormattedText();
                    ft.color = color.name().toLowerCase();
                } else {
                    ft.removeFormat();
                }
            }


            ft.text += out[i].substring(1);
        }
        if (!ft.text.isEmpty()) {
            fts.add(ft);
        }
        if (fts.size() > 1) {
            StringBuilder json = new StringBuilder();
            json.append("{\"extra\":[\"\"");
            for (FormattedText ct : fts) {
                json.append(',');
                json.append(ct.toString());
            }
            json.append("],\"text\":\"\"}");
            return json.toString();
        }

        return fts.size() == 0 ? "\"\"" : fts.get(0).toString();
    }

    public enum ChatMessageType {
        CHAT, SYSTEM, ACTION_BAR;

        ChatMessageType() {
        }
    }

    public static class FormattedText {
        public String text = "";
        public String color;
        public boolean bold;
        public boolean italic;
        public boolean underlined;
        public boolean strikethrought;
        public boolean obfuscated;


        public FormattedText() {
        }

        public FormattedText(FormattedText ft) {
            this.color = ft.color;
            this.bold = ft.bold;
            this.italic = ft.italic;
            this.underlined = ft.underlined;
            this.strikethrought = ft.strikethrought;
            this.obfuscated = ft.obfuscated;
        }

        public void removeFormat() {
            this.bold = (this.italic = this.underlined = this.strikethrought = this.obfuscated = false);
        }

        public String toString() {
            if (this.color == null) {
                if (((((this.bold == this.italic) == this.underlined) == this.strikethrought) == this.obfuscated ? 1 : 0) == 0)
                    return ChatAPI.quoteJson(this.text);
            }
            StringBuilder out = new StringBuilder();
            out.append("{\"text\":").append(ChatAPI.quoteJson(this.text));
            if (this.color != null) {
                out.append(",\"color\":\"").append(this.color).append('"');
            }
            if (this.bold) {
                out.append(",\"bold\":true");
            }
            if (this.italic) {
                out.append(",\"italic\":true");
            }
            if (this.underlined) {
                out.append(",\"underlined\":true");
            }
            if (this.strikethrought) {
                out.append(",\"strikethrought\":true");
            }
            if (this.obfuscated) {
                out.append(",\"obfuscated\":true");
            }
            return out.append('}').toString();
        }
    }

    public static class Event {
        private final String action;
        private final String value;

        public Event(String action, String value) {
            this.action = action;
            this.value = value;
        }

        public String toString() {
            return "{\"action\":\"" + this.action + "\",\"value\":" + this.value + "}";
        }
    }

    public static class ChatPart {
        private final ChatAPI.Event hoverEvent;
        private final ChatAPI.Event clickEvent;
        private final String insertion;
        private final String text;

        public ChatPart(String textJson, ChatAPI.Event hover, ChatAPI.Event click, String insertion) {
            this.text = textJson;
            this.clickEvent = click;
            this.hoverEvent = hover;
            this.insertion = insertion;
        }

        public String toString() {
            if ((this.hoverEvent == null) && (this.clickEvent == null) && (this.insertion == null))
                return this.text;
            StringBuilder out = new StringBuilder(this.text);
            if (out.charAt(0) == '"') {
                out = new StringBuilder("{\"text\":" + this.text);
            } else {
                out.deleteCharAt(out.length() - 1);
            }
            if (this.hoverEvent != null)
                out.append(",\"hoverEvent\":").append(this.hoverEvent.toString());
            if (this.clickEvent != null)
                out.append(",\"clickEvent\":").append(this.clickEvent.toString());
            if (this.insertion != null)
                out.append(",\"insertion\":").append(ChatAPI.quoteJson(this.insertion));
            return out.append('}').toString();
        }
    }
}



/* Location:           D:\pluginok\ServerLib\out\artifacts\SpigotLib.jar

 * Qualified Name:     gyurix.spigotlib.ChatAPI

 * JD-Core Version:    0.7.0.1

 */