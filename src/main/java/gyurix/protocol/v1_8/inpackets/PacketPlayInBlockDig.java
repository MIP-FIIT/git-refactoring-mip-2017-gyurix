package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.Reflection;
import gyurix.protocol.WrappedPacket;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.utils.Direction;

import java.lang.reflect.Method;

public class PacketPlayInBlockDig extends WrappedPacket {
    public BlockLocation block;
    public Direction direction;
    public DigType digType;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.BlockDig.newPacket(block.toVanillaBlockPosition(), direction.toVanillaDirection(), digType.toVanillaDigType());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.BlockDig.getPacketData(packet);
        block = new BlockLocation(data[0]);
        direction = Direction.valueOf(data[1].toString());
        digType = DigType.valueOf(data[2].toString());
    }

    public enum DigType {
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM;
        private static final Method valueOf;

        static {
            valueOf = Reflection.getMethod(Reflection.getNMSClass("PacketPlayInBlockDig$EnumPlayerDigType"), "valueOf",String.class);
        }

        public Object toVanillaDigType() {
            try {
                return valueOf.invoke(null, name());
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}