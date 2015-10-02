package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.Reflection;
import gyurix.protocol.WrappedPacket;

import java.lang.reflect.Method;

public class PacketPlayInSettings extends WrappedPacket {
    public String locale;
    public int viewDistance;
    public ChatVisibility chatVisibility;
    public boolean chatColors;
    public int skinParts;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.Settings.newPacket(locale, viewDistance, chatVisibility.toVanillaChatVisibility(), chatColors, skinParts);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.Settings.getPacketData(packet);
        locale = (String) data[0];
        viewDistance = (Integer) data[1];
        chatVisibility = ChatVisibility.valueOf(data[2].toString());
        chatColors = (Boolean) data[3];
        skinParts = (Integer) data[4];
    }

    public enum ChatVisibility {
        FULL, SYSTEM, HIDDEN;
        private static final Method valueOf;

        static {
            valueOf = Reflection.getMethod(Reflection.getNMSClass("EntityHuman$EnumChatVisibility"), "valueOf");
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