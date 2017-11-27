package gyurix.api;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import gyurix.commands.Command;
import gyurix.json.JsonAPI;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import static gyurix.json.JsonAPI.serialize;
import static gyurix.spigotlib.Config.BungeeAPI.*;
import static gyurix.spigotlib.Config.debug;
import static gyurix.spigotlib.SU.utf8;
import static org.apache.commons.lang.StringUtils.join;

/**
 * BungeeAPI is the implementation of the
 * <a href=https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/>Spigot - Bungee communication protocol</a>
 */
public class BungeeAPI implements PluginMessageListener {
    public static boolean enabled;
    private static PlayerInfo emptyPlayer = new PlayerInfo();
    private static ServerInfo emptyServer = new ServerInfo();
    private static HashMap<String, PlayerInfo> players = new HashMap<>();
    private static String serverName = "N/A";
    private static HashMap<String, ServerInfo> servers;

    /**
     * Checks if the BungeeAPI is enabled in the config, if it's not, then throws an exception
     *
     * @throws RuntimeException - If the BungeeAPI is not enabled
     */
    private static void checkEnabled() {
        if (!enabled)
            throw new RuntimeException("BungeeAPI is not enabled, please enable it in SpigotLib config");
    }

    public static boolean executeBungeeCommands(String[] commands, String... players) {
        return sendMessageToBungee("BungeeCommands", join(players, ","), serialize(commands));
    }

    public static boolean executePlayerCommands(Command[] commands, String... players) {
        return forwardToPlayer("CommandExecution", serialize(commands).getBytes(utf8), players);
    }

    public static boolean executeServerCommands(String[] commands, String... servers) {
        return forwardToServer("CommandExecution", serialize(commands).getBytes(utf8), servers);
    }

    public static boolean executeServerCommands(Command[] commands, String... servers) {
        return forwardToServer("CommandExecution", serialize(commands).getBytes(utf8), servers);
    }

    public static boolean forwardToAllServer(String channel, byte[] message) {
        Player p = getAnyPlayer();
        if (p == null || message == null)
            return false;
        p.sendPluginMessage(Main.pl, "BungeeCord", makeForwardingData("ALL", channel, true, message));
        return true;
    }

    public static boolean forwardToPlayer(String channel, byte[] message, String... players) {
        Player p = getAnyPlayer();
        if (p == null || players.length == 0 || message == null)
            return false;
        for (String s : players)
            p.sendPluginMessage(Main.pl, "BungeeCord", makeForwardingData(s, channel, false, message));
        return true;
    }

    public static boolean forwardToPlayer(String channel, byte[] message, Iterable<String> players) {
        Player p = getAnyPlayer();
        if (p == null || !players.iterator().hasNext() || message == null)
            return false;
        players.forEach((s) -> p.sendPluginMessage(Main.pl, "BungeeCord", makeForwardingData(s, channel, false, message)));
        return true;
    }

    public static boolean forwardToServer(String channel, byte[] message, String... servers) {
        Player p = getAnyPlayer();
        if (p == null || servers.length == 0 || message == null)
            return false;
        for (String s : servers)
            p.sendPluginMessage(Main.pl, "BungeeCord", makeForwardingData(s, channel, false, message));
        return true;
    }

    /**
     * Returns a Player whose plugin messaging channel can be used for Spigot - Bungee communication.
     *
     * @return The first Player found in the online player list or null if there are no online players
     */
    private static Player getAnyPlayer() {
        return Bukkit.getOnlinePlayers().stream().findAny().orElse(null);
    }

    public static String getIp(Player plr) {
        checkEnabled();
        return players.getOrDefault(plr.getUniqueId(), emptyPlayer).getIp();
    }

    public static Integer getPort(Player plr) {
        checkEnabled();
        return players.getOrDefault(plr.getUniqueId(), emptyPlayer).getPort();
    }

    public static String getServerIp(String server) {
        checkEnabled();
        return servers.getOrDefault(server, emptyServer).ip;
    }

    public static String getServerName() {
        checkEnabled();
        return serverName;
    }

    public static Short getServerPort(String server) {
        checkEnabled();
        return servers.getOrDefault(server, emptyServer).getPort();
    }

    public static UUID getUUID(Player plr) {
        checkEnabled();
        return players.getOrDefault(plr.getName(), emptyPlayer).getUuid();
    }

    public static UUID getUUID(String pln) {
        checkEnabled();
        return players.getOrDefault(pln, emptyPlayer).getUuid();
    }

    public static boolean kick(String message, String... players) {
        if (message == null || players.length == 0 || getAnyPlayer() == null)
            return false;
        for (String s : players)
            sendMessageToBungee("KickPlayer", s, message);
        return true;
    }

    public static boolean kick(String message, Iterable<String> players) {
        if (message == null || !players.iterator().hasNext() || getAnyPlayer() == null)
            return false;
        players.forEach((s) -> sendMessageToBungee("KickPlayer", s, message));
        return true;
    }

    /**
     * Creates a ByteArrayDataOutput from the given data and converts it to byte array
     *
     * @param data - The data which should be written to the ByteArrayDataOutput
     * @return The created ByteArrayDataOutput converted to byte array
     */
    private static byte[] makeDataOut(String... data) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        for (String element : data)
            out.writeUTF(element);
        return out.toByteArray();
    }

    /**
     * Creates the data for forwarding a message
     *
     * @param channel    - Receivers channel
     * @param serverMode - true if the receiver should be a server, false if it should be a player
     * @param receiver   - Receivers name
     * @param message    - The message which should be forwarded
     * @return The data required for forwarding
     */
    private static byte[] makeForwardingData(String receiver, String channel, boolean serverMode, byte[] message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(serverMode ? "Forward" : "ForwardToPlayer");
        out.writeUTF(receiver);
        out.writeUTF(channel);
        out.writeShort(message.length);
        out.write(message);
        return out.toByteArray();
    }

    public static Integer playerCount(String server) {
        checkEnabled();
        return servers.getOrDefault(server, emptyServer).getPlayerCount();
    }

    public static String[] playerList(String server) {
        checkEnabled();
        return servers.getOrDefault(server, emptyServer).getPlayers();
    }

    public static boolean requestCurrentServerName() {
        return sendMessageToBungee("GetServer");
    }

    public static void requestIP(Player... players) {
        byte[] msg = makeDataOut("IP");
        for (Player p : players)
            p.sendPluginMessage(Main.pl, "BungeeCord", msg);
    }

    public static void requestIP(Iterable<Player> players) {
        byte[] msg = makeDataOut("IP");
        players.forEach((p) -> p.sendPluginMessage(Main.pl, "BungeeCord", msg));
    }

    public static boolean requestPlayerCount(Iterable<String> servers) {
        if (!servers.iterator().hasNext() || getAnyPlayer() == null)
            return false;
        servers.forEach((s) -> sendMessageToBungee("PlayerCount", s));
        return true;
    }

    public static boolean requestPlayerCount(String... servers) {
        if (servers.length == 0 || getAnyPlayer() == null)
            return false;
        for (String s : servers)
            sendMessageToBungee("PlayerCount", s);
        return true;
    }

    public static boolean requestPlayerList(Iterable<String> servers) {
        if (!servers.iterator().hasNext() || getAnyPlayer() == null)
            return false;
        servers.forEach((s) -> sendMessageToBungee("PlayerList", s));
        return true;
    }

    public static boolean requestPlayerList(String... servers) {
        if (servers.length == 0 || getAnyPlayer() == null)
            return false;
        for (String s : servers)
            sendMessageToBungee("PlayerList", s);
        return true;
    }

    public static boolean requestServerIP(Iterable<String> servers) {
        if (!servers.iterator().hasNext() || getAnyPlayer() == null)
            return false;
        servers.forEach((s) -> sendMessageToBungee("ServerIP", s));
        return true;
    }

    public static boolean requestServerIP(String... servers) {
        if (servers.length == 0 || getAnyPlayer() == null)
            return false;
        for (String s : servers)
            sendMessageToBungee("ServerIP", s);
        return true;
    }

    public static boolean requestServerNames() {
        return sendMessageToBungee("GetServers");
    }

    public static boolean requestUUID(Iterable<String> players) {
        if (!players.iterator().hasNext() || getAnyPlayer() == null)
            return false;
        players.forEach((s) -> sendMessageToBungee("UUIDOther", s));
        return true;
    }

    public static boolean requestUUID(String... players) {
        if (players.length == 0 || getAnyPlayer() == null)
            return false;
        for (String s : players)
            sendMessageToBungee("UUIDOther", s);
        return true;
    }

    public static void send(String server, Player... players) {
        byte[] msg = makeDataOut("Connect", server);
        for (Player p : players)
            p.sendPluginMessage(Main.pl, "BungeeCord", msg);
    }

    public static void send(String server, Collection<Player> players) {
        byte[] msg = makeDataOut("Connect", server);
        players.forEach((p) -> p.sendPluginMessage(Main.pl, "BungeeCord", msg));
    }

    public static boolean send(String server, String... players) {
        if (server == null || players.length == 0 || getAnyPlayer() == null)
            return false;
        for (String s : players)
            sendMessageToBungee("ConnectOther", s, server);
        return true;
    }

    public static boolean send(String server, Iterable<String> players) {
        if (server == null || !players.iterator().hasNext() || getAnyPlayer() == null)
            return false;
        players.forEach((s) -> sendMessageToBungee("ConnectOther", s, server));
        return true;
    }

    public static boolean sendMessage(String msg, String... players) {
        if (msg == null || players.length == 0 || getAnyPlayer() == null)
            return false;
        for (String s : players)
            sendMessageToBungee("Message", s, msg);
        return true;
    }

    public static boolean sendMessage(String msg, Iterable<String> players) {
        if (msg == null || !players.iterator().hasNext() || getAnyPlayer() == null)
            return false;
        players.forEach((s) -> sendMessageToBungee("Message", s, msg));
        return true;
    }

    /**
     * Sends the given message to the Bungee using the messaging channel of the first found player on the server.
     *
     * @param msg - The sendable message
     * @return true - If the message was sent, false otherwise
     */
    public static boolean sendMessageToBungee(String... msg) {
        Player p = getAnyPlayer();
        if (p == null)
            return false;
        p.sendPluginMessage(Main.pl, "BungeeCord", makeDataOut(msg));
        return true;
    }

    public static String[] serverNames() {
        checkEnabled();
        return servers.keySet().toArray(new String[servers.keySet().size()]);
    }

    public static boolean start() {
        checkEnabled();
        if (Config.BungeeAPI.servers > 0)
            RunnableIDS.serversRID = SU.sch.scheduleSyncRepeatingTask(Main.pl,
                    BungeeAPI::requestServerNames, 0, Config.BungeeAPI.servers);

        if (currentServerName > 0)
            RunnableIDS.currentServerRID = SU.sch.scheduleSyncRepeatingTask(Main.pl,
                    BungeeAPI::requestCurrentServerName, 0, currentServerName);

        if (playerCount > 0)
            RunnableIDS.playerCountRID = SU.sch.scheduleSyncRepeatingTask(Main.pl, () -> {
                requestPlayerCount(servers.keySet());
                requestPlayerCount("ALL");
            }, 2, playerCount);

        if (playerList > 0)
            RunnableIDS.playerListRID = SU.sch.scheduleSyncRepeatingTask(Main.pl, () -> {
                requestPlayerList(servers.keySet());
                requestPlayerList("ALL");
            }, 2, playerList);

        if (uuidAll > 0)
            RunnableIDS.uuidAllRID = SU.sch.scheduleSyncRepeatingTask(Main.pl, () ->
                    requestUUID(totalPlayerList()), 4, uuidAll);

        if (serverIP > 0)
            RunnableIDS.serverIPRID = SU.sch.scheduleSyncRepeatingTask(Main.pl, () ->
                    requestServerIP(servers.keySet()), 4, serverIP);

        if (ipOnJoin)
            SU.sch.scheduleSyncDelayedTask(Main.pl, () ->
                    requestIP((Collection<Player>) Bukkit.getOnlinePlayers()), 4);

        return true;
    }

    public static Integer totalPlayerCount() {
        return servers.getOrDefault("ALL", emptyServer).getPlayerCount();
    }

    public static String[] totalPlayerList() {
        return servers.getOrDefault("ALL", emptyServer).players;
    }

    @Override
    public void onPluginMessageReceived(String channel, final Player player, byte[] bytes) {
        try {
            if (!channel.equals("BungeeCord"))
                return;
            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
            String sub = in.readUTF();
            String pln = player.getName();
            debug.msg("Bungee", "Received plugin message from player " + player.getName() + ": " + sub + " " + new String(bytes));
            switch (sub) {
                case "CommandExecution":
                    final Command[] commands = JsonAPI.deserialize(in.readUTF(), Command[].class);
                    SU.sch.scheduleSyncDelayedTask(Main.pl, () -> {
                        for (Command c : commands) {
                            c.execute(player);
                        }
                    });
                    return;
                case "IP": {
                    PlayerInfo pi = players.computeIfAbsent(pln, PlayerInfo::new);
                    pi.ip = in.readUTF();
                    pi.port = in.readInt();
                    return;
                }
                case "PlayerCount":
                    servers.computeIfAbsent(in.readUTF(), ServerInfo::new).playerCount = in.readInt();
                    return;
                case "PlayerList":
                    servers.get(in.readUTF()).players = in.readUTF().split(", ");
                    return;
                case "GetServers":
                    String[] d = in.readUTF().split(", ");
                    for (String s : d)
                        servers.putIfAbsent(s, new ServerInfo(s));
                    return;
                case "GetServer":
                    serverName = in.readUTF();
                    return;
                case "UUID": {
                    PlayerInfo pi = players.computeIfAbsent(pln, PlayerInfo::new);
                    pi.uuid = UUID.fromString(in.readUTF().replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
                    return;
                }
                case "UUIDOther":
                    PlayerInfo pi = players.computeIfAbsent(pln, PlayerInfo::new);
                    pi.uuid = UUID.fromString(in.readUTF().replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
                    return;
                case "ServerIP":
                    ServerInfo si = servers.computeIfAbsent(in.readUTF(), ServerInfo::new);
                    si.ip = in.readUTF();
                    si.port = in.readShort();
            }
        } catch (Throwable ignored) {
        }
    }

    @Getter
    public static class PlayerInfo {
        private String name, ip;
        private int port;
        private UUID uuid;

        private PlayerInfo(String name) {
            this.name = name;
        }

        private PlayerInfo() {
        }
    }

    private static class RunnableIDS {
        private static int playerCountRID, playerListRID, serversRID, currentServerRID, uuidAllRID, serverIPRID;
    }

    @Getter
    public static class ServerInfo {
        private String name, ip;
        private int playerCount;
        private String[] players = new String[0];
        private short port;

        private ServerInfo(String name) {
            this.name = name;
        }

        private ServerInfo() {

        }
    }
}
