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
    public final LinkedList<UUID> viewers = new LinkedList<>();
    protected boolean visible = true;
    private String title;
    private ScoreboardAPI.ScoreboardDisplayMode displayMode = ScoreboardAPI.ScoreboardDisplayMode.INTEGER;

    public ScoreboardBar(String barname, String teamNamePrefix, int displaySlot) {
        this.barname = barname;
        this.title = barname;
        this.teamNamePrefix = teamNamePrefix;
        this.showPacket = PacketOutType.ScoreboardDisplayObjective.newPacket(displaySlot, barname);
        this.hidePacket = PacketOutType.ScoreboardDisplayObjective.newPacket(displaySlot, "");
    }

    /**
     * Creates a ScoreboardObjective packet.
     *
     * @param id - the type of the objectivePacket:
     *           0 - create
     *           1 - remove
     *           2 - update
     * @return The packet.
     */
    public Object getObjectivePacket(int id) {
        return PacketOutType.ScoreboardObjective.newPacket(this.barname, this.title, this.displayMode.nmsEnum, id);
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
        displayMode = mode;
        sendPackets(getObjectivePacket(2));
    }

    public void setTitle(String newtitle) {
        title = SU.setLength(newtitle, 32);
        sendPackets(getObjectivePacket(2));
    }

    public void sendPackets(Object... packets) {
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
            sendPackets(visible ? this.showPacket : this.hidePacket);
            this.visible = visible;
        }
    }
}

