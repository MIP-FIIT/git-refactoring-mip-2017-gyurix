package gyurix.protocol;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class Protocol
        implements Listener {
    public static final Field handshakeNextState = Reflection.getFirstFieldOfType(Reflection.getNMSClass("PacketHandshakingInSetProtocol"), Reflection.getNMSClass("EnumProtocol"));
    private static final Class networkManagerClass = Reflection.getNMSClass("NetworkManager");
    private static final Class minecraftServerClass = Reflection.getNMSClass("MinecraftServer");
    private static final Class serverConnectionClass = Reflection.getNMSClass("ServerConnection");
    private static final Method getPlayerHandle = Reflection.getMethod(Reflection.getOBCClass("entity.CraftPlayer"), "getHandle");
    private static final Field getConnection = Reflection.getField(Reflection.getNMSClass("EntityPlayer"), "playerConnection");
    private static final Field getManager = Reflection.getFirstFieldOfType(Reflection.getNMSClass("PlayerConnection"), networkManagerClass);
    private static final Field getChannel = Reflection.getFirstFieldOfType(Reflection.getNMSClass("NetworkManager"), Channel.class);
    private static final Field getGameProfile = Reflection.getFirstFieldOfType(Reflection.getNMSClass("PacketLoginInStart"), GameProfile.class);
    private final Map<String, Channel> channelLookup = new MapMaker().weakValues().makeMap();
    private final List<Channel> serverChannels = Lists.newArrayList();
    Object minecraftServer;
    Object serverConnection;
    List networkManagers;
    List<ChannelFuture> channelFutures;
    private ChannelInboundHandlerAdapter serverChannel;
    private ChannelInitializer<Channel> beginInit;
    private ChannelInitializer<Channel> endInit;
    private boolean closed;

    public Protocol() {
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

    private void createServerChannelHandler() {
        this.endInit = new ChannelInitializer<Channel>() {

            protected void initChannel(Channel channel) throws Exception {
                try {
                    List list = Protocol.this.networkManagers;
                    if (!Protocol.this.closed) {
                        Protocol.this.injectChannelInternal(channel);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };
        this.beginInit = new ChannelInitializer<Channel>() {

            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(Protocol.this.endInit);
            }
        };
        this.serverChannel = new ChannelInboundHandlerAdapter() {

            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                Channel channel = (Channel) msg;
                channel.pipeline().addFirst(Protocol.this.beginInit);
                ctx.fireChannelRead(msg);
            }
        };
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
        if (this.closed) {
            return;
        }
        Channel channel = this.getChannel(e.getPlayer());
        this.injectPlayer(e.getPlayer());
    }

    private void registerChannelHandler() {
        this.createServerChannelHandler();
        for (ChannelFuture ch : this.channelFutures) {
            Channel serverChannel = ch.channel();
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

                @Override
                public void run() {
                    try {
                        pipeline.remove(Protocol.this.serverChannel);
                    } catch (Throwable var1_1) {
                        // empty catch block
                    }
                }
            });
        }
    }

    private void registerPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.injectPlayer(player);
        }
    }

    public void sendPacket(Player player, Object packet) {
        this.sendPacket(this.getChannel(player), packet);
    }

    public void sendPacket(Channel channel, Object packet) {
        channel.pipeline().writeAndFlush(packet);
    }

    public void receivePacket(Player player, Object packet) {
        this.receivePacket(this.getChannel(player), packet);
    }

    public void receivePacket(Channel channel, Object packet) {
        channel.pipeline().context("encoder").fireChannelRead(packet);
    }

    public void injectPlayer(Player player) {
        this.injectChannelInternal(this.getChannel(player)).player = player;
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

    public Channel getChannel(Player player) {
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

    public void uninjectPlayer(Player player) {
        this.uninjectChannel(this.getChannel(player));
    }

    public void uninjectChannel(final Channel channel) {
        channel.eventLoop().execute(new Runnable() {

            @Override
            public void run() {
                channel.pipeline().remove("SpigotLib");
            }
        });
    }

    public boolean hasInjected(Player player) {
        return this.hasInjected(this.getChannel(player));
    }

    public boolean hasInjected(Channel channel) {
        return channel.pipeline().get("SpigotLib") != null;
    }

    public final void close() {
        if (!this.closed) {
            this.closed = true;
            for (Player player : SU.srv.getOnlinePlayers()) {
                this.uninjectPlayer(player);
            }
            HandlerList.unregisterAll(this);
            this.unregisterChannelHandler();
        }
    }

    public final class NewChannelHandler
            extends ChannelDuplexHandler {
        public Player player;

        public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
            try {
                Channel channel = ctx.channel();
                PacketInEvent e = new PacketInEvent(channel, this, packet);
                if (e.getType() == PacketInType.LoginInStart) {
                    GameProfile profile = (GameProfile) getGameProfile.get(packet);
                    Protocol.this.channelLookup.put(profile.getName(), channel);
                }
                if (Config.packetAPI)
                    SU.pm.callEvent(e);
                if (!e.isCancelled()) {
                    super.channelRead(ctx, packet);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
            try {
                PacketOutEvent e = new PacketOutEvent(ctx.channel(), this, packet);
                if (Config.packetAPI)
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

