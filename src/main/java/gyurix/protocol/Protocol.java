package gyurix.protocol;

import com.google.common.collect.Lists;
import gyurix.spigotlib.Config;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Protocol implements Listener {
    private static final ConcurrentHashMap<PacketInType, ArrayList<PacketInListener>> inListeners = new ConcurrentHashMap<>();
    private static final HashMap<PacketOutType, ArrayList<PacketOutListener>> outListeners = new HashMap<>();
    private static final HashMap<PacketInListener, PacketInType> inListenerTypes = new HashMap<>();
    private static final HashMap<PacketOutListener, PacketOutType> outListenerTypes = new HashMap<>();
    private static final HashMap<Plugin, ArrayList<PacketInListener>> pluginInListeners = new HashMap<>();
    private static final HashMap<Plugin, ArrayList<PacketOutListener>> pluginOutListeners = new HashMap<>();

    /**
     * Sends the given vanilla packet to a player
     *
     * @param player - The target player
     * @param packet - The sendable packet
     */
    public abstract void sendPacket(Player player, Object packet);

    /**
     * Sends the given vanilla packet to a channel
     *
     * @param channel - The target players channel
     * @param packet  - The sendable packet
     */
    public abstract void sendPacket(Channel channel, Object packet);


    /**
     * Simulates receiving the given vanilla packet from a player
     *
     * @param player - The sender player
     * @param packet - The sendable packet
     */
    public abstract void receivePacket(Player player, Object packet);

    /**
     * Simulates receiving the given vanilla packet from a channel
     *
     * @param channel - The sender players channel
     * @param packet  - The sendable packet
     */
    public abstract void receivePacket(Channel channel, Object packet);

    /**
     * Returns the channel of a Player
     *
     * @param plr - The target Player
     * @return The channel of the target Player
     */
    public abstract Channel getChannel(Player plr);

    /**
     * Returns the Player belonging to the given channel
     *
     * @param channel - The target Player
     * @return The Player for who is the given channel belongs to, or null if the Channel and the Player object is not yet matched.
     */
    public abstract Player getPlayer(Channel channel);

    public void registerIncomingListener(Plugin plugin, PacketInListener listener, PacketInType packetType) {
        if (inListenerTypes.containsKey(listener))
            throw new RuntimeException("The given listener is already registered.");
        ArrayList<PacketInListener> pil = inListeners.get(packetType);
        if (pil == null)
            inListeners.put(packetType, Lists.newArrayList(listener));
        else
            pil.add(listener);
        inListenerTypes.put(listener, packetType);
        pil = pluginInListeners.get(plugin);
        if (pil == null)
            pluginInListeners.put(plugin, Lists.newArrayList(listener));
        else
            pil.add(listener);
    }

    public void registerOutgoingListener(Plugin plugin, PacketOutListener listener, PacketOutType packetType) {
        if (outListenerTypes.containsKey(listener))
            throw new RuntimeException("The given listener is already registered.");
        ArrayList<PacketOutListener> pol = outListeners.get(packetType);
        if (pol == null)
            outListeners.put(packetType, Lists.newArrayList(listener));
        else
            pol.add(listener);
        outListenerTypes.put(listener, packetType);
        pol = pluginOutListeners.get(plugin);
        if (pol == null)
            pluginOutListeners.put(plugin, Lists.newArrayList(listener));
        else
            pol.add(listener);
    }

    public void unregisterIncomingListener(Plugin pl) {
        ArrayList<PacketInListener> pol = pluginInListeners.remove(pl);
        if (pol == null)
            return;
        for (PacketInListener l : pol)
            inListeners.remove(inListenerTypes.remove(l));
    }

    public void unregisterOutgoingListener(Plugin pl) {
        ArrayList<PacketOutListener> pol = pluginOutListeners.remove(pl);
        if (pol == null)
            return;
        for (PacketOutListener l : pol)
            outListeners.remove(outListenerTypes.remove(l));
    }

    public void dispatchPacketInEvent(PacketInEvent event) {
        ArrayList<PacketInListener> ll = inListeners.get(event.getType());
        if (ll != null)
            for (PacketInListener l : ll) {
                try {
                    l.onPacketIN(event);
                } catch (Throwable e) {
                    System.err.println("Error on dispatching PacketInEvent for packet type " + event.getType() + ".");
                    if (Config.debug)
                        e.printStackTrace();
                }
            }
    }

    public void dispatchPacketOutEvent(PacketOutEvent event) {
        ArrayList<PacketOutListener> ll = outListeners.get(event.getType());
        if (ll != null)
            for (PacketOutListener l : ll) {
                try {
                    l.onPacketOUT(event);
                } catch (Throwable e) {
                    System.err.println("Error on dispatching PacketOutEvent for packet type " + event.getType() + ".");
                    if (Config.debug)
                        e.printStackTrace();
                }
            }
    }

    /**
     * Closes the PacketAPI
     */
    public abstract void close();

    public abstract PacketCapture getCapturer(Player plr);

    public abstract void setCapturer(Player plr, PacketCapture packetCapture);

    public interface PacketInListener{
        void onPacketIN(PacketInEvent e);
    }

    public interface PacketOutListener {
        void onPacketOUT(PacketOutEvent e);
    }
}
