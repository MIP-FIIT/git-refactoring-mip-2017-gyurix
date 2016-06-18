package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;

public class PacketPlayInFlying
        extends WrappedPacket {
    public boolean hasLook;
    public boolean hasPos;
    public boolean onGround;
    public float pitch;
    public double x;
    public double y;
    public float yaw;
    public double z;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.Flying.newPacket(x, y, z, Float.valueOf(yaw), Float.valueOf(pitch), onGround, hasPos, hasLook);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.Flying.getPacketData(packet);
        x = (Double) data[0];
        y = (Double) data[1];
        z = (Double) data[2];
        yaw = ((Float) data[3]).floatValue();
        pitch = ((Float) data[4]).floatValue();
        onGround = (Boolean) data[5];
        hasPos = (Boolean) data[6];
        hasLook = (Boolean) data[7];
    }
}

