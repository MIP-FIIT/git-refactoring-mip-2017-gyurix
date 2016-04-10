package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.event.PacketInType;
import gyurix.protocol.utils.ItemStackWrapper;
import gyurix.protocol.wrappers.WrappedPacket;

public class PacketPlayInSetCreativeSlot
        extends WrappedPacket {
    public ItemStackWrapper itemStack;
    public int slot;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.SetCreativeSlot.newPacket(this.slot, this.itemStack.toNMS());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.SetCreativeSlot.getPacketData(packet);
        this.slot = (Integer) data[0];
        this.itemStack = new ItemStackWrapper(data[1]);
    }
}

