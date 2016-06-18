package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;
import org.bukkit.Particle;

public class PacketPlayOutWorldParticles extends WrappedPacket {
    public float b;
    public float c;
    public float d;
    public float e;
    public float f;
    public float g;
    public float h;
    public int i;
    public boolean j;
    public int[] k;
    public Particle particle;

    public PacketPlayOutWorldParticles() {
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.WorldParticles.newPacket();
    }

    @Override
    public void loadVanillaPacket(Object packet) {

    }
}
