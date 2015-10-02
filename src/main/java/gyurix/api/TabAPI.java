package gyurix.api;

import gyurix.protocol.PacketOutType;
import gyurix.spigotlib.ChatAPI;
import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * @author GyuriX
 *
 * An API for managing tab headers and footers
 */
public class TabAPI {
    /**
     *
     * @param header tab header text
     * @param footer tab footer text
     *
     * The tab header and footer will be set to the given values for every player on the server
     */
    public static void setGlobalHeaderFooter(String header, String footer) {
        setLocalHeaderFooter(header, footer, Bukkit.getOnlinePlayers());
    }

    /**
     *
     * @param header tab header text
     * @param footer tab footer text
     * @param plrs target players, only they will see the given tab header and footer
     *
     */
    public static void setLocalHeaderFooter(String header, String footer, Player... plrs) {
        Object h = ChatAPI.toICBC(ChatAPI.TextToJson(header));
        Object f = ChatAPI.toICBC(ChatAPI.TextToJson(footer));
        Object packet = PacketOutType.PlayerListHeaderFooter.newPacket(h, f);
        for (Player p : plrs)
            SU.tp.sendPacket(p, packet);
    }

    /**
     *
     * @param header tab header text
     * @param footer tab footer text
     * @param plrs target players, only they will see the given tab header and footer
     */
    public static void setLocalHeaderFooter(String header, String footer, Collection<? extends Player> plrs) {
        Object h = ChatAPI.toICBC(ChatAPI.TextToJson(header));
        Object f = ChatAPI.toICBC(ChatAPI.TextToJson(footer));
        Object packet = PacketOutType.PlayerListHeaderFooter.newPacket(h, f);
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }
}