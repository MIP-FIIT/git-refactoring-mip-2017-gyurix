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
        arrobject[0] = this.location.toNMS();
        arrobject[1] = this.face == null ? 255 : this.face.ordinal();
        arrobject[2] = this.itemStack == null ? null : this.itemStack.toNMS();
        arrobject[3] = Float.valueOf(this.cursorX);
        arrobject[4] = Float.valueOf(this.cursorY);
        arrobject[5] = Float.valueOf(this.cursorZ);
        arrobject[6] = this.timestamp;
        return PacketInType.BlockPlace.newPacket(arrobject);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.BlockPlace.getPacketData(packet);
        this.location = new BlockLocation(data[0]);
        this.face = Direction.get((Integer) data[1]);
        this.itemStack = data[2] == null ? null : new ItemStackWrapper(data[2]);
        this.cursorX = ((Float) data[3]).floatValue();
        this.cursorY = ((Float) data[4]).floatValue();
        this.cursorZ = ((Float) data[5]).floatValue();
        this.timestamp = (Long) data[6];
    }
}

