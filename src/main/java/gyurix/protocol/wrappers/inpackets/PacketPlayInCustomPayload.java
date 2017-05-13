package gyurix.protocol.wrappers.inpackets;

import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;
import io.netty.buffer.ByteBuf;

public class PacketPlayInCustomPayload extends WrappedPacket implements StringSerializable {
    public String channel;
    public ByteBuf data;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.CustomPayload.newPacket(channel, data);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketInType.CustomPayload.getPacketData(packet);
        channel = (String) d[0];
        data = (ByteBuf) d[1];
    }

    @Override
    public String toString() {
        return channel;
    }
}

