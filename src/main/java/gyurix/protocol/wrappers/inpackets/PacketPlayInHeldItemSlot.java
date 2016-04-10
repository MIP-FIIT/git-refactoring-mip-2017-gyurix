package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;

public class PacketPlayInHeldItemSlot
        extends WrappedPacket {
    public int itemInHandIndex;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.HeldItemSlot.newPacket(this.itemInHandIndex);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        this.itemInHandIndex = (Integer) PacketInType.HeldItemSlot.getPacketData(packet)[0];
    }
}

