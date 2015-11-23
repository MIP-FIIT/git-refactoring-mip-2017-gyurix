package gyurix.scoreboard;

import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class Sidebar
        extends ScoreboardBar {
    public ArrayList<SidebarLine> lines = new ArrayList();

    public Sidebar() {
        super("SBAPI-sidebar", "SLSB", 1);
        for (int i = 1; i < 16; ++i) {
            SidebarLine line = new SidebarLine(this, (char) (280 + i));
            this.lines.add(line);
            line.setText("\u00a76\u00a7lLine - \u00a7e\u00a7l" + i);
            line.number = 100 - i;
        }
    }

    public void showLine(int line) {
        SidebarLine sl = this.lines.get(line - 1);
        if (sl.hide) {
            sl.hide = false;
            Iterator<UUID> it = this.viewers.iterator();
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

    public void hideLine(int line) {
        SidebarLine sl = this.lines.get(line - 1);
        if (!sl.hide) {
            Iterator<UUID> it = this.viewers.iterator();
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

    public void setLine(int line, String text) {
        SidebarLine sl = this.lines.get(line - 1);
        sl.setText(text);
        Iterator<UUID> it = this.viewers.iterator();
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
        SidebarLine sl = this.lines.get(line - 1);
        sl.number = number;
        Iterator<UUID> it = this.viewers.iterator();
        while (it.hasNext()) {
            Player p = Bukkit.getPlayer(it.next());
            if (p == null) {
                it.remove();
                continue;
            }
            sl.updateNumber(p);
        }
    }

    @Override
    public void addViewer(Player plr) {
        this.viewers.add(plr.getUniqueId());
        this.sendUpdatePacket(plr);
        for (SidebarLine line : this.lines) {
            if (line.hide) continue;
            line.show(plr);
        }
        if (this.visible) {
            SU.tp.sendPacket(plr, this.showPacket);
        }
    }

    @Override
    public void addViewerFirstBar(Player plr) {
        this.viewers.add(plr.getUniqueId());
        this.sendCreatePacket(plr);
        for (SidebarLine line : this.lines) {
            if (line.hide) continue;
            line.show(plr);
        }
        if (this.visible) {
            SU.tp.sendPacket(plr, this.showPacket);
        }
    }

    @Override
    public void moveViewer(ScoreboardBar oldBar, Player plr) {
        System.out.println("Move: " + plr.getName());
        UUID uuid = plr.getUniqueId();
        oldBar.viewers.remove(uuid);
        this.viewers.add(uuid);
        Sidebar old = (Sidebar) oldBar;
        for (int i = 0; i < 15; ++i) {
            old.lines.get(i).hide(plr);
            this.lines.get(i).show(plr);
        }
        this.sendUpdatePacket(plr);
        if (!old.visible && this.visible) {
            SU.tp.sendPacket(plr, this.showPacket);
        }
        if (!this.visible && old.visible) {
            SU.tp.sendPacket(plr, this.hidePacket);
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
        if (this.visible) {
            SU.tp.sendPacket(plr, this.hidePacket);
        }
        for (SidebarLine line : this.lines) {
            line.hide(plr);
        }
    }
}

