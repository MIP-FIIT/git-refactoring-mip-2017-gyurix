package gyurix.protocol.v1_8.inpackets;

import gyurix.chat.ChatTag;
import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;
import gyurix.protocol.utils.BlockLocation;
import gyurix.spigotlib.ChatAPI;

import java.lang.reflect.Array;

public class PacketPlayInUpdateSign extends WrappedPacket {
    public BlockLocation block;
    public ChatTag[] lines;

    @Override
    public Object getVanillaPacket() {
        Object[] lines = (Object[]) Array.newInstance(ChatAPI.icbcClass, 4);
        for (int i = 0; i < 4; ++i) {
            lines[i] = this.lines[i].toICBC();
        }
        return PacketInType.UpdateSign.newPacket(this.block.toVanillaBlockPosition(), lines);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.UpdateSign.getPacketData(packet);
        block = new BlockLocation(data[0]);
        lines = new ChatTag[4];
        Object[] packetLines = (Object[]) data[1];
        for (int i = 0; i < 4; ++i) {
            lines[i] = ChatTag.fromICBC(packetLines[i]);
        }
    }
}

