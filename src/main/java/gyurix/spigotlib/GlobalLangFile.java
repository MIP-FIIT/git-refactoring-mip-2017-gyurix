package gyurix.spigotlib;

import gyurix.chat.ChatTag;
import gyurix.configfile.ConfigData;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.ChatAPI.ChatMessageType;
import gyurix.spigotutils.ServerVersion;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class GlobalLangFile {
    public static final HashMap<String, HashMap<String, String>> map = new HashMap<>();

    public static String get(String lang, String adr) {
        HashMap<String, String> m;
        String msg;
        if (lang != null && !lang.isEmpty()) {
            m = map.get(lang);
            if (m != null) {
                msg = m.get(adr);
                if (msg != null) {
                    return msg;
                }
                if (Config.debug)
                    SU.log(Main.pl, "§cThe requested key (" + adr + ") is missing from language " + lang + ". Using servers default language...");
            }
            SU.log(Main.pl, "§cThe requested language (" + lang + ") is not available.");
        }
        if ((m = map.get(Config.defaultLang)) != null) {
            msg = m.get(adr);
            if (msg != null) {
                return msg;
            }
            if (Config.debug)
                SU.log(Main.pl, "§cThe requested key (" + adr + ") is missing from servers default language (" + Config.defaultLang + "). Trying to find it in any other language...");
        }
        for (HashMap<String, String> l : map.values()) {
            String msg2 = l.get(adr);
            if (msg2 == null) continue;
            return msg2;
        }
        SU.log(Main.pl, "§cThe requested key (" + adr + ") wasn't found in any language.");
        return "§cNot found (§f" + lang + "." + adr + "§c)\\-T§eClick here to set the missing language data\\-S/sl lang " + lang + "." + adr + " ";
    }

    public static PluginLang loadLF (String pn, InputStream stream, String fn) {
        try {
            byte[] bytes = new byte[stream.available()];
            stream.read(bytes);
            load(new String(bytes, "UTF-8").split("\r?\n"));
            load(new String(Files.readAllBytes(new File(fn).toPath()), "UTF-8").split("\r?\n"));
            return new PluginLang(pn);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void load(String[] data) {
        String adr = ".en";
        StringBuilder cs = new StringBuilder();
        int lvl = 0;
        int line = 0;
        for (String s : data) {
            int blockLvl = 0;
            ++line;
            while (s.charAt(blockLvl) == ' ') {
                ++blockLvl;
            }
            String[] d = ((s = s.substring(blockLvl)) + " ").split(" *: +", 2);
            if (d.length == 1) {
                s = ConfigData.unescape(s);
                if (cs.length() != 0) {
                    cs.append('\n');
                }
                cs.append(s);
                continue;
            }
            put(adr.substring(1), cs.toString());
            cs.setLength(0);
            if (blockLvl == lvl + 2) {
                adr = adr + "." + d[0];
                lvl += 2;
            } else if (blockLvl == lvl) {
                adr = adr.substring(0, adr.lastIndexOf(".") + 1) + d[0];
            } else if (blockLvl < lvl && blockLvl % 2 == 0) {
                while (blockLvl != lvl) {
                    lvl -= 2;
                    adr = adr.substring(0, adr.lastIndexOf("."));
                }
                adr = adr.substring(0, adr.lastIndexOf(".") + 1) + d[0];
            } else {
                throw new RuntimeException("Block leveling error in line " + line + "!");
            }
            if (d[1].isEmpty()) continue;
            cs.append(d[1].substring(0, d[1].length() - 1));
        }
        put(adr.substring(1), cs.toString());
    }

    private static void put (String adr, String value) {
        if (!adr.contains(".")) {
            if (!map.containsKey(adr)) {
                map.put(adr, new HashMap());
            }
        } else {
            HashMap<String, String> m = map.get(adr.substring(0, adr.indexOf(".")));
            m.put(adr.substring(adr.indexOf(".") + 1), value);
        }
    }

    public static PluginLang loadLF(String pn, String fn) {
        try {
            load(new String(Files.readAllBytes(new File(fn).toPath()), "UTF-8").split("\r?\n"));
            return new PluginLang(pn);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void unloadLF(PluginLang lng) {
        for (HashMap<String, String> m : map.values()) {
            Iterator<Entry<String, String>> i = m.entrySet().iterator();
            while (i.hasNext()) {
                Entry<String, String> e = i.next();
                if (!e.getKey().matches(".*\\." + lng.pln + ".*")) continue;
                i.remove();
            }
        }
    }

    public static class PluginLang {
        public final String pln;
        public final String prefixLink = "prefix";

        private PluginLang(String plugin) {
            pln = plugin;
        }

        public String get(Player plr, String adr, String... repl) {
            String msg = GlobalLangFile.get(SU.getPlayerConfig(plr).getString("lang"), pln + "." + adr);
            for (String s : (">" + msg + "<").split(">[^<]*<")) {
                String r;
                int spaceID = s.indexOf(" ");
                String name = spaceID == -1 ? s : s.substring(0, spaceID);
                String[] pm = spaceID == -1 ? new String[]{} : s.substring(spaceID + 1).split(" ");
                if (name.startsWith("*")) {
                    msg = msg.replace("<" + s + ">", get(plr, name.substring(1), pm));
                    continue;
                }
                int replNameId = ArrayUtils.indexOf(repl, name) + 1;
                if (replNameId == repl.length || replNameId == 0) continue;
                msg = msg.replace("<" + s + ">", (r = repl[replNameId]).startsWith(".") ? get(plr, r.substring(1), repl) : r);
            }
            return msg;
        }

        public void msg(CommandSender sender, String msg, String... repl) {
            msg(get(sender instanceof Player ? (Player) sender : null, "prefix"), sender, msg, repl);
        }

        public void msg(String prefix, CommandSender sender, String msg, String... repl) {
            Player plr = sender instanceof Player ? (Player) sender : null;
            msg = prefix + get(plr, msg, repl);
            if (plr == null || Reflection.ver.isBellow(ServerVersion.v1_7)) {
                sender.sendMessage(ChatTag.stripExtras(msg));
            } else {
                ChatAPI.sendJsonMsg(ChatMessageType.CHAT, msg, plr);
            }
        }

    }

}

