package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;

public class PacketPlayInSteerVehicle
        extends WrappedPacket {
    public float sideways;
    public float forward;
    public boolean jump;
    public boolean unmount;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.SteerVehicle.newPacket(Float.valueOf(this.sideways), Float.valueOf(this.forward), this.jump, this.unmount);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.SteerVehicle.getPacketData(packet);
        this.sideways = ((Float) data[0]).floatValue();
        this.forward = ((Float) data[1]).floatValue();
        this.jump = (Boolean) data[2];
        this.unmount = (Boolean) data[3];
    }
}

