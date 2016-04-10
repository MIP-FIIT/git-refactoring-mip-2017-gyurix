package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.LocationData;
import gyurix.spigotutils.ServerVersion;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutEntityTeleport extends WrappedPacket {
    public int entityId;
    public boolean onGround;
    public byte pitch;
    public double x;
    public double y;
    public byte yaw;
    public double z;

    public PacketPlayOutEntityTeleport() {

    }

    public PacketPlayOutEntityTeleport(int entityId, LocationData loc) {
        this.entityId = entityId;
        setLocation(loc);
    }

    @Override
    public Object getVanillaPacket() {
        if (Reflection.ver == ServerVersion.v1_8)
            return PacketOutType.EntityTeleport.newPacket(entityId, (int) x, (int) y, (int) z, yaw, pitch, onGround);
        else
            return PacketOutType.EntityTeleport.newPacket(entityId, x, y, z, yaw, pitch, onGround);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.EntityTeleport.getPacketData(packet);
        entityId = (int) d[0];
        if (Reflection.ver == ServerVersion.v1_8) {
            x = (int) d[1];
            y = (int) d[2];
            z = (int) d[3];
        } else {
            x = (double) d[1];
            y = (double) d[2];
            z = (double) d[3];
        }
        yaw = (byte) d[4];
        pitch = (byte) d[5];
        onGround = (boolean) d[6];
    }

    public void setLocation(LocationData loc) {
        x = loc.x;
        y = loc.y;
        z = loc.z;
        yaw = (byte) (loc.yaw / 360.0 * 256);
        pitch = (byte) (loc.pitch / 360.0 * 256);
    }
}
