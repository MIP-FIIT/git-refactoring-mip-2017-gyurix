package gyurix.protocol;

import io.netty.channel.Channel;

public class PacketInEvent extends PacketEvent {
    private final PacketInType type;

    public PacketInEvent(Channel channel, Protocol.NewChannelHandler handler, Object packet) {
        super(channel, handler, packet);
        this.type = PacketInType.getType(packet);
    }


    public Object[] getPacketData() {
        return this.type.getPacketData(this.packet);
    }

    public void setPacketData(Object... data) {
        this.type.fillPacket(getPacketObject(), data);
    }

    public PacketInType getType() {
        return this.type;
    }

    public boolean setPacketData(int id, Object o) {
        try {
            type.fs.get(id).set(this.packet, o);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }
}



/* Location:           D:\pluginok\ServerLib\out\artifacts\SpigotLib.jar

 * Qualified Name:     gyurix.protocol.PacketInEvent

 * JD-Core Version:    0.7.0.1

 */