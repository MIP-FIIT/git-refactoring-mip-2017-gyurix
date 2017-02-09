package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.DataWatcher;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.LocationData;
import gyurix.spigotutils.ServerVersion;

import java.util.UUID;

/**
 * Created by GyuriX on 2016.03.06..
 */
public class PacketPlayOutNamedEntitySpawn extends WrappedPacket {
    public int entityId;
    public UUID entityUUID;
    public DataWatcher meta;
    public byte pitch;
    public double x;
    public double y;
    public byte yaw;
    public double z;

    public PacketPlayOutNamedEntitySpawn() {

    }

    public PacketPlayOutNamedEntitySpawn(int eid, UUID eUUID, LocationData loc, DataWatcher data) {
        entityId = eid;
        entityUUID = eUUID;
        setLocation(loc);
        meta = data;
    }

    public void setLocation(LocationData loc) {
        x = loc.x;
        y = loc.y;
        z = loc.z;
        yaw = (byte) (loc.yaw / 360.0 * 256);
        pitch = (byte) (loc.pitch / 360.0 * 256);
    }

    @Override
    public Object getVanillaPacket() {
        if (Reflection.ver == ServerVersion.v1_9)
            return PacketOutType.NamedEntitySpawn.newPacket(entityId, entityUUID, (int) x, (int) y, (int) z, yaw, pitch, meta.toNMS());
        else
            return PacketOutType.NamedEntitySpawn.newPacket(entityId, entityUUID, x, y, z, yaw, pitch, meta.toNMS());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.NamedEntitySpawn.getPacketData(packet);
        entityId = (int) d[0];
        entityUUID = (UUID) d[1];
        if (Reflection.ver == ServerVersion.v1_9) {
            x = (double) d[2];
            y = (double) d[3];
            z = (double) d[4];
        } else {
            x = (int) d[2];
            y = (int) d[3];
            z = (int) d[4];
        }
        yaw = (byte) d[5];
        pitch = (byte) d[6];
        meta = new DataWatcher(d[7]);
    }
}
