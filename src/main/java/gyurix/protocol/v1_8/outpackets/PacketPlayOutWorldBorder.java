package gyurix.protocol.v1_8.outpackets;

import java.lang.reflect.Method;

import gyurix.protocol.PacketOutType;
import gyurix.protocol.Reflection;
import gyurix.protocol.WrappedPacket;

public class PacketPlayOutWorldBorder extends WrappedPacket {
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
        return PacketOutType.WorldBorder.newPacket(action.toVanillaPlayerAction(),portalTeleportBoundary,centerX,centerZ,newRadius,oldRadius,time,warningTime,warningBlocks);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data=PacketOutType.WorldBorder.getPacketData(packet);
        action=WorldBorderAction.valueOf(data[0].toString());
        portalTeleportBoundary=(Integer)data[1];
        centerX=(Double)data[2];
        centerZ=(Double)data[3];
        newRadius=(Double)data[4];
        oldRadius=(Double)data[5];
        time=(Long)data[6];
        warningTime=(Integer)data[7];
        warningBlocks=(Integer)data[8];
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
            valueOf = Reflection.getMethod(Reflection.getNMSClass("PacketPlayOutWorldBorder$EnumWorldBorderAction"), "valueOf");
        }

        public Object toVanillaPlayerAction() {
            try {
                return valueOf.invoke(null, name());
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
