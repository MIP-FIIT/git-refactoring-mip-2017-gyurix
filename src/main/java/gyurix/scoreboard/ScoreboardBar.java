package gyurix.scoreboard;

import gyurix.protocol.event.PacketOutType;
import gyurix.scoreboard.ScoreboardAPI.ScoreboardDisplayMode;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.HashSet;

public abstract class ScoreboardBar {
    public final String barname;
    public final Object hidePacket;
    public final Object showPacket;
    public final String teamNamePrefix;
    public final HashSet<Player> viewers = new HashSet<>();
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
    public ScoreboardBar (String barname, String teamNamePrefix, int displaySlot) {
        this.barname = barname;
        title = barname;
        this.teamNamePrefix = teamNamePrefix;
        showPacket = PacketOutType.ScoreboardDisplayObjective.newPacket(displaySlot, barname);
        hidePacket = PacketOutType.ScoreboardDisplayObjective.newPacket(displaySlot, "");
    }

    protected abstract void addViewer (Player var1);

    protected abstract void addViewerFirstBar (Player var1);

    public ScoreboardDisplayMode getDisplayMode () {
        return displayMode;
    }

    /**
     * Sets the display mode of this Scoreboard, it has no effect on sidebar.
     *
     * @param mode - The new displaymode
     */
    public void setDisplayMode (ScoreboardDisplayMode mode) {
        if (displayMode == mode)
            return;
        displayMode = mode;
        sendPackets(getObjectivePacket(2));
    }

    public void sendPackets (Object... packets) {
        for (Object p : packets)
            for (Player plr : viewers)
                SU.tp.sendPacket(plr, p);
    }

    /**
     * Creates a ScoreboardObjective packet.
     *
     * @param id - the type of the objectivePacket: 0 - create 1 - remove 2 - update
     * @return The packet.
     */
    protected Object getObjectivePacket (int id) {
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

    protected abstract void moveViewer(ScoreboardBar oldBar, Player plr);

    protected abstract void removeViewer(Player plr);

    /**
     * Sets the title of this scoreboard bar.
     *
     * @param newtitle - The new title
     */
    public void setTitle (String newtitle) {
        newtitle = SU.setLength(newtitle, 32);
        if (title.equals(newtitle))
            return;
        title = newtitle;
        sendPackets(getObjectivePacket(2));
    }
}

