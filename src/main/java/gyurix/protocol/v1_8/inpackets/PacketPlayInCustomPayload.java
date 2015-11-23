package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;

public class PacketPlayInCustomPayload
        extends WrappedPacket {
    public String message;
    public Object serializer;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.CustomPayload.newPacket(this.message, this.serializer);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.CustomPayload.getPacketData(packet);
        this.message = (String) data[0];
        this.serializer = data[1];
    }
}

