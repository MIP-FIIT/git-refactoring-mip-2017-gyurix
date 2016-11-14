package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.DataWatcher;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.LocationData;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutSpawnEntityLiving extends WrappedPacket {
    public int entityId;
    public UUID entityUUID;
    public DataWatcher meta;
    /**
     * 48 - Mob 49 - Monster 50 - Creeper 51 - Skeleton 52 - Spider 53 - Giant Zombie 54 - Zombie 55 - Slime 56 - Ghast
     * 57 - Zombie Pigman 58 - Enderman 59 - Cave Spider 60 - Silverfish 61 - Blaze 62 - Magma Cube 63 - Ender Dragon 64
     * - Wither 65 - Bat 66 - Witch 67 - Endermite 68 - Guardian 69 - Shulker 90 - Pig 91 - Sheep 92 - Cow 93 - Chicken
     * 94 - Squid 95 - Wolf 96 - Mooshroom 97 - Snowman 98 - Ocelot 99 - Iron Golem 100 - Horse 101 - Rabbit 120 -
     * Villager
     */
    public int type;
    public int velX, velY, velZ;
    public double x, y, z;
    public float yaw, pitch, headPitch;

    public PacketPlayOutSpawnEntityLiving() {

    }

    public PacketPlayOutSpawnEntityLiving(int entityId, UUID entityUUID, int type, LocationData loc, float headPitch, Vector velocity, DataWatcher meta) {
        this.entityId = entityId;
        this.entityUUID = entityUUID;
        this.type = type;
        setLocation(loc);
        this.headPitch = headPitch;
        if (velocity != null)
            setVelocity(velocity);
        this.meta = meta;
    }

    public Location getLocation() {
        return new Location(null, x, y, z, yaw, pitch);
    }

    public void setLocation(LocationData loc) {
        x = loc.x;
        y = loc.y;
        z = loc.z;
        yaw = loc.yaw;
        pitch = loc.pitch;
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.SpawnEntityLiving.newPacket(entityId, entityUUID, type, x, y, z, velX, velY, velZ,
                (byte) (yaw / 360.0f * 256), (byte) (pitch / 360.0f * 256), (byte) (headPitch / 360.0f * 256), meta.toNMS());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.SpawnEntityLiving.getPacketData(packet);
        entityId = (int) d[0];
        entityUUID = (UUID) d[1];
        type = (int) d[2];
        x = (double) d[3];
        y = (double) d[4];
        z = (double) d[5];
        velX = (int) d[6];
        velY = (int) d[7];
        velZ = (int) d[8];
        yaw = (float) ((byte) d[9] / 256.0 * 360);
        pitch = (float) ((byte) d[10] / 256.0 * 360);
        headPitch = (float) ((byte) d[11] / 256.0 * 360);
        meta = new DataWatcher(d[12]);
    }

    public Vector getVelocity() {
        return new Vector(velX / 8000.0, velY / 8000.0, velZ / 8000.0);
    }

    public void setVelocity(Vector velocity) {
        velX = (int) (velocity.getX() * 8000);
        velY = (int) (velocity.getY() * 8000);
        velZ = (int) (velocity.getZ() * 8000);
    }
}
