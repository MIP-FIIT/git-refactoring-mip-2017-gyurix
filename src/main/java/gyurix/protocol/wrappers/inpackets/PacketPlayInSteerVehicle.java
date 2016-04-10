package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;

public class PacketPlayInSteerVehicle
        extends WrappedPacket {
    public float forward;
    public boolean jump;
    public float sideways;
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

