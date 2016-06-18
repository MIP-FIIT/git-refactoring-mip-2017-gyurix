package gyurix.protocol.event;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;

public class PacketInEvent
        extends PacketEvent {
    private final PacketInType type;

    public PacketInEvent(Channel channel, Player plr, Object packet) {
        super(channel, plr, packet);
        type = PacketInType.getType(packet);
    }

    @Override
    public Object[] getPacketData() {
        return type.getPacketData(packet);
    }

    @Override
    public void setPacketData(Object... data) {
        type.fillPacket(getPacket(), data);
    }

    public PacketInType getType() {
        return type;
    }

    @Override
    public boolean setPacketData(int id, Object o) {
        try {
            type.fs.get(id).set(packet, o);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}

