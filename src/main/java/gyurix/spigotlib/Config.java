package gyurix.spigotlib;

import gyurix.configfile.ConfigSerialization;
import gyurix.economy.EconomyAPI;
import gyurix.mysql.MySQLDatabase;
import gyurix.spigotutils.BackendType;
import gyurix.spigotutils.TPSMeter;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.HashMap;

public class Config {
    public static BungeeAPI bungeeAPI;
    @ConfigSerialization.ConfigOptions(comment = "Debug mode, if disabled, then some of the stack traces won't be visible in console.")
    public static boolean debug;
    @ConfigSerialization.ConfigOptions(comment = "Servers default language.")
    public static String defaultLang = "en";
    @ConfigSerialization.ConfigOptions(comment = "EconomyAPI settings")
    public static EconomyAPI economy = new EconomyAPI();
    @ConfigSerialization.ConfigOptions(serialize = false)
    public static HashMap<String, Enchantment> enchantAliases = new HashMap<>();
    @ConfigSerialization.ConfigOptions(serialize = false)
    public static HashMap<String, ArrayList<String>> enchants = new HashMap<>();
    @ConfigSerialization.ConfigOptions(comment = "Enable / disable packet events which costs some performance, but gives\n" +
            "ability for developers to catch packets and code awesome plugin features :)")
    public static boolean packetAPI;
    @ConfigSerialization.ConfigOptions(comment = "Hook to the clips PlaceholderAPI in order to obtain more variables.")
    public static boolean phaHook = false;
    @ConfigSerialization.ConfigOptions(comment = "PlayerFile settings")
    public static PlayerFile playerFile;
    public static String start = "§eSzerver indítása...";
    @ConfigSerialization.ConfigOptions(comment = "Allows sync and async metrics of the truth server tps, for the <tps>\n" +
            "variable, it also helps to detect informations about possible server crashes.")
    public static TPSMeter tpsMeter;

    public Config() {
    }

    public static class BungeeAPI {
        public static boolean ipOnJoin, uuidOnJoin;
        public static int playerCount, playerList, servers, currentServerName, uuidAll, serverIP;
    }

    public static class PlayerFile {
        public static BackendType backend;
        public static String file = "players.yml";
        public static MySQLDatabase mysql;
    }
}

