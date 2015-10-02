package gyurix.api;

import gyurix.protocol.PacketOutType;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.ChatAPI;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * @author GyuriX
 *         <p></p>
 *         An API for managing per player Title and Subtitle messages.
 *         <p></p>
 *         The correct method usage order for setting the title and the subtitle is the following:
 *         1. setShowTime
 *         2. setSubTitle
 *         3. setTitle
 *         The set title method will actually make the title and the subtitle visible for the target players.
 */
public class TitleAPI {
    /**
     * The array of the NMS EnumTitleAction enum values
     */
    public static Object[] enums;

    /**
     * This method is just for initializing the NMS EnumTitleAction values.
     * You should NEVER use this method.
     */
    public static void init() {
        for (Class c : Reflection.getNMSClass("PacketPlayOutTitle").getClasses())
            if (c.getName().endsWith("EnumTitleAction")) {
                enums = c.getEnumConstants();
                return;
            }
    }

    /**
     *
     * Set and show the title message
     *
     * @param title title message, which will be shown for the given players
     * @param plrs  collection of the target players, who will see the new title message with the
     *              previously set title showtimes and subtitle
     *              <p></p>
     *              You can use an empty string for the title if you would like to just show the subtitle for the given players.
     */
    public static void setTitle(String title, Collection<? extends Player> plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[0], ChatAPI.toICBC(ChatAPI.TextToJson(title)));
        for (Player p : plrs)
            SU.tp.sendPacket(p, packet);
    }

    /**
     *
     * Set the subtitle message of the target players.
     *
     * @param subtitle the new subtitle setting
     * @param plrs     collection of the target players, who will receive this subtitle update. They will only
     *                 see the new subtitle, if they can currently see the title, or at the next time, when they will see the title.
     */
    public static void setSubTitle(String subtitle, Collection<? extends Player> plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[1], ChatAPI.toICBC(ChatAPI.TextToJson(subtitle)));
        for (Player p : plrs)
            SU.tp.sendPacket(p, packet);
    }

    /**
     *
     * Set the title timings of the target players.
     *
     * @param fadein  time in ticks (1/20 seconds) before showing the title without transparency
     * @param show    time in ticks (1/20 seconds) while the title is shown without transparency
     * @param fadeout time in ticks (1/20 seconds) while the title will be more and more transparent and finally disappear
     * @param plrs    collection of the target players, who will receive these settings, you need to
     *                use this method BEFORE showing the title using the setTitle method
     */
    public static void setShowTime(int fadein, int show, int fadeout, Collection<? extends Player> plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[2], null, fadein, show, fadeout);
        for (Player p : plrs)
            SU.tp.sendPacket(p, packet);
    }

    /**
     * A method for clearing(hiding) the title for the target players.
     * If this method is used while the title is not shown, the title might appear.
     *
     * @param plrs collection of the target players
     */
    public static void clear(Collection<? extends Player> plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[3], null);
        for (Player p : plrs)
            SU.tp.sendPacket(p, packet);
    }

    /**
     * A method for removing the previously set title settings, subtitles and titles.
     *
     * @param plrs collection of the target players
     */
    public static void reset(Collection<? extends Player> plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[4], null);
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    /**
     * @param title title message, which will be shown for the given players
     * @param plrs  target players, who will see the new title message with the
     *              previously set title showtimes and subtitle
     *              <p></p>
     *              You can use an empty string for the title if you would like to just show the subtitle for the given players.
     */
    public static void setTitle(String title, Player... plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[0], ChatAPI.toICBC(ChatAPI.TextToJson(title)));
        for (Player p : plrs)
            SU.tp.sendPacket(p, packet);
    }

    /**
     * @param subtitle the new subtitle setting
     * @param plrs     target players, who will receive this subtitle update. They will only
     *                 see the new subtitle, if they can currently see the title, or at the next time, when they will see the title.
     */
    public static void setSubTitle(String subtitle, Player... plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[1], ChatAPI.toICBC(ChatAPI.TextToJson(subtitle)));
        for (Player p : plrs)
            SU.tp.sendPacket(p, packet);
    }

    /**
     * @param fadein  time in ticks (1/20 seconds) before showing the title without transparency
     * @param show    time in ticks (1/20 seconds) while the title is shown without transparency
     * @param fadeout time in ticks (1/20 seconds) while the title will be more and more transparent and finally disappear
     * @param plrs    target players, who will receive these settings, you need to
     *                use this method BEFORE showing the title using the setTitle method
     */
    public static void setShowTime(int fadein, int show, int fadeout, Player... plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[2], null, fadein, show, fadeout);
        for (Player p : plrs)
            SU.tp.sendPacket(p, packet);
    }

    /**
     * A method for clearing(hiding) the title for the target players.
     * If this method is used while the title is not shown, the title might appear.
     *
     * @param plrs target players
     */
    public static void clear(Player... plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[3], null);
        for (Player p : plrs)
            SU.tp.sendPacket(p, packet);
    }

    /**
     * A method for removing the previously set title settings, subtitles and titles.
     *
     * @param plrs target players
     */
    public static void reset(Player... plrs) {
        Object packet = PacketOutType.Title.newPacket(enums[4], null);
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }
}