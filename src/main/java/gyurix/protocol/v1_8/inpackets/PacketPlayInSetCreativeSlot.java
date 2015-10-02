package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;
import gyurix.protocol.utils.ItemStackWrapper;

public class PacketPlayInSetCreativeSlot extends WrappedPacket
{
  public int slot;
  public ItemStackWrapper itemStack;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.SetCreativeSlot.newPacket(slot,itemStack.toVanillaStack());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data=PacketInType.SetCreativeSlot.getPacketData(packet);
        slot=(Integer)data[0];
        itemStack=new ItemStackWrapper(data[1]);
    }
}