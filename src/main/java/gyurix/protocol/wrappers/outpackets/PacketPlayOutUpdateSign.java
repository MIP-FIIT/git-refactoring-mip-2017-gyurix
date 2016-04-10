package gyurix.protocol.wrappers.outpackets;

import gyurix.chat.ChatTag;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.wrappers.WrappedPacket;
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
        return PacketOutType.UpdateSign.newPacket(null, block.toNMS(), lines);
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

