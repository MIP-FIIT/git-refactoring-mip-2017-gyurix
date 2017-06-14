package gyurix.protocol.manager;

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
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPromise;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static gyurix.protocol.Reflection.*;
import static gyurix.spigotlib.Main.pl;

public class ProtocolImpl extends Protocol {
    private static final Map<String, Channel> channelLookup = new MapMaker().weakValues().makeMap();
    private static final Field getGameProfile = getFirstFieldOfType(getNMSClass("PacketLoginInStart"), GameProfile.class);
    private static final Class minecraftServerClass = getNMSClass("MinecraftServer");
    private static final Class serverConnectionClass = getNMSClass("ServerConnection");
    private static Object oldH, oldChildH;
    private static Field oldHChildF;
    private static Method oldHInitM;
    private ChannelFuture cf;
    private boolean closed;
    private ChannelInboundHandlerAdapter serverChannel;

    public ProtocolImpl() {
    }

    public final void close() throws Throwable {
        if (closed)
            return;
        closed = true;
        HandlerList.unregisterAll(this);
        unregisterChannelHandler();
        for (Player player : SU.srv.getOnlinePlayers())
            uninjectPlayer(player);
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
        cf = (ChannelFuture) ((List) getFirstFieldOfType(serverConnectionClass, List.class).get(serverConnection)).iterator().next();
        Field f = getLastFieldOfType(serverConnectionClass, List.class);
        f.set(serverConnection, new NetworkManagerList((List) f.get(serverConnection)));
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

    private void createServerChannelHandler() {
        serverChannel = new ChannelInitializer() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                NetworkManagerList.lastChannel.put(Thread.currentThread().getName(), channel);
                oldHInitM.invoke(oldChildH, channel);
            }
        };
    }

    public boolean hasInjected(Player player) {
        return hasInjected(getChannel(player));
    }

    public boolean hasInjected(Channel channel) {
        return channel.pipeline().get("SpigotLib") != null;
    }

    private NewChannelHandler injectChannelInternal(final Channel channel) {
        NewChannelHandler interceptor = (NewChannelHandler) channel.pipeline().get("SpigotLib");
        if (interceptor == null)
            channel.pipeline().addBefore("packet_handler", "SpigotLib", interceptor = new NewChannelHandler());
        return interceptor;
    }

    public void injectPlayer(final Player plr) {
        injectChannelInternal(getChannel(plr)).player = plr;
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
            injectPlayer(e.getPlayer());
        } catch (Throwable err) {
            SU.error(SU.cs, err, "SpigotLib", "gyurix");
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

    private void registerChannelHandler() throws IllegalAccessException {
        createServerChannelHandler();
        Channel serverCh = cf.channel();
        oldH = serverCh.pipeline().first();
        oldHChildF = Reflection.getField(oldH.getClass(), "childHandler");
        oldChildH = oldHChildF.get(oldH);
        oldHInitM = Reflection.getMethod(oldChildH.getClass(), "initChannel", Channel.class);
        oldHChildF.set(oldH, serverChannel);
    }

    private void registerPlayers() {
        for (Player player : SU.srv.getOnlinePlayers())
            injectPlayer(player);
    }

    public void uninjectChannel(final Channel channel) {
        if (channel == null)
            return;
        channel.pipeline().remove("SpigotLib");
    }

    public void uninjectPlayer(Player player) {
        uninjectChannel(getChannel(player));
    }

    private void unregisterChannelHandler() throws IllegalAccessException {
        oldHChildF.set(oldH, oldChildH);
    }

    public static final class NewChannelHandler extends ChannelDuplexHandler {
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
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
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

