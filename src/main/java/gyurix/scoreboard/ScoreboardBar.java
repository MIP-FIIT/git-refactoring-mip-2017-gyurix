package gyurix.scoreboard;

import gyurix.protocol.event.PacketOutType;
import gyurix.scoreboard.ScoreboardAPI.ScoreboardDisplayMode;
import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

public abstract class ScoreboardBar {
    public final String barname;
    public final Object hidePacket;
    public final Object showPacket;
    public final String teamNamePrefix;
    public final LinkedList<UUID> viewers = new LinkedList<>();
    protected boolean visible = true;
    private ScoreboardDisplayMode displayMode = ScoreboardDisplayMode.INTEGER;
    private String title;

    /**
     * Construct new ScoreboardBar
     *
     * @param barname        - The name of this ScoreboardBar
     * @param teamNamePrefix - The prefix of the Scoreboard team packets
     * @param displaySlot    - The slot of this ScoreboardBar (0: list, 1: sidebar, 2: below name)
     */
    public ScoreboardBar(String barname, String teamNamePrefix, int displaySlot) {
        this.barname = barname;
        title = barname;
        this.teamNamePrefix = teamNamePrefix;
        showPacket = PacketOutType.ScoreboardDisplayObjective.newPacket(displaySlot, barname);
        hidePacket = PacketOutType.ScoreboardDisplayObjective.newPacket(displaySlot, "");
    }

    public abstract void addViewer(Player var1);

    public abstract void addViewerFirstBar(Player var1);

    public ScoreboardDisplayMode getDisplayMode() {
        return displayMode;
    }

    /**
     * Sets the display mode of this Scoreboard, it has no effect on sidebar.
     *
     * @param mode - The new displaymode
     */
    public void setDisplayMode(ScoreboardDisplayMode mode) {
        if (displayMode == mode) {
            return;
        }
        displayMode = mode;
        sendPackets(getObjectivePacket(2));
    }

    /**
     * Creates a ScoreboardObjective packet.
     *
     * @param id - the type of the objectivePacket: 0 - create 1 - remove 2 - update
     * @return The packet.
     */
    Object getObjectivePacket(int id) {
        return PacketOutType.ScoreboardObjective.newPacket(barname, title, displayMode.nmsEnum, id);
    }

    /**
     * Check if this scoreboard bar is visible or not
     *
     * @return The scoreboard bars visibility
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Toggles the visibility of this scoreboard bar
     *
     * @param visible - The new visibility state
     */
    public void setVisible(boolean visible) {
        if (visible != this.visible) {
            sendPackets(visible ? showPacket : hidePacket);
            this.visible = visible;
        }
    }

    public abstract void moveViewer(ScoreboardBar oldBar, Player plr);

    public abstract void removeViewer(Player plr);

    public void sendPackets(Object... packets) {
        for (Object p : packets) {
            Iterator<UUID> it = viewers.iterator();
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

    /**
     * Sets the title of this scoreboard bar.
     *
     * @param newtitle - The new title
     */
    public void setTitle(String newtitle) {
        title = SU.setLength(newtitle, 32);
        sendPackets(getObjectivePacket(2));
    }
}

