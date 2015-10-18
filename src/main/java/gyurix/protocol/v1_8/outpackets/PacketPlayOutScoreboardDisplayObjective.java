package gyurix.protocol.v1_8.outpackets;

import gyurix.protocol.PacketOutType;
import gyurix.protocol.WrappedPacket;

/**
 * Created by GyuriX on 2015.10.16..
 */
public class PacketPlayOutScoreboardDisplayObjective extends WrappedPacket{
    public int slot;
    public String name;
    @Override
    public Object getVanillaPacket() {
        return PacketOutType.ScoreboardDisplayObjective.newPacket(slot,name);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] o=PacketOutType.ScoreboardDisplayObjective.getPacketData(packet);
        slot= (int) o[0];
        name= (String) o[1];
    }
}
