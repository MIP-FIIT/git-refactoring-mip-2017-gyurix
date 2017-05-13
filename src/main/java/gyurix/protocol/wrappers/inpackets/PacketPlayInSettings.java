package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;

import java.lang.reflect.Method;

public class PacketPlayInSettings extends WrappedPacket {
    public boolean chatColors;
    public ChatVisibility chatVisibility;
    public String locale;
    public int skinParts;
    public int viewDistance;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.Settings.newPacket(locale, viewDistance, chatVisibility.toVanillaChatVisibility(), chatColors, skinParts);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.Settings.getPacketData(packet);
        locale = (String) data[0];
        viewDistance = (Integer) data[1];
        chatVisibility = data[2] == null ? ChatVisibility.FULL : ChatVisibility.valueOf(data[2].toString());
        chatColors = (Boolean) data[3];
        skinParts = (Integer) data[4];
    }

    public enum ChatVisibility {
        FULL,
        SYSTEM,
        HIDDEN;

        private static final Method valueOf;

        static {
            valueOf = Reflection.getMethod(Reflection.getNMSClass("EntityHuman$EnumChatVisibility"), "valueOf", String.class);
        }

        ChatVisibility() {
        }

        public Object toVanillaChatVisibility() {
            try {
                return valueOf.invoke(null, name());
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}

