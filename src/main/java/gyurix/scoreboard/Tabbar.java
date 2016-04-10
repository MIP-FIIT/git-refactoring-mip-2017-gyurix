package gyurix.scoreboard;

import gyurix.protocol.event.PacketOutType;
import gyurix.spigotlib.ChatAPI;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class Tabbar
        extends ScoreboardBar {
    public String footer = "\u00a7c\u00a7l--------------------\nTabbar, made by gyuriX\n\u00a7c\u00a7l--------------------";
    public String header = "\u00a7b\u00a7lTest header";
    public HashMap<UUID, TabPlayer> pls = new HashMap();

    public Tabbar() {
        super("SBAPI-tabbar", "SLTB", 0);
    }

    public void addPlayer(UUID uuid, TabPlayer tabPlayer) {
        this.pls.put(uuid, tabPlayer);
        this.sendPackets(tabPlayer.getTabnameSetPacket(), tabPlayer.getSetScorePacket());
    }

    @Override
    public void addViewer(Player plr) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §eadd viewer - §f" + plr.getName());
        this.viewers.add(plr.getUniqueId());
        SU.tp.sendPacket(plr, getObjectivePacket(2));
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
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §eadd viewer first bar - §f" + plr.getName());
        this.viewers.add(plr.getUniqueId());
        SU.tp.sendPacket(plr, getObjectivePacket(0));
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
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §emove viewer - §f" + plr.getName());
        Tabbar old = (Tabbar) oldBar;
        this.viewers.add(plr.getUniqueId());
        SU.tp.sendPacket(plr, getObjectivePacket(2));
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

    public void removePlayer(UUID uuid) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §cremove player - §f" + SU.getName(uuid));
        TabPlayer tp = this.pls.remove(uuid);
        if (tp != null) {
            this.sendPackets(tp.getRemoveScorePacket(), tp.getTabnameRestorePacket());
        }
    }

    @Override
    public void removeViewer(Player plr) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §eremove viewer - §f" + plr.getName());
        if (!this.viewers.remove(plr.getUniqueId()))
            return;
        if (!plr.isOnline())
            return;
        for (TabPlayer p : this.pls.values()) {
            SU.tp.sendPacket(plr, p.getRemoveScorePacket());
            SU.tp.sendPacket(plr, p.getTabnameRestorePacket());
        }
        SU.tp.sendPacket(plr, PacketOutType.PlayerListHeaderFooter.newPacket(ChatAPI.toICBC(ChatAPI.TextToJson("")), ChatAPI.toICBC(ChatAPI.TextToJson(""))));
        if (this.visible) {
            SU.tp.sendPacket(plr, this.hidePacket);
        }
    }

    /**
     * Sets the header-footer of this Tabbar
     *
     * @param header - new header
     * @param footer - new footer
     */
    public void setHeaderFooter(String header, String footer) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §bset header-footer:\n" + header + "\n§f-------------------------------------------------------------\n" + footer);
        this.header = header;
        this.footer = footer;
        sendPackets(PacketOutType.PlayerListHeaderFooter.newPacket(ChatAPI.toICBC(ChatAPI.TextToJson(header)), ChatAPI.toICBC(ChatAPI.TextToJson(footer))));
    }

    /**
     * Set the number of a Player in the tabbar
     *
     * @param plr
     * @param number
     */
    public void setNumber(Player plr, int number) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §bset number - " + plr.getName() + " - §f" + number);
        TabPlayer sp = this.pls.get(plr.getUniqueId());
        sp.number = number;
        this.sendPackets(sp.getSetScorePacket(), this.showPacket);
    }

    /**
     * Set the tabname of a player
     *
     * @param plID - The players UUID.
     * @param name - The new tab name of the player
     */
    public void setTabName(UUID plID, String name) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §bset tabname - " + SU.getName(plID) + " - §f" + name);
        TabPlayer tp = this.pls.get(plID);
        tp.tabname = name;
        this.sendPackets(tp.getTabnameSetPacket());
    }
}

