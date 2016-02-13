package gyurix.protocol.v1_8.outpackets;

import gyurix.chat.ChatTag;
import gyurix.protocol.Reflection;
import gyurix.protocol.WrappedPacket;
import gyurix.protocol.utils.GameProfile;
import org.bukkit.GameMode;

import java.lang.reflect.Method;

public class PacketPlayOutPlayerInfoBak extends WrappedPacket {
    PlayerInfoAction action;

    @Override
    public Object getVanillaPacket() {
        return null;
    }

    @Override
    public void loadVanillaPacket(Object var1) {

    }

    public enum PlayerInfoAction {
        ADD_PLAYER,
        UPDATE_GAME_MODE,
        UPDATE_LATENCY,
        UPDATE_DISPLAY_NAME,
        REMOVE_PLAYER;
        private static final Method valueOf;

        static {
            valueOf = Reflection.getMethod(Reflection.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction"), "valueOf", String.class);
        }

        public Object getVanillaAction() {
            try {
                return valueOf.invoke(null, name());
            } catch (Throwable e) {
                return null;
            }
        }
    }

    public static class PlayerInfoData {
        public int ping;
        public GameMode gameMode;
        public GameProfile gameProfile;
        public ChatTag displayName;
    }
}
