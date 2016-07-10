package gyurix.map;

import gyurix.protocol.Protocol.PacketOutListener;
import gyurix.protocol.event.PacketOutEvent;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutMap;

/**
 * Created by GyuriX on 2016. 07. 07..
 */
public class MapPacketCanceler implements PacketOutListener {
    @Override
    public void onPacketOUT (PacketOutEvent e) {
        PacketPlayOutMap p = new PacketPlayOutMap();
        p.loadVanillaPacket(e.getPacket());
        if (p.mapId == 1 && (p.x != 128 || p.z != 128))
            e.setCancelled(true);
    }
}
