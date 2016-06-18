package gyurix.api;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.spigotlib.ChatAPI;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.NullUtils;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * An API used for title management
 */
public class TitleAPI {
    public static Object[] enums;

    public static void clear(Collection<? extends Player> plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[3], null);
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void clear(Player... plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[3], null);
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void init() {
        for (Class c : Reflection.getNMSClass("PacketPlayOutTitle").getClasses()) {
            if (!c.getName().endsWith("EnumTitleAction")) continue;
            enums = c.getEnumConstants();
            return;
        }
    }

    public static void reset(Collection<? extends Player> plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[4], null);
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void reset(Player... plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[4], null);
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void set(String title, String subtitle, int fadeIn, int showtime, int fadeOut, Collection<? extends Player> plrs) {
        setShowTime(fadeIn, showtime, fadeOut, plrs);
        setSubTitle(NullUtils.to0(subtitle), plrs);
        setTitle(title, plrs);
    }

    public static void set(String title, String subtitle, int fadeIn, int showtime, int fadeOut, Player... plrs) {
        setShowTime(fadeIn, showtime, fadeOut, plrs);
        setSubTitle(NullUtils.to0(subtitle), plrs);
        setTitle(title, plrs);
    }

    public static void setShowTime(int fadein, int show, int fadeout, Collection<? extends Player> plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[2], null, fadein, show, fadeout);
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void setShowTime(int fadein, int show, int fadeout, Player... plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[2], null, fadein, show, fadeout);
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

    public static void setSubTitle(String subtitle, Player... plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[1], ChatAPI.toICBC(ChatAPI.TextToJson(subtitle)));
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void setTitle(String title, Collection<? extends Player> plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[0], ChatAPI.toICBC(ChatAPI.TextToJson(title)));
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void setTitle(String title, Player... plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[0], ChatAPI.toICBC(ChatAPI.TextToJson(title)));
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }
}

