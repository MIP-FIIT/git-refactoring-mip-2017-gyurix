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
        return PacketInType.CustomPayload.newPacket(this.message, this.serializer);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.CustomPayload.getPacketData(packet);
        this.message = (String) data[0];
        this.serializer = (ByteBuf) data[1];
    }
}

