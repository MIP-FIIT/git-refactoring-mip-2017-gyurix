package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.WrappedData;
import gyurix.protocol.wrappers.WrappedPacket;

import java.lang.reflect.Method;

public class PacketPlayOutWorldBorder
        extends WrappedPacket {
    public WorldBorderAction action;
    public double centerX;
    public double centerZ;
    public double newRadius;
    public double oldRadius;
    public int portalTeleportBoundary;
    public long time;
    public int warningBlocks;
    public int warningTime;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.WorldBorder.newPacket(action.toNMS(), portalTeleportBoundary, centerX, centerZ, newRadius, oldRadius, time, warningTime, warningBlocks);
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

    public enum WorldBorderAction implements WrappedData {
        SET_SIZE,
        LERP_SIZE,
        SET_CENTER,
        INITIALIZE,
        SET_WARNING_TIME,
        SET_WARNING_BLOCKS;

        private static final Method valueOf = Reflection.getMethod(Reflection.getNMSClass("PacketPlayOutWorldBorder$EnumWorldBorderAction"), "valueOf", String.class);

        public Object toNMS() {
            try {
                return valueOf.invoke(null, this.name());
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}

