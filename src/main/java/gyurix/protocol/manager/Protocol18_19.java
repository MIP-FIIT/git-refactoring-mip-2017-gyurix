package gyurix.protocol.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import gyurix.protocol.Protocol;
import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInEvent;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.event.PacketOutEvent;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import io.netty.channel.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class Protocol18_19 extends Protocol implements Listener {
    private static final Field getChannel = Reflection.getFirstFieldOfType(Reflection.getNMSClass("NetworkManager"), Channel.class);
    private static final Field getConnection = Reflection.getField(Reflection.getNMSClass("EntityPlayer"), "playerConnection");
    private static final Field getGameProfile = Reflection.getFirstFieldOfType(Reflection.getNMSClass("PacketLoginInStart"), GameProfile.class);
    private static final Method getPlayerHandle = Reflection.getMethod(Reflection.getOBCClass("entity.CraftPlayer"), "getHandle");
    private static final Class minecraftServerClass = Reflection.getNMSClass("MinecraftServer");
    private static final Class networkManagerClass = Reflection.getNMSClass("NetworkManager");
    private static final Field getManager = Reflection.getFirstFieldOfType(Reflection.getNMSClass("PlayerConnection"), networkManagerClass);
    private static final Class serverConnectionClass = Reflection.getNMSClass("ServerConnection");
    private final Map<String, Channel> channelLookup = new MapMaker().weakValues().makeMap();
    private final List<Channel> serverChannels = Lists.newArrayList();
    List<ChannelFuture> channelFutures;
    Object minecraftServer;
    List networkManagers;
    Object serverConnection;
    private ChannelInitializer<Channel> beginInit;
    private boolean closed;
    private ChannelInitializer<Channel> endInit;
    private ChannelInboundHandlerAdapter serverChannel;

    public Protocol18_19() {
        try {
            this.minecraftServer = Reflection.getFirstFieldOfType(Reflection.getOBCClass("CraftServer"), minecraftServerClass).get(SU.srv);
            this.serverConnection = Reflection.getFirstFieldOfType(minecraftServerClass, serverConnectionClass).get(this.minecraftServer);
            this.channelFutures = (List) Reflection.getFirstFieldOfType(serverConnectionClass, List.class).get(this.serverConnection);
            this.networkManagers = (List) Reflection.getLastFieldOfType(serverConnectionClass, List.class).get(this.serverConnection);
            this.registerChannelHandler();
            this.registerPlayers();
        } catch (Throwable e) {
            System.err.println("Error on initializing Protocol.");
            e.printStackTrace();
        }
    }

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

    private void createServerChannelHandler() {
        this.endInit = new ChannelInitializer<Channel>() {

            protected void initChannel(Channel channel) throws Exception {
                try {
                    if (!closed)
                        injectChannelInternal(channel);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };
        this.beginInit = new ChannelInitializer<Channel>() {

            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(endInit);
            }
        };
        this.serverChannel = new ChannelInboundHandlerAdapter() {
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                Channel channel = (Channel) msg;
                channel.pipeline().addFirst(beginInit);
                ctx.fireChannelRead(msg);
            }
        };
    }

    @Override
    public PacketCapture getCapturer(Player plr) {
        return getChannel(plr).pipeline().get(NewChannelHandler.class).pc;
    }

    public Channel getChannel(Player player) {
        if (player == null) {
            System.err.println("Getting channel of ");
        }
        Channel channel = this.channelLookup.get(player.getName());
        if (channel == null) {
            try {
                Object connection = getConnection.get(getPlayerHandle.invoke(player));
                Object manager = getManager.get(connection);
                channel = (Channel) getChannel.get(manager);
                this.channelLookup.put(player.getName(), channel);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return channel;
    }

    @Override
    public Player getPlayer(Channel channel) {
        NewChannelHandler ch = channel.pipeline().get(NewChannelHandler.class);
        if (ch == null)
            return null;
        return ch.player;
    }

    public boolean hasInjected(Player player) {
        return this.hasInjected(this.getChannel(player));
    }

    public boolean hasInjected(Channel channel) {
        return channel.pipeline().get("SpigotLib") != null;
    }

    private NewChannelHandler injectChannelInternal(final Channel channel) {
        NewChannelHandler interceptor = (NewChannelHandler) channel.pipeline().get("SpigotLib");
        if (interceptor == null) {
            final NewChannelHandler newInterceptor = interceptor = new NewChannelHandler();
            try {
                channel.pipeline().addBefore("packet_handler", "SpigotLib", newInterceptor);
            } catch (Throwable e) {
                System.err.println("Scheduled interception");
                SU.sch.scheduleSyncDelayedTask(Main.pl, new Runnable() {

                    @Override
                    public void run() {
                        try {
                            channel.pipeline().addBefore("packet_handler", "SpigotLib", newInterceptor);
                        } catch (Throwable e) {
                            System.err.println("Failed interception");
                        }
                    }
                });
            }
        }
        return interceptor;
    }

    public void injectPlayer(Player player) {
        this.injectChannelInternal(this.getChannel(player)).player = player;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
        if (!Main.fullyEnabled) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, Config.start);
            return;
        }
        if (this.closed) {
            return;
        }
        Channel channel = this.getChannel(e.getPlayer());
        this.injectPlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        channelLookup.remove(e.getPlayer().getName());
    }

    public void receivePacket(Player player, Object packet) {
        receivePacket(getChannel(player), packet);
    }

    public void receivePacket(Channel channel, Object packet) {
        if (packet instanceof WrappedPacket)
            packet = ((WrappedPacket) packet).getVanillaPacket();
        channel.pipeline().context("encoder").fireChannelRead(packet);
    }

    private void registerChannelHandler() {
        this.createServerChannelHandler();
        for (ChannelFuture ch : this.channelFutures) {
            Channel serverChannel = ch.channel();
            this.serverChannels.add(serverChannel);
            serverChannel.pipeline().addFirst(this.serverChannel);
        }
    }

    private void registerPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.injectPlayer(player);
        }
    }

    public void sendPacket(Player player, Object packet) {
        sendPacket(getChannel(player), packet);
    }

    public void sendPacket(Channel channel, Object packet) {
        if (packet instanceof WrappedPacket)
            packet = ((WrappedPacket) packet).getVanillaPacket();
        channel.pipeline().writeAndFlush(packet);
    }

    @Override
    public void setCapturer(Player plr, PacketCapture packetCapture) {
        NewChannelHandler handler = getChannel(plr).pipeline().get(NewChannelHandler.class);
        PacketCapture pc = handler.pc;
        if (pc != null)
            pc.stop();
        handler.pc = packetCapture;
    }

    public void uninjectChannel(final Channel channel) {
        channel.eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                channel.pipeline().remove("SpigotLib");
            }
        });
    }

    public void uninjectPlayer(Player player) {
        this.uninjectChannel(this.getChannel(player));
    }

    private void unregisterChannelHandler() {
        if (this.serverChannel == null) {
            return;
        }
        for (Channel ch : this.serverChannels) {
            final ChannelPipeline pipeline = ch.pipeline();
            ch.eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        pipeline.remove(serverChannel);
                    } catch (Throwable e) {
                        SU.error(SU.cs, e, "SpigotLib", "gyurix");
                    }
                }
            });
        }
    }

    public final class NewChannelHandler
            extends ChannelDuplexHandler {
        public PacketCapture pc;
        public Player player;

        public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
            try {
                Channel channel = ctx.channel();
                if (pc != null)
                    pc.capIn(packet);

                PacketInEvent e = new PacketInEvent(channel, player, packet);
                if (e.getType() == PacketInType.LoginInStart) {
                    GameProfile profile = (GameProfile) getGameProfile.get(packet);
                    channelLookup.put(profile.getName(), channel);
                }
                dispatchPacketInEvent(e);
                packet = e.getPacket();
                if (!e.isCancelled()) {
                    super.channelRead(ctx, packet);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
            try {
                if (pc != null)
                    pc.capOut(packet);
                PacketOutEvent e = new PacketOutEvent(ctx.channel(), player, packet);
                dispatchPacketOutEvent(e);
                packet = e.getPacket();
                if (!e.isCancelled()) {
                    super.write(ctx, packet, promise);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }


    }

}

