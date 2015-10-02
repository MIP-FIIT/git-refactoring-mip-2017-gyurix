package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;

public class PacketPlayInFlying extends WrappedPacket
{
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
        return PacketInType.Flying.newPacket(x,y,z,yaw,pitch,onGround,hasPos,hasLook);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data=PacketInType.Flying.getPacketData(packet);
        x=(Double)data[0];
        y=(Double)data[1];
        z=(Double)data[2];
        yaw=(Float)data[3];
        pitch=(Float)data[4];
        onGround=(Boolean)data[5];
        hasPos=(Boolean)data[6];
        hasLook=(Boolean)data[7];
    }
}