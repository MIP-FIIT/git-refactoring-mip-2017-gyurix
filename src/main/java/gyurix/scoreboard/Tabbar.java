package gyurix.scoreboard;

import gyurix.protocol.PacketOutType;
import gyurix.spigotlib.ChatAPI;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Tabbar
        extends ScoreboardBar {
    public String header = "\u00a7b\u00a7lTest header";
    public String footer = "\u00a7c\u00a7l--------------------\nTabbar, made by gyuriX\n\u00a7c\u00a7l--------------------";
    public ArrayList<UUID> viewers = new ArrayList();
    public HashMap<UUID, TabPlayer> pls = new HashMap();

    public Tabbar() {
        super("SBAPI-tabbar", "SLTB", 0);
    }

    public void addPlayer(UUID uuid, TabPlayer tabPlayer) {
        this.pls.put(uuid, tabPlayer);
        this.sendPackets(tabPlayer.getTabnameSetPacket(), tabPlayer.getSetScorePacket());
    }

    public void removePlayer(UUID uuid) {
        TabPlayer tp = this.pls.remove(uuid);
        if (tp != null) {
            this.sendPackets(tp.getRemoveScorePacket(), tp.getTabnameRestorePacket());
        }
    }

    public void setHeaderFooter(String header, String footer) {
        Object[] arrobject = new Object[1];
        Object[] arrobject2 = new Object[2];
        this.header = header;
        arrobject2[0] = ChatAPI.toICBC(ChatAPI.TextToJson(this.header));
        this.footer = footer;
        arrobject2[1] = ChatAPI.toICBC(ChatAPI.TextToJson(this.footer));
        arrobject[0] = PacketOutType.PlayerListHeaderFooter.newPacket(arrobject2);
        this.sendPackets(arrobject);
    }

    public void setTabName(UUID plID, String name) {
        TabPlayer tp = this.pls.get(plID);
        tp.tabname = name;
        this.sendPackets(tp.getTabnameSetPacket());
    }

    public void setNumber(Player plr, int number) {
        TabPlayer sp = this.pls.get(plr.getUniqueId());
        sp.number = number;
        this.sendPackets(sp.getSetScorePacket(), this.showPacket);
    }

    @Override
    public void addViewer(Player plr) {
        System.out.println("\u00a7cTB: \u00a7aAdd viewer: " + plr.getName());
        this.viewers.add(plr.getUniqueId());
        this.sendUpdatePacket(plr);
        for (TabPlayer p : this.pls.values()) {
            SU.tp.sendPacket(plr, p.getSetScorePacket());
            SU.tp.sendPacket(plr, p.getTabnameSetPacket());
        }
        if (this.visible) {
            SU.tp.sendPacket(plr, this.showPacket);
        }
        SU.tp.sendPacket(plr, PacketOutType.PlayerListHeaderFooter.newPacket(ChatAPI.toICBC(ChatAPI.TextToJson(this.header)), ChatAPI.toICBC(ChatAPI.TextToJson(this.footer))));
    }

    @Override
    public void addViewerFirstBar(Player plr) {
        System.out.println("\u00a7cTB: \u00a72Add viewer FIRST BAR: " + plr.getName());
        this.viewers.add(plr.getUniqueId());
        this.sendCreatePacket(plr);
        for (TabPlayer p : this.pls.values()) {
            SU.tp.sendPacket(plr, p.getSetScorePacket());
            SU.tp.sendPacket(plr, p.getTabnameSetPacket());
        }
        if (this.visible) {
            SU.tp.sendPacket(plr, this.showPacket);
        }
        SU.tp.sendPacket(plr, PacketOutType.PlayerListHeaderFooter.newPacket(ChatAPI.toICBC(ChatAPI.TextToJson(this.header)), ChatAPI.toICBC(ChatAPI.TextToJson(this.footer))));
    }

    @Override
    public void moveViewer(ScoreboardBar oldBar, Player plr) {
        System.out.println("\u00a7cTB: \u00a7eMove viewer: " + plr.getName());
        Tabbar old = (Tabbar) oldBar;
        this.viewers.add(plr.getUniqueId());
        this.sendUpdatePacket(plr);
        for (TabPlayer p2 : this.pls.values()) {
            TabPlayer op = old.pls.get(p2.profile.getId());
            if (op == null || op.number != p2.number) {
                SU.tp.sendPacket(plr, p2.getSetScorePacket());
            }
            if (op != null && op.tabname.equals(p2.tabname)) continue;
            SU.tp.sendPacket(plr, p2.getTabnameSetPacket());
        }
        for (TabPlayer p2 : old.pls.values()) {
            if (this.pls.containsKey(p2.profile.getId())) continue;
            SU.tp.sendPacket(plr, p2.getRemoveScorePacket());
            SU.tp.sendPacket(plr, p2.getTabnameRestorePacket());
        }
        if (!old.visible && this.visible) {
            SU.tp.sendPacket(plr, this.showPacket);
        }
        if (!this.visible && old.visible) {
            SU.tp.sendPacket(plr, this.hidePacket);
        }
        if (!old.header.equals(this.header) || !old.footer.equals(this.footer)) {
            SU.tp.sendPacket(plr, PacketOutType.PlayerListHeaderFooter.newPacket(ChatAPI.toICBC(ChatAPI.TextToJson(this.header)), ChatAPI.toICBC(ChatAPI.TextToJson(this.footer))));
        }
    }

    @Override
    public void removeViewer(Player plr) {
        if (!this.viewers.remove(plr.getUniqueId())) {
            return;
        }
        if (!plr.isOnline()) {
            return;
        }
        for (TabPlayer p : this.pls.values()) {
            SU.tp.sendPacket(plr, p.getRemoveScorePacket());
            SU.tp.sendPacket(plr, p.getTabnameRestorePacket());
        }
        SU.tp.sendPacket(plr, PacketOutType.PlayerListHeaderFooter.newPacket(ChatAPI.toICBC(ChatAPI.TextToJson("")), ChatAPI.toICBC(ChatAPI.TextToJson(""))));
        if (this.visible) {
            SU.tp.sendPacket(plr, this.hidePacket);
        }
    }
}

