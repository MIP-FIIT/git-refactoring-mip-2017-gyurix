package gyurix.protocol.v1_8.outpackets;

import gyurix.protocol.PacketOutType;
import gyurix.protocol.WrappedPacket;
import gyurix.spigotlib.ChatAPI;

public class PacketPlayOutOpenWindow
        extends WrappedPacket {
    public int id;
    public String type;
    public String title;
    public int slots;
    public int entityId;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.OpenWindow.newPacket(this.id, this.type, ChatAPI.toICBC(this.title), this.slots, this.entityId);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] o = PacketOutType.OpenWindow.getPacketData(packet);
        this.id = (Integer) o[0];
        this.type = (String) o[1];
        this.title = ChatAPI.toJson(o[2]);
        this.slots = (Integer) o[3];
        this.entityId = (Integer) o[4];
    }
}

