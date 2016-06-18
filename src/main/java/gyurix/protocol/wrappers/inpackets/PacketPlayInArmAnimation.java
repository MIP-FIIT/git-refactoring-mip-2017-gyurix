package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;

public class PacketPlayInArmAnimation
        extends WrappedPacket {
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

