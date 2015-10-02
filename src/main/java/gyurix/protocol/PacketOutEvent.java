package gyurix.protocol;

import io.netty.channel.Channel;

public class PacketOutEvent extends PacketEvent {
    private final PacketOutType type;

    public PacketOutEvent(Channel channel, Protocol.NewChannelHandler handler, Object packet) {
        super(channel, handler, packet);
        this.type = PacketOutType.getType(packet);
    }


    public Object[] getPacketData() {
        return this.type.getPacketData(this.packet);
    }

    public void setPacketData(Object... data) {
        this.type.fillPacket(this.packet, data);
    }

    public PacketOutType getType() {
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

 * Qualified Name:     gyurix.protocol.PacketOutEvent

 * JD-Core Version:    0.7.0.1

 */