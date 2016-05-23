package gyurix.spigotlib;

import com.google.common.collect.Lists;
import gyurix.animation.AnimationAPI;
import gyurix.api.BungeeAPI;
import gyurix.api.TitleAPI;
import gyurix.api.VariableAPI;
import gyurix.configfile.ConfigData;
import gyurix.configfile.ConfigFile;
import gyurix.configfile.ConfigSerialization;
import gyurix.configfile.DefaultSerializers;
import gyurix.economy.EconomyAPI;
import gyurix.economy.EconomyVaultHook;
import gyurix.nbt.NBTApi;
import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.manager.Protocol18_19;
import gyurix.protocol.utils.WrapperFactory;
import gyurix.scoreboard.ScoreboardAPI;
import gyurix.spigotutils.BackendType;
import gyurix.spigotutils.ServerVersion;
import gyurix.spigotutils.TPSMeter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import javax.script.ScriptEngineManager;
import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

public class Main extends JavaPlugin implements Listener {
    public static final String[] commands = new String[]{"chm", "abm", "sym", "title", "titledata", "titlehide", "vars",
            "hasperm", "packets", "lang", "lf", "pf", "save", "reload", "errors", "velocity", "setamount", "item"};
    public static final String version = "4.0DEV";
    public static File dir;
    public static boolean fullyEnabled = false;
    public static ConfigFile kf;
    public static GlobalLangFile.PluginLang lang;
    public static Main pl;

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

    public void load() throws Throwable {
        SU.cs.sendMessage("§2[§aStartup§2]§e Loading configuration and language file...");
        SU.saveResources(this, "lang.yml", "config.yml", "enchants.yml");
        kf = new ConfigFile(this.getResource("config.yml"));
        kf.load(new File(dir + File.separator + "config.yml"));
        Main.kf.data.deserialize(Config.class);
        kf.save();
        lang = GlobalLangFile.loadLF("spigotlib", dir + File.separator + "lang.yml");
        SU.cs.sendMessage("§2[§aStartup§2]§e Loading enchants file...");
        Type[] types = ((ParameterizedType) Config.class.getField("enchants").getGenericType()).getActualTypeArguments();
        Config.enchants = new ConfigFile(new File(dir + File.separator + "enchants.yml")).data.deserialize(HashMap.class, types);
        for (Map.Entry<String, ArrayList<String>> e : Config.enchants.entrySet()) {
            Enchantment ec = Enchantment.getByName(e.getKey());
            for (String s : e.getValue()) {
                Config.enchantAliases.put(s, ec);
            }
        }
        if (Config.PlayerFile.backend == BackendType.FILE) {
            SU.cs.sendMessage("§2[§aStartup§2]§e Loading §cFILE§e backend for §cplayer file§e...");
            SU.pf = new ConfigFile(new File(dir + File.separator + Config.PlayerFile.file));
        } else if (Config.PlayerFile.backend == BackendType.MYSQL) {
            SU.cs.sendMessage("§2[§aStartup§2]§e Loading §cMySQL§e backend for §cplayer file§e...");
            SU.pf = new ConfigFile(Config.PlayerFile.mysql, Config.PlayerFile.mysql.table, "key", "value");
            SU.loadPlayerConfig(null);
        }
        SU.cs.sendMessage("§2[§aStartup§2]§e Loading AnimationAPI...");
        AnimationAPI.init();
        SU.cs.sendMessage("§2[§aStartup§2]§e Loading ReflectionAPI...");
        Reflection.init();
        ConfigSerialization.interfaceBasedClasses.put(ItemStack.class, Reflection.getOBCClass("inventory.CraftItemStack"));
        if (Reflection.ver == ServerVersion.v1_8 || Reflection.ver == ServerVersion.v1_9) {
            SU.cs.sendMessage("§2[§aStartup§2]§e The server version is compatible (§c" + Reflection.ver + "§e), starting PacketAPI, ChatAPI, TitleAPI, NBTApi, ScoreboardAPI...");
            WrapperFactory.init();
            PacketInType.init();
            PacketOutType.init();
            ChatAPI.init();
            TitleAPI.init();
            NBTApi.init();
            ScoreboardAPI.init();
        } else {
            SU.cs.sendMessage("§2[§aStartup§2]§e Found§c INCOMPATIBLE SERVER VERSION: §e" + Reflection.ver + "§c, so the following features was NOT loaded, so they WILL NOT work:" +
                    " §ePacketAPI, Offline player management, ChatAPI, TitleAPI, NBTApi, ScoreboardAPI§c. The other features might work. For additional help contact the plugins developer, §cgyuriX§e!");
        }
        SU.cs.sendMessage("§2[§aStartup§2]§e Preparing PlaceholderAPI and Vault hooks...");
        SU.vault = SU.pm.getPlugin("Vault") != null;
        if (SU.vault && EconomyAPI.vaultHookType == EconomyAPI.VaultHookType.PROVIDER) {
            SU.cs.sendMessage("§2[§aStartup§2]§e The plugin §cVault§e is present, hooking to it as §cEconomy PROVIDER§e...");
            EconomyVaultHook.init();
        }
        VariableAPI.phaHook = SU.pm.getPlugin("PlaceholderAPI") != null ? Config.phaHook : false;
    }

    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!sender.hasPermission("spigotlib.use")) {
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
                case "perm": {
                    if (d == null) {
                        String f = lang.get(plr, "perms.fillformat");
                        String denyperm = lang.get(plr, "perms.denyformat");
                        String allowperm = lang.get(plr, "perms.allowformat");
                        StringBuilder sb = new StringBuilder();
                        for (Player p : pls) {
                            Set<PermissionAttachmentInfo> perms = p.getEffectivePermissions();
                            for (PermissionAttachmentInfo perm : perms) {
                                sb.append('\n');
                                for (Map.Entry<String, Boolean> e : perm.getAttachment().getPermissions().entrySet()) {
                                    if (e.getValue())
                                        sb.append('\n').append(allowperm.replace("<perm>", e.getKey()));
                                    else
                                        sb.append('\n').append(denyperm.replace("<perm>", e.getKey()));
                                }
                            }
                            sender.sendMessage(f.replace("<perms>", sb.toString()));
                        }
                    } else {
                        for (Player p : pls)
                            lang.msg(sender, p.hasPermission(d[0]) ? "perms.yes" : "perms.no", "perm", d[0]);
                    }
                }
                case "debug": {
                    Config.debug = !Config.debug;
                    lang.msg(sender, "debug." + (Config.debug ? "on" : "off"));
                    return true;
                }
                case "reload": {
                    if (args[1].equals("config")) {
                        kf.reload();
                        Main.kf.data.deserialize(Config.class);
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
                case "save": {
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
                case "velocity": {
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
                        for (Player p : pls) {
                            p.setVelocity(v);
                            lang.msg(sender, "velocity.set");
                        }
                    }
                    return true;
                }
                case "migratetodb": {
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
                }
                case "lang": {
                    if (args.length == 1) {
                        lang.msg(sender, "lang.list", "langs", StringUtils.join(GlobalLangFile.map.keySet(), ", "));
                        return true;
                    }
                    if (!sender.hasPermission("spigotlib.command.lang.others"))
                        pls = Lists.newArrayList(plr);

                    if (d == null) {
                        for (Player p : pls) {
                            String lng = SU.getPlayerConfig(p).getString("lang");
                            if (lng == null)
                                lng = Config.defaultLang;
                            CommandSender cs = p == null ? SU.cs : p;
                            lang.msg(sender, "lang." + (cs.getName().equals(p.getName()) ? "own" : "other"), "player", cs.getName(), "lang", lng);
                        }
                        return true;
                    }
                    d[0] = d[0].toLowerCase();
                    if (!GlobalLangFile.map.keySet().contains(d[0])) {
                        lang.msg(sender, "lang.notfound", "lang", d[0]);
                        return true;
                    }
                    for (Player p : pls) {
                        SU.getPlayerConfig(p).setString("lang", d[0]);
                        CommandSender cs = p == null ? SU.cs : p;
                        lang.msg(sender, "lang.set" + (cs.getName().equals(p.getName()) ? "" : ".other"), "player", cs.getName(), "lang", d[0]);
                    }
                    return true;
                }
                default: {
                    lang.msg(sender, "notdone");
                    return true;
                }
            }
        } catch (Throwable e) {
            SU.error(sender, e, "SpigotLib", "gyurix");

        }
        return true;
    }

    public void onDisable() {
        SU.log(this, "§4[§cShutdown§4]§e Saving players...");
        if (Config.PlayerFile.backend == BackendType.FILE)
            SU.pf.save();
        else if (Config.PlayerFile.backend == BackendType.MYSQL) {
            ArrayList<String> list = new ArrayList<>();
            for (String s : SU.pf.getStringKeyList()) {
                SU.pf.subConfig(s, "uuid='" + s + "'").mysqlUpdate(list, null);
            }
            SU.pf.db.batch(list, null);
        }
        SU.log(this, "§4[§cShutdown§4]§e Stopping TPSMeter...");
        TPSMeter.meter.cancel(true);
        if (SU.tp != null) {
            SU.log(this, "§4[§cShutdown§4]§e Stopping PacketAPI...");
            SU.tp.close();
        }
        SU.log(this, "§4[§cShutdown§4]§e Stopping AnimationAPI...");
        AnimationAPI.sch.shutdownNow();
        if (Reflection.ver == ServerVersion.v1_8 || Reflection.ver == ServerVersion.v1_9) {
            SU.log(this, "§4[§cShutdown§4]§e Stopping ScoreboardAPI...");
            for (Player p : Bukkit.getOnlinePlayers()) {
                ScoreboardAPI.setSidebar(p, null);
                ScoreboardAPI.setTabbar(p, null);
                ScoreboardAPI.setNametagBar(p, null);
            }
        }
        SU.log(this, "§4[§cShutdown§4]§e Unloading plugins depending on SpigotLib...");
        for (Plugin p : Lists.newArrayList(SU.pm.getPlugins())) {
            PluginDescriptionFile pdf = p.getDescription();
            if (pdf.getDepend() != null && pdf.getDepend().contains("SpigotLib")) {
                SU.log(this, "§4[§cShutdown§4]§e Unloading plugin §f" + p.getName() + "§e...");
                SU.unloadPlugin(p);
            }
        }
        SU.log(this, "§4[§cShutdown§4]§a The SpigotLib has shutted down properly.");
    }

    public void onEnable() {
        if (Reflection.ver == ServerVersion.v1_8 || Reflection.ver == ServerVersion.v1_9) {
            SU.cs.sendMessage("§2[§aStartup§2]§e Starting PacketAPI...");
            SU.tp = new Protocol18_19();
            SU.pm.registerEvents(SU.tp, this);
        }
        SU.cs.sendMessage("§2[§aStartup§2]§e Initializing offline player manager...");
        SU.initOfflinePlayerManager();
        SU.pm.registerEvents(this, this);
        SU.cs.sendMessage("§2[§aStartup§2]§e Starting BungeeAPI...");
        SU.msg.registerOutgoingPluginChannel(this, "BungeeCord");
        SU.msg.registerIncomingPluginChannel(this, "BungeeCord", new BungeeAPI());
        if (Config.PlayerFile.backend == BackendType.MYSQL) {
            SU.cs.sendMessage("§2[§aStartup§2]§e Loading player data of online players from the MySQL...");
            for (Player p : Bukkit.getOnlinePlayers()) {
                SU.loadPlayerConfig(p.getUniqueId());
            }
        }
        if (!SU.vault)
            SU.cs.sendMessage("§2[§aStartup§2]§e The plugin §cVault§e is not present, skipping hook...");
        else if (SU.vault && EconomyAPI.vaultHookType == EconomyAPI.VaultHookType.NONE)
            SU.cs.sendMessage("§2[§aStartup§2]§e The plugin §cVault§e is present, but the hook is disabled in config, so skipping hook...");
        if (SU.vault && EconomyAPI.vaultHookType == EconomyAPI.VaultHookType.USER) {
            SU.cs.sendMessage("§2[§aStartup§2]§e The plugin §cVault§e is present, hooking to it as §cEconomy USER§e...");
            RegisteredServiceProvider<Economy> rsp = SU.srv.getServicesManager().getRegistration(Economy.class);
            if (rsp != null)
                SU.econ = rsp.getProvider();
            if (EconomyAPI.migrate) {
                SU.log(this, "§bMigrating economy data from old Economy " + SU.econ.getName() + "... ");
                EconomyAPI.vaultHookType = EconomyAPI.VaultHookType.NONE;
                for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
                    EconomyAPI.setBalance(op.getUniqueId(), new BigDecimal(SU.econ.getBalance(op)));
                    System.out.println("Done player " + op.getName());
                }
                EconomyAPI.vaultHookType = EconomyAPI.VaultHookType.PROVIDER;
                EconomyAPI.migrate = false;
                SU.log(this, "§bFinished data migration, please restart the server!");
                setEnabled(false);
                return;
            }
        }
        SU.cs.sendMessage("§2[§aStartup§2]§e Scheduling §cTpsMeter§e startup...");
        SU.sch.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                Config.tpsMeter.start();
                SU.cs.sendMessage("§2[§aStartup§2]§a Started SpigotLib §e" + version + "§a properly.");
            }
        }, 1);
        SU.sch.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                fullyEnabled = true;
            }
        }, 40);
    }

    public void onLoad() {
        pl = this;
        try {
            SU.srv = this.getServer();
            SU.pm = SU.srv.getPluginManager();
            SU.cs = SU.srv.getConsoleSender();
            SU.msg = SU.srv.getMessenger();
            SU.sm = SU.srv.getServicesManager();
            SU.sch = SU.srv.getScheduler();
            SU.js = new ScriptEngineManager().getEngineByName("JavaScript");
            dir = this.getDataFolder();
        } catch (Throwable e) {
            SU.log(this, "§cFailed to get default Bukkit managers :-( The plugin is shutting down...");
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            SU.pm.disablePlugin(this);
            return;
        }
        try {
            DefaultSerializers.init();
            ConfigHook.registerSerializers();
            ConfigHook.registerVariables();
        } catch (Throwable e) {
            SU.log(this, "§cFailed to load config hook :-( The plugin is shutting down...");
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            SU.pm.disablePlugin(this);
            return;
        }
        try {
            this.load();
        } catch (Throwable e) {
            SU.log(this, "Failed to load plugin, trying to reset the config...");
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            resetConfig();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player plr = e.getPlayer();
        UUID id = plr.getUniqueId();
        SU.loadPlayerConfig(id);
        if (Reflection.ver == ServerVersion.v1_8 || Reflection.ver == ServerVersion.v1_9)
            ScoreboardAPI.playerJoin(plr);
        if (BungeeAPI.running) {
            if (Config.BungeeAPI.ipOnJoin)
                BungeeAPI.requestIP(plr);
            if (Config.BungeeAPI.uuidOnJoin)
                BungeeAPI.requestUUID(plr.getName());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player plr = e.getPlayer();
        UUID uid = plr.getUniqueId();
        SU.savePlayerConfig(uid);
        SU.unloadPlayerConfig(uid);
        ScoreboardAPI.playerLeave(plr);
    }

    @EventHandler
    public void onPluginUnload(PluginDisableEvent e) {
        Plugin pl = e.getPlugin();
        if (SU.tp != null) {
            SU.tp.unregisterIncomingListener(pl);
            SU.tp.unregisterOutgoingListener(pl);
        }
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
                return SU.filterStart(new String[]{"config", "pf", "lf"}, args[1]);
            } else {
                return null;
            }
        }
        return out;
    }

    @EventHandler
    public void registerServiceEvent(ServiceRegisterEvent e) {
        RegisteredServiceProvider p = e.getProvider();
        String sn = p.getService().getName();
        SU.log(this, "Register service - " + sn);
        switch (sn) {
            case "net.milkbowl.vault.chat.Chat":
                SU.chat = (Chat) p.getProvider();
                break;
            case "net.milkbowl.vault.economy.Economy":
                SU.econ = (Economy) p.getProvider();
                break;
            case "net.milkbowl.vault.permission.Permission":
                SU.perm = (Permission) p.getProvider();
                break;
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
            SU.log(this, "§cFailed to reset the config :-( The plugin is shutting down...");
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            SU.pm.disablePlugin(this);
            return;
        }
        try {
            load();
        } catch (Throwable e) {
            SU.log(this, "§cFailed to load plugin after config reset :-( The plugin is shutting down...");
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            SU.pm.disablePlugin(this);
        }
    }

    @EventHandler
    public void unregisterServiceEvent(ServiceUnregisterEvent e) {
        RegisteredServiceProvider p = e.getProvider();
        String sn = p.getService().getName();
        SU.log(this, "Unregister service - " + sn);
        switch (sn) {
            case "net.milkbowl.vault.chat.Chat":
                SU.chat = (Chat) p.getProvider();
                break;
            case "net.milkbowl.vault.economy.Economy":
                SU.econ = (Economy) p.getProvider();
                break;
            case "net.milkbowl.vault.permission.Permission":
                SU.perm = (Permission) p.getProvider();
                break;
        }
    }
}

