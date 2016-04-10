package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;

public class PacketPlayInCloseWindow
        extends WrappedPacket {
    public int id;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.CloseWindow.newPacket(this.id);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        this.id = (Integer) PacketInType.CloseWindow.getPacketData(packet)[0];
    }
}

