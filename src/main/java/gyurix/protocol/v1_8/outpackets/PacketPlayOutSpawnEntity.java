package gyurix.protocol.v1_8.outpackets;

import gyurix.protocol.PacketOutType;
import gyurix.protocol.WrappedPacket;

public class PacketPlayOutSpawnEntity
        extends WrappedPacket {
    public int entityId;
    public double x;
    public double y;
    public double z;
    public float speedX;
    public float speedY;
    public float speedZ;
    public float pitch;
    public float yaw;
    public int entityTypeId;
    public int objectData;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.SpawnEntity.newPacket(this.entityId, (int) (this.x * 32.0), (int) (this.y * 32.0), (int) (this.z * 32.0), this.convertSpeed(this.speedX), this.convertSpeed(this.speedY), this.convertSpeed(this.speedZ), (int) ((double) (this.pitch * 256.0f) / 360.0), (int) ((double) (this.yaw * 256.0f) / 360.0), this.entityTypeId, this.objectData);
    }

    public int convertSpeed(float num) {
        return (int) (((double) num < -3.9 ? -3.9 : ((double) num > 3.9 ? 3.9 : (double) num)) * 8000.0);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] o = PacketOutType.SpawnEntity.getPacketData(packet);
        this.entityId = (Integer) o[0];
        this.x = (double) ((Integer) o[1]).intValue() / 32.0;
        this.y = (double) ((Integer) o[2]).intValue() / 32.0;
        this.z = (double) ((Integer) o[3]).intValue() / 32.0;
        this.speedX = (float) ((Integer) o[4]).intValue() / 8000.0f;
        this.speedY = (float) ((Integer) o[5]).intValue() / 8000.0f;
        this.speedZ = (float) ((Integer) o[6]).intValue() / 8000.0f;
        this.pitch = (float) ((Integer) o[7]).intValue() / 256.0f * 360.0f;
        this.yaw = (float) ((Integer) o[8]).intValue() / 256.0f * 360.0f;
        this.entityTypeId = (Integer) o[9];
        this.objectData = (Integer) o[10];
    }
}

