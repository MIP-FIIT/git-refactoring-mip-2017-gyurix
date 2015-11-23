package gyurix.protocol.v1_8.outpackets;

import gyurix.protocol.PacketOutType;
import gyurix.protocol.WrappedPacket;

public class PacketPlayOutScoreboardDisplayObjective
        extends WrappedPacket {
    public int slot;
    public String name;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.ScoreboardDisplayObjective.newPacket(this.slot, this.name);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] o = PacketOutType.ScoreboardDisplayObjective.getPacketData(packet);
        this.slot = (Integer) o[0];
        this.name = (String) o[1];
    }
}

