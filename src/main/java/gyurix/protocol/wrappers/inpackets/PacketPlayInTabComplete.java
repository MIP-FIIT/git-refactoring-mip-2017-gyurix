package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.event.PacketInType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.wrappers.WrappedPacket;

public class PacketPlayInTabComplete
        extends WrappedPacket {
    public BlockLocation block;
    public String text;

    @Override
    public Object getVanillaPacket() {
        Object[] arrobject = new Object[2];
        arrobject[0] = this.text;
        arrobject[1] = this.block == null ? null : this.block.toNMS();
        return PacketInType.TabComplete.newPacket(arrobject);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.TabComplete.getPacketData(packet);
        this.text = (String) data[0];
        this.block = data[1] == null ? null : new BlockLocation(data[1]);
    }
}

