package gyurix.scoreboard;

import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class ScoreboardAPI {
    public static NametagBar defaultNB;
    public static Sidebar defaultSB;
    public static Tabbar defaultTB;
    public static int id = 1;
    public static HashMap<UUID, NametagBar> nametags = new HashMap<>();
    public static Object removeScore;
    public static Object setScore;
    public static HashMap<UUID, Sidebar> sidebars = new HashMap<>();
    public static HashMap<UUID, Tabbar> tabbars = new HashMap<>();
    public static Object updateTabName;

    public static void init() {
        Class cl = Reflection.getNMSClass("IScoreboardCriteria$EnumScoreboardHealthDisplay");
        ScoreboardDisplayMode.INTEGER.nmsEnum = Reflection.getEnum(cl, "INTEGER");
        ScoreboardDisplayMode.HEARTS.nmsEnum = Reflection.getEnum(cl, "HEARTS");
        cl = Reflection.getNMSClass("PacketPlayOutScoreboardScore$EnumScoreboardAction");
        setScore = Reflection.getEnum(cl, "CHANGE");
        removeScore = Reflection.getEnum(cl, "REMOVE");
        cl = Reflection.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
        updateTabName = Reflection.getEnum(cl, "UPDATE_DISPLAY_NAME");
        defaultSB = new Sidebar();
        defaultTB = new Tabbar();
        defaultNB = new NametagBar();
    }

    public static void playerJoin(Player plr) {
        defaultTB.addPlayer(plr.getUniqueId(), new TabPlayer(plr));
        defaultNB.addNametag(new Nametag(plr));
    }

    public static void playerLeave(Player plr) {
        UUID id = plr.getUniqueId();
        tabbars.remove(id);
        sidebars.remove(id);
        nametags.remove(id);
        defaultNB.removeNametag(plr.getName());
        defaultTB.removePlayer(id);
    }

    public static void setNametagBar(Player plr, NametagBar bar) {
        boolean remove = bar == null;
        UUID uuid = plr.getUniqueId();
        boolean first = !nametags.containsKey(uuid);
        if (first && remove) {
            return;
        }
        NametagBar nb = nametags.get(uuid);
        nametags.put(uuid, bar);
        if (first) {
            bar.addViewerFirstBar(plr);
            return;
        }
        if (remove) {
            if (nb != null) {
                nb.removeViewer(plr);
            }
            nametags.remove(uuid);
            return;
        }
        if (nb == null) {
            bar.addViewer(plr);
        } else {
            bar.moveViewer(nb, plr);
        }
    }

    public static void setSidebar(Player plr, Sidebar bar) {
        boolean remove = bar == null;
        UUID uuid = plr.getUniqueId();
        boolean first = !sidebars.containsKey(uuid);
        if (first && remove) {
            return;
        }
        Sidebar sb = sidebars.get(uuid);
        sidebars.put(uuid, bar);
        if (first) {
            bar.addViewerFirstBar(plr);
            return;
        }
        if (remove) {
            if (sb != null) {
                sb.removeViewer(plr);
            }
            sidebars.put(uuid, null);
            return;
        }
        if (sb == null) {
            bar.addViewer(plr);
        } else {
            bar.moveViewer(sb, plr);
        }
    }

    public static void setTabbar(Player plr, Tabbar bar) {
        boolean remove = bar == null;
        UUID uuid = plr.getUniqueId();
        boolean first = !tabbars.containsKey(uuid);
        if (first && remove) {
            return;
        }
        Tabbar tb = tabbars.get(uuid);
        tabbars.put(uuid, bar);
        if (first) {
            bar.addViewerFirstBar(plr);
            return;
        }
        if (remove) {
            if (tb != null) {
                tb.removeViewer(plr);
            }
            tabbars.remove(uuid);
            return;
        }
        if (tb == null) {
            bar.addViewer(plr);
        } else {
            bar.moveViewer(tb, plr);
        }
    }

    static String[] specialSplit(String in, char uniqueChar) {
        if ((in = SU.optimizeColorCodes(in)).length() < 17) {
            return new String[]{in, "\u00a7" + uniqueChar, ""};
        }
        String[] out = new String[3];
        out[0] = in.substring(0, 16);
        if (out[0].endsWith("\u00a7")) {
            out[0] = out[0].substring(0, 15);
            in = out[0] + " " + in.substring(15);
        }
        StringBuilder formats = new StringBuilder();
        int prev = 32;
        for (int i = 0; i < 16; ++i) {
            char c = in.charAt(i);
            if (prev == 167) {
                if (c >= '0' && c <= '9' || c >= 'a' && c <= 'f') {
                    formats.setLength(0);
                }
                formats.append('\u00a7').append(c);
            }
            prev = c;
        }
        in = SU.setLength(formats.toString() + in.substring(16), 54);
        if (in.length() < 17) {
            out[1] = "\u00a7" + uniqueChar;
            out[2] = in;
        } else {
            int id = in.length() - 16;
            out[1] = "\u00a7" + uniqueChar + in.substring(0, id);
            out[2] = in.substring(id);
        }
        return out;
    }

    public enum ScoreboardDisplayMode {
        INTEGER,
        HEARTS;

        public Object nmsEnum;

        ScoreboardDisplayMode() {
        }
    }

}

