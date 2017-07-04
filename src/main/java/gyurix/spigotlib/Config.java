package gyurix.spigotlib;

import gyurix.economy.EconomyAPI;
import gyurix.mysql.MySQLDatabase;
import gyurix.spigotutils.BackendType;
import gyurix.spigotutils.TPSMeter;

public class Config {
    public static int animationApiThreads;
    public static BungeeAPI bungee;
    public static boolean debug;
    public static String defaultLang = "en";
    public static boolean disableWeatherChange, forceReducedMode, hideLogo;
    public static EconomyAPI economy = new EconomyAPI();
    public static boolean phaHook, silentErrors;
    public static boolean playerEval, allowAllPermsForAuthor;
    public static PlayerFile playerFile;
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

