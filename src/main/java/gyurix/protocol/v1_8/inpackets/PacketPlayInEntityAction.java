package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.Reflection;
import gyurix.protocol.WrappedPacket;

import java.lang.reflect.Method;

public class PacketPlayInEntityAction extends WrappedPacket{
    public int entityId,jumpBoost;
    public PlayerAction action;
    @Override
    public Object getVanillaPacket() {
        return PacketInType.EntityAction.newPacket(entityId,action.toVanillaPlayerAction(),jumpBoost);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data=PacketInType.EntityAction.getPacketData(packet);
        entityId=(Integer)data[0];
        action=(PlayerAction)data[1];
        jumpBoost=(Integer)data[2];
    }
    public enum PlayerAction {
        START_SNEAKING,
        STOP_SNEAKING,
        STOP_SLEEPING,
        START_SPRINTING,
        STOP_SPRINTING,
        RIDING_JUMP,
        OPEN_INVENTORY;
        private static final Method valueOf;

        static {
            valueOf = Reflection.getMethod(Reflection.getNMSClass("PacketPlayInEntityAction$EnumPlayerAction"), "valueOf");
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