package gyurix.protocol.v1_8.outpackets;

import gyurix.protocol.PacketOutType;
import gyurix.protocol.Reflection;
import gyurix.protocol.WrappedPacket;

import java.lang.reflect.Method;

public class PacketPlayOutWorldBorder
        extends WrappedPacket {
    public WorldBorderAction action;
    public int portalTeleportBoundary;
    public double centerX;
    public double centerZ;
    public double newRadius;
    public double oldRadius;
    public long time;
    public int warningBlocks;
    public int warningTime;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.WorldBorder.newPacket(this.action.toVanillaPlayerAction(), this.portalTeleportBoundary, this.centerX, this.centerZ, this.newRadius, this.oldRadius, this.time, this.warningTime, this.warningBlocks);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketOutType.WorldBorder.getPacketData(packet);
        this.action = WorldBorderAction.valueOf(data[0].toString());
        this.portalTeleportBoundary = (Integer) data[1];
        this.centerX = (Double) data[2];
        this.centerZ = (Double) data[3];
        this.newRadius = (Double) data[4];
        this.oldRadius = (Double) data[5];
        this.time = (Long) data[6];
        this.warningTime = (Integer) data[7];
        this.warningBlocks = (Integer) data[8];
    }

    public enum WorldBorderAction {
        SET_SIZE,
        LERP_SIZE,
        SET_CENTER,
        INITIALIZE,
        SET_WARNING_TIME,
        SET_WARNING_BLOCKS;

        private static final Method valueOf;

        static {
            valueOf = Reflection.getMethod(Reflection.getNMSClass("PacketPlayOutWorldBorder$EnumWorldBorderAction"), "valueOf", String.class);
        }

        WorldBorderAction() {
        }

        public Object toVanillaPlayerAction() {
            try {
                return valueOf.invoke(null, this.name());
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}

