package gyurix.scoreboard;

import com.mojang.authlib.GameProfile;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.UUID;

public class ScoreboardAPI {
    public static Sidebar defaultSB;
    public static Tabbar defaultTB;
    public static NametagBar defaultNB;
    public static Object setScore;
    public static Object removeScore;
    public static Object updateTabName;
    public static HashMap<UUID, Tabbar> tabbars;
    public static HashMap<UUID, Sidebar> sidebars;
    public static HashMap<UUID, NametagBar> nametags;
    public static Constructor tabPlayer;
    public static int id;

    static {
        tabbars = new HashMap();
        sidebars = new HashMap();
        nametags = new HashMap();
        id = 1;
    }

    public static void init() {
        String s;
        Class cl = Reflection.getNMSClass("IScoreboardCriteria$EnumScoreboardHealthDisplay");
        for (Object o22 : cl.getEnumConstants()) {
            s = o22.toString();
            if (s.equals("INTEGER")) {
                ScoreboardDisplayMode.INTEGER.nmsEnum = o22;
                continue;
            }
            if (!s.equals("HEARTS")) continue;
            ScoreboardDisplayMode.HEARTS.nmsEnum = o22;
        }
        cl = Reflection.getNMSClass("PacketPlayOutScoreboardScore$EnumScoreboardAction");
        for (Object o22 : cl.getEnumConstants()) {
            s = o22.toString();
            if (s.equals("CHANGE")) {
                setScore = o22;
                continue;
            }
            if (!s.equals("REMOVE")) continue;
            removeScore = o22;
        }
        cl = Reflection.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
        for (Object o22 : cl.getEnumConstants()) {
            s = o22.toString();
            if (!s.equals("UPDATE_DISPLAY_NAME")) continue;
            updateTabName = o22;
        }
        tabPlayer = Reflection.getConstructor(Reflection.getNMSClass("PacketPlayOutPlayerInfo$PlayerInfoData"), Reflection.getNMSClass("PacketPlayOutPlayerInfo"), GameProfile.class, Integer.TYPE, Reflection.getNMSClass("WorldSettings$EnumGamemode"), Reflection.getNMSClass("IChatBaseComponent"));
        defaultSB = new Sidebar();
        defaultTB = new Tabbar();
        defaultNB = new NametagBar();
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

    public static void playerJoin(Player plr) {
        defaultTB.addPlayer(plr.getUniqueId(), new TabPlayer(plr));
        defaultNB.addNametag(new Nametag(plr));
    }

    public static void playerLeave(Player plr) {
        Tabbar tb = tabbars.remove(plr.getUniqueId());
        if (tb != null) {
            tb.removePlayer(plr.getUniqueId());
        }
        Sidebar sb = sidebars.remove(plr.getUniqueId());
        NametagBar nt = nametags.remove(plr.getUniqueId());
        if (nt != null) {
            defaultNB.removeViewer(plr);
        }
    }

    public static void setSidebar(Player plr, Sidebar bar) {
        boolean first;
        boolean remove = bar == null;
        UUID uuid = plr.getUniqueId();
        boolean bl = first = !sidebars.containsKey(uuid);
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
            return;
        }
        if (sb == null) {
            bar.addViewer(plr);
        } else {
            bar.moveViewer(sb, plr);
        }
    }

    public static void setTabbar(Player plr, Tabbar bar) {
        boolean first;
        boolean remove = bar == null;
        UUID uuid = plr.getUniqueId();
        boolean bl = first = !tabbars.containsKey(uuid);
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
            return;
        }
        if (tb == null) {
            bar.addViewer(plr);
        } else {
            bar.moveViewer(tb, plr);
        }
    }

    public static void setNametagBar(Player plr, NametagBar bar) {
        boolean first;
        boolean remove = bar == null;
        UUID uuid = plr.getUniqueId();
        boolean bl = first = !nametags.containsKey(uuid);
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
            return;
        }
        if (nb == null) {
            bar.addViewer(plr);
        } else {
            bar.moveViewer(nb, plr);
        }
    }

    public enum ScoreboardDisplayMode {
        INTEGER,
        HEARTS;

        public Object nmsEnum;

        ScoreboardDisplayMode() {
        }
    }

}

