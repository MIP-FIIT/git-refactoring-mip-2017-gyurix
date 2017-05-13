package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.utils.Direction;
import gyurix.protocol.utils.HandType;
import gyurix.protocol.utils.ItemStackWrapper;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.ServerVersion;

public class PacketPlayInBlockPlace extends WrappedPacket {
    public float cursorX;
    public float cursorY;
    public float cursorZ;
    public Direction face;
    public HandType hand;
    public ItemStackWrapper itemStack;
    public BlockLocation location;
    public long timestamp;

    @Override
    public Object getVanillaPacket() {
        Object[] d;
        if (Reflection.ver.isBellow(ServerVersion.v1_8)) {
            d = new Object[7];
            d[0] = location.toNMS();
            d[1] = face == null ? 255 : face.ordinal();
            d[2] = itemStack == null ? null : itemStack.toNMS();
            d[3] = cursorX;
            d[4] = cursorY;
            d[5] = cursorZ;
            d[6] = timestamp;
        } else {
            d = new Object[2];
            d[0] = hand.toNMS();
            d[1] = timestamp;
        }
        return PacketInType.BlockPlace.newPacket(d);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.BlockPlace.getPacketData(packet);
        if (Reflection.ver.isBellow(ServerVersion.v1_8)) {
            location = new BlockLocation(data[0]);
            face = Direction.get((Integer) data[1]);
            itemStack = data[2] == null ? null : new ItemStackWrapper(data[2]);
            cursorX = (Float) data[3];
            cursorY = (Float) data[4];
            cursorZ = (Float) data[5];
            timestamp = (Long) data[6];
        } else {
            hand = HandType.valueOf(data[0].toString());
            timestamp = (Long) data[1];
        }
    }
}

