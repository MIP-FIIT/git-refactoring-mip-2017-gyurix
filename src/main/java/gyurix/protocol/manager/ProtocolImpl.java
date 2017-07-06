package gyurix.protocol.manager;

import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import gyurix.protocol.Protocol;
import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInEvent;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.event.PacketOutEvent;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.EntityUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPromise;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static gyurix.protocol.Reflection.*;
import static gyurix.spigotlib.Main.pl;

public class ProtocolImpl extends Protocol {
    static final Field getGameProfile = getFirstFieldOfType(getNMSClass("PacketLoginInStart"), GameProfile.class),
            playerConnectionF = getField(getNMSClass("EntityPlayer"), "playerConnection"),
            networkManagerF = getField(getNMSClass("PlayerConnection"), "networkManager"),
            channelF = getField(getNMSClass("NetworkManager"), "channel");
    private static final Map<String, Channel> channelLookup = new MapMaker().weakValues().makeMap();
    private static final Class minecraftServerClass = getNMSClass("MinecraftServer");
    private static final Class serverConnectionClass = getNMSClass("ServerConnection");
    private static Object oldH;
    private static Field oldHChildF;
    private static Method oldHInitM;
    private ChannelFuture cf;
    private boolean closed;
    private ServerChannelHandler serverChannel;

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
        Channel c = channelLookup.get(plr.getName());
        if (c == null)
            try {
                Object nmsPlayer = EntityUtils.getNMSEntity(plr);
                Object playerConnection = playerConnectionF.get(nmsPlayer);
                Object networkManager = networkManagerF.get(playerConnection);
                Channel channel = (Channel) channelF.get(networkManager);
                channelLookup.put(plr.getName(), c = channel);
            } catch (Throwable ignored) {
            }
        return c;
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
        f.set(serverConnection, Collections.synchronizedList(new NetworkManagerList((List) f.get(serverConnection))));
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

    public boolean hasInjected(Player player) {
        return hasInjected(getChannel(player));
    }

    public boolean hasInjected(Channel channel) {
        return channel != null && channel.pipeline().get("SpigotLib") != null;
    }

    private void injectChannelInternal(final Channel channel, Player plr) {
        if (channel == null)
            return;
        NewChannelHandler interceptor = (NewChannelHandler) channel.pipeline().get("SpigotLib");
        if (interceptor == null)
            channel.pipeline().addBefore("packet_handler", "SpigotLib", interceptor = new NewChannelHandler());
        interceptor.player = plr;
    }

    public void injectPlayer(final Player plr) {
        injectChannelInternal(getChannel(plr), plr);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
        try {
            if (closed)
                return;
            injectPlayer(e.getPlayer());
        } catch (Throwable err) {
            SU.error(SU.cs, err, "SpigotLib", "gyurix");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerJoinEvent e) {
        try {
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
        Channel serverCh = cf.channel();
        oldH = serverCh.pipeline().get(Reflection.getClass("io.netty.bootstrap.ServerBootstrap$ServerBootstrapAcceptor"));
        oldHChildF = Reflection.getField(oldH.getClass(), "childHandler");
        serverChannel = new ServerChannelHandler((ChannelHandler) oldHChildF.get(oldH));
        oldHChildF.set(oldH, serverChannel);
        oldHInitM = Reflection.getMethod(serverChannel.childHandler.getClass(), "initChannel", Channel.class);
    }

    private void registerPlayers() {
        for (Player player : SU.srv.getOnlinePlayers()) {
            injectPlayer(player);
        }
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
        oldHChildF.set(oldH, serverChannel.childHandler);
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

    public static class ServerChannelHandler extends ChannelInitializer {
        public final ChannelHandler childHandler;

        public ServerChannelHandler(ChannelHandler childHandler) {
            this.childHandler = childHandler;
        }

        @Override
        protected void initChannel(Channel channel) throws Exception {
            NetworkManagerList.lastChannel.put(Thread.currentThread().getName(), channel);
            oldHInitM.invoke(childHandler, channel);
        }
    }

}

