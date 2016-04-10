package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

public class PacketPlayOutScoreboardDisplayObjective
        extends WrappedPacket {
    public String name;
    public int slot;

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

