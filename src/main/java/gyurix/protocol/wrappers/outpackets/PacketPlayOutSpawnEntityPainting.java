package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.utils.Direction;
import gyurix.protocol.wrappers.WrappedPacket;

import java.util.UUID;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutSpawnEntityPainting extends WrappedPacket {
    public int entityId;
    public String title;
    public UUID entityUUID;
    public Direction facing;
    public BlockLocation location;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.SpawnEntityPainting.newPacket(entityId, entityUUID, location.toNMS(), facing.toNMS(), title);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.SpawnEntityPainting.getPacketData(packet);
        entityId = (int) d[0];
        entityUUID = (UUID) d[1];
        location = new BlockLocation(d[2]);
        facing = Direction.valueOf(d[3].toString());
        title = (String) d[4];
    }
}
