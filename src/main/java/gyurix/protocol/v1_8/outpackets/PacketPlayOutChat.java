package gyurix.protocol.v1_8.outpackets;

import gyurix.chat.ChatTag;
import gyurix.json.JsonAPI;
import gyurix.protocol.PacketOutType;
import gyurix.protocol.WrappedPacket;
import gyurix.spigotlib.ChatAPI;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Created by gyurix on 25/11/2015.
 */
public class PacketPlayOutChat extends WrappedPacket {
    public ChatTag tag;
    public byte type;

    public PacketPlayOutChat() {
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.Chat.newPacket(ChatAPI.toICBC(tag.toString()), null, type);
    }

    @Override
    public void loadVanillaPacket(Object obj) {
        Object[] data = PacketOutType.Chat.getPacketData(obj);
        if (data[1] != null) {
            tag = ChatTag.fromBaseComponents((BaseComponent[]) data[1]);
        } else
            tag = (ChatTag) JsonAPI.deserialize(ChatAPI.toJson(data[0]), ChatTag.class);
        type = (byte) data[2];
    }
}
