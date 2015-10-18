package gyurix.protocol.v1_8.outpackets;

import gyurix.protocol.PacketOutType;
import gyurix.protocol.WrappedPacket;
import gyurix.spigotlib.ChatAPI;

/**
 * Created by GyuriX on 2015.10.04..
 */
public class PacketPlayOutOpenWindow extends WrappedPacket{
    public int id;
    public String type;
    public String title;
    public int slots;
    public int entityId;
    @Override
    public Object getVanillaPacket() {
        return PacketOutType.OpenWindow.newPacket(id,type, ChatAPI.toICBC(title), slots ,entityId);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] o=PacketOutType.OpenWindow.getPacketData(packet);
        id=(Integer)o[0];
        type=(String)o[1];
        title=ChatAPI.toJson(o[2]);
        slots=(Integer)o[3];
        entityId=(Integer)o[4];
    }
}
