package gyurix.api;

import gyurix.chat.ChatTag;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutTitle;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutTitle.TitleAction;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.NullUtils;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * An API used for title management
 */
public class TitleAPI {

    public static void clear(Collection<? extends Player> plrs) {
        Object packet = new PacketPlayOutTitle(TitleAction.CLEAR, null, 0, 0, 0).getVanillaPacket();
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void clear(Player... plrs) {
        Object packet = new PacketPlayOutTitle(TitleAction.CLEAR, null, 0, 0, 0).getVanillaPacket();
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void reset(Collection<? extends Player> plrs) {
        Object packet = new PacketPlayOutTitle(TitleAction.RESET, null, 0, 0, 0).getVanillaPacket();
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void reset(Player... plrs) {
        Object packet = new PacketPlayOutTitle(TitleAction.RESET, null, 0, 0, 0).getVanillaPacket();
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void set(String title, String subtitle, int fadeIn, int showtime, int fadeOut, Collection<? extends Player> plrs) {
        setShowTime(fadeIn, showtime, fadeOut, plrs);
        setSubTitle(NullUtils.to0(subtitle), plrs);
        setTitle(title, plrs);
    }

    public static void setShowTime(int fadein, int show, int fadeout, Collection<? extends Player> plrs) {
        Object packet = new PacketPlayOutTitle(TitleAction.TIMES, null, fadein, show, fadeout).getVanillaPacket();
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void setSubTitle(String subtitle, Collection<? extends Player> plrs) {
        Object packet = new PacketPlayOutTitle(TitleAction.SUBTITLE, ChatTag.fromColoredText(subtitle), 0, 0, 0).getVanillaPacket();
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void setTitle(String title, Collection<? extends Player> plrs) {
        Object packet = new PacketPlayOutTitle(TitleAction.TITLE, ChatTag.fromColoredText(title), 0, 0, 0).getVanillaPacket();
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void set(String title, String subtitle, int fadeIn, int showtime, int fadeOut, Player... plrs) {
        setShowTime(fadeIn, showtime, fadeOut, plrs);
        setSubTitle(NullUtils.to0(subtitle), plrs);
        setTitle(title, plrs);
    }

    public static void setShowTime(int fadein, int show, int fadeout, Player... plrs) {
        Object packet = new PacketPlayOutTitle(TitleAction.TIMES, null, fadein, show, fadeout).getVanillaPacket();
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void setSubTitle(String subtitle, Player... plrs) {
        Object packet = new PacketPlayOutTitle(TitleAction.SUBTITLE, ChatTag.fromColoredText(subtitle), 0, 0, 0).getVanillaPacket();
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void setTitle(String title, Player... plrs) {
        Object packet = new PacketPlayOutTitle(TitleAction.TITLE, ChatTag.fromColoredText(title), 0, 0, 0).getVanillaPacket();
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }
}

