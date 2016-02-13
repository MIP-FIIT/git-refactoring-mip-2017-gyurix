package gyurix.api;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import gyurix.animation.AnimationAPI;
import gyurix.commands.Command;
import gyurix.configfile.ConfigSerialization;
import gyurix.json.JsonAPI;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by gyurix on 20/12/2015.
 */
public class BungeeAPI implements PluginMessageListener {
    private static HashMap<UUID, String> ips = new HashMap<>();
    private static HashMap<UUID, Integer> ports = new HashMap<>();
    private static HashMap<String, String> serverIps = new HashMap<>();
    private static HashMap<String, Short> serverPorts = new HashMap<>();
    private static HashMap<String, Integer> playerCounts = new HashMap<>();
    private static HashMap<String, String[]> players = new HashMap<>();
    private static HashMap<String, UUID> uuids = new HashMap<>();
    private static String[] servers = new String[0];
    private static String serverName = "N/A";

    public static void startAPI() {
        if (!BungeeSettings.enabled)
            return;
        BungeeSettings.serverNamesFuture = AnimationAPI.sch.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
                if (!pc.isEmpty()) {
                    Player p = pc.iterator().next();
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("GetServers");
                    p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
                }
            }
        }, 0L, BungeeSettings.serverNames, TimeUnit.MILLISECONDS);
        BungeeSettings.currentServerNameFuture = AnimationAPI.sch.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
                if (!pc.isEmpty()) {
                    Player p = pc.iterator().next();
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("GetServer");
                    p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
                }
            }
        }, 0L, BungeeSettings.currentServerName, TimeUnit.MILLISECONDS);
        BungeeSettings.serverIpsFuture = AnimationAPI.sch.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
                if (!pc.isEmpty()) {
                    Player p = pc.iterator().next();
                    for (String s : servers) {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("ServerIP");
                        out.writeUTF(s);
                        p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
                    }
                }
            }
        }, 150L, BungeeSettings.serverIps, TimeUnit.MILLISECONDS);
        BungeeSettings.playerCountsFuture = AnimationAPI.sch.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
                if (!pc.isEmpty()) {
                    Player p = pc.iterator().next();
                    for (String s : servers) {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("PlayerCount");
                        out.writeUTF(s);
                        p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
                    }
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("PlayerCount");
                    out.writeUTF("ALL");
                    p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
                }
            }
        }, 150L, BungeeSettings.playerCounts, TimeUnit.MILLISECONDS);
        BungeeSettings.playersFuture = AnimationAPI.sch.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
                if (!pc.isEmpty()) {
                    Player p = pc.iterator().next();
                    for (String s : servers) {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("PlayerList");
                        out.writeUTF(s);
                        p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
                    }
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("PlayerList");
                    out.writeUTF("ALL");
                    p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
                }
            }
        }, 150L, BungeeSettings.players, TimeUnit.MILLISECONDS);
        BungeeSettings.uuidsFuture = AnimationAPI.sch.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
                if (!pc.isEmpty()) {
                    Player p = pc.iterator().next();
                    for (String s : totalPlayerList()) {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("UUIDOther");
                        out.writeUTF(s);
                        p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
                    }
                }
            }
        }, 150L, BungeeSettings.uuids, TimeUnit.MILLISECONDS);
    }

    public static void stopAPI() {
        if (!BungeeSettings.enabled)
            return;
        BungeeSettings.serverIpsFuture.cancel(false);
        BungeeSettings.serverNamesFuture.cancel(false);
        BungeeSettings.currentServerNameFuture.cancel(false);
        BungeeSettings.playersFuture.cancel(false);
        BungeeSettings.playerCountsFuture.cancel(false);
        BungeeSettings.uuidsFuture.cancel(false);
    }

    public static void send(String server, Player... players) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        for (Player p : players) {
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
    }

    public static void send(String server, Collection<Player> players) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        for (Player p : players) {
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
    }

    public static boolean send(String server, String... players) {
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("ConnectOther");
            out.writeUTF(s);
            out.writeUTF(server);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean send(String server, Iterable<String> players) {
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("ConnectOther");
            out.writeUTF(s);
            out.writeUTF(server);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static String getIp(Player plr) {
        return ips.get(plr.getUniqueId());
    }

    public static Integer getPort(Player plr) {
        return ports.get(plr.getUniqueId());
    }

    public static Integer playerCount(String server) {
        return playerCounts.get(server);
    }

    public static Integer totalPlayerCount() {
        return playerCounts.get("ALL");
    }

    public static String[] playerList(String server) {
        return players.get(server);
    }

    public static String[] totalPlayerList() {
        return players.get("ALL");
    }

    public static String[] serverNames() {
        return servers;
    }

    public static boolean sendMessage(String msg, String... players) {
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Message");
            out.writeUTF(s);
            out.writeUTF(msg);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean sendMessage(String msg, Iterable<String> players) {
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Message");
            out.writeUTF(s);
            out.writeUTF(msg);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static String getServerName() {
        return serverName;
    }

    public static boolean forwardToServer(String channel, byte[] message, String... servers) {
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : servers) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Forward");
            out.writeUTF(s);
            out.writeUTF(channel);
            out.writeShort(message.length);
            out.write(message);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean forwardToAllServer(String channel, byte[] message) {
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF(channel);
        out.writeShort(message.length);
        out.write(message);
        p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        return true;
    }

    public static boolean forwardToPlayer(String channel, byte[] message, String... players) {
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("ForwardToPlayer");
            out.writeUTF(s);
            out.writeUTF(channel);
            out.writeShort(message.length);
            out.write(message);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean forwardToPlayer(String channel, byte[] message, Iterable<String> players) {
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("ForwardToPlayer");
            out.writeUTF(s);
            out.writeUTF(channel);
            out.writeShort(message.length);
            out.write(message);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static UUID getUUID(Player plr) {
        return uuids.get(plr.getName());
    }

    public static UUID getUUID(String pln) {
        return uuids.get(pln);
    }

    public static String getServerIp(String server) {
        return serverIps.get(server);
    }

    public static Short getServerPort(String server) {
        return serverPorts.get(server);
    }

    public static boolean kick(String message, String... players) {
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("KickPlayer");
            out.writeUTF(s);
            out.writeUTF(message);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean kick(String message, Iterable<String> players) {
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("KickPlayer");
            out.writeUTF(s);
            out.writeUTF(message);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean executePlayerCommands(Command[] commands, String... players) {
        String json = JsonAPI.serialize(commands);
        return forwardToPlayer("CommandExecution", json.getBytes(), players);
    }

    public static boolean executeServerCommands(Command[] commands, String... servers) {
        String json = JsonAPI.serialize(commands);
        return forwardToServer("CommandExecution", json.getBytes(), servers);
    }

    public static boolean executeBungeeCommands(String[] commands, String... players) {
        String json = JsonAPI.serialize(commands);
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("BungeeCommand");
            out.writeUTF(s);
            out.writeUTF(json);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    @Override
    public void onPluginMessageReceived(String channel, final Player player, byte[] bytes) {
        if (!channel.equals("BungeeCord"))
            return;
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        String sub = in.readUTF();
        UUID uid = player.getUniqueId();
        if (Config.debug)
            System.out.println("Received plugin message from player " + player.getName() + ": " + sub + " " + new String(bytes));
        switch (sub) {
            case "CommandExecution": {
                final Command[] commands = (Command[]) JsonAPI.deserialize(in.readUTF(), Command[].class);
                SU.sch.scheduleSyncDelayedTask(Main.pl, new Runnable() {
                    @Override
                    public void run() {
                        for (Command c : commands) {
                            c.execute(player);
                        }
                    }
                });
                return;
            }
            case "IP": {
                ips.put(uid, in.readUTF());
                ports.put(uid, in.readInt());
                return;
            }
            case "PlayerCount": {
                playerCounts.put(in.readUTF(), in.readInt());
                return;
            }
            case "PlayerList": {
                players.put(in.readUTF(), in.readUTF().split(", "));
                return;
            }
            case "GetServers": {
                servers = in.readUTF().split(", ");
                return;
            }
            case "GetServer": {
                serverName = in.readUTF();
                return;
            }
            case "UUID": {
                uuids.put(player.getName(), UUID.fromString(in.readUTF().replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")));
                return;
            }
            case "UUIDOther": {
                uuids.put(in.readUTF(), UUID.fromString(in.readUTF().replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")));
                return;
            }
            case "ServerIP": {
                String server = in.readUTF();
                serverIps.put(server, in.readUTF());
                serverPorts.put(server, in.readShort());
                return;
            }
        }
    }

    public static class BungeeSettings {
        public static boolean enabled = true;
        public static long playerIps, serverIps, playerCounts, players, uuids, serverNames, currentServerName;
        public static boolean uuidOnJoin, ipOnJoin;
        @ConfigSerialization.ConfigOptions(serialize = false)
        public static ScheduledFuture serverNamesFuture, currentServerNameFuture,
                serverIpsFuture, playerCountsFuture, playersFuture, uuidsFuture;
    }
}
