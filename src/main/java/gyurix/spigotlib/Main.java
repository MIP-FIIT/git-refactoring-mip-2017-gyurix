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
import gyurix.nbt.NBTApi;
import gyurix.protocol.*;
import gyurix.protocol.utils.ItemStackWrapper;
import gyurix.scoreboard.ScoreboardAPI;
import net.milkbowl.vault.economy.Economy;
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
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.logging.Logger;

/**
 * Main class for initializing the modules of the SpigotLib
 */
public class Main extends JavaPlugin implements Listener {
    /**
     * Version of the SpigotLib
     */
    public static final String version = "1.6";
    /**
     * Logger for SpigotLib debug
     */
    public static Logger log;
    /**
     * Instance of the language file of the SpigotLib
     */
    public static GlobalLangFile.PluginLang lang;
    /**
     * Instance of the ConfigFile object used for managing the SpigotLibs configuration
     */
    public static ConfigFile kf;
    /**
     * Instance of the SpigotLib plugin
     */
    public static Plugin pl;
    /**
     * SpigotLib directory (plugins/SpigotLib)
     */
    public static File dir;

    /**
     * Loads the configuration and initializes the modules of the SpigotLib
     */
    public void load(){
        SU.saveResources(this, "lang.yml", "config.yml");
        kf = new ConfigFile(new File(dir + File.separator + "config.yml"));
        kf.data.deserialize(Config.class);

        SU.pf = new ConfigFile(new File(dir + File.separator + "players.yml"));

        lang = GlobalLangFile.loadLF("spigotlib", dir + File.separator + "lang.yml");
        Reflection.init();
        ConfigSerialization.interfaceBasedClasses.put(ItemStack.class, Reflection.getOBCClass("inventory.CraftItemStack"));
        SU.init();

        AnimationAPI.init();
        PacketInType.init();
        PacketOutType.init();
        ChatAPI.init();
        TitleAPI.init();
        NBTApi.init();
        ScoreboardAPI.init();
        if (SU.pm.getPlugin("Vault")!=null)
            EconomyVaultHook.init();
    }

    /**
     * Resets the configuration and the language file of the SpigotLib. Used when the
     * plugin fails to load.
     */
    public void resetConfig(){
        try{
            File oldConf=new File(dir+File.separator+"config.yml");
            File backupConf=new File(dir+File.separator+"config.yml.bak");
            if (backupConf.exists()){
                backupConf.delete();
            }
            oldConf.renameTo(backupConf);
            File oldLang=new File(dir+File.separator+"lang.yml");
            File backupLang=new File(dir+File.separator+"lang.yml.bak");
            if (backupLang.exists()){
                backupLang.delete();
            }
            oldLang.renameTo(backupLang);
        }
        catch (Throwable e){
            e.printStackTrace();
            log.severe("§cFailed to reset the config :-( The plugin is shutting down...");
            SU.pm.disablePlugin(this);
            return;
        }
        try {
            load();
        }
        catch (Throwable e){
            e.printStackTrace();
            log.severe("§cFailed to load plugin after config reset :-( The plugin is shutting down...");
            SU.pm.disablePlugin(this);
        }
    }

    /**
     * Sets instance pointers in SU class, initializes the configuration system and invokes the load method
     */
    public void onLoad() {
        log = getLogger();
        pl = this;
        SU.srv = getServer();
        SU.pm = SU.srv.getPluginManager();
        SU.cs = SU.srv.getConsoleSender();

        dir = getDataFolder();
        try{
            DefaultSerializers.init();
            ConfigHook.registerSerializers();
            ConfigHook.registerVariables();
        }
        catch (Throwable e){
            e.printStackTrace();
            log.severe("§cFailed to load config hook :-( The plugin is shutting down...");
            SU.pm.disablePlugin(this);
            return;
        }

        try{
            load();
        }
        catch (Throwable e){
            e.printStackTrace();
            System.err.println("Failed to load plugin, trying to reset the config...");
            resetConfig();
        }
    }

    /**
     * Registers Bukkit events and initializes the Offline player management system
     */
    public void onEnable() {
        SU.pm.registerEvents(this, this);
        SU.pm.registerEvents(SU.tp, this);
        SU.initOfflinePlayerManager();
    }

    /**
     * Saves the configuration and closes the PacketAPI
     */
    public void onDisable() {
        SU.pf.save();
        kf.save();
        SU.tp.close();
    }

    /**
     * Handles PlayerJoinEvent for saving the players name+UUID to the ConnectionLog database,
     * and for handling the join message system.
     * @param e PlayerJoinEvent
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player plr = e.getPlayer();
        if (Config.ConnectionLog.enabled) {
            String ip = plr.getAddress().getAddress().toString().substring(1);
            HashSet<UUID> uuids = Config.ConnectionLog.ipUUIDBase.get(ip);
            if (uuids == null)
                Config.ConnectionLog.ipUUIDBase.put(ip, uuids = new HashSet<UUID>());
            uuids.add(plr.getUniqueId());
        }
        ScoreboardAPI.playerJoin(plr);
        UUID id=plr.getUniqueId();
        ConfigFile pf=SU.getPlayerConfig(plr);
        if (EconomyAPI.liveSync&&EconomyAPI.getBalance(id)==null){
            for (RegisteredServiceProvider<Economy> p:Bukkit.getServicesManager().getRegistrations(Economy.class)){
                if (p.getPlugin()!=this){
                    EconomyAPI.setBalance(plr.getUniqueId(),new BigDecimal(p.getProvider().getBalance(plr)));
                }
            }
        }
        for (EconomyAPI.BalanceData bd:EconomyAPI.balanceTypes.values()){
            if (EconomyAPI.getBalance(id, bd.name)==null){
                pf.setObject("balance."+bd.name,bd.defaultValue);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAfterPlayerJoin(PlayerJoinEvent e){
        if (!Config.joinMessage)
            return;
        Player plr=e.getPlayer();
        e.setJoinMessage(null);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission("spigotlib.joinmsg"))
                return;
            String msg = lang.get(p, "messages.join", "player", plr.getName());
            if (!msg.equals("<none>")) {
                ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, msg, p);
            }
        }
    }

    /**
     * Handles the PlayerLeave event for leave message handling and for saving the
     * player file.
     * @param e PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent e) {
        if (!Config.leaveMessage)
            return;
        Player plr = e.getPlayer();
        e.setQuitMessage(null);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plr != p) {
                if (!p.hasPermission("spigotlib.leavemsg"))
                    return;
                String msg = lang.get(p, "messages.leave", "player", e.getPlayer().getName());
                if (!msg.equals("<none>"))
                    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, msg, p);
            }
        }
        if (Config.AntiItemHack.enabled)
            SU.getPlayerConfig(plr).removeData("antiitemhack.lastitem");
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
        if (old.getTitle() != null)
            meta.setTitle(SU.setLength(old.getTitle().replace("§", "&"), 16));
        e.setNewBookMeta(meta);
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent e) {
        if (!Config.kickMessage)
            return;
        Player plr = e.getPlayer();
        e.setLeaveMessage(null);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plr != p) {
                if (!p.hasPermission("spigotlib.kickmsg"))
                    return;
                String msg = lang.get(p, "messages.kick", "player", e.getPlayer().getName(), "reason", e.getReason());
                if (!msg.equals("<none>"))
                    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, msg, p);
            }
        }
    }

    /**
     *
     * @param slot
     * @return
     */
    public int convertPacketSlot(int slot) {
        if (slot > 8 && slot < 36) {
            return slot;
        } else if (slot > 35 && slot < 45) {
            return slot - 36;
        } else if (slot > 44 || slot < 0) {
            return -1;
        } else if (slot < 5) {
            return -2;
        }
        return 31 + slot;
    }

    public String creativeSlotCheck(Player plr, int slot, ItemStack is) {
        int invid = convertPacketSlot(slot);
        if (is == null || is.getType() == Material.AIR) {
            try {
                SU.getPlayerConfig(plr).setObject("antiitemhack.lastitem", plr.getInventory().getItem(invid));
            } catch (NullPointerException e) {
            }
            return null;
        }
        switch (invid) {
            case -1:
                return null;
            case -2:
                return "itemhack.craftitem";
        }
        String id = String.valueOf(is.getTypeId());
        switch (slot) {
            case 5:
                return Config.AntiItemHack.helmets.contains(id) ? null : "itemhack.helmet";
            case 6:
                return Config.AntiItemHack.chestplates.contains(id) ? null : "itemhack.chestplate";
            case 7:
                return Config.AntiItemHack.leggings.contains(id) ? null : "itemhack.leggings";
            case 8:
                return Config.AntiItemHack.boots.contains(id) ? null : "itemhack.boots";
        }
        if (SU.itemEqual((ItemStack) SU.getPlayerConfig(plr).get("antiitemhack.lastitem", ItemStack.class), is)) {
            return null;
        }
        if (is.getAmount() > is.getType().getMaxStackSize()) {
            return "itemhack.stacksize";
        }
        return (SU.containsItem(Config.AntiItemHack.whitelist, is) || SU.containsItem(plr.getInventory(), is)) ?
                null : "itemhack.invaliditem";

    }

    @EventHandler
    public void packetInHandler(PacketInEvent e) {
        Player plr = e.getPlayer();
        String uid = plr == null ? "?" : e.getPlayer().getUniqueId().toString();
        String name = plr == null ? "?" : e.getPlayer().getName();
        if (Config.debug && e.getType() == null) {
            log.severe("[SpigotLib] -> Protocol -> Missing in packet type " + e.getPacketObject().getClass().getName());
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            HashSet<PacketOutType> detect = (HashSet) SU.getPlayerConfig(p).getData("packet.detect.in." + uid).objectData;
            if (detect != null && detect.contains(e.getType())) {
                p.sendMessage("§9§l----> PacketInDetector - " + name + " - " + e.getType().name());
                p.sendMessage("§b" + StringUtils.join(e.getPacketData(), "\n§b"));
            }
        }
        if (e.getType() == PacketInType.HandshakingInSetProtocol) {
            if (Config.ConnectionLog.enabled) {
                try {
                    String ip = ((InetSocketAddress) e.getChannel().remoteAddress()).getAddress().toString().substring(1);
                    if (Config.ConnectionLog.blacklist.contains(ip))
                        return;
                    boolean login = Protocol.handshakeNextState.get(e.getPacketObject()).toString().equals("LOGIN");
                    HashSet<UUID> uuids = Config.ConnectionLog.ipUUIDBase.get(ip);
                    if (uuids == null || uuids.isEmpty()) {
                        SU.cs.sendMessage((login ? Config.ConnectionLog.loginunknown : Config.ConnectionLog.pingunknown).replace("<ip>", ip));
                    } else {
                        ArrayList<String> list = new ArrayList<String>();
                        String n = "?";
                        for (UUID id : uuids) {
                            n = "?";
                            try {
                                n = Bukkit.getOfflinePlayer(id).getName();
                            } catch (Throwable err) {
                            }
                            list.add(n + "(" + id + ")");
                        }
                        if (list.size() == 1)
                            SU.cs.sendMessage((login ? Config.ConnectionLog.login : Config.ConnectionLog.ping)
                                    .replace("<ip>", ip).replace("<uuid>", "" + uuids.iterator().next()).replace("<name>", n));
                        else
                            SU.cs.sendMessage((login ? Config.ConnectionLog.loginmore : Config.ConnectionLog.pingmore).replace("<ip>", ip) +
                                    StringUtils.join(list, ", "));
                    }
                } catch (IllegalAccessException e1) {
                    errorLog(null, e1);
                    e1.printStackTrace();
                }
            }
        } else if (e.getType() == PacketInType.TabComplete) {
            if (!Config.tabCompletePerm)
                return;
            e.setCancelled(!e.getPlayer().hasPermission("tab.complete"));

        } else if (e.getType() == PacketInType.SetCreativeSlot) {
            if (!Config.AntiItemHack.enabled)
                return;
            try {
                int slotid = 0;
                ItemStack is = null;
                for (Object o : e.getPacketData()) {
                    if (o != null && o.getClass() == Integer.class) {
                        slotid = (Integer) o;
                    } else if (o != null) {
                        is = new ItemStackWrapper(o).toBukkitStack();
                    }
                }
                String result = creativeSlotCheck(plr, slotid, is);
                if (result != null) {
                    lang.msg(lang.get(plr, "itemhack"), plr, result);
                    e.setCancelled(true);
                    plr.updateInventory();
                }

            } catch (Throwable err) {
                err.printStackTrace();
            }
        } else if (e.getType() == PacketInType.UpdateSign) {
            if (!Config.AntiSignHack.enabled)
                return;
            Object[] darray = e.getPacketData();
            Object[] lines = (Object[]) darray[darray[0].getClass().isArray() ? 0 : 1];
            for (Object l : lines) {
                String json = ChatAPI.toJson(l);
                if (!json.startsWith("\"") || !json.endsWith("\"")) {
                    lang.msg(plr, "signhack.json");
                    e.setCancelled(true);
                    return;
                }
                json = StringEscapeUtils.unescapeJava(json.substring(1, json.length() - 1));
                if (json.length() > Config.AntiSignHack.limit) {
                    lang.msg(plr, "signhack.limit");
                    e.setCancelled(true);
                    return;
                }
                for (char c : json.toCharArray()) {
                    if (c < 32 || c == '§') {
                        lang.msg(plr, "signhack.characters");
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        } else if (e.getType() == PacketInType.Chat) {
            if (!Config.Chat.enabled)
                return;
            e.setCancelled(true);
            ConfigFile pf = SU.getPlayerConfig(plr);
            String msg = (String) e.getPacketData()[0];
            boolean puffered = msg.length() >= Config.Chat.Long.pufferAfter;
            if (!Config.Chat.newLineCharacter.equals("") && plr.hasPermission("chat.multiline")) {
                msg = msg.replace(Config.Chat.newLineCharacter, "\n");
                if (Config.Chat.allowEmptyLines && plr.hasPermission("chat.emptylines")) {
                    msg = msg.replace("\n\n", "\n \n");
                    msg = msg.replace("\n\n", "\n \n");
                }
                for (char c : Config.Chat.colors.toCharArray()) {
                    if (plr.hasPermission("chat.color." + c))
                        msg = msg.replace(Config.Chat.colorPrefix + c, "§" + c);
                }
            }
            if (Config.Chat.Long.enabled && plr.hasPermission("chat.longer")) {
                int limit = 100;
                for (Object group : Config.Chat.Long.lengthLimit.entrySet()) {
                    if (plr.hasPermission("chat.longerlimit." + ((Map.Entry) group).getKey()) && (Integer) ((Map.Entry) group).getValue() > limit)
                        limit = (Integer) ((Map.Entry) group).getValue();
                }
                ConfigData puffer = pf.getData("chat.puffer");
                if (puffered && puffer.stringData.length() + msg.length() < limit) {
                    puffer.stringData += msg;
                } else {
                    msg = puffer.stringData + msg;
                    msg = msg.length() > limit ? msg.substring(0, limit) : msg;
                    int id = msg.indexOf(" ");
                    if (id == -1)
                        id = msg.length();
                    if (msg.startsWith("/")) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new AsyncCommandExecutionAvoid(msg, plr));
                    } else
                        plr.chat(msg);
                    puffer.stringData = "";
                }
            } else {
                int id = msg.indexOf(" ");
                if (id == -1)
                    id = msg.length();
                if (msg.startsWith("/")) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this, new AsyncCommandExecutionAvoid(msg, plr));
                } else
                    plr.chat(msg);
            }
        }
    }

    @EventHandler
    public void packetOutDetect(PacketOutEvent e) {
        Player plr = e.getPlayer();
        String uid = plr == null ? "?" : e.getPlayer().getUniqueId().toString();
        String name = plr == null ? "?" : e.getPlayer().getName();
        if (Config.debug && e.getType() == null) {
            log.severe("[SpigotLib] -> Protocol -> Missing out packet type " + e.getPacketObject().getClass().getName());
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            HashSet<PacketOutType> detect = (HashSet) SU.getPlayerConfig(p).getData("packet.detect.in." + uid).objectData;
            if (detect != null && detect.contains(e.getType())) {
                p.sendMessage("§9§l----> PacketOutDetector - " + name + " - " + e.getType().name());
                p.sendMessage("§b" + StringUtils.join(e.getPacketData(), "\n§b"));
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!sender.hasPermission("spigotlib.use")) {
                lang.msg(sender, "noperm");
                return true;
            }
            Player plr = sender instanceof Player ? (Player) sender : null;
            if (args.length == 0) {
                String msg = "§b§l§n---> SpigotLib - by:§6§l GyuriX§b§l §n - v:§6§l " + version + "§b§l §n <---\n" + lang.get(plr, "help");
                if (plr == null) {
                    sender.sendMessage(msg);
                } else
                    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, msg, plr);
                return true;
            }
            if (!sender.hasPermission("spigotlib.command." + args[0])) {
                lang.msg(sender, "noperm.command");
                return true;
            }
            Collection<Player> pls = null;
            String msg = null;
            if (args.length >= 2) {
                if (args[1].equals("*")) {
                    pls = (Collection<Player>) Bukkit.getOnlinePlayers();
                } else {
                    pls = new ArrayList();
                    for (String pn : args[1].split(",")) {
                        Player p = Bukkit.getPlayer(pn);
                        if (p != null)
                            pls.add(p);
                    }
                }
                msg = StringUtils.join(args, " ", 2, args.length);
            }
            if (args[0].equals("chm")) {
                for (Player p : pls) {
                    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.CHAT, VariableAPI.fillVariables(msg, plr, p), p);
                }
            } else if (args[0].equals("abm")) {
                for (Player p : pls) {
                    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, VariableAPI.fillVariables(msg, plr, p), p);
                }
            } else if (args[0].equals("sym")) {
                for (Player p : pls) {
                    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, VariableAPI.fillVariables(msg, plr, p), p);
                }
            } else if (args[0].equals("title")) {
                String[] titles = msg.split("\n");
                String title = titles[0];
                String sub = titles.length >= 2 ? titles[1] : "";
                TitleAPI.setTitle(title, pls);
                TitleAPI.setSubTitle(sub, pls);
            } else if (args[0].equals("titledata")) {
                String[] data = msg.split(" ");
                TitleAPI.setShowTime(Integer.valueOf(data[0]).intValue(), Integer.valueOf(data[1]).intValue(), Integer.valueOf(data[2]).intValue(), pls);
            } else if (args[0].equals("titlehide")) {
                TitleAPI.reset(pls);
            } else if (args[0].equals("vars")) {
                if (args.length == 1) {
                    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, StringUtils.join(new TreeSet(VariableAPI.handlers.keySet()), ", "), plr);
                } else
                    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, VariableAPI.fillVariables(StringUtils.join(args, ' ', 1, args.length), plr, plr), plr);
            } else if (args[0].equals("hasperm")) {
                String yes = lang.get(plr, "permission.yes");
                String no = lang.get(plr, "permission.no");
                for (Player p : pls) {
                    sender.sendMessage((p.hasPermission(msg) ? yes : no).replace("<player>", p.getName()));
                }
            } else if (args[0].equals("packets")) {
                lang.msg(sender, "packet", "in", StringUtils.join(PacketInType.values(), ", "), "out",
                        StringUtils.join(PacketOutType.values(), ", "));
                return true;
            } else if (args[0].equals("errors")) {
                sender.sendMessage(StringUtils.join(Config.errors, "\n \n"));
            } else if (args[0].equals("lang")) {
                if (args.length == 1) {
                    lang.msg(sender, "lang.list", "langs", StringUtils.join(GlobalLangFile.map.keySet(), ", "));
                } else if (args.length == 2) {
                    if (GlobalLangFile.map.containsKey(args[1])) {
                        if (plr == null) {
                            lang.msg(sender, "noconsole");
                            return true;
                        }
                        SU.getPlayerConfig(plr).setString("lang", args[2]);
                        lang.msg(sender, "lang.set.own", "lang", args[1]);
                    } else {
                        for (Player p : pls) {
                            String l = SU.getPlayerConfig(plr).getString("lang");
                            if (l.isEmpty())
                                l = Config.defaultLang;
                            lang.msg(sender, "lang.lang", "player", p.getName(), "lang", l);
                        }
                    }
                } else {
                    for (Player p : pls) {
                        SU.getPlayerConfig(p).setString("lang", args[2]);
                        lang.msg(sender, "lang.set", "player", p.getName(), "lang", args[2]);
                    }
                    SU.pf.save();
                }
                return true;
            } else if (args[0].equals("lf")) {
                StringBuilder out = new StringBuilder();
                if (args.length == 1) {
                    sender.sendMessage("§9§l---> Language file viewer <---");
                    for (Map.Entry e : GlobalLangFile.map.entrySet()) {
                        if (!((String) e.getKey()).contains(".")) {
                            out.append("\n\\|§b").append((String) e.getKey()).append("\\-R/spigotlib lf ").append((String) e.getKey()).append("\\|");
                        }
                    }
                } else {
                    sender.sendMessage("§9§l---> Language file viewer - " + args[1] + " <---");
                    int len = args[1].length();
                    for (Map.Entry e : GlobalLangFile.map.entrySet()) {
                        if (((String) e.getKey()).startsWith(args[1])) {
                            String sub = ((String) e.getKey()).substring(len);
                            if (!sub.contains(".")) {
                                out.append("\n\\|§b").append(sub).append(": \\-R/spigotlib lf ").append((String) e.getKey()).append("\\|§f").append((String) e.getValue());
                            }
                        }
                    }
                }
                ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, out.length() == 0 ? " " : out.substring(3), plr);
                return true;
            } else if (args[0].equals("pf")) {
                if (args.length==1){
                    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, SU.pf.toString(), plr);
                }
                else{
                    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, SU.getPlayerConfig(SU.getUUID(args[1])).toString(), plr);
                }
                return true;
            } else if (args[0].equals("reload")) {
                if (args.length==0)
                    lang.msg(sender, "invalidcmd");
                if (args[1].equals("config")) {
                    kf.reload();
                    kf.data.deserialize(Config.class);
                    lang.msg(sender, "reload.config");
                } else if (args[1].equals("lf")) {
                    GlobalLangFile.unloadLF(lang);
                    SU.saveResources(this, "lang.yml");
                    lang = GlobalLangFile.loadLF("spigotlib", getDataFolder() + File.separator + "lang.yml");
                    lang.msg(sender, "reload.lf");
                } else if (args[1].equals("pf")) {
                    SU.pf.reload();
                    lang.msg(sender, "reload.pf");
                } else {
                    lang.msg(sender, "invalidcmd");
                }
                return true;
            } else if (args[0].equals("save")) {
                if (args[1].equals("pf")) {
                    SU.pf.save();
                    lang.msg(sender, "save.pf");
                } else {
                    lang.msg(sender, "invalidcmd");
                }
                return true;
            } else if (args[0].equals("velocity")) {
                if (args.length == 4) {
                    if (plr == null) {
                        lang.msg(sender, "noconsole");
                        return true;
                    }
                    plr.setVelocity(new Vector(Double.valueOf(args[1]).doubleValue(), Double.valueOf(args[2]).doubleValue(), Double.valueOf(args[3]).doubleValue()));
                } else {
                    Vector v;
                    if (args.length >= 5) {
                        v = new Vector(Double.valueOf(args[2]).doubleValue(), Double.valueOf(args[3]).doubleValue(), Double.valueOf(args[4]).doubleValue());
                        for (Player p : pls) {
                            p.setVelocity(v);
                        }
                    } else {
                        lang.msg(sender, "invalidcmd");
                        return true;
                    }
                }
            } else if (args[0].equals("setamount")) {
                if (args.length == 2) {
                    if (plr == null) {
                        lang.msg(sender, "noconsole");
                        return true;
                    }
                    ItemStack is = plr.getItemInHand();
                    is.setAmount(Integer.valueOf(args[1]));
                    plr.setItemInHand(is);
                } else {
                    int amount;
                    if (args.length >= 3) {
                        amount = Integer.valueOf(args[2]);
                        for (Player p : pls) {
                            ItemStack is = p.getItemInHand();
                            is.setAmount(amount);
                            p.setItemInHand(is);
                        }
                    } else {
                        lang.msg(sender, "invalidcmd");
                        return true;
                    }
                }
            } else if (args[0].equals("item")) {
                if (args.length == 1) {
                    if (plr == null) {
                        lang.msg(sender, "noconsole");
                        return true;
                    }
                    lang.msg(sender, "item.own", "item", SU.itemToString(plr.getItemInHand()));
                } else if (args.length == 2) {
                    for (Player p : pls) {
                        lang.msg(sender, "item.player", "name", p.getName(), "item", SU.itemToString(p.getItemInHand()));
                    }
                } else {
                    String s = StringUtils.join(args, ' ', 2, args.length);
                    ItemStack is = SU.stringToItemStack(s);
                    for (Player p : pls) {
                        p.setItemInHand(is);
                        lang.msg(sender, "item.player.set", "name", p.getName(), "item", s);
                    }
                }

                return true;
            } else {
                lang.msg(sender, "invalidcmd");
                return true;
            }
            sender.sendMessage("§b§lSpigotLib - Command executed successfully");
            return true;
        } catch (Throwable e) {
            errorLog(sender, e);
        }
        return true;
    }

    public static class AsyncCommandExecutionAvoid implements Runnable {
        private final String msg;
        private final Player plr;

        public AsyncCommandExecutionAvoid(String msg, Player plr) {
            this.msg = msg;
            this.plr = plr;
        }

        public void run() {
            plr.chat(msg);
        }
    }

    public static void errorLog(CommandSender sender, Throwable e) {
        if (sender != null)
            sender.sendMessage("§c§lSpigotLib ► ERROR REPORTER ► " + e.getClass().getName() + " - " + e.getMessage() + ":");
        int i = 1;
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getName()).append(" - ").append(e.getMessage());
        for (StackTraceElement s : e.getStackTrace()) {
            String loc = s.toString();
            if (loc.contains("gyurix")) {
                if (sender != null)
                    sender.sendMessage("§c " + i++ + ": §e" + loc);
                sb.append('\n').append(loc);
            }
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
}