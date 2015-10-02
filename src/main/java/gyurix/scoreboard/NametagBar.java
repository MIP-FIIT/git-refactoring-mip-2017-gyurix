package gyurix.scoreboard;

import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;


/**
 * The type Nametag bar.
 */
public class NametagBar extends ScoreboardBar {
    /**
     * The map of the players in this NametagBar.
     */
    public HashMap<String, Nametag> pls = new HashMap<String, Nametag>();
    /**
     * The viewers of this NametagBar.
     */
    public HashSet<UUID> viewers = new HashSet<UUID>();
    /**
     * True if the numbers of the NametagBar are hidden.
     */
    public boolean hide;

    /**
     * Instantiates a new NametagBar.
     */
    public NametagBar() {
        super("SBAPI-nametag", 2);
    }

    /**
     * Add nametag.
     *
     * @param nt the Nametag to be added
     */
    public void addNametag(Nametag nt) {
        pls.put(nt.name,nt);
        sendPackets(nt.getTeamPacket(0), nt.getSetScorePacket());
    }

    /**
     * Remove nametag.
     *
     * @param name the name
     */
    public void removeNametag(String name){
        Nametag nt=pls.remove(name);
        if (nt!=null)
            sendPackets(nt.getTeamPacket(1),nt.getRemoveScorePacket());
    }

    /**
     * Sets data.
     *
     * @param pln the pln
     * @param prefix the prefix
     * @param suffix the suffix
     * @param hideNameTag the hide name tag
     */
    public void setData(String pln, String prefix, String suffix, boolean hideNameTag) {
        Nametag nt = pls.get(pln);
        if (nt==null)
            return;
        nt.prefix = prefix;
        nt.suffix = suffix;
        nt.hide = hideNameTag;
        sendPackets(nt.getTeamPacket(2));
    }

    /**
     * Set number.
     *
     * @param pln the pln
     * @param number the number
     */
    public void setNumber(String pln, int number){
        Nametag nt = pls.get(pln);
        if (nt==null)
            return;
        nt.number=number;
        sendPackets(nt.getSetScorePacket());
    }


    /**
     * Hide numbers in the NametagBar.
     */
    public void hide(){
        if (hide) {
            System.err.println("§eHIDING ALREADY SHOWED NAMETAG BAR?");
            return;
        }
        hide=true;
        sendPackets(hidePacket);
    }

    /**
     * Show numbers in the NametagBar.
     */
    public void show(){
        if (!hide) {
            System.err.println("§eSHOWING ALREADY SHOWN NAMETAG BAR?");
            return;
        }
        hide=false;
        sendPackets(showPacket);
    }
    @Override
    public void addViewer(Player plr) {
        System.out.println("§bNB: §aAdd viewer: "+plr.getName());
        viewers.add(plr.getUniqueId());
        sendUpdatePacket(plr);
        for (Nametag n : pls.values()) {
            SU.tp.sendPacket(plr, n.getTeamPacket(2));
            SU.tp.sendPacket(plr, n.getSetScorePacket());
        }
        if (!hide) {
            SU.tp.sendPacket(plr, showPacket);
        }
    }

    @Override
    public void addViewerFirstBar(Player plr) {
        System.out.println("§bNB: §2Add viewer FIRST BAR: "+plr.getName());
        viewers.add(plr.getUniqueId());
        sendCreatePacket(plr);
        for (Nametag n : pls.values()) {
            SU.tp.sendPacket(plr, n.getTeamPacket(0));
            SU.tp.sendPacket(plr, n.getSetScorePacket());
        }
        if (!hide) {
            SU.tp.sendPacket(plr, showPacket);
        }
    }

    @Override
    public void moveViewer(ScoreboardBar oldBar, Player plr) {
        System.out.println("§bNB: §eMove viewer: "+plr.getName());
        viewers.add(plr.getUniqueId());
        NametagBar old = (NametagBar) oldBar;
        sendUpdatePacket(plr);
        for (Nametag n : ((NametagBar) oldBar).pls.values()) {
            if (pls.containsKey(n.name)) {
                SU.tp.sendPacket(plr, n.getTeamPacket(1));
                SU.tp.sendPacket(plr, n.getRemoveScorePacket());
            }
        }
        for (Nametag n : pls.values()) {
            SU.tp.sendPacket(plr, n.getTeamPacket(old.pls.containsKey(n.name) ? 2 : 0));
            SU.tp.sendPacket(plr, n.getSetScorePacket());
        }
        if (old.hide&&!hide)
            SU.tp.sendPacket(plr,showPacket);
        else if (!old.hide&&hide)
            SU.tp.sendPacket(plr,hidePacket);
    }

    @Override
    public void removeViewer(Player plr) {
        System.out.println("§bNB: §cRemove viewer: "+plr.getName());
        if (!viewers.remove(plr.getUniqueId()))
            return;
        if (!plr.isOnline())
            return;
        for (Nametag n : pls.values()) {
            SU.tp.sendPacket(plr, n.getTeamPacket(1));
            SU.tp.sendPacket(plr, n.getRemoveScorePacket());
        }
        if (!hide)
            SU.tp.sendPacket(plr,hidePacket);
    }
}
