package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;

public class PacketPlayInTransaction extends WrappedPacket
{
  public int windowId;
  public short actionId;
  public boolean accepted;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.Transaction.newPacket(windowId,actionId,accepted);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data=PacketInType.Transaction.getPacketData(packet);
        windowId=(Integer)data[0];
        actionId=(Short)data[1];
        accepted=(Boolean)data[2];
    }
}
