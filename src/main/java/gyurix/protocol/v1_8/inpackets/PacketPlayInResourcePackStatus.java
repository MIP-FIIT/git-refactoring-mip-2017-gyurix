package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.Reflection;
import gyurix.protocol.WrappedPacket;

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
        this.status = (ResourcePackStatus) data[1];
    }

    public enum ResourcePackStatus {
        SUCCESSFULLY_LOADED,
        DECLINED,
        FAILED_DOWNLOAD,
        ACCEPTED;

        private static final Method valueOf;

        static {
            valueOf = Reflection.getMethod(Reflection.getNMSClass("PacketPlayInResourcePackStatus$EnumPlayerAction"), "valueOf", String.class);
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

