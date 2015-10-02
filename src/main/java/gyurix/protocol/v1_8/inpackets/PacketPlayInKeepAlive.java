package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;

public class PacketPlayInKeepAlive extends WrappedPacket {
    public int id;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.KeepAlive.newPacket(id);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        id = (Integer) PacketInType.KeepAlive.getPacketData(packet)[0];
    }
}