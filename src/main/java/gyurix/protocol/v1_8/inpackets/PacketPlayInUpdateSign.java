package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;
import gyurix.protocol.utils.BlockLocation;
import gyurix.spigotlib.ChatAPI;

public class PacketPlayInUpdateSign
        extends WrappedPacket {
    public BlockLocation block;
    public String[] lineJsons;

    @Override
    public Object getVanillaPacket() {
        Object[] lines = new Object[4];
        for (int i = 0; i < 4; ++i) {
            lines[i] = ChatAPI.toICBC(this.lineJsons[i]);
        }
        return PacketInType.UpdateSign.newPacket(this.block.toVanillaBlockPosition(), lines);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.UpdateSign.getPacketData(packet);
        this.block = new BlockLocation(data[0]);
        this.lineJsons = new String[4];
        Object[] lines = (Object[]) data[1];
        for (int i = 0; i < 4; ++i) {
            lines[i] = ChatAPI.toJson(lines[i]);
        }
    }
}

