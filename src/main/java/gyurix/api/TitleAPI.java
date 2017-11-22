package gyurix.api;

import gyurix.chat.ChatTag;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutTitle;
import gyurix.spigotutils.NullUtils;
import org.bukkit.entity.Player;

import java.util.Collection;

import static gyurix.protocol.wrappers.outpackets.PacketPlayOutTitle.TitleAction.*;
import static gyurix.spigotlib.SU.tp;

/**
 * TitleAPI - API for managing title, subtitle and actionbar messages for server versions 1.8.8 and later
 */
public class TitleAPI {

    /**
     * Clears the title message of the given collection of players
     *
     * @param players - The collection of players whose title bar should be cleared
     */
    public static void clear(Collection<? extends Player> players) {
        Object packet = new PacketPlayOutTitle(CLEAR, null, 0, 0, 0).getVanillaPacket();
        players.forEach((p) -> tp.sendPacket(p, packet));
    }

    /**
     * Clears the title message of the given players
     *
     * @param players - The players whose title bar should be cleared
     */
    public static void clear(Player... players) {
        Object packet = new PacketPlayOutTitle(CLEAR, null, 0, 0, 0).getVanillaPacket();
        for (Player p : players)
            tp.sendPacket(p, packet);
    }

    /**
     * Resets the title message of the given collection of players
     *
     * @param players - The players whose title bar should be reset
     */
    public static void reset(Collection<? extends Player> players) {
        Object packet = new PacketPlayOutTitle(RESET, null, 0, 0, 0).getVanillaPacket();
        players.forEach((p) -> tp.sendPacket(p, packet));
    }

    /**
     * Resets the title message of the given players
     *
     * @param players - The players whose title bar should be reset
     */
    public static void reset(Player... players) {
        Object packet = new PacketPlayOutTitle(RESET, null, 0, 0, 0).getVanillaPacket();
        for (Player p : players)
            tp.sendPacket(p, packet);
    }

    /**
     * Sends a title message to the given collection of players
     *
     * @param title    - Title text of the title message
     * @param subtitle - Subtitle text of the title message
     * @param fadeIn   - Fade in time in ticks (1 tick = 0.05 sec)
     * @param showtime - Show time in ticks
     * @param fadeOut  - Fade out time in ticks
     * @param players  - Receivers of the title message
     */
    public static void set(String title, String subtitle, int fadeIn, int showtime, int fadeOut, Collection<? extends Player> players) {
        setShowTime(fadeIn, showtime, fadeOut, players);
        setSubTitle(NullUtils.to0(subtitle), players);
        setTitle(title, players);
    }

    /**
     * Sends a title message to the given players
     *
     * @param title    - Title text of the title message
     * @param subtitle - Subtitle text of the title message
     * @param fadeIn   - Fade in time in ticks (1 tick = 0.05 sec)
     * @param showtime - Show time in ticks
     * @param fadeOut  - Fade out time in ticks
     * @param players  - Receivers of the title message
     */
    public static void set(String title, String subtitle, int fadeIn, int showtime, int fadeOut, Player... players) {
        setShowTime(fadeIn, showtime, fadeOut, players);
        setSubTitle(NullUtils.to0(subtitle), players);
        setTitle(title, players);
    }

    /**
     * @param fadein
     * @param show
     * @param fadeout
     * @param players
     */
    public static void setShowTime(int fadein, int show, int fadeout, Collection<? extends Player> players) {
        Object packet = new PacketPlayOutTitle(TIMES, null, fadein, show, fadeout).getVanillaPacket();
        for (Player p : players)
            tp.sendPacket(p, packet);
    }

    /**
     * @param fadein
     * @param show
     * @param fadeout
     * @param players
     */
    public static void setShowTime(int fadein, int show, int fadeout, Player... players) {
        Object packet = new PacketPlayOutTitle(TIMES, null, fadein, show, fadeout).getVanillaPacket();
        for (Player p : players)
            tp.sendPacket(p, packet);
    }

    /**
     * @param subtitle
     * @param players
     */
    public static void setSubTitle(String subtitle, Collection<? extends Player> players) {
        Object packet = new PacketPlayOutTitle(SUBTITLE, ChatTag.fromColoredText(subtitle), 0, 0, 0).getVanillaPacket();
        for (Player p : players)
            tp.sendPacket(p, packet);
    }

    /**
     * @param subtitle
     * @param players
     */
    public static void setSubTitle(String subtitle, Player... players) {
        Object packet = new PacketPlayOutTitle(SUBTITLE, ChatTag.fromColoredText(subtitle), 0, 0, 0).getVanillaPacket();
        for (Player p : players)
            tp.sendPacket(p, packet);
    }

    /**
     * @param title
     * @param players
     */
    public static void setTitle(String title, Collection<? extends Player> players) {
        Object packet = new PacketPlayOutTitle(TITLE, ChatTag.fromColoredText(title), 0, 0, 0).getVanillaPacket();
        players.forEach((p) -> tp.sendPacket(p, packet));
    }

    /**
     * @param title
     * @param players
     */
    public static void setTitle(String title, Player... players) {
        Object packet = new PacketPlayOutTitle(TITLE, ChatTag.fromColoredText(title), 0, 0, 0).getVanillaPacket();
        for (Player p : players)
            tp.sendPacket(p, packet);
    }
}

