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
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static gyurix.protocol.Reflection.getFirstFieldOfType;
import static gyurix.protocol.Reflection.getNMSClass;
import static gyurix.spigotlib.Main.pl;

public class ProtocolImpl extends Protocol {
    private static final Field getGameProfile = getFirstFieldOfType(getNMSClass("PacketLoginInStart"), GameProfile.class);
    private static final Class minecraftServerClass = getNMSClass("MinecraftServer");
    private static final Class serverConnectionClass = getNMSClass("ServerConnection");
    private final Map<String, Channel> channelLookup = new MapMaker().weakValues().makeMap();
    private final List<Channel> serverChannels = Lists.newArrayList();
    private ChannelInitializer<Channel> beginInit;
    private List<ChannelFuture> channelFutures;
    private boolean closed;
    private ChannelInitializer<Channel> endInit;
    private ChannelInboundHandlerAdapter serverChannel;

    public ProtocolImpl() {
    }

    public final void close() {
        if (!closed) {
            closed = true;
            for (Player player : SU.srv.getOnlinePlayers()) {
                uninjectPlayer(player);
            }
            HandlerList.unregisterAll(this);
            unregisterChannelHandler();
        }
    }

    @Override
    public PacketCapture getCapturer(Player plr) {
        return getChannel(plr).pipeline().get(NewChannelHandler.class).pc;
    }

    public Channel getChannel(Player plr) {
        return channelLookup.get(plr.getName());
    }

    @Override
    public Player getPlayer(Object channel) {
        NewChannelHandler ch = ((Channel) channel).pipeline().get(NewChannelHandler.class);
        if (ch == null)
            return null;
        return ch.player;
    }

    public final void init() throws Throwable {
        Object minecraftServer = getFirstFieldOfType(Reflection.getOBCClass("CraftServer"), minecraftServerClass).get(SU.srv);
        Object serverConnection = getFirstFieldOfType(minecraftServerClass, serverConnectionClass).get(minecraftServer);
        channelFutures = (List) getFirstFieldOfType(serverConnectionClass, List.class).get(serverConnection);
        registerChannelHandler();
        registerPlayers();
    }

    public void receivePacket(Player player, Object packet) {
        receivePacket(getChannel(player), packet);
    }

    public void receivePacket(Object channel, Object packet) {
        if (packet instanceof WrappedPacket)
            packet = ((WrappedPacket) packet).getVanillaPacket();
        ((Channel) channel).pipeline().context("encoder").fireChannelRead(packet);
    }

    public void sendPacket(Player player, Object packet) {
        Object channel = getChannel(player);
        if (channel == null || packet == null) {
            SU.error(SU.cs, new RuntimeException("§cFailed to send packet " + packet + " to player " + (player == null ? "null" : player.getName())), "SpigotLib", "gyurix");
            return;
        }
        sendPacket(channel, packet);
    }

    public void sendPacket(Object channel, Object packet) {
        if (channel == null || packet == null) {
            SU.error(SU.cs, new RuntimeException("§cFailed to send packet " + packet + " to channel " + channel), "SpigotLib", "gyurix");
            return;
        }
        if (packet instanceof WrappedPacket)
            packet = ((WrappedPacket) packet).getVanillaPacket();
        ((Channel) channel).pipeline().writeAndFlush(packet);
    }

    @Override
    public void setCapturer(Player plr, PacketCapture packetCapture) {
        NewChannelHandler handler = getChannel(plr).pipeline().get(NewChannelHandler.class);
        PacketCapture pc = handler.pc;
        if (pc != null)
            pc.stop();
        handler.pc = packetCapture;
    }

    private void registerChannelHandler() {
        createServerChannelHandler();
        for (ChannelFuture ch : channelFutures) {
            Channel serverChannel = ch.channel();
            serverChannels.add(serverChannel);
            serverChannel.pipeline().addFirst(this.serverChannel);
        }
    }

    private void registerPlayers() {
        for (Player player : SU.srv.getOnlinePlayers()) {
            injectPlayer(player);
        }
    }

    private void createServerChannelHandler() {
        endInit = new ChannelInitializer<Channel>() {

            protected void initChannel(Channel channel) throws Exception {
                try {
                    if (!closed)
                        injectChannelInternal(channel);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };
        beginInit = new ChannelInitializer<Channel>() {

            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(endInit);
            }
        };
        serverChannel = new ChannelInboundHandlerAdapter() {
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                Channel channel = (Channel) msg;
                channel.pipeline().addFirst(beginInit);
                ctx.fireChannelRead(msg);
            }
        };
    }

    public void injectPlayer(final Player plr) {
        injectChannelInternal(getChannel(plr)).player = plr;
    }

    private NewChannelHandler injectChannelInternal(final Channel channel) {
        NewChannelHandler interceptor = (NewChannelHandler) channel.pipeline().get("SpigotLib");
        if (interceptor == null) {
            final NewChannelHandler newInterceptor = interceptor = new NewChannelHandler();
            try {
                channel.pipeline().addBefore("packet_handler", "SpigotLib", newInterceptor);
            } catch (Throwable e) {
                System.err.println("Scheduled interception");
                SU.sch.scheduleSyncDelayedTask(pl, new Runnable() {

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

    public void uninjectPlayer(Player player) {
        uninjectChannel(getChannel(player));
    }

    private void unregisterChannelHandler() {
        if (serverChannel == null) {
            return;
        }
        for (Channel ch : serverChannels) {
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

    public void uninjectChannel(final Channel channel) {
        if (channel == null)
            return;
        channel.eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                channel.pipeline().remove("SpigotLib");
            }
        });
    }

    public boolean hasInjected(Player player) {
        return hasInjected(getChannel(player));
    }

    public boolean hasInjected(Channel channel) {
        return channel.pipeline().get("SpigotLib") != null;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
        try {
            if (!Main.fullyEnabled) {
                e.disallow(Result.KICK_OTHER, Config.earlyJoinKickMsg);
                return;
            }
            if (closed)
                return;
            Channel channel = getChannel(e.getPlayer());
            injectPlayer(e.getPlayer());
        } catch (Throwable err) {
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent e) {
        final String pln = e.getPlayer().getName();
        SU.sch.scheduleSyncDelayedTask(pl, new Runnable() {
            @Override
            public void run() {
                Player p = Bukkit.getPlayer(pln);
                if (p == null)
                    channelLookup.remove(pln);
            }
        });
    }

    public final class NewChannelHandler extends ChannelDuplexHandler {
        public PacketCapture pc;
        public Player player;

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
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
            }
        }


    }

}

