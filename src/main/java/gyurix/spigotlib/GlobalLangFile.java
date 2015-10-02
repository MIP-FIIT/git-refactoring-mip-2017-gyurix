package gyurix.spigotlib;

import gyurix.configfile.ConfigData;
import gyurix.utils.ArrayUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

/**
 * The global language file containing all the PluginLangs
 */
public class GlobalLangFile {
    /**
     * The Actual storage of the Strings from the language files
     */
    public static final HashMap<String,HashMap<String,String>> map = new HashMap<>();

    /**
     * Get the String of the given address and language from any kind of plugins language.
     * Used for getting the strings of the plugin langs.
     * @param lang requested language
     * @param adr requested address
     * @return the output String
     */
    public static String get(String lang, String adr) {
        if (lang!=null&&!lang.isEmpty()){
            HashMap<String,String> m = map.get(lang);
            if (m!=null){
                String msg=m.get(adr);
                if (msg!=null){
                    return msg;
                }
                Main.log.severe("§cThe requested key ("+adr+") is missing from language "+lang+". Using servers default language...");
            }
            Main.log.severe("§cThe requested language ("+lang+") is not available.");
        }
        HashMap<String,String> m=map.get(Config.defaultLang);
        if (m!=null){
            String msg=m.get(adr);
            if (msg!=null){
                return msg;
            }
            Main.log.severe("§cThe requested key ("+adr+") is missing from servers default language ("+Config.defaultLang+"). Trying to find it in any other language...");
        }
        for (HashMap<String,String> l:map.values()){
            String msg=l.get(adr);
            if (msg!=null){
                return msg;
            }
        }
        Main.log.severe("§cThe requested key ("+adr+") wasn't found in any language.");
        return lang+"."+adr;
    }

    /**
     * Loads a language file of a plugin.
     * @param pn name of the plugin, used in the plugins language file
     * @param fn filename of the language file (usually getDataFolder()+File.separator+"lang.yml")
     * @return The PluginLang object representing the language file
     */
    public static PluginLang loadLF(String pn, String fn) {
        try {
            load(new String(Files.readAllBytes(new File(fn).toPath()), "UTF-8").split("\r?\n"));
            return new PluginLang(pn);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Loads the built in (language file in the plugins jar file)
     * and the external (file in the plugins folder) language file of a plugin
     * @param pn name of the plugin, used in the plugins language file
     * @param stream input stream of the built in language file (usually getResource("lang.yml"))
     * @param fn external filename of the language file (usually getDataFolder()+File.separator+"lang.yml")
     * @return the PluginLang object representing the plugins language file
     */
    public static PluginLang loadLF(String pn, InputStream stream, String fn) {
        try {
            byte[] bytes=new byte[stream.available()];
            stream.read(bytes);
            load(new String(bytes, "UTF-8").split("\r?\n"));
            load(new String(Files.readAllBytes(new File(fn).toPath()), "UTF-8").split("\r?\n"));
            return new PluginLang(pn);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Unloads the language file of a plugin, you need to use this when your plugin
     * is on disable or when you would like to reload it
     * @param lng
     */
    public static void unloadLF(PluginLang lng) {
        for (HashMap<String,String> m:map.values()){
            Iterator<Map.Entry<String, String>> i = m.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<String, String> e = i.next();
                if (e.getKey().matches(".*\\." + lng.pln + ".*")) {
                    i.remove();
                }
            }
        }
    }
    private static void put(String adr, String value){
        if (!adr.contains(".")){
            if (!map.containsKey(adr))
                map.put(adr,new HashMap<String, String>());
        }
        else{
            HashMap<String,String> m=map.get(adr.substring(0,adr.indexOf(".")));
            m.put(adr.substring(adr.indexOf(".")+1),value);
        }
    }
    private static void load(String[] data) {
        String adr = ".en";
        StringBuilder cs = new StringBuilder();
        int lvl = 0;
        int line=0;
        for (String s : data) {
            ++line;
            int blockLvl = 0;
            while ((s.length() > blockLvl) && (s.charAt(blockLvl) == ' '))
                blockLvl++;
            s = s.substring(blockLvl);
            String[] d = (s + " ").split(" *: +", 2);
            if (d.length == 1) {
                s = ConfigData.unescape(s);
                if (cs.length() != 0)
                    cs.append('\n');
                cs.append(s);
            } else {
                put(adr.substring(1), cs.toString());
                cs.setLength(0);
                if (blockLvl == lvl + 2) {
                    adr = adr + "." + d[0];
                    lvl += 2;
                } else if (blockLvl == lvl) {
                    adr = adr.substring(0, adr.lastIndexOf(".") + 1) + d[0];
                } else if ((blockLvl < lvl) && (blockLvl % 2 == 0)) {
                    while (blockLvl != lvl) {
                        lvl -= 2;
                        adr = adr.substring(0, adr.lastIndexOf("."));
                    }
                    adr = adr.substring(0, adr.lastIndexOf(".") + 1) + d[0];
                } else {
                    throw new RuntimeException("Block leveling error in line "+line+"!");
                }
                if (!d[1].isEmpty())
                    cs.append(d[1].substring(0, d[1].length() - 1));
            }
        }
        put(adr.substring(1), cs.toString());
    }

    /**
     * The representation of a plugis language file
     */
    public static class PluginLang {
        /**
         * Name of the plugin
         */
        public final String pln;
        /**
         * Link for the plugins prefix
         */
        public final String prefixLink = "prefix";

        private PluginLang(String plugin) {
            this.pln = plugin;
        }

        /**
         * Used for getting a String from the language file.
         * @param plr the Player
         * @param adr the address of the string
         * @param repl the replacements in a form of
         *             replaceableString1,replacementString1,
         *             replaceableString2,replacementString2
         * @return The requested String
         */
        public String get(Player plr, final String adr, String... repl) {
            String msg = GlobalLangFile.get(SU.getPlayerConfig(plr).getString("lang"),pln+"."+adr);
            for (String s : (">" + msg + "<").split(">[^<]*<")) {
                int spaceID = s.indexOf(" ");
                String name = spaceID == -1 ? s : s.substring(0, spaceID);
                String[] pm = spaceID == -1 ? new String[0] : s.substring(spaceID + 1).split(" ");
                if (name.startsWith("*")) {
                    msg = msg.replace("<" + s + ">", get(plr, name.substring(1), pm));
                } else {
                    int replNameId = ArrayUtils.indexOf(repl, name) + 1;
                    if ((replNameId != repl.length) && (replNameId != 0)) {
                        String r = repl[replNameId];
                        msg = msg.replace("<" + s + ">", r.startsWith(".") ? get(plr, r.substring(1), repl) : r);
                    }
                }
            }
            return msg;
        }

        /**
         * Used for sending a message from the languge file to a CommandSender
         * @param sender CommandSender
         * @param msg message
         * @param repl the replacements in a form of
         *             replaceableString1,replacementString1,
         *             replaceableString2,replacementString2
         */
        public void msg(CommandSender sender, String msg, String... repl) {
            msg(get((sender instanceof Player) ? (Player) sender : null, this.prefixLink), sender, msg, repl);
        }

        /**
         * Used for sending a message from the languge file to a CommandSender with the given prefix
         * @param sender CommandSender
         * @param msg message
         * @param repl the replacements in a form of
         *             replaceableString1,replacementString1,
         *             replaceableString2,replacementString2
         */
        public void msg(String prefix, CommandSender sender, String msg, String... repl) {
            Player plr = (sender instanceof Player) ? (Player) sender : null;
            msg = prefix + get(plr, msg, repl);
            if (plr == null) {
                sender.sendMessage(msg);
            } else {
                ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.CHAT, msg, plr);
            }
        }
    }
}