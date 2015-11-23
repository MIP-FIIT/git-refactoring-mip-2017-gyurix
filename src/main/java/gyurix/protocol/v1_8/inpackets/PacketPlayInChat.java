package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;

public class PacketPlayInChat
        extends WrappedPacket {
    public String message;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.Chat.newPacket(this.message);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        this.message = (String) PacketInType.Chat.getPacketData(packet)[0];
    }
}

