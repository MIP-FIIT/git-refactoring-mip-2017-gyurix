package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutSpawnEntityExperienceOrb extends WrappedPacket {
    public int count;
    public int entityId;
    public double x;
    public double y;
    public double z;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.SpawnEntityExperienceOrb.newPacket(entityId, x, y, z, count);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.SpawnEntityExperienceOrb.getPacketData(packet);
        entityId = (int) d[0];
        x = (double) d[1];
        y = (double) d[2];
        z = (double) d[3];
        count = (int) d[4];

    }
}
