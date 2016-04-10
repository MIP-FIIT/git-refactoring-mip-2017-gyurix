package gyurix.protocol.event;

import gyurix.protocol.wrappers.WrappedPacket;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class PacketEvent
        extends Event
        implements Cancellable {
    private static final HandlerList hl = new HandlerList();
    private final Channel channel;
    private final Player player;
    protected Object packet;
    private boolean cancelled;

    public PacketEvent(Channel channel, Player plr, Object packet) {
        this.channel = channel;
        this.packet = packet;
        this.player = plr;
    }

    public static HandlerList getHandlerList() {
        return hl;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public HandlerList getHandlers() {
        return hl;
    }

    public Object getPacket() {
        return packet;
    }

    public void setPacket(Object packet) {
        this.packet = packet;
    }

    public abstract Object[] getPacketData();

    public abstract void setPacketData(Object... var1);

    public Player getPlayer() {
        return this.player;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public void setPacket(WrappedPacket packet) {
        this.packet = packet.getVanillaPacket();
    }

    public abstract boolean setPacketData(int var1, Object var2);
}

