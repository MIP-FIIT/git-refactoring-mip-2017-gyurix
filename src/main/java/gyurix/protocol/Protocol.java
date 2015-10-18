package gyurix.protocol;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import gyurix.spigotlib.SU;
import io.netty.channel.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.PluginDisableEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the PacketAPI, with this API you can simple send and detect all the clientbound and serverbound packets without
 * caring about the Spigot version. It works on every 1.8.x version.
 */
public class Protocol
        implements Listener {
    private static final Class networkManagerClass = Reflection.getNMSClass("NetworkManager");
    private static final Class minecraftServerClass = Reflection.getNMSClass("MinecraftServer");
    private static final Class serverConnectionClass = Reflection.getNMSClass("ServerConnection");
    private static final Method getPlayerHandle = Reflection.getMethod(Reflection.getOBCClass("entity.CraftPlayer"), "getHandle");
    private static final Field getConnection = Reflection.getField(Reflection.getNMSClass("EntityPlayer"), "playerConnection");
    Object minecraftServer, serverConnection;
    List networkManagers;
    List<ChannelFuture> channelFutures;
    private static final Field getManager = Reflection.getFirstFieldOfType(Reflection.getNMSClass("PlayerConnection"), networkManagerClass);
    private static final Field getChannel = Reflection.getFirstFieldOfType(Reflection.getNMSClass("NetworkManager"), Channel.class);
    private static final Field getGameProfile = Reflection.getFirstFieldOfType(Reflection.getNMSClass("PacketLoginInStart"), GameProfile.class);
    public static final Field handshakeNextState = Reflection.getFirstFieldOfType(Reflection.getNMSClass("PacketHandshakingInSetProtocol"), Reflection.getNMSClass("EnumProtocol"));
    private final Map<String, Channel> channelLookup = new MapMaker().weakValues().makeMap();
    private final Set<Channel> uninjectedChannels = new HashSet<Channel>();
    private final List<Channel> serverChannels = Lists.newArrayList();
    private ChannelInboundHandlerAdapter serverChannel;
    private ChannelInitializer<Channel> beginInit, endInit;
    private boolean closed;

    public Protocol() {
        try {
            Protocol.this.minecraftServer = Reflection.getFirstFieldOfType(Reflection.getOBCClass("CraftServer"), minecraftServerClass).get(SU.srv);
            Protocol.this.serverConnection = Reflection.getFirstFieldOfType(minecraftServerClass, serverConnectionClass).get(Protocol.this.minecraftServer);
            Protocol.this.channelFutures = ((List) Reflection.getFirstFieldOfType(serverConnectionClass, List.class).get(Protocol.this.serverConnection));
            Protocol.this.networkManagers = ((List) Reflection.getLastFieldOfType(serverConnectionClass, List.class).get(Protocol.this.serverConnection));
            registerChannelHandler();
            registerPlayers();
        } catch (Throwable e) {
            System.err.println("Error on initializing Protocol.");
            e.printStackTrace();
        }
    }

    private void createServerChannelHandler() {
        this.endInit = new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel)
                    throws Exception {
                try {
                    synchronized (networkManagers) {
                        if (!closed) {
                            injectChannelInternal(channel);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };
        this.beginInit = new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel)
                    throws Exception {
                channel.pipeline().addLast(endInit);
            }
        };
        this.serverChannel = new ChannelInboundHandlerAdapter() {
            public void channelRead(ChannelHandlerContext ctx, Object msg)
                    throws Exception {
                Channel channel = (Channel) msg;
                channel.pipeline().addFirst(beginInit);
                ctx.fireChannelRead(msg);
            }
        };
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
        if (this.closed) {
            return;
        }
        Channel channel = getChannel(e.getPlayer());
        if (!this.uninjectedChannels.contains(channel)) {
            injectPlayer(e.getPlayer());
        }
    }

    private void registerChannelHandler() {
        createServerChannelHandler();
        for (Object ch : this.channelFutures) {
            Channel serverChannel = ((ChannelFuture) ch).channel();
            this.serverChannels.add(serverChannel);
            serverChannel.pipeline().addFirst(this.serverChannel);
        }
    }

    private void unregisterChannelHandler() {
        if (this.serverChannel == null) {
            return;
        }
        for (Channel ch : this.serverChannels) {
            final ChannelPipeline pipeline = ch.pipeline();
            ch.eventLoop().execute(new Runnable() {
                public void run() {
                    try {
                        pipeline.remove(serverChannel);
                    } catch (Throwable e) {
                    }
                }
            });
        }
    }

    private void registerPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            injectPlayer(player);
        }
    }

    /**
     * Sends the given packet to the given player
     *
     * @param player target player
     * @param packet packet to be sent
     */
    public void sendPacket(Player player, Object packet) {
        sendPacket(getChannel(player), packet);
    }

    /**
     * Sends the given packet to the given channel
     *
     * @param channel target channel
     * @param packet  packet to be sent
     */
    public void sendPacket(Channel channel, Object packet) {
        channel.pipeline().writeAndFlush(packet);
    }

    /**
     * Packet receive emulation from the given player
     *
     * @param player place of the emulation
     * @param packet emulated packet
     */
    public void receivePacket(Player player, Object packet) {
        receivePacket(getChannel(player), packet);
    }

    /**
     * Packet receive emulation from the given channel
     *
     * @param channel place of the emulation
     * @param packet  emulated packet
     */
    public void receivePacket(Channel channel, Object packet) {
        channel.pipeline().context("encoder").fireChannelRead(packet);
    }

    /**
     * Injects a player
     *
     * @param player player to be injected
     */
    public void injectPlayer(Player player) {
        injectChannelInternal(getChannel(player)).player = player;
    }

    private NewChannelHandler injectChannelInternal(Channel channel) {
        try {
            NewChannelHandler interceptor = (NewChannelHandler) channel.pipeline().get("SpigotLib");
            if (interceptor == null) {
                interceptor = new NewChannelHandler();
                try {
                    channel.pipeline().addBefore("packet_handler", "SpigotLib", interceptor);
                } catch (Throwable e) {
                    try {
                        Thread.sleep(0, 200);
                        return injectChannelInternal(channel);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                        return null;
                    }
                } finally {
                    this.uninjectedChannels.remove(channel);
                }
            }
            return interceptor;
        } catch (IllegalArgumentException e) {
        }
        return (NewChannelHandler) channel.pipeline().get("SpigotLib");
    }

    /**
     * Returns the channel of a player
     *
     * @param player channel owner player
     * @return The channel of the given player.
     */
    public Channel getChannel(Player player) {
        Channel channel = this.channelLookup.get(player.getName());
        if (channel == null) {
            try {
                Object connection = getConnection.get(getPlayerHandle.invoke(player));
                Object manager = getManager.get(connection);
                this.channelLookup.put(player.getName(), channel = (Channel) getChannel.get(manager));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return channel;
    }

    /**
     * Uninjects a player, used when the SpigotLib plugin is on disable
     *
     * @param player uninjectable Player
     */
    public void uninjectPlayer(Player player) {
        uninjectChannel(getChannel(player));
    }

    /**
     * Uninjects a channel, used when the SpigotLib plugin is on disable
     *
     * @param channel uninjectable Channel
     */
    public void uninjectChannel(final Channel channel) {
        if (!this.closed) {
            this.uninjectedChannels.add(channel);
        }
        channel.eventLoop().execute(new Runnable() {
            public void run() {
                channel.pipeline().remove("SpigotLib");
            }
        });
    }

    /**
     * Checks if a player is already injected
     *
     * @param player checkable Player
     * @return true, if the given Channel is already injected.
     */
    public boolean hasInjected(Player player) {
        return hasInjected(getChannel(player));
    }

    /**
     * Checks if a channel is injected
     *
     * @param channel checkable Channel
     * @return true, if the given Channel is already injected.
     */
    public boolean hasInjected(Channel channel) {
        return channel.pipeline().get("SpigotLib") != null;
    }

    /**
     * Closes the whole PacketAPI, used on disabling the SpigotLib
     */
    public final void close() {
        if (!this.closed) {
            this.closed = true;
            for (Player player : SU.srv.getOnlinePlayers()) {
                uninjectPlayer(player);
            }
            HandlerList.unregisterAll(this);
            unregisterChannelHandler();
        }
    }

    /**
     * The new ChannelHandler class for accessing Packet stream before the Spigot handles its packets.
     */
    public final class NewChannelHandler
            extends ChannelDuplexHandler {
        public Player player;

        public NewChannelHandler() {
        }

        public void channelRead(ChannelHandlerContext ctx, Object packet)
                throws Exception {
            try {
                Channel channel = ctx.channel();
                PacketInEvent e = new PacketInEvent(channel, this, packet);
                if (e.getType() == PacketInType.LoginInStart) {
                    GameProfile profile = (GameProfile) Protocol.getGameProfile.get(packet);
                    channelLookup.put(profile.getName(), channel);
                }
                SU.pm.callEvent(e);
                if (!e.isCancelled()) {
                    super.channelRead(ctx, packet);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise)
                throws Exception {
            try {
                PacketOutEvent e = new PacketOutEvent(ctx.channel(), this, packet);
                SU.pm.callEvent(e);
                if (!e.isCancelled()) {
                    super.write(ctx, packet, promise);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
