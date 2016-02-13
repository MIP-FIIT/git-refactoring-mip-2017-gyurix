package gyurix.spigotlib;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import gyurix.animation.AnimationAPI;
import gyurix.api.BungeeAPI;
import gyurix.api.TitleAPI;
import gyurix.api.VariableAPI;
import gyurix.commands.CommandMapReplacer;
import gyurix.configfile.ConfigData;
import gyurix.configfile.ConfigFile;
import gyurix.configfile.ConfigSerialization;
import gyurix.configfile.DefaultSerializers;
import gyurix.economy.EconomyAPI;
import gyurix.economy.EconomyVaultHook;
import gyurix.inventory.InventoryAPI;
import gyurix.nbt.NBTApi;
import gyurix.protocol.PacketCapture;
import gyurix.protocol.PacketInType;
import gyurix.protocol.PacketOutType;
import gyurix.protocol.Reflection;
import gyurix.protocol.v1_8.Protocol18;
import gyurix.scoreboard.ScoreboardAPI;
import gyurix.spigotlib.features.AntiCreativeItemHack;
import gyurix.spigotlib.features.ConnectionLog;
import gyurix.spigotlib.features.LongerChat;
import gyurix.spigotutils.BackendType;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

public class Main
        extends JavaPlugin
        implements Listener {
    public static final String version = "3.0";
    public static final String[] commands;
    public static Logger log;
    public static GlobalLangFile.PluginLang lang;
    public static ConfigFile kf;
    public static Plugin pl;
    public static File dir;
    public static boolean vault;

    static {
        commands = new String[]{"chm", "abm", "sym", "title", "titledata", "titlehide", "vars", "hasperm", "packets", "lang", "lf", "pf", "save", "reload", "errors", "velocity", "setamount", "item"};
    }

    public static void errorLog(CommandSender sender, Throwable e) {
        if (sender != null) {
            sender.sendMessage("§c§lSpigotLib \u25ba ERROR REPORTER \u25ba " + e.getClass().getName() + " - " + e.getMessage() + ":");
        }
        int i = 1;
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getName()).append(" - ").append(e.getMessage());
        for (StackTraceElement s : e.getStackTrace()) {
            String loc = s.toString();
            if (!loc.contains("gyurix")) continue;
            if (sender != null) {
                sender.sendMessage("§c " + i++ + ": §e" + loc);
            }
            sb.append('\n').append(loc);
        }
        String err = sb.toString();
        if (Config.errors.contains(err)) {
            if (sender != null) {
                sender.sendMessage("§c§lThis error has appeared already, you can find it in the errorlog.");
            }
        } else {
            if (sender != null) {
                sender.sendMessage("§c§lThis error seems to be a new bug.");
                sender.sendMessage("§c§lPlease report this error to the dev, §e§lgyuriX§c§l!");
            }
            Config.errors.add(err);
            log.severe("SpigotLib Error Reporter - found a new type of error, w:\n\n" + err + "\n\nYou should report this bug to the plugins dev, gyuriX");
        }
    }

    public void load() {
        SU.saveResources(this, "lang.yml", "config.yml", "antiitemhack.yml", "connections.log", "enchants.yml");
        kf = new ConfigFile(this.getResource("config.yml"));
        kf.load(new File(dir + File.separator + "config.yml"));
        Main.kf.data.deserialize(Config.class);
        kf.save();
        new ConfigFile(new File(dir + File.separator + "config.yml")).data.deserialize(Config.AntiItemHack.class);
        if (Config.PlayerFile.backend == BackendType.FILE)
            SU.pf = new ConfigFile(new File(dir + File.separator + Config.PlayerFile.file));
        else if (Config.PlayerFile.backend == BackendType.MYSQL) {
            SU.pf = new ConfigFile(Config.PlayerFile.mysql, Config.PlayerFile.mysql.table, "key", "value");
            SU.loadPlayerConfig(null);
        }
        lang = GlobalLangFile.loadLF("spigotlib", dir + File.separator + "lang.yml");
        Reflection.init();
        ConfigSerialization.interfaceBasedClasses.put(ItemStack.class, Reflection.getOBCClass("inventory.CraftItemStack"));
        SU.init();
        AnimationAPI.init();
        SU.tp = new Protocol18();
        PacketInType.init();
        PacketOutType.init();
        ChatAPI.init();
        TitleAPI.init();
        NBTApi.init();
        ScoreboardAPI.init();
        CommandMapReplacer.init();
        vault = SU.pm.getPlugin("Vault") != null;
        if (vault && EconomyAPI.vaultHookType == EconomyAPI.VaultHookType.PROVIDER)
            EconomyVaultHook.init();
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
            log.severe("§cFailed to reset the config :-( The plugin is shutting down...");
            SU.pm.disablePlugin(this);
            return;
        }
        try {
            this.load();
        } catch (Throwable e) {
            e.printStackTrace();
            log.severe("§cFailed to load plugin after config reset :-( The plugin is shutting down...");
            SU.pm.disablePlugin(this);
        }
    }

    public void onLoad() {
        log = this.getLogger();
        pl = this;
        SU.srv = this.getServer();
        SU.pm = SU.srv.getPluginManager();
        SU.cs = SU.srv.getConsoleSender();
        SU.msg = SU.srv.getMessenger();
        dir = this.getDataFolder();
        try {
            DefaultSerializers.init();
            ConfigHook.registerSerializers();
            ConfigHook.registerVariables();
        } catch (Throwable e) {
            e.printStackTrace();
            log.severe("§cFailed to load config hook :-( The plugin is shutting down...");
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
        SU.tp.registerIncomingListener(this, new AntiCreativeItemHack(), PacketInType.SetCreativeSlot);
        SU.tp.registerIncomingListener(this, new LongerChat(), PacketInType.Chat);
        SU.tp.registerIncomingListener(this, new ConnectionLog(), PacketInType.HandshakingInSetProtocol);
        SU.msg.registerOutgoingPluginChannel(this, "BungeeCord");
        SU.msg.registerIncomingPluginChannel(this, "BungeeCord", new BungeeAPI());
        BungeeAPI.startAPI();
        SU.initOfflinePlayerManager();
        if (Config.PlayerFile.backend == BackendType.MYSQL) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                SU.loadPlayerConfig(p.getUniqueId());
            }
        }
        if (vault && EconomyAPI.vaultHookType == EconomyAPI.VaultHookType.USER) {
            EconomyAPI.oldEconomy = SU.srv.getServicesManager().getRegistration(Economy.class).getProvider();
            if (EconomyAPI.migrate) {
                System.out.println("Migrating economy data from old Economy " + EconomyAPI.oldEconomy.getName() + "... ");
                EconomyAPI.vaultHookType = EconomyAPI.VaultHookType.NONE;
                for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
                    EconomyAPI.setBalance(op.getUniqueId(), new BigDecimal(EconomyAPI.oldEconomy.getBalance(op)));
                    System.out.println("Done player " + op.getName());
                }
                EconomyAPI.vaultHookType = EconomyAPI.VaultHookType.PROVIDER;
                EconomyAPI.migrate = false;
                System.out.println("Finished data migration, please restart the server!");
                setEnabled(false);
                return;
            }
        }
        Config.tpsMeter.start();
        System.out.println("Started SpigotLib properly.");
    }

    public void onDisable() {
        if (Config.PlayerFile.backend == BackendType.FILE)
            SU.pf.save();
        else if (Config.PlayerFile.backend == BackendType.MYSQL) {
            ArrayList<String> list = new ArrayList<>();
            for (String s : SU.pf.getStringKeyList()) {
                SU.pf.subConfig(s, "uuid='" + s + "'").mysqlUpdate(list, null);
            }
            SU.pf.db.batch(list, null);
        }
        kf.save();
        SU.tp.close();
        ConnectionLog.logFile.close();
        BungeeAPI.stopAPI();
        System.out.println("Stopped SpigotLib properly.");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player plr = e.getPlayer();
        UUID id = plr.getUniqueId();
        SU.loadPlayerConfig(id);
        if (BungeeAPI.BungeeSettings.enabled) {
            if (BungeeAPI.BungeeSettings.ipOnJoin) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("IP");
                plr.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
            }
            if (BungeeAPI.BungeeSettings.uuidOnJoin) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("UUID");
                plr.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
            }
        }
        if (Config.ConnectionLog.enabled) {
            String ip = plr.getAddress().getAddress().toString().substring(1);
            LinkedHashSet<String> uuids = Sets.newLinkedHashSet(Lists.newArrayList(SU.pf.getString(ip).split(" ")));
            uuids.remove("");
            uuids.add(plr.getUniqueId().toString());
            SU.pf.setString(ip, StringUtils.join(uuids, " "));
        }
        ScoreboardAPI.playerJoin(plr);
    }

    @EventHandler
    public void onPluginUnload(PluginDisableEvent e) {
        Plugin pl = e.getPlugin();
        InventoryAPI.unregister(pl);
        SU.tp.unregisterIncomingListener(pl);
        SU.tp.unregisterOutgoingListener(pl);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player plr = e.getPlayer();
        UUID uid = plr.getUniqueId();
        if (Config.AntiItemHack.enabled) {
            SU.getPlayerConfig(plr).removeData("antiitemhack.lastitem");
        }
        SU.savePlayerConfig(uid);
        SU.unloadPlayerConfig(uid);
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
            meta.setTitle(SU.setLength(old.getTitle().replace("§", "&"), 16));
        }
        e.setNewBookMeta(meta);
    }

    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!sender.hasPermission("spigotlib.use")) {
                sender.sendMessage("\n \n \n§e▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n§l" +
                        "      －－＞  §6§lＳＰＩＧＯＴＬＩＢ  －  ＭＡＩＮ  ＭＥＮＵ§e§l  ＜－－\n" +
                        "§6Coded by §e§lgyuriX§6, contact him on Skype (gyuriskipe) if you need a custom plugin.\n \n \n" +
                        "§4§lIt looks like you don't have access for the plugin :(\n" +
                        "§d§lAsk the server owner if you need perm for it.");
                return true;
            }
            Player plr = sender instanceof Player ? (Player) sender : null;
            if (args.length == 0) {
                String msg2 = "\n \n \n§e▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n§l" +
                        "      －－＞  §6§lＳＰＩＧＯＴＬＩＢ  －  ＭＡＩＮ  ＭＥＮＵ§e§l  ＜－－\n" +
                        "§eCoded by §6§lgyuriX§e, contact him on Skype (gyuriskipe) if you need a custom plugin.\n \n \n" + lang.get(plr, "help");
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
            ArrayList<Player> pls = new ArrayList<>();
            String fullMsg = StringUtils.join(args, " ", 1, args.length);
            if (args.length >= 2) {
                if (args[1].startsWith("p:")) {
                    args[1] = args[1].substring(2);
                    if (args[1].equals("*")) {
                        pls = (ArrayList<Player>) Bukkit.getOnlinePlayers();
                    } else {
                        pls = new ArrayList<>();
                        for (String pn : args[1].split(",")) {
                            Player p2 = Bukkit.getPlayer(pn);
                            if (p2 == null)
                                continue;
                            pls.add(p2);
                        }
                    }
                    fullMsg = StringUtils.join(args, " ", 2, args.length);
                } else if (plr != null) {
                    pls.add(plr);
                }
            }
            String[] d = fullMsg.split(" ");
            if (d.length == 1 && d[0].isEmpty())
                d = null;
            switch (args[0]) {
                case "chm":
                    for (Player p : pls) {
                        ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.CHAT, VariableAPI.fillVariables(fullMsg, plr, p), p);
                    }
                    lang.msg(sender, "executed");
                    return true;
                case "sym":
                    for (Player p : pls) {
                        ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, VariableAPI.fillVariables(fullMsg, plr, p), p);
                    }
                    lang.msg(sender, "executed");
                    return true;
                case "abm":
                    for (Player p : pls) {
                        ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, VariableAPI.fillVariables(fullMsg, plr, p), p);
                    }
                    lang.msg(sender, "executed");
                    return true;
                case "title": {
                    String title = "";
                    String sub = "";
                    if (d.length > 2) {
                        TitleAPI.setShowTime(Integer.valueOf(d[0]), Integer.valueOf(d[1]), Integer.valueOf(d[2]), pls);
                        if (d.length > 3)
                            title = d[3];
                        if (d.length > 4)
                            sub = d[4];
                    } else {
                        if (d.length > 0)
                            title = d[0];
                        if (d.length > 1)
                            sub = d[1];
                    }
                    TitleAPI.setSubTitle(sub, pls);
                    TitleAPI.setTitle(title, pls);
                    lang.msg(sender, "executed");
                    return true;
                }
                case "vars": {
                    if (d == null) {
                        lang.msg(sender, "vars", "vars", StringUtils.join(new TreeSet<>(VariableAPI.handlers.keySet()), ", "));
                    } else {
                        String f = lang.get(plr, "vars.fillformat");
                        StringBuilder filled = new StringBuilder();
                        for (Player p : pls) {
                            filled.append('\n').append(f.replace("<player>", p.getName()).replace("<value>", VariableAPI.fillVariables(fullMsg, p)));
                        }
                        lang.msg(sender, "vars.filled", "original", fullMsg, "filled", filled.length() == 0 ? "" : filled.substring(1));
                    }
                    return true;
                }
                case "perms": {
                    if (d == null) {
                        String f = lang.get(plr, "perms.fillformat");
                        String denyperm = lang.get(plr, "perms.denyformat");
                        String allowperm = lang.get(plr, "perms.allowformat");
                        StringBuilder filled = new StringBuilder();
                        for (Player p : pls) {
                            Set<PermissionAttachmentInfo> perms = p.getEffectivePermissions();

                            for (PermissionAttachmentInfo perm : perms) {
                                //perm.get
                            }
                            filled.append('\n').append(StringUtils.join(p.getEffectivePermissions(), ","));
                        }
                        lang.msg(sender, "vars.filled", "original", fullMsg, "filled", filled.length() == 0 ? "" : filled.substring(1));
                    }
                }
                case "cappackets": {
                    PacketCapture pc = SU.tp.getCapturer(plr);
                    if (pc == null) {
                        plr.sendMessage("Started capturing packets about you.");
                        SU.tp.setCapturer(plr, new PacketCapture(plr.getName()));
                    } else {
                        plr.sendMessage("Stopped capturing packets about you.");
                        SU.tp.setCapturer(plr, null);
                    }
                    return true;
                }
                case "debug": {
                    Config.debug = !Config.debug;
                    lang.msg(sender, "debug." + (Config.debug ? "on" : "off"));
                    return true;
                }
                default: {
                    if (args[0].equals("reload")) {
                        if (args[1].equals("config")) {
                            BungeeAPI.stopAPI();
                            kf.reload();
                            Main.kf.data.deserialize(Config.class);
                            BungeeAPI.startAPI();
                            lang.msg(sender, "reload.config");
                            return true;
                        } else if (args[1].equals("lf")) {
                            GlobalLangFile.unloadLF(lang);
                            SU.saveResources(this, "lang.yml");
                            lang = GlobalLangFile.loadLF("spigotlib", this.getDataFolder() + File.separator + "lang.yml");
                            lang.msg(sender, "reload.lf");
                            return true;
                        } else if (args[1].equals("pf")) {
                            if (Config.PlayerFile.backend == BackendType.FILE) {
                                SU.pf.reload();
                            } else {
                                SU.pf.data.mapData = new LinkedHashMap<>();
                                for (Player pl : Bukkit.getOnlinePlayers()) {
                                    SU.loadPlayerConfig(pl.getUniqueId());
                                }
                                SU.loadPlayerConfig(null);
                            }
                            lang.msg(sender, "reload.pf");
                            return true;
                        }
                        lang.msg(sender, "invalidcmd");
                        return true;
                    }
                    if (args[0].equals("save")) {
                        if (args[1].equals("pf")) {
                            if (Config.PlayerFile.backend == BackendType.FILE)
                                SU.pf.save();
                            else {
                                for (ConfigData cd : new ArrayList<>(SU.pf.data.mapData.keySet())) {
                                    SU.savePlayerConfig(cd.stringData.length() == 40 ? UUID.fromString(cd.stringData) : null);
                                }
                            }
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
                            plr.setVelocity(new org.bukkit.util.Vector(Double.valueOf(args[1]).doubleValue(), Double.valueOf(args[2]).doubleValue(), Double.valueOf(args[3]).doubleValue()));
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
                    }
                    if (args[0].equals("migratetodb")) {
                        SU.pf.db = Config.PlayerFile.mysql;
                        SU.pf.dbKey = "key";
                        SU.pf.dbValue = "value";
                        SU.pf.dbTable = Config.PlayerFile.mysql.table;
                        lang.msg(sender, "migrate.start");
                        ArrayList<String> l = new ArrayList<>();
                        l.add("DROP TABLE IF EXISTS " + Config.PlayerFile.mysql.table);
                        l.add("CREATE TABLE " + Config.PlayerFile.mysql.table + " (uuid VARCHAR(40), `key` TEXT(1), `value` TEXT(1))");

                        for (Map.Entry<ConfigData, ConfigData> e : SU.pf.data.mapData.entrySet()) {
                            ConfigFile kf = SU.pf.subConfig("" + e.getKey(), "uuid='" + e.getKey() + "'");
                            kf.mysqlUpdate(l, null);
                        }
                        ConfigFile kff = SU.pf.subConfig("CONSOLE", "uuid='CONSOLE'");
                        kff.mysqlUpdate(l, null);
                        Config.PlayerFile.mysql.batch(l, new Runnable() {
                            @Override
                            public void run() {
                                lang.msg(sender, "migrate.end");
                            }
                        });
                        Config.PlayerFile.backend = BackendType.MYSQL;
                        kf.save();
                        return true;
                    } else {
                        lang.msg(sender, "notdone");
                    }
                    return true;
                }
            }
/*            if (args[0].equals("vars")) {

                
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
                    sender.sendMessage("§9§l---> Language file viewer <---");
                    for (Map.Entry<String, HashMap<String, String>> e : GlobalLangFile.map.entrySet()) {
                        if (e.getKey().contains(".")) continue;
                        out.append("\n\\|§b").append(e.getKey()).append("\\-R/spigotlib lf ").append(e.getKey()).append("\\|");
                    }
                } else {
                    sender.sendMessage("§9§l---> Language file viewer - " + args[1] + " <---");
                    int len = args[1].length();
                    for (Map.Entry<String, HashMap<String, String>> e : GlobalLangFile.map.entrySet()) {
                        String sub;
                        if (!e.getKey().startsWith(args[1]) || (sub = e.getKey().substring(len)).contains("."))
                            continue;
                        out.append("\n\\|§b").append(sub).append(": \\-R/spigotlib lf ").append(e.getKey()).append("\\|§f").append((String) ((Object) e.getValue()));
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
            }*/
            /*if (args[0].equals("setamount")) {
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
            return true;*/
        } catch (Throwable e) {
            if (Config.debug)
                Main.errorLog(sender, e);
            else {
                try {
                    lang.msg(sender, "wrongsyntax");
                } catch (Throwable e2) {
                    Main.errorLog(sender, e2);
                }
            }
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> out = new ArrayList<>();
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
            if (args[0].equals("reload")) {
                return Lists.newArrayList(SU.filterStart(new String[]{"config", "pf", "lf"}, args[1], false));
            } else {
                return null;
            }
        }
        return out;
    }
}

