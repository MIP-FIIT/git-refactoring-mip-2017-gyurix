package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutEntityHeadRotation extends WrappedPacket {
    public int entityId;
    public byte rotation;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.EntityHeadRotation.newPacket(entityId, rotation);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.EntityHeadRotation.getPacketData(packet);
        entityId = (int) d[0];
        rotation = (byte) d[1];
    }
}
