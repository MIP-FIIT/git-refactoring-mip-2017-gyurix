package gyurix.scoreboard;

import gyurix.spigotlib.Config;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class NametagBar
        extends ScoreboardBar {
    public HashMap<String, Nametag> pls = new HashMap();

    public NametagBar () {
        super("SBAPI-nametag", "SLNB", 2);
    }

    public void addNametag (Nametag nt) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §aadd nametag - §f" + nt.name);
        if (pls.containsKey(nt.name))
            throw new RuntimeException("Adding the same name twice to the NameTagBar");
        pls.put(nt.name, nt);
        sendPackets(nt.getTeamPacket(0));
        if (visible)
            sendPackets(nt.getSetScorePacket());
    }

    @Override
    public void addViewer (Player plr) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §eadd viewer - §f" + plr.getName());
        viewers.add(plr);
        if (visible) {
            SU.tp.sendPacket(plr, getObjectivePacket(0));
            SU.tp.sendPacket(plr, showPacket);
            for (Nametag n : pls.values())
                SU.tp.sendPacket(plr, n.getSetScorePacket());
        }
        for (Nametag n : pls.values())
            SU.tp.sendPacket(plr, n.getTeamPacket(0));
    }

    @Override
    public void addViewerFirstBar (Player plr) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §eadd viewer first bar - §f" + plr.getName());
        viewers.add(plr);
        if (visible) {
            SU.tp.sendPacket(plr, getObjectivePacket(0));
            SU.tp.sendPacket(plr, showPacket);
            for (Nametag n : pls.values())
                SU.tp.sendPacket(plr, n.getSetScorePacket());
        }
        for (Nametag n : pls.values())
            SU.tp.sendPacket(plr, n.getTeamPacket(0));
    }

    @Override
    public void moveViewer (ScoreboardBar oldBar, Player plr) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §emove viewer - §f" + plr.getName());
        NametagBar old = (NametagBar) oldBar;
        viewers.add(plr);
        old.viewers.remove(plr);
        if (old.visible) {
            for (Nametag n : old.pls.values()) {
                if (!pls.containsKey(n.name)) continue;
                SU.tp.sendPacket(plr, n.getTeamPacket(1));
                SU.tp.sendPacket(plr, n.getRemoveScorePacket());
            }
            if (visible) {
                getObjectivePacket(2);
                for (Nametag n2 : pls.values()) {
                    SU.tp.sendPacket(plr, n2.getTeamPacket(old.pls.containsKey(n2.name) ? 2 : 0));
                    SU.tp.sendPacket(plr, n2.getSetScorePacket());
                }
            } else {
                getObjectivePacket(1);
                for (Nametag n2 : pls.values())
                    SU.tp.sendPacket(plr, n2.getTeamPacket(old.pls.containsKey(n2.name) ? 2 : 0));
            }
        } else {
            for (Nametag n : pls.values()) {
                SU.tp.sendPacket(plr, n.getTeamPacket(old.pls.containsKey(n.name) ? 2 : 0));
                SU.tp.sendPacket(plr, n.getTeamPacket(1));
            }
            if (visible) {
                SU.tp.sendPacket(plr, getObjectivePacket(0));
                for (Nametag n2 : pls.values()) {
                    SU.tp.sendPacket(plr, n2.getTeamPacket(old.pls.containsKey(n2.name) ? 2 : 0));
                    SU.tp.sendPacket(plr, n2.getSetScorePacket());
                }
            } else {
                for (Nametag n2 : pls.values())
                    SU.tp.sendPacket(plr, n2.getTeamPacket(old.pls.containsKey(n2.name) ? 2 : 0));
            }
        }
    }

    @Override
    public void removeViewer (Player plr) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §eremove viewer - §f" + plr.getName());
        if (!viewers.remove(plr))
            return;
        if (!plr.isOnline())
            return;
        for (Nametag n : pls.values())
            SU.tp.sendPacket(plr, n.getTeamPacket(1));
        if (visible)
            SU.tp.sendPacket(plr, getObjectivePacket(1));
    }

    @Override
    public void setVisible (boolean visible) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §bset visibility - §f" + visible);
        if (visible != this.visible) {
            if (visible) {
                sendPackets(getObjectivePacket(0));
                for (Nametag n : pls.values()) {
                    sendPackets(n.getSetScorePacket());
                }
                sendPackets(showPacket);
            } else
                sendPackets(getObjectivePacket(1));
            this.visible = visible;
        }
    }

    public void removeNametag (String name) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §cremove nametag - §f" + name);
        Nametag nt = pls.remove(name);
        if (nt != null) {
            sendPackets(nt.getTeamPacket(1));
            if (visible)
                sendPackets(nt.getRemoveScorePacket());
        }
    }

    public void setData (String pln, String prefix, String suffix, boolean hideNameTag) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §bset data - §f" + pln + " - " + prefix + "§f - " + suffix);
        Nametag nt = pls.get(pln);
        if (nt == null) {
            return;
        }
        nt.prefix = prefix;
        nt.suffix = suffix;
        nt.hide = hideNameTag;
        sendPackets(nt.getTeamPacket(2));
    }

    public void setNumber (String pln, int number) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §bset number - §f" + pln + " - " + number);
        Nametag nt = pls.get(pln);
        if (nt == null) {
            return;
        }
        nt.number = number;
        if (visible)
            sendPackets(nt.getSetScorePacket());
    }
}

