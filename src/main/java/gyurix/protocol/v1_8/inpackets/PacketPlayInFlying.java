package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;

public class PacketPlayInFlying
        extends WrappedPacket {
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public boolean onGround;
    public boolean hasPos;
    public boolean hasLook;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.Flying.newPacket(this.x, this.y, this.z, Float.valueOf(this.yaw), Float.valueOf(this.pitch), this.onGround, this.hasPos, this.hasLook);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.Flying.getPacketData(packet);
        this.x = (Double) data[0];
        this.y = (Double) data[1];
        this.z = (Double) data[2];
        this.yaw = ((Float) data[3]).floatValue();
        this.pitch = ((Float) data[4]).floatValue();
        this.onGround = (Boolean) data[5];
        this.hasPos = (Boolean) data[6];
        this.hasLook = (Boolean) data[7];
    }
}

