package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutEntityLook extends WrappedPacket {
    public int entityId;
    public boolean onGround;
    public byte pitch;
    public byte yaw;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.EntityLook.newPacket(entityId, yaw, pitch, onGround);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.EntityLook.getPacketData(packet);
        entityId = (int) d[0];
        yaw = (byte) d[1];
        pitch = (byte) d[2];
        onGround = (boolean) d[3];
    }
}
