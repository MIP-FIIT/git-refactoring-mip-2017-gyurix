package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;
import gyurix.protocol.utils.BlockLocation;

public class PacketPlayInTabComplete
        extends WrappedPacket {
    public String text;
    public BlockLocation block;

    @Override
    public Object getVanillaPacket() {
        Object[] arrobject = new Object[2];
        arrobject[0] = this.text;
        arrobject[1] = this.block == null ? null : this.block.toVanillaBlockPosition();
        return PacketInType.TabComplete.newPacket(arrobject);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.TabComplete.getPacketData(packet);
        this.text = (String) data[0];
        this.block = data[1] == null ? null : new BlockLocation(data[1]);
    }
}

