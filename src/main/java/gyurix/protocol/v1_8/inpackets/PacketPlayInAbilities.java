package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;

public class PacketPlayInAbilities extends WrappedPacket {
    public boolean isInvulnerable;
    public boolean isFlying;
    public boolean canFly;
    public boolean canInstantlyBuild;
    public float flySpeed;
    public float walkSpeed;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.Abilities.newPacket(isInvulnerable,isFlying,canFly,canInstantlyBuild,flySpeed,walkSpeed);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data=PacketInType.Abilities.getPacketData(packet);
        isInvulnerable=(Boolean)data[0];
        isFlying=(Boolean)data[1];
        canFly=(Boolean)data[2];
        canInstantlyBuild=(Boolean)data[3];
        flySpeed=(Float)data[4];
        walkSpeed=(Float)data[5];
    }
}
