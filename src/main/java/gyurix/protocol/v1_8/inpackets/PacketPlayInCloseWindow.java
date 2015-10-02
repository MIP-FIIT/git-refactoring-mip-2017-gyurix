package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;

public class PacketPlayInCloseWindow extends WrappedPacket{
    public int id;
    @Override
    public Object getVanillaPacket() {
        return PacketInType.CloseWindow.newPacket(id);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        id= (Integer) PacketInType.CloseWindow.getPacketData(packet)[0];
    }
}