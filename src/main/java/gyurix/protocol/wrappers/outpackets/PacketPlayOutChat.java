package gyurix.protocol.wrappers.outpackets;

import gyurix.chat.ChatTag;
import gyurix.json.JsonAPI;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.ChatAPI;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Created by gyurix on 25/11/2015.
 */
public class PacketPlayOutChat extends WrappedPacket {
    public ChatTag tag;
    /**
     * The type of this chat message 0: chat (chat box) 1: system message (chat box) 2: action bar
     */
    public byte type;


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
            tag = JsonAPI.deserialize(ChatAPI.toJson(data[0]), ChatTag.class);
        type = (byte) data[2];
    }
}
