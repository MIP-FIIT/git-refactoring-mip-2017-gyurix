package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;

public class PacketPlayInTransaction
        extends WrappedPacket {
    public boolean accepted;
    public short actionId;
    public int windowId;

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

