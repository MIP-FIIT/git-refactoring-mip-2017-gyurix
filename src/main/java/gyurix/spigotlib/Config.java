package gyurix.spigotlib;

import gyurix.api.BungeeAPI;
import gyurix.commands.Command;
import gyurix.configfile.ConfigSerialization;
import gyurix.configfile.PostLoadable;
import gyurix.economy.EconomyAPI;
import gyurix.mysql.MySQLDatabase;
import gyurix.spigotutils.BackendType;
import gyurix.spigotutils.TPSMeter;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class Config {
    @ConfigSerialization.ConfigOptions(comment = "The version of the current config. You should NOT change this value.")
    public static int version;
    @ConfigSerialization.ConfigOptions(comment = "Path for automatic SpigotLib config backups on every save.")
    public static String backup;
    @ConfigSerialization.ConfigOptions(comment = "Log all the commands with time and location from both player, console and cmd blocks.")
    public static boolean commandLog = true;
    @ConfigSerialization.ConfigOptions(comment = "Debug mode, if disabled, then some of the stack traces won't be visible in console.")
    public static boolean debug;
    @ConfigSerialization.ConfigOptions(comment = "Servers default language.")
    public static String defaultLang;
    @ConfigSerialization.ConfigOptions(comment = "Hook to the clips PlaceholderAPI in order to obtain more variables.")
    public static boolean phaHook = false;
    @ConfigSerialization.ConfigOptions(comment = "Make tab completion to need the tab.complete perm.")
    public static boolean tabCompletePerm;
    @ConfigSerialization.ConfigOptions(comment = "Settings of SpigotLibs chat management and longer chat features.")
    public static Chat chat;
    @ConfigSerialization.ConfigOptions(comment = "Enable / disable packet events which costs some performance, but gives\n" +
            "ability for developers to catch packets and code awesome plugin features :)\n" +
            "If you disable the packetAPI, the following features of SpigotLib won't work: AntiItemHack, ConnectionLog, ChatManagement")
    public static boolean packetAPI;
    @ConfigSerialization.ConfigOptions(comment = "Here you are able to setup custom commands for your server. The commands\n" +
            "syntax is based on the SpigotLibs CommandAPI. The perms for each commands are customcommand.<commandname>")
    public static HashMap<String, ArrayList<Command>> customCommands;
    @ConfigSerialization.ConfigOptions(comment = "Allows sync and async metrics of the truth server tps, for the <tps>\n" +
            "variable, it also helps to detect informations about possible server crashes.")
    public static TPSMeter tpsMeter;
    @ConfigSerialization.ConfigOptions(comment = "Log the IP addresses, names and UUIDs of everyone, who try to join/ping to the server")
    public static ConnectionLog connectionLog;
    @ConfigSerialization.ConfigOptions(comment = "Settings for custom TabComplete management")
    public static TabComplete tabComplete;
    @ConfigSerialization.ConfigOptions(comment = "Settings of BungeeAPI")
    public static BungeeAPI.BungeeSettings bungeeSettings;
    @ConfigSerialization.ConfigOptions(comment = "PlayerFile settings")
    public static PlayerFile playerFile;
    @ConfigSerialization.ConfigOptions(comment = "EconomyAPI settings")
    public static EconomyAPI economy;
    @ConfigSerialization.ConfigOptions(comment = "Error log, please report all of these errors to plugins dev, gyuriX.")
    public static ArrayList<String> errors;
    @ConfigSerialization.ConfigOptions(comment = "Deny the placement of signs with extra json data, colored text, too long text")
    public static AntiSignHack antiSignHack;
    @ConfigSerialization.ConfigOptions(comment = "Packet name mapping for PacketAPI to reach compatibility between different Minecraft versions.\nDON'T CHANGE IT, IF YOU DON'T KNOW EXACTLY WHAT ARE YOU DOING!")
    public static HashMap<String, HashMap<String, String>> packetMapping;


    public static class ConnectionLog implements PostLoadable {
        public static boolean enabled;
        @ConfigSerialization.ConfigOptions(comment = "You can blacklist here some ips, which you would not like to see in the log.")
        public static ArrayList<String> blacklist;
        public static String ping;
        public static String pingmore;
        public static String login;
        public static String loginmore;
        public static String loginunknown;
        public static String pingunknown;
        public static String time;

        @Override
        public void postLoad() {
            File f = new File(Main.pl.getDataFolder() + File.separator + "connections.log");
            try {
                f.createNewFile();
                gyurix.spigotlib.features.ConnectionLog.logFile = new PrintWriter(new FileOutputStream(f, true), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class AntiSignHack {
        public static boolean enabled;
        public static int limit;
    }

    public static class AntiItemHack {
        public static boolean enabled;
        public static String helmets;
        public static String chestplates;
        public static String leggings;
        public static String boots;
        public static ArrayList<ItemStack> whitelist;

        static {
            whitelist = new ArrayList();
        }
    }

    public static class Chat {
        @ConfigSerialization.ConfigOptions(comment = "Enable SpigotLib chat management.")
        public static boolean enabled;
        @ConfigSerialization.ConfigOptions(comment = "List here all the color codes, that should be replaced, if the player has chat.color.<colorcode> perm.")
        public static String colors;
        @ConfigSerialization.ConfigOptions(comment = "The prefix before colorcodes.")
        public static String colorPrefix;
        @ConfigSerialization.ConfigOptions(comment = "Set the new line character, set it to empty for disabling this feature.\nYou need the chat.multiline permission for using this character.")
        public static String newLineCharacter;
        @ConfigSerialization.ConfigOptions(comment = "Allow writing empty lines with more then one, newline characters, if you have the chat.emptyLines permission.")
        public static boolean allowEmptyLines;
        @ConfigSerialization.ConfigOptions(comment = "Configuration for long messages.")
        public static Long longMessage;

        public static class Long {
            @ConfigSerialization.ConfigOptions(comment = "Enable using this feature, if you have chat.longer perm.")
            public static boolean enabled;
            @ConfigSerialization.ConfigOptions(comment = "If you write more, than the bellow set amount of characters, your message will be puffered, if not, your whole message will be sent.")
            public static int pufferAfter;
            @ConfigSerialization.ConfigOptions(comment = "Limit maximum length of longer messages, per permissionly (chat.longerlimit.<group> permissions)")
            public static HashMap<String, Integer> lengthLimit;
        }

    }

    public static class TabComplete {
        public static boolean enabled, caseSensitive;
        public static HashMap<String, TabCompleteGroup> groups;

        public TabComplete() {
        }

        public static class TabCompleteGroup {
            public ArrayList<Command> chat = new ArrayList<>(), command = new ArrayList<>(), wrongchat = new ArrayList<>(), wrongcmd = new ArrayList<>();
            public HashMap<String, ArrayList<Command>> prefixes = new HashMap<>();

            public TabCompleteGroup() {

            }
        }
    }

    public static class PlayerFile {
        public static String file = "players.yml";
        public static MySQLDatabase mysql;
        public static BackendType backend;
    }
}

