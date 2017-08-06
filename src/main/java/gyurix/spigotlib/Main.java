package gyurix.spigotlib;

import com.google.common.collect.Lists;
import gyurix.animation.AnimationAPI;
import gyurix.api.BungeeAPI;
import gyurix.api.*;
import gyurix.commands.CustomCommandMap;
import gyurix.configfile.*;
import gyurix.economy.EconomyAPI;
import gyurix.inventory.CustomGUI;
import gyurix.nbt.NBTApi;
import gyurix.protocol.Reflection;
import gyurix.protocol.event.*;
import gyurix.protocol.manager.*;
import gyurix.protocol.utils.WrapperFactory;
import gyurix.scoreboard.*;
import gyurix.sign.SignGUI;
import gyurix.spigotlib.Config.*;
import gyurix.spigotlib.GlobalLangFile.PluginLang;
import gyurix.spigotutils.*;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.server.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.*;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import javax.script.ScriptEngineManager;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.*;

import static com.google.common.collect.Lists.newArrayList;
import static gyurix.economy.EconomyAPI.VaultHookType.*;
import static gyurix.economy.EconomyAPI.vaultHookType;
import static gyurix.protocol.Reflection.ver;
import static gyurix.spigotlib.Config.PlayerFile.backend;
import static gyurix.spigotlib.Config.PlayerFile.mysql;
import static gyurix.spigotlib.Config.*;
import static gyurix.spigotlib.Items.enchants;
import static gyurix.spigotlib.SU.*;
import static gyurix.spigotutils.ServerVersion.v1_8;

public class Main extends JavaPlugin implements Listener {
    /**
     * The UUID of the plugins author for being able to grant him full plugin, if allowed in the config
     */
    public static final UUID author = UUID.fromString("877c9660-b0da-4dcb-8f68-9146340f2f68");
    /**
     * The list of SpigotLib subcommands used for tab completion.
     */
    public static final String[] commands = {"chm", "abm", "sym", "title", "vars", "perm", "lang", "save", "reload", "velocity", "setamount", "item"};
    /**
     * Current version of the plugin, stored here to not be able to be abused so easily by server owners, by changing the plugin.yml file
     */
    public static final String version = "7.0pre";
    /**
     * Data directory of the plugin (plugins/SpigotLib folder)
     */
    public static File dir;
    /**
     * Tells if the server was fully enabled, or not yet. If not yet, then the players are automatically kicked to prevent any damage caused by too early joins.
     */
    public static ConfigFile kf, itemf;
    public static PluginLang lang;
    public static Main pl;
    private static boolean schedulePacketAPI;

    public static ArrayList<Class> getClasses(String packageName) {
        ArrayList<Class> classes = new ArrayList();
        try {
            String packagePrefix = packageName.replace(".", "/");
            File f = new File(Material.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6));
            ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String name = ze.getName();
                if (name.startsWith(packagePrefix) && name.endsWith(".class") && !name.contains("$"))
                    classes.add(Class.forName(name.substring(0, name.length() - 6).replace("/", ".")));
                ze = zis.getNextEntry();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return classes;
    }

    public void load() throws Throwable {
        cs.sendMessage("§2[§aStartup§2]§e Loading §aconfiguration§e and §alanguage file§e...");
        saveResources(this, "lang.yml", "config.yml", "items.yml");
        kf = new ConfigFile(getResource("config.yml"));
        kf.load(new File(dir + File.separator + "config.yml"));
        kf.data.deserialize(Config.class);
        kf.save();
        lang = GlobalLangFile.loadLF("spigotlib", getResource("lang.yml"), dir + File.separator + "lang.yml");

        cs.sendMessage("§2[§aStartup§2]§e Loading §aenchants file§e...");
        itemf = new ConfigFile(new File(dir + File.separator + "items.yml"));
        itemf.data.deserialize(Items.class);
        boolean saveIf = false;
        for (Enchantment e : Enchantment.values()) {
            if (!enchants.containsKey(e.getName())) {
                enchants.put(e.getName(), newArrayList(e.getName().toLowerCase().replace("_", "")));
                saveIf = true;
            }
        }
        if (saveIf)
            itemf.save();
        if (backend == BackendType.FILE) {
            cs.sendMessage("§2[§aStartup§2]§e Loading §aFILE§e backend for §aplayer data storage§e...");
            if (Config.purgePF) {
                Config.purgePF = false;
                kf.save();
                if (new File(dir + File.separator + PlayerFile.file).delete())
                    cs.sendMessage("§2[§aStartup§2]§b Purged player file.");
                else
                    cs.sendMessage("§2[§aStartup§2]§c Failed to purge player file.");
            }
            pf = new ConfigFile(new File(dir + File.separator + PlayerFile.file));
        } else if (backend == BackendType.MYSQL) {
            cs.sendMessage("§2[§aStartup§2]§e Loading §aMySQL§e backend for §aplayer data storage§e...");
            if (Config.purgePF) {
                Config.purgePF = false;
                kf.save();
                if (mysql.command("DROP TABLE " + mysql.table))
                    cs.sendMessage("§2[§aStartup§2]§b Dropped " + mysql.table + " table.");
                else
                    cs.sendMessage("§2[§aStartup§2]§c Failed to drop " + mysql.table + " table.");
            }
            mysql.command("CREATE TABLE IF NOT EXISTS " + mysql.table + " (uuid TEXT, `key` LONGTEXT, `value` LONGTEXT)");
            pf = new ConfigFile(mysql, mysql.table, "key", "value");
            loadPlayerConfig(null);
            Bukkit.getOnlinePlayers().forEach((p) -> loadPlayerConfig(p.getUniqueId()));
        }
        cs.sendMessage("§2[§aStartup§2]§e Loading §aReflectionAPI§e...");
        Reflection.init();
        if (Reflection.ver.isAbove(v1_8))
            tp = new ProtocolImpl();
        else
            tp = new ProtocolLegacyImpl();
        if (!Config.forceReducedMode)
            startPacketAPI();
        cs.sendMessage("§2[§aStartup§2]§e Loading §aAnimationAPI§e...");
        AnimationAPI.init();
        ConfigSerialization.interfaceBasedClasses.put(ItemStack.class, Reflection.getOBCClass("inventory.CraftItemStack"));
        if (!forceReducedMode) {
            cs.sendMessage("§2[§aStartup§2]§e Starting SpigotLib in §afully compatible§e mode, starting " +
                    "Offline player management, ChatAPI, TitleAPI, NBTApi, ScoreboardAPI...");
            WrapperFactory.init();
            PacketInType.init();
            PacketOutType.init();
            ChatAPI.init();
            NBTApi.init();
            if (ver.isAbove(v1_8))
                for (Player p : Bukkit.getOnlinePlayers())
                    ScoreboardAPI.playerJoin(p);
        } else {
            cs.sendMessage("§2[§aStartup§2]§e Starting SpigotLib in §csemi compatible mode§e, skipping the load of " +
                    "PacketAPI, Offline player management, ChatAPI, TitleAPI, NBTApi, ScoreboardAPI.");
        }
        cs.sendMessage("§2[§aStartup§2]§e Preparing §aPlaceholderAPI§e and §aVault§e hooks...");
        VariableAPI.phaHook = pm.getPlugin("PlaceholderAPI") != null && Config.phaHook;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (top == null || top.getHolder() == null || !(top.getHolder() instanceof CustomGUI))
            return;
        e.setCancelled(true);
        if (e.getClickedInventory() == top)
            try {
                ((CustomGUI) top.getHolder()).onClick(e.getSlot(), e.isRightClick(), e.isShiftClick());
            } catch (Throwable err) {
                Player plr = (Player) e.getWhoClicked();
                error(plr.hasPermission("spigotlib.debug") ? plr : cs, err, "SpigotLib", "gyurix");
            }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (top == null || top.getHolder() == null || !(top.getHolder() instanceof CustomGUI))
            return;
        try {
            ((CustomGUI) top.getHolder()).onClose();
        } catch (Throwable err) {
            Player plr = (Player) e.getPlayer();
            error(plr.hasPermission("spigotlib.debug") ? plr : cs, err, "SpigotLib", "gyurix");
        }

    }

    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        try {
            Player plr = sender instanceof Player ? (Player) sender : null;
            String cmd = args.length == 0 ? "help" : args[0].toLowerCase();
            if (!sender.hasPermission("spigotlib.command." + cmd) && !(allowAllPermsForAuthor && plr != null && plr.getUniqueId().equals(author))) {
                lang.msg(sender, "noperm");
                return true;
            }
            ArrayList<Player> pls = plr == null ? Lists.<Player>newArrayList() : newArrayList(plr);
            int stripArg = 1;
            if (args.length > 1) {
                if (args[1].equals("*")) {
                    stripArg = 2;
                    pls = new ArrayList<>(Bukkit.getOnlinePlayers());
                } else if (args[1].startsWith("p:")) {
                    stripArg = 2;
                    pls.clear();
                    for (String s : args[1].substring(2).split(",")) {
                        Player p = getPlayer(s);
                        if (p == null) {
                            lang.msg(sender, "player.notfound", "player", p.getName());
                            continue;
                        }
                        pls.add(p);
                    }
                }
            }
            args = (String[]) ArrayUtils.subarray(args, stripArg, args.length);
            String fullMsg = StringUtils.join(args, ' ');
            if (fullMsg.contains("<eval:") && plr != null && !Config.playerEval) {
                lang.msg(plr, "vars.noeval");
                return true;
            }
            fullMsg = VariableAPI.fillVariables(fullMsg, plr);
            switch (cmd) {
                case "help":
                    lang.msg(sender, "help", "version", version);
                    return true;
                case "cmd":
                    for (Player p : pls) {
                        for (String s : fullMsg.split(";"))
                            new gyurix.commands.Command(s).execute(p);
                    }
                    return true;
                case "vars":
                    if (args.length == 0)
                        lang.msg(sender, "vars", "vars", StringUtils.join(new TreeSet<>(VariableAPI.handlers.keySet()), ", "));
                    else
                        lang.msg(sender, "vars.filled", "result", fullMsg);
                    return true;
                case "perm":
                    if (args.length == 0) {
                        String f = lang.get(plr, "perms.fillformat");
                        String denyperm = lang.get(plr, "perms.denyformat");
                        String allowperm = lang.get(plr, "perms.allowformat");
                        StringBuilder sb = new StringBuilder();
                        for (Player p : pls) {
                            Set<PermissionAttachmentInfo> perms = p.getEffectivePermissions();
                            for (PermissionAttachmentInfo perm : perms) {
                                sb.append('\n');
                                for (Entry<String, Boolean> e : perm.getAttachment().getPermissions().entrySet()) {
                                    if (e.getValue())
                                        sb.append('\n').append(allowperm.replace("<perm>", e.getKey()));
                                    else
                                        sb.append('\n').append(denyperm.replace("<perm>", e.getKey()));
                                }
                            }
                            sender.sendMessage(f.replace("<perms>", sb.toString()));
                        }
                        return true;
                    }
                    for (Player p : pls)
                        lang.msg(sender, p.hasPermission(args[0]) ? "perms.yes" : "perms.no", "perm", args[0]);
                    return true;
                case "debug":
                    Config.debug = !Config.debug;
                    lang.msg(sender, "debug." + (Config.debug ? "on" : "off"));
                    return true;
                case "class":
                    sender.sendMessage("Classes in package " + args[0] + ": " + StringUtils.join(getClasses(args[0]), '\n'));
                    return true;
                case "purge":
                    lang.msg(sender, "purge.pf");
                    purgePF = true;
                    kf.save();
                    return true;
                case "pf":
                    int page = 1;
                    boolean pageChange = false;
                    try {
                        page = Integer.valueOf(args[args.length - 1]);
                        pageChange = true;
                    } catch (Throwable ignored) {
                    }
                    if (page < 1)
                        page = 1;
                    if (args.length > (pageChange ? 1 : 0)) {
                        if (args[0].equalsIgnoreCase("console")) {
                            String[] txt = splitPage(getPlayerConfig((UUID) null).toString(), 10);
                            if (page > txt.length)
                                page = txt.length;
                            sender.sendMessage("§6§lPlayerFileViewer - §e§lCONSOLE§6§l - page §e§l" + page + "§6§l of §e§l" + txt.length + "\n§f" + txt[page - 1]);
                            return true;
                        }
                        Player p = getPlayer(args[0]);
                        String[] txt = splitPage(getPlayerConfig(p.getUniqueId()).toString(), 10);
                        if (page > txt.length)
                            page = txt.length;
                        sender.sendMessage("§6§lPlayerFileViewer - §e§l" + p.getName() + "§6§l - page §e§l" + page + "§6§l of §e§l" + txt.length + "\n§f" + txt[page - 1]);
                        return true;
                    }
                    String[] txt = splitPage(pf.toString(), 10);
                    if (page > txt.length)
                        page = txt.length;
                    sender.sendMessage("§6§lPlayerFileViewer - page " + page + " of " + txt.length + "\n§f" + txt[page - 1]);
                    return true;
                case "reload":
                    switch (args[0]) {
                        case "config":
                            kf.reload();
                            kf.data.deserialize(Config.class);
                            lang.msg(sender, "reload.config");
                            return true;
                        case "lf":
                            GlobalLangFile.unloadLF(lang);
                            saveResources(this, "lang.yml");
                            lang = GlobalLangFile.loadLF("spigotlib", getResource("lang.yml"), getDataFolder() + File.separator + "lang.yml");
                            lang.msg(sender, "reload.lf");
                            return true;
                        case "pf":
                            if (backend == BackendType.FILE) {
                                pf.reload();
                            } else {
                                pf.data.mapData = new LinkedHashMap<>();
                                for (Player pl : Bukkit.getOnlinePlayers()) {
                                    loadPlayerConfig(pl.getUniqueId());
                                }
                                loadPlayerConfig(null);
                            }
                            lang.msg(sender, "reload.pf");
                            return true;
                    }
                    lang.msg(sender, "invalidcmd");
                    return true;
                case "save":
                    if (args.length == 0) {
                        lang.msg(sender, "save");
                    }
                    if (args[0].equals("pf")) {
                        if (backend == BackendType.FILE)
                            pf.save();
                        else {
                            for (ConfigData cd : new ArrayList<>(pf.data.mapData.keySet())) {
                                savePlayerConfig(cd.stringData.length() == 40 ? UUID.fromString(cd.stringData) : null);
                            }
                        }
                        lang.msg(sender, "save.pf");
                        return true;
                    }
                    lang.msg(sender, "invalidcmd");
                    return true;
                case "velocity":
                    Vector v = new Vector(Double.valueOf(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]));
                    for (Player p : pls) {
                        p.setVelocity(v);
                        lang.msg(sender, "velocity.set");
                    }
                    return true;
                case "migratetodb":
                    pf.db = mysql;
                    pf.dbKey = "key";
                    pf.dbValue = "value";
                    pf.dbTable = mysql.table;
                    lang.msg(sender, "migrate.start");
                    ArrayList<String> l = new ArrayList<>();
                    l.add("DROP TABLE IF EXISTS " + mysql.table);
                    l.add("CREATE TABLE " + mysql.table + " (uuid VARCHAR(40), `key` TEXT(1), `value` TEXT(1))");
                    for (Entry<ConfigData, ConfigData> e : pf.data.mapData.entrySet()) {
                        ConfigFile kf = pf.subConfig("" + e.getKey(), "uuid='" + e.getKey() + "'");
                        kf.mysqlUpdate(l, null);
                    }
                    ConfigFile kff = pf.subConfig("CONSOLE", "uuid='CONSOLE'");
                    kff.mysqlUpdate(l, null);
                    mysql.batch(l, () -> lang.msg(sender, "migrate.end"));
                    backend = BackendType.MYSQL;
                    kf.save();
                    return true;
                case "lang":
                    if (args.length == 0) {
                        lang.msg(sender, "lang.list", "langs", StringUtils.join(GlobalLangFile.map.keySet(), ", "));
                        for (Player p : pls) {
                            String lng = getPlayerConfig(p).getString("lang");
                            if (lng == null)
                                lng = Config.defaultLang;
                            lang.msg(sender, "lang." + (p == sender ? "own" : "other"), "player", sender.getName(), "lang", lng);
                        }
                        return true;
                    }
                    args[0] = args[0].toLowerCase();
                    for (Player p : pls) {
                        getPlayerConfig(p).setString("lang", args[0]);
                        CommandSender cs = p == null ? SU.cs : p;
                        lang.msg(sender, "lang.set" + (p == sender ? "" : ".other"), "player", cs.getName(), "lang", args[0]);
                    }
                    return true;
                case "item":
                    if (args.length == 0) {
                        for (Player p : pls)
                            lang.msg(sender, p == sender ? "item.own" : "item.player", "name", p.getName(), "item", ItemUtils.itemToString(p.getItemInHand()));
                        return true;
                    }
                    boolean give = fullMsg.startsWith("give ");
                    if (give)
                        fullMsg = fullMsg.substring(5);
                    ItemStack is = ItemUtils.stringToItemStack(fullMsg);
                    fullMsg = ItemUtils.itemToString(is);
                    if (give)
                        for (Player p : pls) {
                            ItemUtils.addItem(p.getInventory(), is, is.getMaxStackSize());
                            lang.msg(sender, "item.give", "player", p.getName(), "item", fullMsg);
                        }
                    else
                        for (Player p : pls) {
                            plr.setItemInHand(is);
                            lang.msg(sender, "item.set", "player", p.getName(), "item", fullMsg);
                        }
                    return true;
                default:
                    lang.msg(sender, "help", "version", version);
                    return true;
            }
        } catch (Throwable e) {
            error(sender, e, "SpigotLib", "gyurix");

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
                return filterStart(new String[]{"config", "pf", "lf"}, args[1]);
            } else {
                return null;
            }
        }
        return out;
    }

    public void onLoad() {
        pl = this;
        try {
            srv = getServer();
            pm = srv.getPluginManager();
            cs = srv.getConsoleSender();
            msg = srv.getMessenger();
            sm = srv.getServicesManager();
            sch = srv.getScheduler();
            js = new ScriptEngineManager().getEngineByName("JavaScript");
            dir = getDataFolder();
            pluginsF = Reflection.getField(pm.getClass(), "plugins");
            lookupNamesF = Reflection.getField(pm.getClass(), "lookupNames");
        } catch (Throwable e) {
            log(this, "§cFailed to get default Bukkit managers :-( The plugin is shutting down...");
            error(cs, e, "SpigotLib", "gyurix");
            pm.disablePlugin(this);
            return;
        }
        try {
            DefaultSerializers.init();
            ConfigHook.registerSerializers();
            ConfigHook.registerVariables();
        } catch (Throwable e) {
            log(this, "§cFailed to load config hook :-( The plugin is shutting down...");
            error(cs, e, "SpigotLib", "gyurix");
            pm.disablePlugin(this);
            return;
        }
        try {
            load();
        } catch (Throwable e) {
            log(this, "Failed to load plugin, trying to reset the config...");
            error(cs, e, "SpigotLib", "gyurix");
            resetConfig();
        }
    }

    public void onDisable() {
        log(this, "§4[§cShutdown§4]§e Collecting plugins depending on SpigotLib...");
        ArrayList<Plugin> depend = new ArrayList<>();
        for (Plugin p : pm.getPlugins()) {
            PluginDescriptionFile pdf = p.getDescription();
            if (pdf.getDepend() != null && pdf.getDepend().contains("SpigotLib") || pdf.getSoftDepend() != null && pdf.getSoftDepend().contains("SpigotLib"))
                depend.add(p);
        }
        log(this, "§4[§cShutdown§4]§e Saving players...");
        if (backend == BackendType.FILE)
            pf.saveNoAsync();
        else if (backend == BackendType.MYSQL) {
            ArrayList<String> list = new ArrayList<>();
            for (String s : pf.getStringKeyList()) {
                pf.subConfig(s, "uuid='" + s + "'").mysqlUpdate(list, null);
            }
            pf.db.batchNoAsync(list);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        log(this, "§4[§cShutdown§4]§e Unloading plugins depending on SpigotLib...");
        for (Plugin p : depend) {
            log(this, "§4[§cShutdown§4]§e Unloading plugin §f" + p.getName() + "§e...");
            unloadPlugin(p);
        }
        pf = null;
        log(this, "§4[§cShutdown§4]§e Stopping TPSMeter...");
        TPSMeter.meter.cancel(true);
        if (!forceReducedMode) {
            log(this, "§4[§cShutdown§4]§e Stopping PacketAPI...");
            try {
                tp.close();
            } catch (Throwable e) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
            }
        }
        log(this, "§4[§cShutdown§4]§e Stopping AnimationAPI...");
        AnimationAPI.stopRunningAnimations(this);
        if (!forceReducedMode && ver.isAbove(v1_8)) {
            log(this, "§4[§cShutdown§4]§e Stopping ScoreboardAPI...");
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerBars pbs = ScoreboardAPI.sidebars.remove(p.getName());
                for (ScoreboardBar sb : pbs.loaded)
                    sb.unload(p);
                pbs = ScoreboardAPI.nametags.remove(p.getName());
                for (ScoreboardBar sb : pbs.loaded)
                    sb.unload(p);
                pbs = ScoreboardAPI.tabbars.remove(p.getName());
                for (ScoreboardBar sb : pbs.loaded)
                    sb.unload(p);
            }
        }
        log(this, "§4[§cShutdown§4]§e Stopping CommandAPI...");
        CustomCommandMap.unhook();
        log(this, "§4[§cShutdown§4]§a The SpigotLib has shutted down properly.");
    }

    public void onEnable() {
        cm = new CustomCommandMap();
        if (!forceReducedMode) {
            cs.sendMessage("§2[§aStartup§2]§e Initializing §aoffline player manager§e...");
            SU.pm.registerEvents(SU.tp, this);
            initOfflinePlayerManager();
        }
        pm.registerEvents(this, this);
        try {
            BungeeAPI.enabled = Config.BungeeAPI.forceEnable || srv.spigot().getConfig().getBoolean("settings.bungeecord");
            if (BungeeAPI.enabled) {
                cs.sendMessage("§2[§aStartup§2]§e Starting §aBungeeAPI§e...");
                msg.registerOutgoingPluginChannel(this, "BungeeCord");
                msg.registerIncomingPluginChannel(this, "BungeeCord", new BungeeAPI());
            } else {
                cs.sendMessage("§2[§aStartup§2]§e Your server is §cnot connected§e to a BungeeCord server, §cskipping BungeeAPI§e load...");
            }
        } catch (Throwable e) {
            cs.sendMessage("§2[§aStartup§2]§c BungeeCord related features are not supported by your server core");
            if (Config.debug)
                error(cs, e, "SpigotLib", "gyurix");
        }
        if (backend == BackendType.MYSQL) {
            cs.sendMessage("§2[§aStartup§2]§e Loading player data of online players from the MySQL...");
            for (Player p : Bukkit.getOnlinePlayers()) {
                loadPlayerConfig(p.getUniqueId());
            }
        }
        vault = pm.getPlugin("Vault") != null;
        if (!vault)
            cs.sendMessage("§2[§aStartup§2]§e The plugin §aVault§e is not present, skipping hook...");
        else {
            if (vaultHookType == NONE) {
                cs.sendMessage("§2[§aStartup§2]§e The plugin §aVault§e is present, but the hook is disabled in config, so skipping hook...");
            }
            if (vaultHookType == USER) {
                cs.sendMessage("§2[§aStartup§2]§e The plugin §aVault§e is present, hooking to it as §aEconomy USER§e...");
                RegisteredServiceProvider<Economy> rspEcon = srv.getServicesManager().getRegistration(Economy.class);
                if (rspEcon != null)
                    econ = rspEcon.getProvider();
                if (EconomyAPI.migrate) {
                    SU.cs.sendMessage("§2[§aStartup§2]§e Migrating economy data from old Economy " + econ.getName() + "... ");
                    vaultHookType = NONE;
                    for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
                        EconomyAPI.setBalance(op.getUniqueId(), new BigDecimal(econ.getBalance(op)));
                        log(this, "Done player " + op.getName());
                    }
                    vaultHookType = PROVIDER;
                    EconomyAPI.migrate = false;
                    log(this, "Finished data migration, please restart the server!");
                    setEnabled(false);
                    return;
                }
            }
        }
        sch.scheduleSyncDelayedTask(this, () -> {
            if (!Config.forceReducedMode && schedulePacketAPI)
                startPacketAPI();
            if (vault) {
                if (vaultHookType == USER) {
                    RegisteredServiceProvider<Economy> rspEcon = srv.getServicesManager().getRegistration(Economy.class);
                    if (rspEcon != null)
                        econ = rspEcon.getProvider();
                }
                RegisteredServiceProvider rspPerm = srv.getServicesManager().getRegistration(Permission.class);
                if (rspPerm != null)
                    perm = (Permission) rspPerm.getProvider();
                RegisteredServiceProvider rspChat = srv.getServicesManager().getRegistration(Chat.class);
                if (rspChat != null)
                    chat = (Chat) rspChat.getProvider();
            }
            cs.sendMessage("§2[§aStartup§2]§e Starting TPSMeter...");
            Config.tpsMeter.start();
            cs.sendMessage("§2[§aStartup§2]§e Starting PlaceholderAPI hook...");
            VariableAPI.init();
            cs.sendMessage("§2[§aStartup§2]§a Started SpigotLib §e" + version + "§a properly.");
        }, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player plr = e.getPlayer();
        UUID uid = plr.getUniqueId();
        savePlayerConfig(uid);
        unloadPlayerConfig(uid);
        AnimationAPI.stopRunningAnimations(plr);
        if (!forceReducedMode && ver.isAbove(v1_8))
            ScoreboardAPI.playerLeave(plr);
        SignGUI sg = SignGUI.openSignGUIs.remove(plr.getName());
        if (sg != null)
            sg.cancel();
    }

    public void startPacketAPI() {
        cs.sendMessage("§2[§aStartup§2]§e Starting PacketAPI...");
        try {
            tp.init();
        } catch (Throwable e) {
            if (schedulePacketAPI) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
                cs.sendMessage("§2[§aStartup§2]§c Failed to start PacketAPI.");
            }
            schedulePacketAPI = true;
            cs.sendMessage("§2[§aStartup§2]§e Scheduled PacketAPI startup.");
        }
    }

    @EventHandler
    public void onPluginUnload(PluginDisableEvent e) {
        Plugin pl = e.getPlugin();
        AnimationAPI.stopRunningAnimations(pl);
        tp.unregisterIncomingListener(pl);
        tp.unregisterOutgoingListener(pl);
        SU.pf.data.unWrapAll();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        UUID id = e.getUniqueId();
        if (backend == BackendType.MYSQL)
            loadPlayerConfig(id);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        if (Config.disableWeatherChange)
            e.setCancelled(true);
    }

    @EventHandler
    public void registerServiceEvent(ServiceRegisterEvent e) {
        RegisteredServiceProvider p = e.getProvider();
        String sn = p.getService().getName();
        log(this, "Register service - " + sn);
        switch (sn) {
            case "net.milkbowl.vault.chat.Chat":
                chat = (Chat) p.getProvider();
                break;
            case "net.milkbowl.vault.economy.Economy":
                econ = (Economy) p.getProvider();
                break;
            case "net.milkbowl.vault.permission.Permission":
                perm = (Permission) p.getProvider();
                break;
        }
    }

    public void resetConfig() {
        try {
            File oldConf = new File(dir + File.separator + "config.yml");
            File backupConf = new File(dir + File.separator + "config.yml.bak");
            if (backupConf.exists())
                backupConf.delete();
            oldConf.renameTo(backupConf);
            File oldLang = new File(dir + File.separator + "lang.yml");
            File backupLang = new File(dir + File.separator + "lang.yml.bak");
            if (backupLang.exists()) {
                backupLang.delete();
            }
            oldLang.renameTo(backupLang);
        } catch (Throwable e) {
            log(this, "§cFailed to reset the config :-( The plugin is shutting down...");
            error(cs, e, "SpigotLib", "gyurix");
            pm.disablePlugin(this);
            return;
        }
        try {
            load();
        } catch (Throwable e) {
            log(this, "§cFailed to load plugin after config reset :-( The plugin is shutting down...");
            error(cs, e, "SpigotLib", "gyurix");
            pm.disablePlugin(this);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerLogin(PlayerLoginEvent e) {
        Player plr = e.getPlayer();

        if (!forceReducedMode && ver.isAbove(v1_8))
            ScoreboardAPI.playerJoin(plr);
    }

    @EventHandler
    public void unregisterServiceEvent(ServiceUnregisterEvent e) {
        RegisteredServiceProvider p = e.getProvider();
        String sn = p.getService().getName();
        log(this, "Unregister service - " + sn);
        switch (sn) {
            case "net.milkbowl.vault.chat.Chat":
                chat = null;
                break;
            case "net.milkbowl.vault.economy.Economy":
                econ = null;
                break;
            case "net.milkbowl.vault.permission.Permission":
                perm = null;
                break;
        }
    }
}

