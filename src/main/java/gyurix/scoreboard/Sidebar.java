package gyurix.scoreboard;

import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Sidebar extends ScoreboardBar {
    /**
     * The lines of the Sidebar (0-14)
     */
    public final ArrayList<SidebarLine> lines = new ArrayList<>();

    /**
     * Default sidebar constructor
     */
    public Sidebar () {
        super("SBAPI-sidebar", "SLSB", 1);
        for (int i = 1; i < 16; ++i)
            lines.add(new SidebarLine(this, (char) (280 + i), "§6§lLine - §e§l" + i, 100 - i));
    }

    @Override
    protected void addViewer (Player plr) {
        viewers.add(plr);
        SU.tp.sendPacket(plr, getObjectivePacket(2));
        for (SidebarLine line : lines)
            if (!line.hidden)
                line.show(plr);
        if (visible)
            SU.tp.sendPacket(plr, showPacket);
    }

    @Override
    protected void addViewerFirstBar (Player plr) {
        viewers.add(plr);
        SU.tp.sendPacket(plr, getObjectivePacket(0));
        for (SidebarLine line : lines)
            if (!line.hidden)
                line.show(plr);
        if (visible)
            SU.tp.sendPacket(plr, showPacket);
    }

    @Override
    protected void moveViewer (ScoreboardBar oldBar, Player plr) {
        oldBar.viewers.remove(plr);
        viewers.add(plr);
        Sidebar old = (Sidebar) oldBar;
        for (int i = 0; i < 15; ++i) {
            old.lines.get(i).hide(plr);
            lines.get(i).show(plr);
        }
        SU.tp.sendPacket(plr, getObjectivePacket(2));
        if (!old.visible && visible)
            SU.tp.sendPacket(plr, showPacket);
        if (!visible && old.visible)
            SU.tp.sendPacket(plr, hidePacket);
    }

    @Override
    protected void removeViewer (Player plr) {
        if (!viewers.remove(plr))
            return;
        if (!plr.isOnline())
            return;
        if (visible)
            SU.tp.sendPacket(plr, hidePacket);
        for (SidebarLine line : lines)
            line.hide(plr);
    }

    public void hideLine (int line) {
        if (line < 1 || line > 15)
            return;
        SidebarLine sl = lines.get(line - 1);
        if (sl.hidden)
            return;
        for (Player p : viewers)
            sl.hide(p);
        sl.hidden = true;
    }

    public void setLine (int line, String text) {
        if (line < 1 || line > 15)
            return;
        SidebarLine sl = lines.get(line - 1);
        if (sl.text.equals(text))
            return;
        sl.setText(text);
        for (Player p : viewers)
            sl.update(p);
    }

    public void setNumber (int line, int number) {
        if (line < 1 || line > 15)
            return;
        SidebarLine sl = lines.get(line - 1);
        if (sl.number == number)
            return;
        sl.number = number;
        for (Player p : viewers)
            SU.tp.sendPacket(p, sl.getScorePacket(sl.user, ScoreboardAPI.setScore));
    }

    public void showLine (int line) {
        if (line < 1 || line > 15)
            return;
        SidebarLine sl = lines.get(line - 1);
        if (!sl.hidden)
            return;
        sl.hidden = false;
        for (Player p : viewers)
            sl.show(p);
    }
}


