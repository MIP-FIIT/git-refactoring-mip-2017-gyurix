package gyurix.protocol;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class PacketEvent
        extends Event
        implements Cancellable {
    private static final HandlerList hl = new HandlerList();
    protected final Object packet;
    private final Channel channel;
    private final Protocol.NewChannelHandler handler;
    private boolean cancelled;

    public PacketEvent(Channel channel, Protocol.NewChannelHandler handler, Object packet) {
        this.channel = channel;
        this.packet = packet;
        this.handler=handler;
    }

    public static HandlerList getHandlerList() {
        return hl;
    }

    public HandlerList getHandlers() {
        return hl;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public Player getPlayer() {
        return handler.player;
    }

    public Object getPacketObject() {
        return this.packet;
    }

    public abstract Object[] getPacketData();

    public abstract void setPacketData(Object... paramVarArgs);

    public abstract boolean setPacketData(int paramInt, Object paramObject);

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}



/* Location:           D:\pluginok\ServerLib\out\artifacts\SpigotLib.jar

 * Qualified Name:     gyurix.protocol.PacketEvent

 * JD-Core Version:    0.7.0.1

 */