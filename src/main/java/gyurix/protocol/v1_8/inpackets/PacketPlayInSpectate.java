package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;

import java.util.UUID;

public class PacketPlayInSpectate
        extends WrappedPacket {
    private UUID entityUUID;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.Spectate.newPacket(this.entityUUID);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        this.entityUUID = (UUID) PacketInType.Spectate.getPacketData(packet)[0];
    }
}

