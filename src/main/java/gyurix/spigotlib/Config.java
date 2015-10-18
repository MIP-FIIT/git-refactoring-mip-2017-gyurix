package gyurix.spigotlib;

import gyurix.configfile.ConfigSerialization;
import gyurix.economy.EconomyAPI;
import gyurix.spigotutils.TPSMeter;
import org.bukkit.inventory.ItemStack;

import java.util.*;


public class Config {
    @ConfigSerialization.ConfigOptions(comment = "Servers default language.")
    public static String defaultLang;
    @ConfigSerialization.ConfigOptions(comment = "Configurations version.")
    public static int version;
    @ConfigSerialization.ConfigOptions(comment = "Tab completion will need the tab.complete perm.")
    public static boolean tabCompletePerm;

    @ConfigSerialization.ConfigOptions(comment = "Path for auto backups on every save.")
    public static String backup;
    @ConfigSerialization.ConfigOptions(comment = "Debug mode, use it for error reporting.")
    public static boolean debug = false;
    @ConfigSerialization.ConfigOptions(comment = "Chat settings.")
    public static Chat chat;
    @ConfigSerialization.ConfigOptions(comment = "Error log, please report all of these errors to plugins dev, gyuriX.")
    public static ArrayList<String> errors = new ArrayList<String>();
    public static TPSMeter tpsMeter= new TPSMeter();
    public static ConnectionLog connectionLog;
    public static AntiItemHack antiItemHack;
    public static AntiSignHack antiSignHack;
    public static EconomyAPI economy;

    public static HashMap<String,String[]> books;
    @ConfigSerialization.ConfigOptions(comment = "Packet name mapping for PacketAPI to reach compatibility between different Minecraft versions.\n" +
            "DON'T CHANGE IT, IF YOU DON'T KNOW EXACTLY WHAT ARE YOU DOING!")
    public static HashMap<String,HashMap<String,String>> packetMapping;

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
    public static class AntiItemHack{
        public static boolean enabled;
        public static String helmets;
        public static String chestplates;
        public static String leggings;
        public static String boots;
        public static ArrayList<ItemStack> whitelist=new ArrayList<ItemStack>();
    }

    public static class AntiSignHack {
        public static boolean enabled;
        public static int limit;
    }

    public static class ConnectionLog {
        public static boolean enabled;
        @ConfigSerialization.ConfigOptions(comment = "You can blacklist here some ips, which you would not like to see in the log.")
        public static ArrayList<String> blacklist=new ArrayList<String>();
        public static String ping,pingmore="[ConnectionLog] One of the following players tried to ping to the server from ip <ip>:",
                login,loginmore="[ConnectionLog] One of the following players tried to login to the server from ip <ip>:",
                loginunknown,pingunknown;
        public static HashMap<String,HashSet<UUID>> ipUUIDBase= new HashMap<String, HashSet<UUID>>();
        public ConnectionLog(){

        }
    }

}