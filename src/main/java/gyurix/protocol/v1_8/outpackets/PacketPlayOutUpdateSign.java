package gyurix.protocol.v1_8.outpackets;

import gyurix.chat.ChatTag;
import gyurix.protocol.PacketOutType;
import gyurix.protocol.WrappedPacket;
import gyurix.protocol.utils.BlockLocation;
import gyurix.spigotlib.ChatAPI;

import java.lang.reflect.Array;

public class PacketPlayOutUpdateSign extends WrappedPacket {
    public BlockLocation block;
    public ChatTag[] lines;

    @Override
    public Object getVanillaPacket() {
        Object[] lines = (Object[]) Array.newInstance(ChatAPI.icbcClass, 4);
        for (int i = 0; i < 4; ++i) {
            lines[i] = this.lines[i].toICBC();
        }
        return PacketOutType.UpdateSign.newPacket(null, block.toVanillaBlockPosition(), lines);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketOutType.UpdateSign.getPacketData(packet);
        block = new BlockLocation(data[1]);
        lines = new ChatTag[4];
        Object[] packetLines = (Object[]) data[2];
        for (int i = 0; i < 4; ++i) {
            lines[i] = ChatTag.fromICBC(packetLines[i]);
        }
    }
}

