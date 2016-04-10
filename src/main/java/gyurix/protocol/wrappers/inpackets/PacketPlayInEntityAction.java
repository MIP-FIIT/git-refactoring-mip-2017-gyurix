package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;

import java.lang.reflect.Method;

public class PacketPlayInEntityAction
        extends WrappedPacket {
    public PlayerAction action;
    public int entityId;
    public int jumpBoost;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.EntityAction.newPacket(this.entityId, this.action.toVanillaPlayerAction(), this.jumpBoost);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.EntityAction.getPacketData(packet);
        this.entityId = (Integer) data[0];
        this.action = (PlayerAction) data[1];
        this.jumpBoost = (Integer) data[2];
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
            valueOf = Reflection.getMethod(Reflection.getNMSClass("PacketPlayInEntityAction$EnumPlayerAction"), "valueOf", String.class);
        }

        PlayerAction() {
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

