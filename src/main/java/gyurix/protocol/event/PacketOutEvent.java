package gyurix.protocol.event;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;

public class PacketOutEvent
        extends PacketEvent {
    private final PacketOutType type;

    public PacketOutEvent(Channel channel, Player plr, Object packet) {
        super(channel, plr, packet);
        this.type = PacketOutType.getType(packet);
    }

    @Override
    public Object[] getPacketData() {
        return this.type.getPacketData(this.packet);
    }

    @Override
    public void setPacketData(Object... data) {
        this.type.fillPacket(this.packet, data);
    }

    public PacketOutType getType() {
        return this.type;
    }

    @Override
    public boolean setPacketData(int id, Object o) {
        try {
            this.type.fs.get(id).set(this.packet, o);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}

