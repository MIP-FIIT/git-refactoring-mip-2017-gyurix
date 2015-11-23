package gyurix.spigotlib;

import gyurix.animation.AnimationAPI;
import gyurix.api.TitleAPI;
import gyurix.api.VariableAPI;
import gyurix.configfile.ConfigData;
import gyurix.configfile.ConfigFile;
import gyurix.configfile.ConfigSerialization;
import gyurix.configfile.DefaultSerializers;
import gyurix.economy.EconomyAPI;
import gyurix.economy.EconomyVaultHook;
import gyurix.inventory.InventoryAPI;
import gyurix.nbt.NBTApi;
import gyurix.protocol.*;
import gyurix.protocol.utils.ItemStackWrapper;
import gyurix.scoreboard.ScoreboardAPI;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.logging.Logger;

public class Main
        extends JavaPlugin
        implements Listener {
    public static final String version = "pre2.0";
    public static final String[] commands;
    public static Logger log;
    public static GlobalLangFile.PluginLang lang;
    public static ConfigFile kf;
    public static Plugin pl;
    public static File dir;

    static {
        commands = new String[]{"chm", "abm", "sym", "title", "titledata", "titlehide", "vars", "hasperm", "packets", "lang", "lf", "pf", "save", "reload", "errors", "velocity", "setamount", "item"};
    }

    public static void errorLog(CommandSender sender, Throwable e) {
        if (sender != null) {
            sender.sendMessage("\u00a7c\u00a7lSpigotLib \u25ba ERROR REPORTER \u25ba " + e.getClass().getName() + " - " + e.getMessage() + ":");
        }
        int i = 1;
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getName()).append(" - ").append(e.getMessage());
        for (StackTraceElement s : e.getStackTrace()) {
            String loc = s.toString();
            if (!loc.contains("gyurix")) continue;
            if (sender != null) {
                sender.sendMessage("\u00a7c " + i++ + ": \u00a7e" + loc);
            }
            sb.append('\n').append(loc);
        }
        String err = sb.toString();
        if (Config.errors.contains(err)) {
            if (sender != null) {
                sender.sendMessage("\u00a7c\u00a7lThis error has appeared already, you can find it in the errorlog.");
            }
        } else {
            if (sender != null) {
                sender.sendMessage("\u00a7c\u00a7lThis error seems to be a new bug.");
                sender.sendMessage("\u00a7c\u00a7lPlease report this error to the dev, \u00a7e\u00a7lgyuriX\u00a7c\u00a7l!");
            }
            Config.errors.add(err);
            log.severe("SpigotLib Error Reporter - found a new type of error, w:\n\n" + err + "\n\nYou should report this bug to the plugins dev, gyuriX");
        }
    }

    public void load() {
        SU.saveResources(this, "lang.yml", "config.yml");
        kf = new ConfigFile(this.getResource("config.yml"));
        kf.load(new File(dir + File.separator + "config.yml"));
        Main.kf.data.deserialize(Config.class);
        kf.save();
        SU.pf = new ConfigFile(new File(dir + File.separator + "players.yml"));
        lang = GlobalLangFile.loadLF("spigotlib", dir + File.separator + "lang.yml");
        Reflection.init();
        ConfigSerialization.interfaceBasedClasses.put(ItemStack.class, Reflection.getOBCClass("inventory.CraftItemStack"));
        SU.init();
        AnimationAPI.init();
        SU.tp = new Protocol();
        PacketInType.init();
        PacketOutType.init();
        ChatAPI.init();
        TitleAPI.init();
        NBTApi.init();
        ScoreboardAPI.init();
        if (EconomyAPI.vaultHook && SU.pm.getPlugin("Vault") != null) {
            EconomyVaultHook.init();
        }
        if (SU.pm.getPlugin("PlaceholderAPI") != null) {
            VariableAPI.phaHook = Config.phaHook;
        }
    }

    public void resetConfig() {
        try {
            File oldConf = new File(dir + File.separator + "config.yml");
            File backupConf = new File(dir + File.separator + "config.yml.bak");
            if (backupConf.exists()) {
                backupConf.delete();
            }
            oldConf.renameTo(backupConf);
            File oldLang = new File(dir + File.separator + "lang.yml");
            File backupLang = new File(dir + File.separator + "lang.yml.bak");
            if (backupLang.exists()) {
                backupLang.delete();
            }
            oldLang.renameTo(backupLang);
        } catch (Throwable e) {
            e.printStackTrace();
            log.severe("\u00a7cFailed to reset the config :-( The plugin is shutting down...");
            SU.pm.disablePlugin(this);
            return;
        }
        try {
            this.load();
        } catch (Throwable e) {
            e.printStackTrace();
            log.severe("\u00a7cFailed to load plugin after config reset :-( The plugin is shutting down...");
            SU.pm.disablePlugin(this);
        }
    }

    public void onLoad() {
        log = this.getLogger();
        pl = this;
        SU.srv = this.getServer();
        SU.pm = SU.srv.getPluginManager();
        SU.cs = SU.srv.getConsoleSender();
        dir = this.getDataFolder();
        try {
            DefaultSerializers.init();
            ConfigHook.registerSerializers();
            ConfigHook.registerVariables();
        } catch (Throwable e) {
            e.printStackTrace();
            log.severe("\u00a7cFailed to load config hook :-( The plugin is shutting down...");
            SU.pm.disablePlugin(this);
            return;
        }
        try {
            this.load();
        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println("Failed to load plugin, trying to reset the config...");
            this.resetConfig();
        }
    }

    public void onEnable() {
        SU.pm.registerEvents(this, this);
        SU.pm.registerEvents(SU.tp, this);
        SU.initOfflinePlayerManager();
        Config.tpsMeter.start();
    }

    public void onDisable() {
        SU.pf.save();
        kf.save();
        SU.tp.close();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player plr = e.getPlayer();
        if (Config.ConnectionLog.enabled) {
            String ip = plr.getAddress().getAddress().toString().substring(1);
            HashSet uuids = Config.ConnectionLog.ipUUIDBase.get(ip);
            if (uuids == null) {
                uuids = new HashSet();
                Config.ConnectionLog.ipUUIDBase.put(ip, uuids);
            }
            uuids.add(plr.getUniqueId());
        }
        ScoreboardAPI.playerJoin(plr);
        UUID id = plr.getUniqueId();
        EconomyAPI.getBalance(id);
        for (EconomyAPI.BalanceData bd : EconomyAPI.balanceTypes.values()) {
            EconomyAPI.getBalance(id, bd.name);
        }
    }

    @EventHandler
    public void onPluginUnload(PluginDisableEvent e) {
        InventoryAPI.unregister(e.getPlugin());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player plr = e.getPlayer();
        if (Config.AntiItemHack.enabled) {
            SU.getPlayerConfig(plr).removeData("antiitemhack.lastitem");
        }
        SU.pf.save();
        ScoreboardAPI.playerLeave(plr);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void editBook(PlayerEditBookEvent e) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta old = e.getNewBookMeta();
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setPages(old.getPages());
        meta.setAuthor(e.getPlayer().getName());
        if (old.getTitle() != null) {
            meta.setTitle(SU.setLength(old.getTitle().replace("\u00a7", "&"), 16));
        }
        e.setNewBookMeta(meta);
    }

    public int convertPacketSlot(int slot) {
        if (slot > 8 && slot < 36) {
            return slot;
        }
        if (slot > 35 && slot < 45) {
            return slot - 36;
        }
        if (slot > 44 || slot < 0) {
            return -1;
        }
        if (slot < 5) {
            return -2;
        }
        return 31 + slot;
    }

    public String creativeSlotCheck(Player plr, int slot, ItemStack is) {
        int invid = this.convertPacketSlot(slot);
        if (is == null || is.getType() == Material.AIR) {
            try {
                SU.getPlayerConfig(plr).setObject("antiitemhack.lastitem", plr.getInventory().getItem(invid));
            } catch (NullPointerException var5_5) {
                // empty catch block
            }
            return null;
        }
        switch (invid) {
            case -1: {
                return null;
            }
            case -2: {
                return "itemhack.craftitem";
            }
        }
        String id = String.valueOf(is.getTypeId());
        switch (slot) {
            case 5: {
                return Config.AntiItemHack.helmets.contains(id) ? null : "itemhack.helmet";
            }
            case 6: {
                return Config.AntiItemHack.chestplates.contains(id) ? null : "itemhack.chestplate";
            }
            case 7: {
                return Config.AntiItemHack.leggings.contains(id) ? null : "itemhack.leggings";
            }
            case 8: {
                return Config.AntiItemHack.boots.contains(id) ? null : "itemhack.boots";
            }
        }
        if (SU.itemEqual((ItemStack) SU.getPlayerConfig(plr).get("antiitemhack.lastitem", ItemStack.class), is)) {
            return null;
        }
        if (is.getAmount() > is.getType().getMaxStackSize()) {
            return "itemhack.stacksize";
        }
        return SU.containsItem(Config.AntiItemHack.whitelist, is) || SU.containsItem(plr.getInventory(), is) ? null : "itemhack.invaliditem";
    }

    @EventHandler
    public void packetInHandler(PacketInEvent e) {
        block47:
        {
            String name;
            Player plr = e.getPlayer();
            String uid = plr == null ? "?" : e.getPlayer().getUniqueId().toString();
            String string = name = plr == null ? "?" : e.getPlayer().getName();
            if (Config.debug && e.getType() == null) {
                log.severe("[SpigotLib] -> Protocol -> Missing in packet type " + e.getPacketObject().getClass().getName());
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                HashSet detect = (HashSet) SU.getPlayerConfig(p).getData("packet.detect.in." + uid).objectData;
                if (detect == null || !detect.contains(e.getType())) continue;
                p.sendMessage("\u00a79\u00a7l----> PacketInDetector - " + name + " - " + e.getType().name());
                p.sendMessage("\u00a7b" + StringUtils.join(e.getPacketData(), "\n\u00a7b"));
            }
            if (e.getType() == PacketInType.HandshakingInSetProtocol) {
                if (Config.ConnectionLog.enabled) {
                    try {
                        String ip = ((InetSocketAddress) e.getChannel().remoteAddress()).getAddress().toString().substring(1);
                        if (Config.ConnectionLog.blacklist.contains(ip)) {
                            return;
                        }
                        boolean login = Protocol.handshakeNextState.get(e.getPacketObject()).toString().equals("LOGIN");
                        HashSet<UUID> uuids = Config.ConnectionLog.ipUUIDBase.get(ip);
                        if (uuids == null || uuids.isEmpty()) {
                            SU.cs.sendMessage((login ? Config.ConnectionLog.loginunknown : Config.ConnectionLog.pingunknown).replace("<ip>", ip));
                            break block47;
                        }
                        ArrayList<String> list = new ArrayList<String>();
                        String n = "?";
                        for (UUID id : uuids) {
                            n = "?";
                            try {
                                n = Bukkit.getOfflinePlayer(id).getName();
                            } catch (Throwable var12_33) {
                                // empty catch block
                            }
                            list.add(n + "(" + id + ")");
                        }
                        if (list.size() == 1) {
                            SU.cs.sendMessage((login ? Config.ConnectionLog.login : Config.ConnectionLog.ping).replace("<ip>", ip).replace("<uuid>", "" + uuids.iterator().next()).replace("<name>", n));
                            break block47;
                        }
                        SU.cs.sendMessage((login ? Config.ConnectionLog.loginmore : Config.ConnectionLog.pingmore).replace("<ip>", ip) + StringUtils.join(list, ", "));
                    } catch (IllegalAccessException e1) {
                        Main.errorLog(null, e1);
                        e1.printStackTrace();
                    }
                }
            } else if (e.getType() == PacketInType.TabComplete) {
                if (!Config.tabCompletePerm) {
                    return;
                }
                e.setCancelled(!e.getPlayer().hasPermission("tab.complete"));
            } else if (e.getType() == PacketInType.SetCreativeSlot) {
                if (!Config.AntiItemHack.enabled) {
                    return;
                }
                try {
                    int slotid = 0;
                    ItemStack is = null;
                    for (Object o : e.getPacketData()) {
                        if (o != null && o.getClass() == Integer.class) {
                            slotid = (Integer) o;
                            continue;
                        }
                        if (o == null) continue;
                        is = new ItemStackWrapper(o).toBukkitStack();
                    }
                    String result = this.creativeSlotCheck(plr, slotid, is);
                    if (result != null) {
                        lang.msg(lang.get(plr, "itemhack"), plr, result);
                        e.setCancelled(true);
                        plr.updateInventory();
                    }
                } catch (Throwable err) {
                    err.printStackTrace();
                }
            } else if (e.getType() == PacketInType.UpdateSign) {
                if (!Config.AntiSignHack.enabled) {
                    return;
                }
                Object[] darray = e.getPacketData();
                for (Object l : darray) {
                    String json = ChatAPI.toJson(l);
                    if (!json.startsWith("\"") || !json.endsWith("\"")) {
                        lang.msg(plr, "signhack.json");
                        e.setCancelled(true);
                        return;
                    }
                    if ((json = StringEscapeUtils.unescapeJava(json.substring(1, json.length() - 1))).length() > Config.AntiSignHack.limit) {
                        lang.msg(plr, "signhack.limit");
                        e.setCancelled(true);
                        return;
                    }
                    for (char c : json.toCharArray()) {
                        if (c >= ' ' && c != '\u00a7') continue;
                        lang.msg(plr, "signhack.characters");
                        e.setCancelled(true);
                        return;
                    }
                }
            } else if (e.getType() == PacketInType.Chat) {
                boolean puffered;
                if (!Config.Chat.enabled) {
                    return;
                }
                e.setCancelled(true);
                ConfigFile pf = SU.getPlayerConfig(plr);
                String msg = (String) e.getPacketData()[0];
                boolean bl = puffered = msg.length() >= Config.Chat.Long.pufferAfter;
                if (!Config.Chat.newLineCharacter.equals("") && plr.hasPermission("chat.multiline")) {
                    msg = msg.replace(Config.Chat.newLineCharacter, "\n");
                    if (Config.Chat.allowEmptyLines && plr.hasPermission("chat.emptylines")) {
                        msg = msg.replace("\n\n", "\n \n");
                        msg = msg.replace("\n\n", "\n \n");
                    }
                    for (char c : Config.Chat.colors.toCharArray()) {
                        if (!plr.hasPermission("chat.color." + c)) continue;
                        msg = msg.replace(Config.Chat.colorPrefix + c, "\u00a7" + c);
                    }
                }
                if (Config.Chat.Long.enabled && plr.hasPermission("chat.longer")) {
                    int limit = 100;
                    for (Map.Entry<String, Integer> group : Config.Chat.Long.lengthLimit.entrySet()) {
                        if (!plr.hasPermission("chat.longerlimit." + group.getKey()) || group.getValue() <= limit)
                            continue;
                        limit = group.getValue();
                    }
                    ConfigData puffer = pf.getData("chat.puffer");
                    if (puffered && puffer.stringData.length() + msg.length() < limit) {
                        puffer.stringData = puffer.stringData + msg;
                    } else {
                        msg = puffer.stringData + msg;
                        msg = msg.length() > limit ? msg.substring(0, limit) : msg;
                        int id = msg.indexOf(" ");
                        if (id == -1) {
                            id = msg.length();
                        }
                        if (msg.startsWith("/")) {
                            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new AsyncCommandExecutionAvoid(msg, plr));
                        } else {
                            plr.chat(msg);
                        }
                        puffer.stringData = "";
                    }
                } else {
                    int id = msg.indexOf(" ");
                    if (id == -1) {
                        id = msg.length();
                    }
                    if (msg.startsWith("/")) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new AsyncCommandExecutionAvoid(msg, plr));
                    } else {
                        plr.chat(msg);
                    }
                }
            }
        }
    }

    @EventHandler
    public void packetOutDetect(PacketOutEvent e) {
        String name;
        Player plr = e.getPlayer();
        String uid = plr == null ? "?" : e.getPlayer().getUniqueId().toString();
        String string = name = plr == null ? "?" : e.getPlayer().getName();
        if (Config.debug && e.getType() == null) {
            log.severe("[SpigotLib] -> Protocol -> Missing out packet type " + e.getPacketObject().getClass().getName());
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            HashSet detect = (HashSet) SU.getPlayerConfig(p).getData("packet.detect.in." + uid).objectData;
            if (detect == null || !detect.contains(e.getType())) continue;
            p.sendMessage("\u00a79\u00a7l----> PacketOutDetector - " + name + " - " + e.getType().name());
            p.sendMessage("\u00a7b" + StringUtils.join(e.getPacketData(), "\n\u00a7b"));
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        block68:
        {
            String msg;
            String yes;
            Player p;
            Iterator i$;
            String no;
            block69:
            {
                try {
                    Iterator i$2;
                    Player plr;
                    if (!sender.hasPermission("spigotlib.use")) {
                        lang.msg(sender, "noperm");
                        return true;
                    }
                    Player player = plr = sender instanceof Player ? (Player) sender : null;
                    if (args.length == 0) {
                        String msg2 = "\u00a7b\u00a7l\u00a7n---> SpigotLib - by:\u00a76\u00a7l GyuriX\u00a7b\u00a7l \u00a7n - v:\u00a76\u00a7l pre2.0\u00a7b\u00a7l \u00a7n <---\n" + lang.get(plr, "help");
                        if (plr == null) {
                            sender.sendMessage(msg2);
                            return true;
                        }
                        ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, msg2, plr);
                        return true;
                    }
                    if (!sender.hasPermission("spigotlib.command." + args[0])) {
                        lang.msg(sender, "noperm.command");
                        return true;
                    }
                    ArrayList<Player> pls = null;
                    msg = null;
                    if (args.length >= 2) {
                        if (args[1].equals("*")) {
                            pls = (ArrayList<Player>) Bukkit.getOnlinePlayers();
                        } else {
                            pls = new ArrayList<Player>();
                            for (String pn : args[1].split(",")) {
                                Player p2 = Bukkit.getPlayer(pn);
                                if (p2 == null) continue;
                                pls.add(p2);
                            }
                        }
                        msg = StringUtils.join(args, " ", 2, args.length);
                    }
                    if (args[0].equals("chm")) {
                        for (Player p3 : pls) {
                            ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.CHAT, VariableAPI.fillVariables(msg, plr, p3), p3);
                        }
                        break block68;
                    }
                    if (args[0].equals("abm")) {
                        for (Player p4 : pls) {
                            ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, VariableAPI.fillVariables(msg, plr, p4), p4);
                        }
                        break block68;
                    }
                    if (args[0].equals("sym")) {
                        for (Player p5 : pls) {
                            ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, VariableAPI.fillVariables(msg, plr, p5), p5);
                        }
                        break block68;
                    }
                    if (args[0].equals("title")) {
                        String[] titles = msg.split("\n");
                        String title = titles[0];
                        String sub = titles.length >= 2 ? titles[1] : "";
                        TitleAPI.setTitle(title, pls);
                        TitleAPI.setSubTitle(sub, pls);
                        break block68;
                    }
                    if (args[0].equals("titledata")) {
                        String[] data = msg.split(" ");
                        TitleAPI.setShowTime(Integer.valueOf(data[0]), Integer.valueOf(data[1]), Integer.valueOf(data[2]), pls);
                        break block68;
                    }
                    if (args[0].equals("titlehide")) {
                        TitleAPI.reset(pls);
                        break block68;
                    }
                    if (args[0].equals("vars")) {
                        if (args.length == 1) {
                            ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, StringUtils.join(new TreeSet<String>(VariableAPI.handlers.keySet()), ", "), plr);
                        } else {
                            ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, VariableAPI.fillVariables(StringUtils.join(args, ' ', 1, args.length), plr, plr), plr);
                        }
                        break block68;
                    }
                    if (args[0].equals("hasperm")) {
                        yes = lang.get(plr, "permission.yes");
                        no = lang.get(plr, "permission.no");
                        i$ = pls.iterator();
                        break block69;
                    }
                    if (args[0].equals("packets")) {
                        lang.msg(sender, "packet", "in", StringUtils.join(PacketInType.values(), ", "), "out", StringUtils.join(PacketOutType.values(), ", "));
                        return true;
                    }
                    if (args[0].equals("errors")) {
                        sender.sendMessage(StringUtils.join(Config.errors, "\n \n"));
                        break block68;
                    }
                    if (args[0].equals("lang")) {
                        if (args.length == 1) {
                            lang.msg(sender, "lang.list", "langs", StringUtils.join(GlobalLangFile.map.keySet(), ", "));
                            return true;
                        }
                        if (args.length == 2) {
                            if (GlobalLangFile.map.containsKey(args[1])) {
                                if (plr == null) {
                                    lang.msg(sender, "noconsole");
                                    return true;
                                }
                                SU.getPlayerConfig(plr).setString("lang", args[2]);
                                lang.msg(sender, "lang.set.own", "lang", args[1]);
                                return true;
                            }
                            i$2 = pls.iterator();
                            while (i$2.hasNext()) {
                                Player p6 = (Player) i$2.next();
                                String l = SU.getPlayerConfig(plr).getString("lang");
                                if (l.isEmpty()) {
                                    l = Config.defaultLang;
                                }
                                lang.msg(sender, "lang.lang", "player", p6.getName(), "lang", l);
                            }
                            return true;
                        }
                        i$2 = pls.iterator();
                        do {
                            if (!i$2.hasNext()) {
                                SU.pf.save();
                                return true;
                            }
                            Player p7 = (Player) i$2.next();
                            SU.getPlayerConfig(p7).setString("lang", args[2]);
                            lang.msg(sender, "lang.set", "player", p7.getName(), "lang", args[2]);
                        } while (true);
                    }
                    if (args[0].equals("lf")) {
                        StringBuilder out = new StringBuilder();
                        if (args.length == 1) {
                            sender.sendMessage("\u00a79\u00a7l---> Language file viewer <---");
                            for (Map.Entry<String, HashMap<String, String>> e : GlobalLangFile.map.entrySet()) {
                                if (e.getKey().contains(".")) continue;
                                out.append("\n\\|\u00a7b").append(e.getKey()).append("\\-R/spigotlib lf ").append(e.getKey()).append("\\|");
                            }
                        } else {
                            sender.sendMessage("\u00a79\u00a7l---> Language file viewer - " + args[1] + " <---");
                            int len = args[1].length();
                            for (Map.Entry<String, HashMap<String, String>> e : GlobalLangFile.map.entrySet()) {
                                String sub;
                                if (!e.getKey().startsWith(args[1]) || (sub = e.getKey().substring(len)).contains("."))
                                    continue;
                                out.append("\n\\|\u00a7b").append(sub).append(": \\-R/spigotlib lf ").append(e.getKey()).append("\\|\u00a7f").append((String) ((Object) e.getValue()));
                            }
                        }
                        ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, out.length() == 0 ? " " : out.substring(3), plr);
                        return true;
                    }
                    if (args[0].equals("pf")) {
                        if (args.length == 1) {
                            ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, SU.pf.toString(), plr);
                            return true;
                        }
                        ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, SU.getPlayerConfig(SU.getUUID(args[1])).toString(), plr);
                        return true;
                    }
                    if (args[0].equals("reload")) {
                        if (args.length == 0) {
                            lang.msg(sender, "invalidcmd");
                        }
                        if (args[1].equals("config")) {
                            kf.reload();
                            Main.kf.data.deserialize(Config.class);
                            lang.msg(sender, "reload.config");
                            return true;
                        }
                        if (args[1].equals("lf")) {
                            GlobalLangFile.unloadLF(lang);
                            SU.saveResources(this, "lang.yml");
                            lang = GlobalLangFile.loadLF("spigotlib", this.getDataFolder() + File.separator + "lang.yml");
                            lang.msg(sender, "reload.lf");
                            return true;
                        }
                        if (args[1].equals("pf")) {
                            SU.pf.reload();
                            lang.msg(sender, "reload.pf");
                            return true;
                        }
                        lang.msg(sender, "invalidcmd");
                        return true;
                    }
                    if (args[0].equals("save")) {
                        if (args[1].equals("pf")) {
                            SU.pf.save();
                            lang.msg(sender, "save.pf");
                            return true;
                        }
                        lang.msg(sender, "invalidcmd");
                        return true;
                    }
                    if (args[0].equals("velocity")) {
                        if (args.length == 4) {
                            if (plr == null) {
                                lang.msg(sender, "noconsole");
                                return true;
                            }
                            plr.setVelocity(new Vector(Double.valueOf(args[1]).doubleValue(), Double.valueOf(args[2]).doubleValue(), Double.valueOf(args[3]).doubleValue()));
                        } else {
                            if (args.length < 5) {
                                lang.msg(sender, "invalidcmd");
                                return true;
                            }
                            Vector v = new Vector(Double.valueOf(args[2]).doubleValue(), Double.valueOf(args[3]).doubleValue(), Double.valueOf(args[4]).doubleValue());
                            for (Player p8 : pls) {
                                p8.setVelocity(v);
                            }
                        }
                        break block68;
                    }
                    if (args[0].equals("setamount")) {
                        if (args.length == 2) {
                            if (plr == null) {
                                lang.msg(sender, "noconsole");
                                return true;
                            }
                            ItemStack is = plr.getItemInHand();
                            is.setAmount(Integer.valueOf(args[1]).intValue());
                            plr.setItemInHand(is);
                        } else {
                            if (args.length < 3) {
                                lang.msg(sender, "invalidcmd");
                                return true;
                            }
                            int amount = Integer.valueOf(args[2]);
                            for (Player p9 : pls) {
                                ItemStack is = p9.getItemInHand();
                                is.setAmount(amount);
                                p9.setItemInHand(is);
                            }
                        }
                        break block68;
                    }
                    if (args[0].equals("debug")) {
                        Config.debug = !Config.debug;
                        lang.msg(sender, "debug." + (Config.debug ? "on" : "off"));
                        return true;
                    }
                    if (!args[0].equals("item")) {
                        lang.msg(sender, "invalidcmd");
                        return true;
                    }
                    if (args.length == 1) {
                        if (plr == null) {
                            lang.msg(sender, "noconsole");
                            return true;
                        }
                        lang.msg(sender, "item.own", "item", SU.itemToString(plr.getItemInHand()));
                        return true;
                    }
                    if (args.length == 2) {
                        i$2 = pls.iterator();
                        while (i$2.hasNext()) {
                            Player p10 = (Player) i$2.next();
                            lang.msg(sender, "item.player", "name", p10.getName(), "item", SU.itemToString(p10.getItemInHand()));
                        }
                        return true;
                    }
                    String s = StringUtils.join(args, ' ', 2, args.length);
                    ItemStack is = SU.stringToItemStack(s);
                    Iterator i$3 = pls.iterator();
                    while (i$3.hasNext()) {
                        p = (Player) i$3.next();
                        p.setItemInHand(is);
                        lang.msg(sender, "item.player.set", "name", p.getName(), "item", s);
                    }
                    return true;
                } catch (Throwable e) {
                    Main.errorLog(sender, e);
                    return true;
                }
            }
            while (i$.hasNext()) {
                p = (Player) i$.next();
                sender.sendMessage((p.hasPermission(msg) ? yes : no).replace("<player>", p.getName()));
            }
        }
        sender.sendMessage("\u00a7b\u00a7lSpigotLib - Command executed successfully");
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> out = new ArrayList<String>();
        if (!sender.hasPermission("spigotlib.use")) {
            lang.msg(sender, "noperm");
            return out;
        }
        if (args.length == 1) {
            args[0] = args[0].toLowerCase();
            for (String cmd : commands) {
                if (!cmd.startsWith(args[0]) || !sender.hasPermission("spigotlib.command." + cmd)) continue;
                out.add(cmd);
            }
        } else if (args.length == 2) {
            lang.msg(sender, "notdone");
        }
        return out;
    }

    public static class AsyncCommandExecutionAvoid
            implements Runnable {
        private final String msg;
        private final Player plr;

        public AsyncCommandExecutionAvoid(String msg, Player plr) {
            this.msg = msg;
            this.plr = plr;
        }

        @Override
        public void run() {
            this.plr.chat(this.msg);
        }
    }

}

