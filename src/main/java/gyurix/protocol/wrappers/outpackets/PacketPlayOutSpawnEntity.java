package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

import java.util.UUID;

public class PacketPlayOutSpawnEntity extends WrappedPacket {
    public int entityId;
    public int entityTypeId;
    /**
     * 1 - Boat 2 - Item Stack (Slot) 3 - Area Effect Cloud 10 - Minecart 11 - Minecart (storage) {unused since 1.6.x}
     * 12 - Minecart (powered) {unused since 1.6.x} 50 - Activated TNT 51 - EnderCrystal 60 - Arrow (projectile) 61 -
     * Snowball (projectile) 62 - Egg (projectile) 63 - FireBall (ghast projectile) 64 - FireCharge (blaze projectile)
     * 65 - Thrown Enderpearl 66 - Wither Skull (projectile) 67 - Shulker Bullet 70 - Falling Objects 71 - Item frames
     * 72 - Eye of Ender 73 - Thrown Potion 74 - Falling Dragon Egg 75 - Thrown Exp Bottle 76 - Firework Rocket 77 -
     * Leash Knot 78 - ArmorStand 90 - Fishing Float 91 - Spectral Arrow 92 - Tipped Arrow 93 - Dragon Fireball
     */
    public UUID entityUUID;
    public int objectData;
    public float pitch;
    public float speedX;
    public float speedY;
    public float speedZ;
    public double x;
    public double y;
    public float yaw;
    public double z;

    public int convertSpeed(float num) {
        return (int) (((double) num < -3.9 ? -3.9 : (double) num > 3.9 ? 3.9 : (double) num) * 8000.0);
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.SpawnEntity.newPacket(entityId, entityUUID, (int) (x * 32.0), (int) (y * 32.0), (int) (z * 32.0),
                convertSpeed(speedX), convertSpeed(speedY), convertSpeed(speedZ),
                (int) ((double) (pitch * 256.0f) / 360.0), (int) ((double) (yaw * 256.0f) / 360.0), entityTypeId, objectData);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] o = PacketOutType.SpawnEntity.getPacketData(packet);
        entityId = (int) o[0];
        entityUUID = (UUID) o[1];
        x = (double) (int) o[2] / 32.0;
        y = (double) (int) o[3] / 32.0;
        z = (double) (int) o[4] / 32.0;
        speedX = (float) (int) o[5] / 8000.0f;
        speedY = (float) (int) o[6] / 8000.0f;
        speedZ = (float) (int) o[7] / 8000.0f;
        pitch = (float) (int) o[8] / 256.0f * 360.0f;
        yaw = (float) (int) o[9] / 256.0f * 360.0f;
        entityTypeId = (int) o[10];
        objectData = (int) o[11];
    }
}

