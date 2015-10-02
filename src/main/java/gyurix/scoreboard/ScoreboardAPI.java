package gyurix.scoreboard;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import gyurix.protocol.PacketOutType;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;
import javafx.geometry.Side;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

/**
 * An API for managing 
 */
public class ScoreboardAPI {
    /**
     * The default sidebar instance.
     */
    public static Sidebar defaultSB;
    /**
     * The default tabbar instance.
     */
    public static Tabbar defaultTB;
    /**
     * The default nametagbar instance
     */
    public static NametagBar defaultNB;
    /**
     * The
     */
    public static Object setScore,
    /**
     * The Remove score enum value.
     */
    removeScore, /**
     * The Update tab name enum value.
     */
    updateTabName;
    /**
     * Map of the tabbar viewers.
     */
    public static HashMap<UUID,Tabbar> tabbars=new HashMap<UUID, Tabbar>();
    /**
     * Map of the sidebar viewers.
     */
    public static HashMap<UUID,Sidebar> sidebars=new HashMap<UUID, Sidebar>();
    /**
     * Map of the nametagbar viewers.
     */
    public static HashMap<UUID,NametagBar> nametags=new HashMap<UUID, NametagBar>();
    /**
     * The contructor of a tabPlayer.
     */
    public static Constructor tabPlayer;
    /**
     * Counter id for testing this API
     */
    public static int id=1;

    /**
     * The enum Scoreboard display mode.
     */
    public enum ScoreboardDisplayMode{
        /**
         * The INTEGER.
         */
        INTEGER, /**
         * The HEARTS.
         */
        HEARTS;
        /**
         * The Nms enum.
         */
        public Object nmsEnum;
    }

    /**
     * Init void.
     */
    public static void init() {
        Class cl = Reflection.getNMSClass("IScoreboardCriteria$EnumScoreboardHealthDisplay");
        for (Object o : cl.getEnumConstants()) {
            String s = o.toString();
            if (s.equals("INTEGER"))
                ScoreboardDisplayMode.INTEGER.nmsEnum = o;
            else if (s.equals("HEARTS"))
                ScoreboardDisplayMode.HEARTS.nmsEnum = o;
        }
        cl = Reflection.getNMSClass("PacketPlayOutScoreboardScore$EnumScoreboardAction");
        for (Object o : cl.getEnumConstants()) {
            String s = o.toString();
            if (s.equals("CHANGE"))
                setScore = o;
            else if (s.equals("REMOVE"))
                removeScore = o;
        }
        cl = Reflection.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
        for (Object o : cl.getEnumConstants()) {
            String s = o.toString();
            if (s.equals("UPDATE_DISPLAY_NAME"))
                updateTabName = o;
        }
        tabPlayer= Reflection.getConstructor(Reflection.getNMSClass("PacketPlayOutPlayerInfo$PlayerInfoData"),Reflection.getNMSClass("PacketPlayOutPlayerInfo"),
                GameProfile.class,int.class,Reflection.getNMSClass("WorldSettings$EnumGamemode"), Reflection.getNMSClass("IChatBaseComponent"));
        defaultSB=new Sidebar();
        defaultTB=new Tabbar();
        defaultNB=new NametagBar();
    }

    /**
     * Special split.
     *
     * @param in the in
     * @param uniqueChar the unique char
     * @return the string [ ]
     */
    static String[] specialSplit(String in, char uniqueChar) {
        in = SU.optimizeColorCodes(in);
        if (in.length() < 17) {
            return new String[]{in, "§" + uniqueChar, ""};
        }
        String[] out = new String[3];
        out[0] = in.substring(0, 16);
        if (out[0].endsWith("§")) {
            out[0] = out[0].substring(0, 15);
            in=out[0]+" "+in.substring(15);
        }
        StringBuilder formats = new StringBuilder();
        char prev = ' ';
        for (int i = 0; i < 16; i++) {
            char c = in.charAt(i);
            if (prev == '§') {
                if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))
                    formats.setLength(0);
                formats.append('§').append(c);
            }
            prev = c;
        }
        in=SU.setLength(formats.toString()+in.substring(16),54);
        if (in.length()<17){
            out[1]="§"+uniqueChar;
            out[2]=in;
        }
        else{
            int id=in.length()-16;
            out[1]="§"+uniqueChar+in.substring(0,id);
            out[2]=in.substring(id);
        }
        return out;
    }

    /**
     * Player join.
     *
     * @param plr the plr
     */
    public static void playerJoin(Player plr){
        defaultTB.addPlayer(plr.getUniqueId(),new TabPlayer(plr));
        defaultNB.addNametag(new Nametag(plr));
    }

    /**
     * Player leave.
     *
     * @param plr the plr
     */
    public static void playerLeave(Player plr) {
        Tabbar tb=tabbars.remove(plr.getUniqueId());
        if (tb!=null)
            tb.removePlayer(plr.getUniqueId());
        Sidebar sb=sidebars.remove(plr.getUniqueId());
        NametagBar nt=nametags.remove(plr.getUniqueId());
        if (nt!=null)
            defaultNB.removeViewer(plr);
    }

    /**
     * Set sidebar.
     *
     * @param plr the plr
     * @param bar the bar
     */
    public static void setSidebar(Player plr,Sidebar bar){
        boolean remove=bar==null;
        UUID uuid=plr.getUniqueId();
        boolean first=!sidebars.containsKey(uuid);
        if (first&&remove)
            return;
        Sidebar sb=sidebars.get(uuid);
        sidebars.put(uuid,bar);
        if (first){
            bar.addViewerFirstBar(plr);
            return;
        }
        else if (remove){
            if (sb!=null)
                sb.removeViewer(plr);
            return;
        }
        if (sb==null)
            bar.addViewer(plr);
        else
            bar.moveViewer(sb,plr);
    }

    /**
     * Set tabbar.
     *
     * @param plr the plr
     * @param bar the bar
     */
    public static void setTabbar(Player plr,Tabbar bar){
        boolean remove=bar==null;
        UUID uuid=plr.getUniqueId();
        boolean first=!tabbars.containsKey(uuid);
        if (first&&remove)
            return;
        Tabbar tb=tabbars.get(uuid);
        tabbars.put(uuid, bar);
        if (first){
            bar.addViewerFirstBar(plr);
            return;
        }
        else if (remove){
            if (tb!=null)
                tb.removeViewer(plr);
            return;
        }
        if (tb==null)
            bar.addViewer(plr);
        else
            bar.moveViewer(tb,plr);
    }

    /**
     * Set nametag bar.
     *
     * @param plr the plr
     * @param bar the bar
     */
    public static void setNametagBar(Player plr,NametagBar bar){
        boolean remove=bar==null;
        UUID uuid=plr.getUniqueId();
        boolean first=!nametags.containsKey(uuid);
        if (first&&remove)
            return;
        NametagBar nb=nametags.get(uuid);
        nametags.put(uuid, bar);
        if (first){
            bar.addViewerFirstBar(plr);
            return;
        }
        else if (remove){
            if (nb!=null)
                nb.removeViewer(plr);
            return;
        }
        if (nb==null)
            bar.addViewer(plr);
        else
            bar.moveViewer(nb,plr);
    }
}
