package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.utils.HandType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.ServerVersion;

public class PacketPlayInArmAnimation extends WrappedPacket {
    public HandType hand;
    public long timestamp;

    @Override
    public Object getVanillaPacket() {
        return Reflection.ver.isAbove(ServerVersion.v1_9) ? PacketInType.ArmAnimation.newPacket(hand.toNMS()) : PacketInType.ArmAnimation.newPacket(timestamp);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object d = PacketInType.ArmAnimation.getPacketData(packet)[0];
        if (Reflection.ver.isAbove(ServerVersion.v1_9))
            hand = HandType.valueOf(d.toString());
        else
            timestamp = (long) d;
    }
}

