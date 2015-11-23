package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;

public class PacketPlayInTransaction
        extends WrappedPacket {
    public int windowId;
    public short actionId;
    public boolean accepted;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.Transaction.newPacket(this.windowId, this.actionId, this.accepted);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.Transaction.getPacketData(packet);
        this.windowId = (Integer) data[0];
        this.actionId = (Short) data[1];
        this.accepted = (Boolean) data[2];
    }
}

