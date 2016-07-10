package gyurix.scoreboard;

import com.google.common.collect.Lists;
import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.ServerVersion;
import org.bukkit.entity.Player;

public class SidebarLine {
    public final Sidebar bar;
    public final char uniqueChar;
    public boolean hidden;
    public int number;
    public String oldUser;
    public String prefix = "";
    public String suffix = "";
    public String text;
    public String user;

    public SidebarLine (Sidebar bar, char ch) {
        this.bar = bar;
        uniqueChar = ch;
        oldUser = user = "§" + uniqueChar;
    }

    public SidebarLine (Sidebar bar, char ch, String text, int number) {
        this.bar = bar;
        uniqueChar = ch;
        oldUser = user = "§" + uniqueChar;
        this.text = text;
        this.number = number;
    }

    public void hide (Player plr) {
        if (hidden)
            return;
        SU.tp.sendPacket(plr, getTeamPacket(user, 1));
        SU.tp.sendPacket(plr, getScorePacket(user, ScoreboardAPI.removeScore));
    }

    /**
     * Get the team packet of this Scoreboard line
     *
     * @param user   - The user in the team
     * @param action - The action option of the packet: 0 - create team 1 - remove team 2 - update team info 3 - add
     *               players 4 - remove players
     * @return The Team packet.
     */
    public Object getTeamPacket (String user, int action) {
        if (Reflection.ver == ServerVersion.v1_8)
            return PacketOutType.ScoreboardTeam.newPacket(bar.teamNamePrefix + uniqueChar, bar.teamNamePrefix + uniqueChar, prefix, suffix, "always", 0, Lists.newArrayList(user), action, 0);
        else
            return PacketOutType.ScoreboardTeam.newPacket(bar.teamNamePrefix + uniqueChar, bar.teamNamePrefix + uniqueChar, prefix, suffix, "always", "always", 0, Lists.newArrayList(user), action, 0);
    }

    /**
     * Get the score packet of this Scoreboard line
     *
     * @param user   - Target user
     * @param action - The action (setScore or removeScore)
     * @return The Score packet.
     */
    public Object getScorePacket (String user, Object action) {
        return PacketOutType.ScoreboardScore.newPacket(user, bar.barname, number, action);
    }

    public void setText (String text) {
        oldUser = user;
        String[] set = ScoreboardAPI.specialSplit(text, uniqueChar);
        prefix = set[0];
        user = set[1];
        suffix = set[2];
        this.text = text;
    }

    public void show (Player plr) {
        if (hidden)
            return;
        SU.tp.sendPacket(plr, getTeamPacket(user, 0));
        SU.tp.sendPacket(plr, getScorePacket(user, ScoreboardAPI.setScore));
    }

    public void update (Player plr) {
        SU.tp.sendPacket(plr, getTeamPacket(user, 2));
        if (!user.equals(oldUser)) {
            SU.tp.sendPacket(plr, getTeamPacket(user, 3));
            SU.tp.sendPacket(plr, getScorePacket(user, ScoreboardAPI.setScore));
            SU.tp.sendPacket(plr, getScorePacket(oldUser, ScoreboardAPI.removeScore));
            SU.tp.sendPacket(plr, getTeamPacket(oldUser, 4));
        }
    }
}

