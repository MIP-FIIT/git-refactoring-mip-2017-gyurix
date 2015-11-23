package gyurix.api;

import gyurix.protocol.PacketOutType;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.ChatAPI;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.Collection;

public class TitleAPI {
    public static Object[] enums;

    public static void init() {
        for (Class c : Reflection.getNMSClass("PacketPlayOutTitle").getClasses()) {
            if (!c.getName().endsWith("EnumTitleAction")) continue;
            enums = c.getEnumConstants();
            return;
        }
    }

    public static void setTitle(String title, Collection<? extends Player> plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[0], ChatAPI.toICBC(ChatAPI.TextToJson(title)));
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void setSubTitle(String subtitle, Collection<? extends Player> plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[1], ChatAPI.toICBC(ChatAPI.TextToJson(subtitle)));
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void setShowTime(int fadein, int show, int fadeout, Collection<? extends Player> plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[2], null, fadein, show, fadeout);
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void clear(Collection<? extends Player> plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[3], null);
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void reset(Collection<? extends Player> plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[4], null);
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static /* varargs */ void setTitle(String title, Player... plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[0], ChatAPI.toICBC(ChatAPI.TextToJson(title)));
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static /* varargs */ void setSubTitle(String subtitle, Player... plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[1], ChatAPI.toICBC(ChatAPI.TextToJson(subtitle)));
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static /* varargs */ void setShowTime(int fadein, int show, int fadeout, Player... plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[2], null, fadein, show, fadeout);
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static /* varargs */ void clear(Player... plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[3], null);
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static /* varargs */ void reset(Player... plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[4], null);
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }
}

