package gyurix.api;

import gyurix.protocol.event.PacketOutType;
import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

import static gyurix.spigotlib.ChatAPI.TextToJson;
import static gyurix.spigotlib.ChatAPI.toICBC;

/**
 * TabAPI - Used for setting global and local tab headers and footers
 */
public class TabAPI {
    /**
     * Sets the player list header and footer for every online player
     *
     * @param header - The new header
     * @param footer - The new footer
     */
    public static void setGlobalHeaderFooter(String header, String footer) {
        setLocalHeaderFooter(header, footer, Bukkit.getOnlinePlayers());
    }

    /**
     * Sets the player list header and footer for the given collection of players
     *
     * @param header - The new header
     * @param footer - The new footer
     * @param plrs   - Target players
     */
    public static void setLocalHeaderFooter(String header, String footer, Collection<? extends Player> plrs) {
        Object h = toICBC(TextToJson(header));
        Object f = toICBC(TextToJson(footer));
        Object packet = PacketOutType.PlayerListHeaderFooter.newPacket(h, f);
        plrs.forEach((p) -> SU.tp.sendPacket(p, packet));
    }

    /**
     * Sets the player list header and footer for the given players
     *
     * @param header - The new header
     * @param footer - The new footer
     * @param plrs   - Target players
     */
    public static void setLocalHeaderFooter(String header, String footer, Player... plrs) {
        Object h = toICBC(TextToJson(header));
        Object f = toICBC(TextToJson(footer));
        Object packet = PacketOutType.PlayerListHeaderFooter.newPacket(h, f);
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }
}

