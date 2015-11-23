package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;

public class PacketPlayInAbilities
        extends WrappedPacket {
    public boolean isInvulnerable;
    public boolean isFlying;
    public boolean canFly;
    public boolean canInstantlyBuild;
    public float flySpeed;
    public float walkSpeed;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.Abilities.newPacket(this.isInvulnerable, this.isFlying, this.canFly, this.canInstantlyBuild, Float.valueOf(this.flySpeed), Float.valueOf(this.walkSpeed));
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.Abilities.getPacketData(packet);
        this.isInvulnerable = (Boolean) data[0];
        this.isFlying = (Boolean) data[1];
        this.canFly = (Boolean) data[2];
        this.canInstantlyBuild = (Boolean) data[3];
        this.flySpeed = ((Float) data[4]).floatValue();
        this.walkSpeed = ((Float) data[5]).floatValue();
    }
}

