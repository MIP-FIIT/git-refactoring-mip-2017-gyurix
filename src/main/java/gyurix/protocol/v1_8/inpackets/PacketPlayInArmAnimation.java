package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;


public class PacketPlayInArmAnimation extends WrappedPacket {
    public long timestamp;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.ArmAnimation.newPacket(timestamp);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        timestamp = (Long) PacketInType.ArmAnimation.getPacketData(packet)[0];
    }
}