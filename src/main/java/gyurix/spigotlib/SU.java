package gyurix.spigotlib;

import com.google.common.collect.Lists;
import gyurix.configfile.ConfigFile;
import gyurix.protocol.Protocol;
import gyurix.protocol.Reflection;
import gyurix.protocol.utils.GameProfile;
import gyurix.spigotutils.BackendType;
import gyurix.spigotutils.ServerVersion;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

import javax.script.ScriptEngine;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Logger;

/**
 * SpigotLib utilities class
 */
public final class SU {
    /**
     * The instance of current Chat provider in Vault
     */
    public static Chat chat;
    /**
     * The main instance of the ConsoleCommandSender object.
     */
    public static ConsoleCommandSender cs;
    /**
     * The instance of current Economy provider in Vault
     */
    public static Economy econ;
    /**
     * An instance of the Javascript script engine, used for the eval variable
     */
    public static ScriptEngine js;
    /**
     * The main instance of the Messenger object.
     */
    public static Messenger msg;
    /**
     * The instance of current Permission provider in Vault
     */
    public static Permission perm;
    /**
     * Player configuration file instance (players.yml file in the SpigotLib)
     */
    public static ConfigFile pf;
    /**
     * The main instance of the PluginManager object.
     */
    public static PluginManager pm;
    /**
     * An instance of the Random number generator
     */
    public static Random rand = new Random();
    /**
     * The main instance of the BukkitScheduler object.
     */
    public static BukkitScheduler sch;
    /**
     * The main instance of the ServicesManager object
     */
    public static ServicesManager sm;
    /**
     * The main instance of the CraftServer object.
     */
    public static Server srv;
    /**
     * PacketAPI instance
     */
    public static Protocol tp;
    /**
     * True if Vault is found on the server
     */
    public static boolean vault;
    private static Field entityF, pingF;
    private static Constructor entityPlayerC, playerInterractManagerC;
    private static Method getBukkitEntityM, loadDataM, saveDataM;
    private static Object worldServer, mcServer;

    /**
     * A truth check if an iterable contains the given typed item or not
     *
     * @param source ItemStack iterable
     * @param is     checked ItemStack
     * @return True if the ItemStack iterable contains the checked ItemStack in any amount, false otherwise.
     */
    public static boolean containsItem(Iterable<ItemStack> source, ItemStack is) {
        for (ItemStack i : source) {
            if (itemSimiliar(i, is))
                return true;
        }
        return false;
    }

    /**
     * Sends an error report to the given sender and to console. The report only includes the stack trace parts, which
     * contains the authors name
     *
     * @param sender - The CommandSender who should receive the error report
     * @param err    - The error
     * @param plugin - The plugin where the error appeared
     * @param author - The author name, which will be searched in the error report
     */
    public static void error(CommandSender sender, Throwable err, String plugin, String author) {
        StringBuilder report = new StringBuilder();
        report.append("§4§l").append(plugin).append(" - ERROR REPORT - ")
                .append(err.getClass().getSimpleName());
        if (err.getMessage() != null)
            report.append('\n').append(err.getMessage());
        int i = 0;
        for (StackTraceElement el : err.getStackTrace()) {
            if (el.getClassName() != null && el.getClassName().contains(author))
                report.append("\n§c #").append(++i)
                        .append(": §eLINE §a").append(el.getLineNumber())
                        .append("§e in FILE §6").append(el.getFileName())
                        .append("§e (§7").append(el.getClassName())
                        .append("§e.§b").append(el.getMethodName())
                        .append("§e)");
        }
        String rep = report.toString();
        SU.cs.sendMessage(rep);
        if (sender != null && sender != SU.cs)
            sender.sendMessage(rep);
    }

    /**
     * Escape multi line text to a single line one
     *
     * @param text multi line escapeable text input
     * @return The escaped text
     */
    public static String escapeText(String text) {
        return text.replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("|", "\\|")
                .replace(" ", "_")
                .replace("\n", "|");
    }

    /**
     * Fills variables in a String
     *
     * @param s    - The String
     * @param vars - The variables and their values, which should be filled
     * @return The variable filled String
     */
    public static String fillVariables(String s, final Object... vars) {
        String last = null;
        for (Object v : vars) {
            if (last == null)
                last = (String) v;
            else {
                s = s.replace("<" + last + ">", String.valueOf(v));
                last = null;
            }
        }
        return s;
    }

    /**
     * Fills variables in a String
     *
     * @param s    - The String
     * @param vars - The variables and their values, which should be filled
     * @return The variable filled String
     */
    public static String fillVariables(String s, final HashMap<String, Object> vars) {
        for (Map.Entry<String, Object> v : vars.entrySet())
            s = s.replace("<" + v.getKey() + ">", String.valueOf(v.getValue()));
        return s;
    }

    /**
     * Filters the startings of the given data
     *
     * @param data  - The data to be filtered
     * @param start - Filter every string which starts with this one
     * @return The filtered Strings
     */
    public static ArrayList<String> filterStart(String[] data, String start) {
        start = start.toLowerCase();
        ArrayList<String> ld = new ArrayList<>();
        for (String s : data) {
            if (s.toLowerCase().startsWith(start))
                ld.add(s);
        }
        Collections.sort(ld);
        return ld;
    }

    /**
     * Filters the startings of the given data
     *
     * @param data  - The data to be filtered
     * @param start - Filter every string which starts with this one
     * @return The filtered Strings
     */
    public static ArrayList<String> filterStart(Iterable<String> data, String start) {
        start = start.toLowerCase();
        ArrayList<String> ld = new ArrayList<>();
        for (String s : data) {
            if (s.toLowerCase().startsWith(start))
                ld.add(s);
        }
        Collections.sort(ld);
        return ld;
    }

    /**
     * Get the numeric id of the given itemname, it works for both numeric and text ids.
     *
     * @param name the case insensitive material name of the item or the numeric id of the item.
     * @return the numeric id of the requested item or 1, if the given name is incorrect or null
     */
    public static int getId(String name) {
        try {
            return Material.valueOf(name.toUpperCase()).getId();
        } catch (Throwable e) {
            try {
                return Integer.valueOf(name);
            } catch (Throwable e2) {
                return 1;
            }
        }
    }

    /**
     * Get the name of an offline player based on it's UUID.
     *
     * @param id UUID of the target player
     * @return The name of the requested player or null if the name was not found.
     */
    public static String getName(UUID id) {
        Player plr = Bukkit.getPlayer(id);
        if (plr != null)
            return plr.getName();
        OfflinePlayer op = Bukkit.getOfflinePlayer(id);
        if (op == null)
            return null;
        return op.getName();
    }

    /**
     * Get the ping of a player in milliseconds
     *
     * @param plr target player
     * @return The ping of the given player in milliseconds.
     */
    public static int getPing(Player plr) {
        try {
            return pingF.getInt(entityF.get(plr));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Get an online player or optionally load an offline player based on its name
     *
     * @param name name of the player, which should be got / loaded.
     * @return The online player / loaded offline player who has the given name, or null if no such player have found.
     */
    public static Player getPlayer(String name) {
        if (name.length() > 16) {
            UUID uuid = UUID.fromString(name);
            Player p = Bukkit.getPlayer(uuid);
            if (p == null)
                p = loadPlayer(uuid);
            return p;
        }
        Player p = Bukkit.getPlayer(name);
        if (p == null)
            p = loadPlayer(getUUID(name));
        return p;
    }

    /**
     * Get the configuration part of a player or the CONSOLE
     *
     * @param plr the player, whos configuration part will be returned
     * @return the configuration part of the given player, or the configuration part of the CONSOLE,
     * if the given player is null.
     */
    public static ConfigFile getPlayerConfig(Player plr) {
        return getPlayerConfig(plr == null ? null : plr.getUniqueId());
    }

    /**
     * Get the configuration part of an online/offline player using based on his UUID, or the
     * configuration part of the CONSOLE, if the given UUID is null.
     *
     * @param plr the UUID of the online/offline player
     * @return the configuration part of the given player, or the configuration part of the CONSOLE,
     * if the given player UUID is null.
     */
    public static ConfigFile getPlayerConfig(UUID plr) {
        String pln = plr == null ? "CONSOLE" : plr.toString();
        if (pf.data.mapData == null)
            pf.data.mapData = new LinkedHashMap();
        return pf.subConfig(pln);
    }

    /**
     * Get GameProfile of the given player. The GameProfile contains the players name, UUID and skin.
     *
     * @param plr target player
     * @return the GameProfile of the target player
     */
    public static GameProfile getProfile(Player plr) {
        try {
            return new GameProfile(plr.getClass().getMethod("getProfile").invoke(plr));
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the UUID of an offline player based on his name.
     *
     * @param name name of the target player
     * @return The UUID of the requested player, or null if it was not found.
     */
    public static UUID getUUID(String name) {
        Player plr = Bukkit.getPlayer(name);
        if (plr != null)
            return plr.getUniqueId();
        OfflinePlayer[] offlinePls = Bukkit.getOfflinePlayers();
        for (OfflinePlayer p : offlinePls) {
            if ((p.getName() != null) && (p.getName().equals(name)))
                return p.getUniqueId();
        }
        name = name.toLowerCase();
        for (OfflinePlayer p : offlinePls) {
            if ((p.getName() != null) && (p.getName().toLowerCase().equals(name)))
                return p.getUniqueId();
        }
        for (OfflinePlayer p : offlinePls) {
            if ((p.getName() != null) && (p.getName().toLowerCase().contains(name)))
                return p.getUniqueId();
        }
        return null;
    }

    static void initOfflinePlayerManager() {
        try {
            Class mcServerClass = Reflection.getNMSClass("MinecraftServer");
            Class entityPlayerClass = Reflection.getNMSClass("EntityPlayer");
            Class craftPlayerClass = Reflection.getOBCClass("entity.CraftPlayer");
            Class pIMClass = Reflection.getNMSClass("PlayerInteractManager");
            Class worldServerClass = Reflection.getNMSClass("WorldServer");

            entityF = Reflection.getField(Reflection.getOBCClass("entity.CraftEntity"), "entity");
            pingF = Reflection.getNMSClass("EntityPlayer").getField("ping");
            mcServer = mcServerClass.getMethod("getServer", new Class[0]).invoke(null);
            playerInterractManagerC = pIMClass.getConstructor(Reflection.getNMSClass("World"));
            worldServer = mcServerClass.getMethod("getWorldServer", Integer.TYPE).invoke(mcServer, 0);
            entityPlayerC = entityPlayerClass.getConstructor(mcServerClass, worldServerClass, Reflection.getUtilClass("com.mojang.authlib.GameProfile"), pIMClass);
            getBukkitEntityM = entityPlayerClass.getMethod("getBukkitEntity");
            loadDataM = craftPlayerClass.getMethod("loadData");
            saveDataM = craftPlayerClass.getMethod("saveData");
        } catch (Throwable e) {
            log(Main.pl, "§cError in initializing offline player manager.");
            error(cs, e, "SpigotLib", "gyurix");
        }
    }

    /**
     * A truth check for two items, if they are actually totally same or not
     *
     * @param item1 first item of the equal checking
     * @param item2 second item of the equal checking
     * @return True if the two itemstack contains exactly the same abilities (id, count, durability, metadata), false otherwise
     */
    public static boolean itemEqual(ItemStack item1, ItemStack item2) {
        return itemToString(item1).equals(itemToString(item2));
    }

    /**
     * A truth check for two items, if they type is actually totally same or not.
     * The only allowed difference between the stacks could be only their count.
     *
     * @param item1 first item of the similiar checking
     * @param item2 second item of the similiar checking
     * @return True if the two itemstack contains exactly the same abilities (id, durability, metadata),
     * the item counts could be different; false otherwise.
     */
    public static boolean itemSimiliar(ItemStack item1, ItemStack item2) {
        if (item1 == item2)
            return true;
        if (item1 == null || item2 == null)
            return false;
        item1 = item1.clone();
        item1.setAmount(1);
        item2 = item2.clone();
        item2.setAmount(1);
        return itemToString(item1).equals(itemToString(item2));
    }

    /**
     * Converts an ItemStack to it's representing string
     *
     * @param in convertable ItemStack
     * @return the conversion output String or "0:-1 0" if the given ItemStack is null
     */
    public static String itemToString(ItemStack in) {
        if (in == null)
            return "0:-1 0";
        StringBuilder out = new StringBuilder();
        out.append(in.getType().name());
        if (in.getDurability() != 0)
            out.append(':').append(in.getDurability());
        if (in.getAmount() != 1)
            out.append(' ').append(in.getAmount());
        ItemMeta meta = in.getItemMeta();
        if (meta == null)
            return out.toString();
        if (Reflection.ver == ServerVersion.v1_8 || Reflection.ver == ServerVersion.v1_9)
            for (ItemFlag f : meta.getItemFlags())
                out.append(" hide:").append(f.name().substring(5));
        if (meta.hasDisplayName())
            out.append(" name:").append(escapeText(meta.getDisplayName()));
        if (meta.hasLore())
            out.append(" lore:").append(escapeText(StringUtils.join(meta.getLore(), '\n')));
        for (Map.Entry<Enchantment, Integer> ench : meta.getEnchants().entrySet())
            out.append(' ').append(Config.enchants.get(ench.getKey().getName()).get(0)).append(':').append(ench.getValue());

        if (meta instanceof BookMeta) {
            BookMeta bmeta = (BookMeta) meta;
            if (bmeta.hasAuthor())
                out.append(" author:").append(bmeta.getAuthor());
            if (bmeta.hasTitle())
                out.append(" title:").append(bmeta.getTitle());
            for (String page : bmeta.getPages())
                out.append(" page:").append(escapeText(page));
        }
        if (Reflection.ver == ServerVersion.v1_8 || Reflection.ver == ServerVersion.v1_9)
            if (meta instanceof BannerMeta) {
                BannerMeta bmeta = (BannerMeta) meta;
                out.append(" color:").append(bmeta.getBaseColor() == null ? "BLACK" : bmeta.getBaseColor().name());
                for (Pattern p : bmeta.getPatterns())
                    out.append(' ').append(p.getPattern().getIdentifier()).append(":").append(p.getColor().name());
            }
        if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta bmeta = (LeatherArmorMeta) meta;
            Color c = bmeta.getColor();
            if (!c.equals(Bukkit.getItemFactory().getDefaultLeatherColor()))
                out.append(" color:").append(c.getRed()).append(',').append(c.getGreen()).append(',').append(c.getBlue());
        } else if (meta instanceof FireworkMeta) {
            FireworkMeta bmeta = (FireworkMeta) meta;
            out.append(" power:").append(bmeta.getPower());
            for (FireworkEffect e : bmeta.getEffects()) {
                out.append(' ').append(e.getType().name()).append(":");
                boolean pref = false;
                if (!e.getColors().isEmpty()) {
                    pref = true;
                    out.append("colors:");
                    for (Color c : e.getColors()) {
                        out.append(c.getRed()).append(',').append(c.getGreen()).append(',').append(c.getBlue()).append(';');
                    }
                    out.setLength(out.length() - 1);
                }
                if (!e.getFadeColors().isEmpty()) {
                    if (pref)
                        out.append("|");
                    else
                        pref = true;
                    out.append("fades:");
                    for (Color c : e.getFadeColors()) {
                        out.append(c.getRed()).append(',').append(c.getGreen()).append(',').append(c.getBlue()).append(';');
                    }
                    out.setLength(out.length() - 1);
                }
                if (e.hasFlicker()) {
                    if (pref)
                        out.append("|");
                    else
                        pref = true;
                    out.append("flicker");
                }
                if (e.hasTrail()) {
                    if (pref)
                        out.append("|");
                    out.append("trail");
                }
            }
        } else if (meta instanceof PotionMeta) {
            PotionMeta bmeta = (PotionMeta) meta;
            for (PotionEffect e : bmeta.getCustomEffects()) {
                out.append(' ').append(e.getType().getName()).append(':').append(e.getDuration()).append(':').append(e.getAmplifier());
                if (Reflection.ver == ServerVersion.v1_8 || Reflection.ver == ServerVersion.v1_9)
                    if (!e.hasParticles())
                        out.append(":np");
                if (!e.isAmbient())
                    out.append(":na");
            }
        } else if (meta instanceof SkullMeta) {
            SkullMeta bmeta = (SkullMeta) meta;
            if (bmeta.hasOwner())
                out.append(" owner:").append(escapeText(bmeta.getOwner()));
        } else if (meta instanceof EnchantmentStorageMeta) {
            for (Map.Entry<Enchantment, Integer> e : ((EnchantmentStorageMeta) meta).getStoredEnchants().entrySet()) {
                out.append(" +").append(e.getKey().getName()).append(':').append(e.getValue());
            }
        }

        return out.toString();
    }

    /**
     * Load an offline player to be handleable like an online one.
     *
     * @param uuid uuid of the loadable offline player
     * @return the loaded Player object, or null if the player was not found.
     */
    public static Player loadPlayer(UUID uuid) {
        try {
            if (uuid == null) {
                return null;
            }
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (player == null) {
                return null;
            }

            Player plr = (Player) getBukkitEntityM.invoke(entityPlayerC.newInstance(mcServer, worldServer, new GameProfile(player.getName(), uuid).toNMS(), playerInterractManagerC.newInstance(worldServer)));
            if (plr != null) {
                loadDataM.invoke(plr);
                return plr;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void loadPlayerConfig(UUID uid) {
        if (Config.PlayerFile.backend == BackendType.MYSQL) {
            String key = uid == null ? "CONSOLE" : uid.toString();
            pf.mysqlLoad(key, "uuid='" + key + "'");
        }
    }

    /**
     * Logs messages from the given plugin. You can use color codes in the msg.
     *
     * @param pl  - The plugin who wants to log the message
     * @param msg - The message which should be logged
     */
    public static void log(Plugin pl, Object... msg) {
        cs.sendMessage("[" + pl.getName() + "] " + StringUtils.join(msg, ", "));
    }

    /**
     * Logs messages from the given plugin. You can use color codes in the msg.
     *
     * @param pl  - The plugin who wants to log the message
     * @param msg - The message which should be logged
     */
    public static void log(Plugin pl, Iterable<Object>... msg) {
        cs.sendMessage("[" + pl.getName() + "] " + StringUtils.join(msg, ", "));
    }

    /**
     * Convertion of a collection of player UUIDs to the Arraylist containing the player names matching with the UUIDs.
     *
     * @param uuids collection of player uuids which will be converted to names
     * @return the convertion result, which is an ArrayList of player names
     */
    public static ArrayList<String> namesFromUUIDs(Collection<UUID> uuids) {
        ArrayList<String> out = new ArrayList<String>();
        for (UUID id : uuids) {
            out.add(getName(id));
        }
        return out;
    }

    /**
     * Convertion of a collection of player names to the Arraylist containing the player UUIDs matching with the names.
     *
     * @param names collection of player names which will be converted to UUIDs
     * @return the convertion result, which is an ArrayList of player UUIDs
     */
    public static ArrayList<UUID> namesToUUIDs(Collection<String> names) {
        ArrayList<UUID> out = new ArrayList<UUID>();
        for (String s : names) {
            out.add(getUUID(s));
        }
        return out;
    }

    /**
     * Optimizes color and formatting code usage in a string by removing redundant color/formatting codes
     *
     * @param in input message containing color and formatting codes
     * @return The color and formatting code optimized string
     */
    public static String optimizeColorCodes(String in) {
        StringBuilder formats = new StringBuilder();
        StringBuilder newformats = new StringBuilder();
        StringBuilder out = new StringBuilder();
        char prev = ' ';
        for (char c : in.toCharArray()) {
            if (prev == '§') {
                if (c >= 'k' && c <= 'o') {
                    if (formats.indexOf("" + c) == -1) {
                        formats.append('§').append(c);
                        newformats.append('§').append(c);
                    }
                } else {
                    if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))) {
                        c = 'f';
                    }
                    if (!(formats.length() == 2 && formats.charAt(1) == c)) {
                        formats.setLength(0);
                        formats.append('§').append(c);
                        newformats.setLength(0);
                        newformats.append('§').append(c);
                    }
                }
            } else if (c != '§') {
                out.append(newformats);
                out.append(c);
                newformats.setLength(0);
            }
            prev = c;
        }
        return out.toString();
    }

    /**
     * Save a loaded offline player. You should use this method when you have loaded an offline player
     * and you have changed some of it's data
     *
     * @param plr Loaded offline players Player object
     */
    public static void savePlayer(Player plr) {
        try {
            saveDataM.invoke(plr);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void savePlayerConfig(UUID uid) {
        String key = uid == null ? "CONSOLE" : uid.toString();
        switch (Config.PlayerFile.backend) {
            case FILE:
                pf.save();
                return;
            case MYSQL: {
                ArrayList<String> list = new ArrayList<>();
                pf.subConfig(key, "uuid='" + key + "'").mysqlUpdate(list, null);
                pf.db.batch(list, null);
            }
        }
    }

    /**
     * Save files from the given plugins jar file to its subfolder in the plugins folder. The files will only be saved
     * if they doesn't exists in the plugins subfolder.
     *
     * @param pl        instane of the plugin
     * @param fileNames names of the saveable files
     */
    public static void saveResources(Plugin pl, String... fileNames) {
        Logger log = pl.getLogger();
        File df = pl.getDataFolder();
        ClassLoader cl = pl.getClass().getClassLoader();
        df.mkdir();
        for (String fn : fileNames) {
            try {
                File f = new File(df + File.separator + fn);
                if (!f.exists()) {
                    if (fn.contains(File.separator)) {
                        new File(fn.substring(0, fn.lastIndexOf(File.separatorChar))).mkdirs();
                    }
                    InputStream is = cl.getResourceAsStream(fn);
                    if (is == null) {
                        log.severe("Error, the requested file (" + fn + ") is missing from the plugins jar file.");
                    } else
                        Files.copy(is, f.toPath());
                }
            } catch (Throwable e) {
                log.severe("Error, on copying file (" + fn + "): ");
                e.printStackTrace();
            }
        }
    }

    /**
     * Set maximum length of a String by cutting the redundant characters off from it
     *
     * @param in  input String
     * @param len maximum length
     * @return The cutted String, which will maximally len characters.
     */
    public static String setLength(String in, int len) {
        return in.length() > len ? in.substring(0, len) : in;
    }

    /**
     * Converts an ItemStack representing string back to the ItemStack
     *
     * @param in string represantation of an ItemStack
     * @return The conversion output ItemStack, or null if the given string is null
     */
    public static ItemStack stringToItemStack(String in) {
        if (in == null)
            return null;
        String[] parts = in.split(" ");
        String[] idParts = parts[0].split(":");
        int id = getId(idParts[0]);
        short dataValue = 0;
        int amount = 1;
        int st = 1;
        try {
            dataValue = Short.valueOf(idParts[1]);
        } catch (Throwable e) {
        }

        try {
            amount = Short.valueOf(parts[1]);
            st = 2;
        } catch (Throwable e) {
        }

        int l = parts.length;
        ItemStack out = new ItemStack(id, amount, dataValue);
        ItemMeta meta = out.getItemMeta();
        ArrayList<String[]> remaining = new ArrayList<>();
        for (int i = st; i < l; i++) {
            String[] s = parts[i].split(":", 2);
            s[0] = s[0].toUpperCase();
            try {
                Enchantment enc = Config.enchantAliases.get(s[0]);
                if (enc == null)
                    enc = Enchantment.getByName(s[0].toUpperCase());
                if (enc == null) {
                    if (Reflection.ver == ServerVersion.v1_8 || Reflection.ver == ServerVersion.v1_9) {
                        if (s[0].equals("HIDE"))
                            meta.addItemFlags(ItemFlag.valueOf("HIDE_" + s[1].toUpperCase()));
                    }
                    if (s[0].equals("NAME")) {
                        meta.setDisplayName(unescapeText(s[1]));
                    } else if (s[0].equals("LORE")) {
                        meta.setLore(Lists.newArrayList(unescapeText(s[1]).split("\n")));
                    } else {
                        remaining.add(s);
                    }
                } else {
                    meta.addEnchant(enc, Integer.valueOf(s[1]), true);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (meta instanceof BookMeta) {
            BookMeta bmeta = (BookMeta) meta;
            for (String[] s : remaining) {
                try {
                    String text = unescapeText(s[1]);
                    if (s[0].equals("AUTHOR")) {
                        bmeta.setAuthor(text);
                    } else if (s[0].equals("TITLE")) {
                        bmeta.setTitle(text);
                    } else if (s[0].equals("PAGE")) {
                        bmeta.addPage(text);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        if (Reflection.ver == ServerVersion.v1_8 || Reflection.ver == ServerVersion.v1_9)
            if (meta instanceof BannerMeta) {
                BannerMeta bmeta = (BannerMeta) meta;
                for (String[] s : remaining) {
                    try {
                        PatternType type = PatternType.getByIdentifier(s[0].toLowerCase());
                        if (type == null) {
                            if (s[0].equals("COLOR")) {
                                bmeta.setBaseColor(DyeColor.valueOf(s[1].toUpperCase()));
                            } else {
                                PatternType pt = PatternType.getByIdentifier(s[0].toLowerCase());
                                if (pt != null) {
                                    bmeta.addPattern(new Pattern(DyeColor.valueOf(s[1].toUpperCase()), pt));
                                }
                            }
                        } else {
                            bmeta.addPattern(new Pattern(DyeColor.valueOf(s[1].toUpperCase()), type));
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta bmeta = (LeatherArmorMeta) meta;
            for (String[] s : remaining) {
                try {
                    if (s[0].equals("COLOR")) {
                        String[] color = s[1].split(",", 3);
                        bmeta.setColor(org.bukkit.Color.fromRGB(Integer.valueOf(color[0]), Integer.valueOf(color[1]), Integer.valueOf(color[2])));
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } else if (meta instanceof FireworkMeta) {
            FireworkMeta bmeta = (FireworkMeta) meta;
            for (String[] s : remaining) {
                try {
                    if (s[0].equals("POWER")) {
                        bmeta.setPower(Integer.valueOf(s[1]));
                    } else {
                        FireworkEffect.Type type = FireworkEffect.Type.valueOf(s[0]);
                        FireworkEffect.Builder build = FireworkEffect.builder().with(type);
                        for (String d : s[1].toUpperCase().split("\\|")) {
                            String[] d2 = d.split(":", 2);
                            if (d2[0].equals("COLORS")) {
                                for (String colors : d2[1].split(";")) {
                                    String[] color = colors.split(",", 3);
                                    build.withColor(Color.fromRGB(Integer.valueOf(color[0]), Integer.valueOf(color[1]), Integer.valueOf(color[2])));
                                }
                            } else if (d2[0].equals("FADES")) {
                                for (String fades : d2[1].split(";")) {
                                    String[] fade = fades.split(",", 3);
                                    build.withFade(Color.fromRGB(Integer.valueOf(fade[0]), Integer.valueOf(fade[1]), Integer.valueOf(fade[2])));
                                }
                            } else if (d2[0].equals("FLICKER")) {
                                build.withFlicker();
                            } else if (d2[0].equals("TRAIL")) {
                                build.withTrail();
                            }
                        }
                        bmeta.addEffect(build.build());

                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } else if (meta instanceof PotionMeta) {
            PotionMeta bmeta = (PotionMeta) meta;
            for (String[] s : remaining) {
                try {
                    PotionEffectType type = PotionEffectType.getByName(s[0]);
                    if (type != null) {
                        String[] s2 = s[1].split(":");
                        if (Reflection.ver == ServerVersion.v1_8 || Reflection.ver == ServerVersion.v1_9) {
                            bmeta.addCustomEffect(new PotionEffect(type, Integer.valueOf(s2[0]), Integer.valueOf(s2[1]),
                                    !ArrayUtils.contains(s2, "na"), !ArrayUtils.contains(s2, "np")), false);
                        } else {
                            bmeta.addCustomEffect(new PotionEffect(type, Integer.valueOf(s2[0]), Integer.valueOf(s2[1]),
                                    !ArrayUtils.contains(s2, "na")), false);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } else if (meta instanceof SkullMeta) {
            SkullMeta bmeta = (SkullMeta) meta;
            for (String[] s : remaining) {
                try {
                    if (s[0].equals("OWNER")) {
                        bmeta.setOwner(unescapeText(s[1]));
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } else if (meta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta bmeta = (EnchantmentStorageMeta) meta;
            for (String[] s : remaining) {
                try {
                    Enchantment enc = Enchantment.getByName(s[0].substring(1));
                    if (enc != null)
                        bmeta.addStoredEnchant(enc, Integer.valueOf(s[1]), true);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        out.setItemMeta(meta);
        return out;
    }

    /**
     * Unescape multi line to single line escaped text
     *
     * @param text multi line escaped text input
     * @return The unescaped text
     */

    public static String unescapeText(String text) {
        return (" " + text).replaceAll("([^\\\\])_", "$1 ")
                .replaceAll("([^\\\\])\\|", "$1\n")
                .replaceAll("([^\\\\])\\\\([_\\|])", "$1$2")
                .replace("\\\\", "\\").substring(1);
    }

    /**
     * Unloads the configuration of the given player or of the console if
     * uid = null
     *
     * @param uid - The UUID of the player, or null for console
     * @return True if the unload was successful, false otherwise
     */
    public static boolean unloadPlayerConfig(UUID uid) {
        if (Config.PlayerFile.backend == BackendType.MYSQL) {
            String key = uid == null ? "CONSOLE" : uid.toString();
            return pf.removeData(key);
        }
        return false;
    }

    /**
     * Unloads a plugin
     *
     * @param p - The unloadable plugin
     */
    public static void unloadPlugin(Plugin p) {
        pm.disablePlugin(p);
        try {
            Field lookupNamesField = pm.getClass().getDeclaredField("lookupNames");
            lookupNamesField.setAccessible(true);
            Map names = (Map) lookupNamesField.get(pm);
            Iterator<Map.Entry<String, Plugin>> it = names.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Plugin> e = it.next();
                if (e.getValue() == p)
                    it.remove();
            }

            Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(pm);

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            Map<String, Command> commands = (Map<String, Command>) knownCommandsField.get(commandMap);
            Iterator<Map.Entry<String, Command>> it2 = commands.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry<String, Command> e = it2.next();
                Command cmd = e.getValue();
                if (cmd instanceof PluginCommand) {
                    PluginCommand c = (PluginCommand) cmd;
                    if (c.getPlugin() == p) {
                        c.unregister(commandMap);
                        it2.remove();
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        pm.disablePlugin(p);
        ClassLoader cl = p.getClass().getClassLoader();
        if ((cl instanceof URLClassLoader)) {
            try {
                ((URLClassLoader) cl).close();
            } catch (Throwable e) {
                SU.error(cs, e, "SpigotLib", "gyurix");
            }
        }
        System.gc();
    }
}