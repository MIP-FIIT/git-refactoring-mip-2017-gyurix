package gyurix.spigotlib;

import gyurix.configfile.ConfigSerialization.ConfigOptions;
import gyurix.economy.EconomyAPI;
import gyurix.mysql.MySQLDatabase;
import gyurix.spigotutils.BackendType;
import gyurix.spigotutils.BlockData;
import gyurix.spigotutils.TPSMeter;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class Config {
    public static int animationApiThreads;
    public static HashMap<BlockData, ItemStack> blocks = new HashMap<>();
    public static BungeeAPI bungee;
    @ConfigOptions(comment = "Debug mode, if disabled, then some of the stack traces won't be visible in console.")
    public static boolean debug;
    @ConfigOptions(comment = "Servers default language.")
    public static String defaultLang = "en";
    public static boolean disableWeatherChange, forceReducedMode;
    public static String earlyJoinKickMsg = "§eStarting server...";
    public static int earlyJoinProtection = 30;
    @ConfigOptions(comment = "EconomyAPI settings")
    public static EconomyAPI economy = new EconomyAPI();
    @ConfigOptions(comment = "Hook to the clips PlaceholderAPI in order to obtain more variables.")
    public static boolean phaHook;
    public static boolean playerEval, allowAllPermsForAuthor;
    @ConfigOptions(comment = "PlayerFile settings")
    public static PlayerFile playerFile;
    @ConfigOptions(comment = "Allows sync and async metrics of the truth server tps, for the <tps>\n" +
            "variable, it also helps to detect informations about possible server crashes.")
    public static TPSMeter tpsMeter;

    public Config() {
    }

    public static class BungeeAPI {
        public static boolean ipOnJoin, uuidOnJoin, forceEnable;
        public static int playerCount, playerList, servers, currentServerName, uuidAll, serverIP;
    }

    public static class PlayerFile {
        public static BackendType backend;
        public static String file = "players.yml";
        public static MySQLDatabase mysql;
    }
}

