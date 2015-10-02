package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;

public class PacketPlayInHeldItemSlot extends WrappedPacket {
    public int itemInHandIndex;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.HeldItemSlot.newPacket(itemInHandIndex);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        itemInHandIndex = (Integer) PacketInType.HeldItemSlot.getPacketData(packet)[0];
    }
}