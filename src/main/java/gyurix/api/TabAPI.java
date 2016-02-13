package gyurix.api;

import gyurix.protocol.PacketOutType;
import gyurix.spigotlib.ChatAPI;
import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

public class TabAPI {
    public static void setGlobalHeaderFooter(String header, String footer) {
        TabAPI.setLocalHeaderFooter(header, footer, Bukkit.getOnlinePlayers());
    }

    public static void setLocalHeaderFooter(String header, String footer, Player... plrs) {
        Object h = ChatAPI.toICBC(ChatAPI.TextToJson(header));
        Object f = ChatAPI.toICBC(ChatAPI.TextToJson(footer));
        Object packet = PacketOutType.PlayerListHeaderFooter.newPacket(h, f);
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void setLocalHeaderFooter(String header, String footer, Collection<? extends Player> plrs) {
        Object h = ChatAPI.toICBC(ChatAPI.TextToJson(header));
        Object f = ChatAPI.toICBC(ChatAPI.TextToJson(footer));
        Object packet = PacketOutType.PlayerListHeaderFooter.newPacket(h, f);
        for (Player p : plrs) {
            SU.tp.sendPacket(p, packet);
        }
    }

    public static void setGlobalTabName(Player p, String value) {

    }
}

