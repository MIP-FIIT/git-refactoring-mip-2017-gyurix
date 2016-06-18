package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.event.PacketInType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.utils.Direction;
import gyurix.protocol.utils.ItemStackWrapper;
import gyurix.protocol.wrappers.WrappedPacket;

public class PacketPlayInBlockPlace
        extends WrappedPacket {
    public float cursorX;
    public float cursorY;
    public float cursorZ;
    public Direction face;
    public ItemStackWrapper itemStack;
    public BlockLocation location;
    public long timestamp;

    @Override
    public Object getVanillaPacket() {
        Object[] arrobject = new Object[7];
        arrobject[0] = location.toNMS();
        arrobject[1] = face == null ? 255 : face.ordinal();
        arrobject[2] = itemStack == null ? null : itemStack.toNMS();
        arrobject[3] = Float.valueOf(cursorX);
        arrobject[4] = Float.valueOf(cursorY);
        arrobject[5] = Float.valueOf(cursorZ);
        arrobject[6] = timestamp;
        return PacketInType.BlockPlace.newPacket(arrobject);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.BlockPlace.getPacketData(packet);
        location = new BlockLocation(data[0]);
        face = Direction.get((Integer) data[1]);
        itemStack = data[2] == null ? null : new ItemStackWrapper(data[2]);
        cursorX = ((Float) data[3]).floatValue();
        cursorY = ((Float) data[4]).floatValue();
        cursorZ = ((Float) data[5]).floatValue();
        timestamp = (Long) data[6];
    }
}

