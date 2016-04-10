package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;

import java.lang.reflect.Method;

public class PacketPlayInResourcePackStatus
        extends WrappedPacket {
    public String hash;
    public ResourcePackStatus status;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.ResourcePackStatus.newPacket(this.hash, this.status.toVanillaRPStatus());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.ResourcePackStatus.getPacketData(packet);
        this.hash = (String) data[0];
        this.status = ResourcePackStatus.valueOf(data[1].toString());
    }

    public enum ResourcePackStatus {
        SUCCESSFULLY_LOADED,
        DECLINED,
        FAILED_DOWNLOAD,
        ACCEPTED;

        private static final Method valueOf;

        static {
            valueOf = Reflection.getMethod(Reflection.getNMSClass("PacketPlayInResourcePackStatus$EnumResourcePackStatus"), "valueOf", String.class);
        }

        ResourcePackStatus() {
        }

        public Object toVanillaRPStatus() {
            try {
                return valueOf.invoke(null, this.name());
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}

