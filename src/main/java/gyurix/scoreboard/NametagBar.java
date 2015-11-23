package gyurix.scoreboard;

import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class NametagBar
        extends ScoreboardBar {
    public HashMap<String, Nametag> pls = new HashMap();
    public HashSet<UUID> viewers = new HashSet();

    public NametagBar() {
        super("SBAPI-nametag", "SLNB", 2);
    }

    public void addNametag(Nametag nt) {
        this.pls.put(nt.name, nt);
        this.sendPackets(nt.getTeamPacket(0), nt.getSetScorePacket());
    }

    public void removeNametag(String name) {
        Nametag nt = this.pls.remove(name);
        if (nt != null) {
            this.sendPackets(nt.getTeamPacket(1), nt.getRemoveScorePacket());
        }
    }

    public void setData(String pln, String prefix, String suffix, boolean hideNameTag) {
        Nametag nt = this.pls.get(pln);
        if (nt == null) {
            return;
        }
        nt.prefix = prefix;
        nt.suffix = suffix;
        nt.hide = hideNameTag;
        this.sendPackets(nt.getTeamPacket(2));
    }

    public void setNumber(String pln, int number) {
        Nametag nt = this.pls.get(pln);
        if (nt == null) {
            return;
        }
        nt.number = number;
        this.sendPackets(nt.getSetScorePacket());
    }

    @Override
    public void addViewer(Player plr) {
        System.out.println("\u00a7bNB: \u00a7aAdd viewer: " + plr.getName());
        this.viewers.add(plr.getUniqueId());
        this.sendUpdatePacket(plr);
        for (Nametag n : this.pls.values()) {
            SU.tp.sendPacket(plr, n.getTeamPacket(2));
            SU.tp.sendPacket(plr, n.getSetScorePacket());
        }
        if (this.visible) {
            SU.tp.sendPacket(plr, this.showPacket);
        }
    }

    @Override
    public void addViewerFirstBar(Player plr) {
        System.out.println("\u00a7bNB: \u00a72Add viewer FIRST BAR: " + plr.getName());
        this.viewers.add(plr.getUniqueId());
        this.sendCreatePacket(plr);
        for (Nametag n : this.pls.values()) {
            SU.tp.sendPacket(plr, n.getTeamPacket(0));
            SU.tp.sendPacket(plr, n.getSetScorePacket());
        }
        if (this.visible) {
            SU.tp.sendPacket(plr, this.showPacket);
        }
    }

    @Override
    public void moveViewer(ScoreboardBar oldBar, Player plr) {
        System.out.println("\u00a7bNB: \u00a7eMove viewer: " + plr.getName());
        this.viewers.add(plr.getUniqueId());
        NametagBar old = (NametagBar) oldBar;
        this.sendUpdatePacket(plr);
        for (Nametag n2 : ((NametagBar) oldBar).pls.values()) {
            if (!this.pls.containsKey(n2.name)) continue;
            SU.tp.sendPacket(plr, n2.getTeamPacket(1));
            SU.tp.sendPacket(plr, n2.getRemoveScorePacket());
        }
        for (Nametag n2 : this.pls.values()) {
            SU.tp.sendPacket(plr, n2.getTeamPacket(old.pls.containsKey(n2.name) ? 2 : 0));
            SU.tp.sendPacket(plr, n2.getSetScorePacket());
        }
        if (!old.visible && this.visible) {
            SU.tp.sendPacket(plr, this.showPacket);
        }
        if (!this.visible && old.visible) {
            SU.tp.sendPacket(plr, this.hidePacket);
        }
    }

    @Override
    public void removeViewer(Player plr) {
        System.out.println("\u00a7bNB: \u00a7cRemove viewer: " + plr.getName());
        if (!this.viewers.remove(plr.getUniqueId())) {
            return;
        }
        if (!plr.isOnline()) {
            return;
        }
        for (Nametag n : this.pls.values()) {
            SU.tp.sendPacket(plr, n.getTeamPacket(1));
            SU.tp.sendPacket(plr, n.getRemoveScorePacket());
        }
        if (this.visible) {
            SU.tp.sendPacket(plr, this.hidePacket);
        }
    }
}

