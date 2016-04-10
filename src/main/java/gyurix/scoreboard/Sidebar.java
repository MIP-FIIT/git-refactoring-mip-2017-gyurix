package gyurix.scoreboard;

import gyurix.spigotlib.Config;
import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class Sidebar extends ScoreboardBar {
    /**
     * The lines of the Sidebar (0-14)
     */
    public ArrayList<SidebarLine> lines = new ArrayList<>();

    /**
     * Default sidebar constructor
     */
    public Sidebar() {
        super("SBAPI-sidebar", "SLSB", 1);
        for (int i = 1; i < 16; ++i) {
            SidebarLine line = new SidebarLine(this, (char) (280 + i));
            lines.add(line);
            line.setText("§6§lLine - §e§l" + i);
            line.number = 100 - i;
        }
    }

    @Override
    public void addViewer(Player plr) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §eadd viewer - §f" + plr.getName());
        viewers.add(plr.getUniqueId());
        SU.tp.sendPacket(plr, getObjectivePacket(2));
        for (SidebarLine line : lines) {
            if (!line.hide)
                line.show(plr);
        }
        if (visible)
            SU.tp.sendPacket(plr, showPacket);
    }

    @Override
    public void addViewerFirstBar(Player plr) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §eadd viewer first bar - §f" + plr.getName());
        viewers.add(plr.getUniqueId());
        SU.tp.sendPacket(plr, getObjectivePacket(0));
        for (SidebarLine line : lines) {
            if (!line.hide)
                line.show(plr);
        }
        if (visible)
            SU.tp.sendPacket(plr, showPacket);
    }

    public void hideLine(int line) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §chide line - " + line);
        SidebarLine sl = lines.get(line - 1);
        if (!sl.hide) {
            Iterator<UUID> it = viewers.iterator();
            while (it.hasNext()) {
                Player p = Bukkit.getPlayer(it.next());
                if (p == null) {
                    it.remove();
                    continue;
                }
                sl.hide(p);
            }
            sl.hide = true;
        }
    }

    @Override
    public void moveViewer(ScoreboardBar oldBar, Player plr) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §cmove viewer - §f" + plr.getName());
        UUID uuid = plr.getUniqueId();
        oldBar.viewers.remove(uuid);
        viewers.add(uuid);
        Sidebar old = (Sidebar) oldBar;
        for (int i = 0; i < 15; ++i) {
            old.lines.get(i).hide(plr);
            lines.get(i).show(plr);
        }
        SU.tp.sendPacket(plr, getObjectivePacket(2));
        if (!old.visible && visible) {
            SU.tp.sendPacket(plr, showPacket);
        }
        if (!visible && old.visible) {
            SU.tp.sendPacket(plr, hidePacket);
        }
    }

    @Override
    public void removeViewer(Player plr) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §eremove viewer - §f" + plr.getName());
        if (!viewers.remove(plr.getUniqueId()))
            return;
        if (!plr.isOnline())
            return;
        if (visible)
            SU.tp.sendPacket(plr, hidePacket);
        for (SidebarLine line : lines)
            line.hide(plr);
    }

    public void setLine(int line, String text) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §bset line - " + line + " - §f" + text);
        SidebarLine sl = lines.get(line - 1);
        sl.setText(text);
        Iterator<UUID> it = viewers.iterator();
        while (it.hasNext()) {
            Player p = Bukkit.getPlayer(it.next());
            if (p == null) {
                it.remove();
                continue;
            }
            sl.update(p);
        }
    }

    public void setNumber(int line, int number) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §bset number - " + line + " - §f" + number);
        SidebarLine sl = lines.get(line - 1);
        sl.number = number;
        Iterator<UUID> it = viewers.iterator();
        while (it.hasNext()) {
            Player p = Bukkit.getPlayer(it.next());
            if (p == null) {
                it.remove();
                continue;
            }
            sl.updateNumber(p);
        }
    }

    public void showLine(int line) {
        if (Config.debug)
            SU.cs.sendMessage("§6[ §eScoreboardAPI §6] §f" + barname + " - §ashow line - " + line);
        SidebarLine sl = lines.get(line - 1);
        if (sl.hide) {
            sl.hide = false;
            Iterator<UUID> it = viewers.iterator();
            while (it.hasNext()) {
                Player p = Bukkit.getPlayer(it.next());
                if (p == null) {
                    it.remove();
                    continue;
                }
                sl.show(p);
            }
        }
    }
}

