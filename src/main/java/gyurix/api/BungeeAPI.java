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

    /**
     * Executes commands on the BungeeCord server
     *
     * @param commands - The executable commands
     * @param players  - Names of players on who the commands should be executed
     * @return true - if the command execution request was sent to BungeeCord, false otherwise
     */
    public static boolean executeBungeeCommands(String[] commands, String... players) {
        return sendMessageToBungee("BungeeCommands", join(players, ","), serialize(commands));
    }

    /**
     * Executes Spigot commands on the given players
     *
     * @param commands - The executable commands
     * @param players  - Names of players on who the commands should be executed
     * @return true - if the command execution request was sent to BungeeCord, false otherwise
     */
    public static boolean executePlayerCommands(Command[] commands, String... players) {
        return forwardToPlayer("CommandExecution", serialize(commands).getBytes(utf8), players);
    }

    /**
     * Executes Spigot commands on the given servers
     *
     * @param commands - The executable commands
     * @param servers  - Names of servers on which the commands should be executed
     * @return true - if the command execution request was sent to BungeeCord, false otherwise
     */
    public static boolean executeServerCommands(String[] commands, String... servers) {
        return forwardToServer("CommandExecution", serialize(commands).getBytes(utf8), servers);
    }

    /**
     * Executes Spigot commands on the given servers
     *
     * @param commands - The executable commands
     * @param servers  - Names of servers on which the commands should be executed
     * @return true - if the command execution request was sent to BungeeCord, false otherwise
     */
    public static boolean executeServerCommands(Command[] commands, String... servers) {
        return forwardToServer("CommandExecution", serialize(commands).getBytes(utf8), servers);
    }

    /**
     * Forwards the given message to every server
     *
     * @param channel - Channel on which the message should be received by Spigot servers
     * @param message - The sendable message
     * @return true - if the forwarding request was sent to BungeeCord, false otherwise
     */
    public static boolean forwardToAllServer(String channel, byte[] message) {
        Player p = getAnyPlayer();
        if (p == null || message == null)
            return false;
        p.sendPluginMessage(Main.pl, "BungeeCord", makeForwardingData("ALL", channel, true, message));
        return true;
    }

    /**
     * Forwards the given message to the given players
     *
     * @param channel - Channel on which the message should be received by Spigot servers
     * @param message - The sendable message
     * @param players - The players who should receive the message
     * @return true - if the forwarding request was sent to BungeeCord, false otherwise
     */
    public static boolean forwardToPlayer(String channel, byte[] message, String... players) {
        Player p = getAnyPlayer();
        if (p == null || players.length == 0 || message == null)
            return false;
        for (String s : players)
            p.sendPluginMessage(Main.pl, "BungeeCord", makeForwardingData(s, channel, false, message));
        return true;
    }

    /**
     * Forwards the given message to the given players
     *
     * @param channel - Channel on which the message should be received by Spigot servers
     * @param message - The sendable message
     * @param players - The players who should receive the message
     * @return true - if the forwarding request was sent to BungeeCord, false otherwise
     */
    public static boolean forwardToPlayer(String channel, byte[] message, Iterable<String> players) {
        Player p = getAnyPlayer();
        if (p == null || !players.iterator().hasNext() || message == null)
            return false;
        players.forEach((s) -> p.sendPluginMessage(Main.pl, "BungeeCord", makeForwardingData(s, channel, false, message)));
        return true;
    }

    /**
     * Forwards the given message to the given servers
     *
     * @param channel - Channel on which the message should be received by Spigot servers
     * @param message - The sendable message
     * @param servers - The servers which should receive the message
     * @return true - if the forwarding request was sent to BungeeCord, false otherwise
     */
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

    /**
     * Gets the real IP of the given player
     *
     * @param plr - Target player
     * @return The real IP of the given player
     */
    public static String getIp(Player plr) {
        checkEnabled();
        return players.getOrDefault(plr.getUniqueId(), emptyPlayer).getIp();
    }

    /**
     * Gets the real port of the given player
     *
     * @param plr - Target player
     * @return The real port of the given player
     */
    public static Integer getPort(Player plr) {
        checkEnabled();
        return players.getOrDefault(plr.getUniqueId(), emptyPlayer).getPort();
    }

    /**
     * Gets the IP of the given server
     *
     * @param server - Target server
     * @return The IP of the given server
     */
    public static String getServerIp(String server) {
        checkEnabled();
        return servers.getOrDefault(server, emptyServer).ip;
    }

    /**
     * Gets the name used in Bungee config for the current server
     *
     * @return The name of the current server
     */
    public static String getServerName() {
        checkEnabled();
        return serverName;
    }

    /**
     * Gets the port number of the given server
     *
     * @param server - Target server
     * @return The port of the given server
     */
    public static Short getServerPort(String server) {
        checkEnabled();
        return servers.getOrDefault(server, emptyServer).getPort();
    }

    /**
     * Gets the real UUID of the given player
     *
     * @param plr - Target player
     * @return The real UUID of the given player
     */
    public static UUID getUUID(Player plr) {
        checkEnabled();
        return players.getOrDefault(plr.getName(), emptyPlayer).getUuid();
    }

    /**
     * Gets the real UUID of the given player
     *
     * @param pln - Target players name
     * @return The real UUID of the given player
     */
    public static UUID getUUID(String pln) {
        checkEnabled();
        return players.getOrDefault(pln, emptyPlayer).getUuid();
    }

    /**
     * Kicks the given players from the Bungee server
     *
     * @param reason  - Kicking reason
     * @param players - Names of the kickable players
     * @return true if the kicking request was sent to the Bungee, false otherwise
     */
    public static boolean kick(String reason, String... players) {
        if (reason == null || players.length == 0 || getAnyPlayer() == null)
            return false;
        for (String s : players)
            sendMessageToBungee("KickPlayer", s, reason);
        return true;
    }

    /**
     * Kicks the given players from the Bungee server
     *
     * @param reason  - Kicking reason
     * @param players - Names of the kickable players
     * @return true if the kicking request was sent to the Bungee, false otherwise
     */
    public static boolean kick(String reason, Iterable<String> players) {
        if (reason == null || !players.iterator().hasNext() || getAnyPlayer() == null)
            return false;
        players.forEach((s) -> sendMessageToBungee("KickPlayer", s, reason));
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

    /**
     * Gets the online player count on the given server
     *
     * @param server - Target server
     * @return The online player count on the given server
     */
    public static Integer playerCount(String server) {
        checkEnabled();
        return servers.getOrDefault(server, emptyServer).getPlayerCount();
    }

    /**
     * Gets the online players on the given server
     *
     * @param server - Target server
     * @return The online player list on the given server
     */
    public static String[] playerList(String server) {
        checkEnabled();
        return servers.getOrDefault(server, emptyServer).getPlayers();
    }

    /**
     * Requests the configured name of the current server from BungeeCord
     *
     * @return true if the request was succesful, false otherwise
     */
    public static boolean requestCurrentServerName() {
        return sendMessageToBungee("GetServer");
    }

    /**
     * Requests the real IP of the given players
     *
     * @param players - Players whose IP we would like to get
     */
    public static void requestIP(Player... players) {
        byte[] msg = makeDataOut("IP");
        for (Player p : players)
            p.sendPluginMessage(Main.pl, "BungeeCord", msg);
    }

    /**
     * Requests the real IP of the given players
     *
     * @param players - Players whose IP we would like to get
     */
    public static void requestIP(Iterable<Player> players) {
        byte[] msg = makeDataOut("IP");
        players.forEach((p) -> p.sendPluginMessage(Main.pl, "BungeeCord", msg));
    }

    /**
     * Requests the online player count on the given servers
     *
     * @param servers - Servers about which we would like to request the player count
     */
    public static boolean requestPlayerCount(Iterable<String> servers) {
        if (!servers.iterator().hasNext() || getAnyPlayer() == null)
            return false;
        servers.forEach((s) -> sendMessageToBungee("PlayerCount", s));
        return true;
    }

    /**
     * Requests the online player count on the given servers
     *
     * @param servers - Servers about which we would like to request the player count
     */
    public static boolean requestPlayerCount(String... servers) {
        if (servers.length == 0 || getAnyPlayer() == null)
            return false;
        for (String s : servers)
            sendMessageToBungee("PlayerCount", s);
        return true;
    }

    /**
     * Requests the online player list on the given servers
     *
     * @param servers - Servers about which we would like to request the player list
     * @return true if the request was sent to the Bungee successfully, false otherwise
     */
    public static boolean requestPlayerList(Iterable<String> servers) {
        if (!servers.iterator().hasNext() || getAnyPlayer() == null)
            return false;
        servers.forEach((s) -> sendMessageToBungee("PlayerList", s));
        return true;
    }

    /**
     * Requests the online player list on the given servers
     *
     * @param servers - Servers about which we would like to request the player list
     * @return true if the request was sent to the Bungee successfully, false otherwise
     */
    public static boolean requestPlayerList(String... servers) {
        if (servers.length == 0 || getAnyPlayer() == null)
            return false;
        for (String s : servers)
            sendMessageToBungee("PlayerList", s);
        return true;
    }

    /**
     * Requests the ip of the given servers
     *
     * @param servers - The servers which IP we would like to request
     * @return true if the request was sent to the Bungee successfully, false otherwise
     */
    public static boolean requestServerIP(Iterable<String> servers) {
        if (!servers.iterator().hasNext() || getAnyPlayer() == null)
            return false;
        servers.forEach((s) -> sendMessageToBungee("ServerIP", s));
        return true;
    }

    /**
     * Requests the ip of the given servers
     *
     * @param servers - The servers which IP we would like to request
     * @return true if the request was sent to the Bungee successfully, false otherwise
     */
    public static boolean requestServerIP(String... servers) {
        if (servers.length == 0 || getAnyPlayer() == null)
            return false;
        for (String s : servers)
            sendMessageToBungee("ServerIP", s);
        return true;
    }

    /**
     * Requests the names of the servers set up in the Bungee config
     *
     * @return true if the request was sent to the Bungee successfully, false otherwise
     */
    public static boolean requestServerNames() {
        return sendMessageToBungee("GetServers");
    }

    /**
     * Requests the real UUID of the given players
     *
     * @param players - The players whose UUID we would like to get
     * @return true if the request was sent to the Bungee successfully, false otherwise
     */
    public static boolean requestUUID(Iterable<String> players) {
        if (!players.iterator().hasNext() || getAnyPlayer() == null)
            return false;
        players.forEach((s) -> sendMessageToBungee("UUIDOther", s));
        return true;
    }

    /**
     * Requests the real UUID of the given players
     *
     * @param players - The players whose UUID we would like to get
     * @return true if the request was sent to the Bungee successfully, false otherwise
     */
    public static boolean requestUUID(String... players) {
        if (players.length == 0 || getAnyPlayer() == null)
            return false;
        for (String s : players)
            sendMessageToBungee("UUIDOther", s);
        return true;
    }

    /**
     * Sends the given players to the given server
     *
     * @param server  - Target server
     * @param players - Sendable players
     */
    public static void send(String server, Player... players) {
        byte[] msg = makeDataOut("Connect", server);
        for (Player p : players)
            p.sendPluginMessage(Main.pl, "BungeeCord", msg);
    }

    /**
     * Sends the given players to the given server
     *
     * @param server  - Target server
     * @param players - Collection of sendable players
     */
    public static void send(String server, Collection<Player> players) {
        byte[] msg = makeDataOut("Connect", server);
        players.forEach((p) -> p.sendPluginMessage(Main.pl, "BungeeCord", msg));
    }

    /**
     * Sends the given players to the given server
     *
     * @param server  - Target server
     * @param players - Names of sendable players
     * @return true if the request was sent to the Bungee successfully, false otherwise
     */
    public static boolean send(String server, String... players) {
        if (server == null || players.length == 0 || getAnyPlayer() == null)
            return false;
        for (String s : players)
            sendMessageToBungee("ConnectOther", s, server);
        return true;
    }

    /**
     * Sends the given players to the given server
     *
     * @param server  - Target server
     * @param players - Names of the sendable players
     * @return true if the request was sent to the Bungee successfully, false otherwise
     */
    public static boolean send(String server, Iterable<String> players) {
        if (server == null || !players.iterator().hasNext() || getAnyPlayer() == null)
            return false;
        players.forEach((s) -> sendMessageToBungee("ConnectOther", s, server));
        return true;
    }

    /**
     * Sends a chat message to the given players
     *
     * @param msg       - Sendable message
     * @param receivers - Receiver list
     * @return true if the request was sent to the Bungee successfully, false otherwise
     */
    public static boolean sendMessage(String msg, String... receivers) {
        if (msg == null || receivers.length == 0 || getAnyPlayer() == null)
            return false;
        for (String s : receivers)
            sendMessageToBungee("Message", s, msg);
        return true;
    }

    /**
     * Sends a chat message to the given players
     *
     * @param msg       - Sendable message
     * @param receivers - Receiver list
     * @return true if the request was sent to the Bungee successfully, false otherwise
     */
    public static boolean sendMessage(String msg, Iterable<String> receivers) {
        if (msg == null || !receivers.iterator().hasNext() || getAnyPlayer() == null)
            return false;
        receivers.forEach((s) -> sendMessageToBungee("Message", s, msg));
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

    /**
     * Gets the names of servers added to the BungeeCord configuration
     *
     * @return The names of servers added to the BungeeCord configuration
     */
    public static String[] serverNames() {
        checkEnabled();
        return servers.keySet().toArray(new String[servers.keySet().size()]);
    }

    /**
     * Starts the BungeeAPI
     * This method should NOT be called, by any plugins. It's only public for making SpigotLib being able to access it.
     *
     * @return true if the API was started succesfully, false otherwise
     */
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

    /**
     * Gets the sum of player counts on every server connected to BungeeCord
     *
     * @return The total player count
     */
    public static Integer totalPlayerCount() {
        return servers.getOrDefault("ALL", emptyServer).getPlayerCount();
    }

    /**
     * Gets the names of every player who is online on the whole BungeeCord network
     *
     * @return The names of every online player
     */
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
                        for (Command c : commands)
                            c.execute(player);
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

    /**
     * Class used for storing queried information about a player
     */
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

    /**
     * Class for storing the runnable ids of the automatic data updater Runnables used by BungeeAPI
     */
    private static class RunnableIDS {
        private static int playerCountRID, playerListRID, serversRID, currentServerRID, uuidAllRID, serverIPRID;
    }

    /**
     * Class used for storing queried information about a server
     */
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
