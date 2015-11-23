package gyurix.scoreboard;

import gyurix.protocol.PacketOutType;
import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

public abstract class ScoreboardBar {
    public final String barname;
    public final String teamNamePrefix;
    public final Object showPacket;
    public final Object hidePacket;
    protected boolean visible = true;
    LinkedList<UUID> viewers = new LinkedList<>();
    private String title;
    private ScoreboardAPI.ScoreboardDisplayMode displayMode = ScoreboardAPI.ScoreboardDisplayMode.INTEGER;

    public ScoreboardBar(String barname, String teamNamePrefix, int displaySlot) {
        this.barname = barname;
        this.title = barname;
        this.teamNamePrefix = teamNamePrefix;
        this.showPacket = PacketOutType.ScoreboardDisplayObjective.newPacket(displaySlot, barname);
        this.hidePacket = PacketOutType.ScoreboardDisplayObjective.newPacket(displaySlot, "");
    }

    public void sendUpdatePacket(Player plr) {
        SU.tp.sendPacket(plr, PacketOutType.ScoreboardObjective.newPacket(this.barname, this.title, this.displayMode.nmsEnum, 2));
    }

    public void sendCreatePacket(Player plr) {
        SU.tp.sendPacket(plr, PacketOutType.ScoreboardObjective.newPacket(this.barname, this.title, this.displayMode.nmsEnum, 0));
    }

    public abstract void addViewer(Player var1);

    public abstract void addViewerFirstBar(Player var1);

    public abstract void moveViewer(ScoreboardBar var1, Player var2);

    public abstract void removeViewer(Player var1);

    public ScoreboardAPI.ScoreboardDisplayMode getDisplayMode() {
        return this.displayMode;
    }

    public void setDisplayMode(ScoreboardAPI.ScoreboardDisplayMode mode) {
        if (this.displayMode == mode) {
            return;
        }
        this.displayMode = mode;
        this.sendPackets(PacketOutType.ScoreboardObjective.newPacket(this.barname, this.title, this.displayMode.nmsEnum, 2));
    }

    public void setTitle(String newtitle) {
        this.title = SU.setLength(newtitle, 32);
        this.sendPackets(PacketOutType.ScoreboardObjective.newPacket(this.barname, this.title, this.displayMode.nmsEnum, 2));
    }

    public /* varargs */ void sendPackets(Object... packets) {
        for (Object p : packets) {
            Iterator<UUID> it = this.viewers.iterator();
            while (it.hasNext()) {
                Player plr = Bukkit.getPlayer(it.next());
                if (plr == null || !plr.isOnline()) {
                    it.remove();
                    continue;
                }
                SU.tp.sendPacket(plr, p);
            }
        }
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        if (visible != this.visible) {
            Object[] arrobject = new Object[1];
            arrobject[0] = visible ? this.showPacket : this.hidePacket;
            this.sendPackets(arrobject);
            this.visible = visible;
        }
    }
}

