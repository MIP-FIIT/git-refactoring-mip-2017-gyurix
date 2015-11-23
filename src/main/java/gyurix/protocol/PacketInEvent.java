package gyurix.protocol;

import io.netty.channel.Channel;

public class PacketInEvent
        extends PacketEvent {
    private final PacketInType type;

    public PacketInEvent(Channel channel, Protocol.NewChannelHandler handler, Object packet) {
        super(channel, handler, packet);
        this.type = PacketInType.getType(packet);
    }

    @Override
    public Object[] getPacketData() {
        return this.type.getPacketData(this.packet);
    }

    @Override
    public /* varargs */ void setPacketData(Object... data) {
        this.type.fillPacket(this.getPacketObject(), data);
    }

    public PacketInType getType() {
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

