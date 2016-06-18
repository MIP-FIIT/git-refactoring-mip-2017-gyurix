package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;
import io.netty.buffer.ByteBuf;

public class PacketPlayInCustomPayload
        extends WrappedPacket {
    public String message;
    public ByteBuf serializer;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.CustomPayload.newPacket(message, serializer);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.CustomPayload.getPacketData(packet);
        message = (String) data[0];
        serializer = (ByteBuf) data[1];
    }
}
