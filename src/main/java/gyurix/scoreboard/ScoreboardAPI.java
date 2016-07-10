package gyurix.scoreboard;

import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class ScoreboardAPI {
    public static NametagBar defaultNB;
    public static Tabbar defaultTB;
    public static int id = 1;
    public static HashMap<Player, NametagBar> nametags = new HashMap<>();
    public static Object removeScore;
    public static Object setScore;
    public static HashMap<Player, Sidebar> sidebars = new HashMap<>();
    public static HashMap<Player, Tabbar> tabbars = new HashMap<>();
    public static Object updateTabName;

    public static void init () {
        Class cl = Reflection.getNMSClass("IScoreboardCriteria$EnumScoreboardHealthDisplay");
        ScoreboardDisplayMode.INTEGER.nmsEnum = Reflection.getEnum(cl, "INTEGER");
        ScoreboardDisplayMode.HEARTS.nmsEnum = Reflection.getEnum(cl, "HEARTS");
        cl = Reflection.getNMSClass("PacketPlayOutScoreboardScore$EnumScoreboardAction");
        setScore = Reflection.getEnum(cl, "CHANGE");
        removeScore = Reflection.getEnum(cl, "REMOVE");
        cl = Reflection.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
        updateTabName = Reflection.getEnum(cl, "UPDATE_DISPLAY_NAME");
        defaultTB = new Tabbar();
        defaultNB = new NametagBar();
    }

    public static void playerJoin (Player plr) {
        defaultTB.addPlayer(plr.getUniqueId(), new TabPlayer(plr));
        defaultNB.addNametag(new Nametag(plr));
    }

    public static void playerLeave (Player plr) {
        UUID id = plr.getUniqueId();
        ScoreboardBar sb = tabbars.remove(plr);
        if (sb != null)
            sb.viewers.remove(plr);
        sb = sidebars.remove(plr);
        if (sb != null)
            sb.viewers.remove(plr);
        sb = nametags.remove(plr);
        if (sb != null)
            sb.viewers.remove(plr);
        defaultNB.removeNametag(plr.getName());
        defaultTB.removePlayer(id);
    }

    public static void setNametagBar (Player plr, NametagBar bar) {
        if (set(plr, nametags.get(plr), bar, !nametags.containsKey(plr)))
            nametags.put(plr, bar);
    }

    private static boolean set (Player plr, ScoreboardBar from, ScoreboardBar to, boolean first) {
        if (from == to)
            return false;
        if (first)
            to.addViewerFirstBar(plr);
        else if (to == null) {
            if (from != null)
                from.removeViewer(plr);
        } else if (from == null)
            to.addViewer(plr);
        else
            to.moveViewer(from, plr);
        return true;
    }

    public static void setSidebar (Player plr, Sidebar bar) {
        if (set(plr, sidebars.get(plr), bar, !sidebars.containsKey(plr)))
            sidebars.put(plr, bar);
    }

    public static void setTabbar (Player plr, Tabbar bar) {
        if (set(plr, tabbars.get(plr), bar, !tabbars.containsKey(plr)))
            tabbars.put(plr, bar);
    }

    public static String[] specialSplit (String in, char uniqueChar) {
        if ((in = SU.optimizeColorCodes(in)).length() < 17)
            return new String[]{in, "§" + uniqueChar, ""};
        String[] out = new String[3];
        out[0] = in.substring(0, 16);
        if (out[0].endsWith("§")) {
            out[0] = out[0].substring(0, 15);
            in = out[0] + ' ' + in.substring(15);
        }
        StringBuilder formats = new StringBuilder();
        int prev = 32;
        for (int i = 0; i < 16; ++i) {
            char c = in.charAt(i);
            if (prev == 167) {
                if (c >= '0' && c <= '9' || c >= 'a' && c <= 'f')
                    formats.setLength(0);
                formats.append('§').append(c);
            }
            prev = c;
        }
        in = SU.setLength(formats + in.substring(16), 54);
        if (in.length() < 17) {
            out[1] = "§" + uniqueChar;
            out[2] = in;
        } else {
            int id = in.length() - 16;
            out[1] = "§" + uniqueChar + in.substring(0, id);
            out[2] = in.substring(id);
        }
        return out;
    }

    public enum ScoreboardDisplayMode {
        INTEGER,
        HEARTS;
        public Object nmsEnum;

        ScoreboardDisplayMode () {
        }
    }

}

