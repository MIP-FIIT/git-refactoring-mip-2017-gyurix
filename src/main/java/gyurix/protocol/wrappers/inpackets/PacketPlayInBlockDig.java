package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.utils.Direction;
import gyurix.protocol.wrappers.WrappedPacket;

import java.lang.reflect.Method;

public class PacketPlayInBlockDig extends WrappedPacket {
    public BlockLocation block;
    public DigType digType;
    public Direction direction;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.BlockDig.newPacket(block.toNMS(), direction == null ? null : direction.toNMS(), digType.toVanillaDigType());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.BlockDig.getPacketData(packet);
        block = new BlockLocation(data[0]);
        direction = data[1] == null ? null : Direction.valueOf(data[1].toString().toUpperCase());
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
            valueOf = Reflection.getMethod(Reflection.getNMSClass("PacketPlayInBlockDig$EnumPlayerDigType"), "valueOf", String.class);
        }

        DigType() {
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

