package gyurix.scoreboard;

import com.google.common.collect.Lists;
import gyurix.protocol.PacketOutType;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

public class SidebarLine {
    public final Sidebar bar;
    public final char uniqueChar;
    public boolean hide;
    public int number;
    public String oldUser;
    public String user;
    public String prefix = "";
    public String suffix = "";

    public SidebarLine(Sidebar bar, char ch) {
        this.bar = bar;
        this.uniqueChar = ch;
        this.oldUser = this.user = "\u00a7" + this.uniqueChar;
    }

    public void show(Player plr) {
        if (this.hide) {
            return;
        }
        SU.tp.sendPacket(plr, PacketOutType.ScoreboardTeam.newPacket(this.bar.teamNamePrefix + this.uniqueChar, this.bar.teamNamePrefix + this.uniqueChar, this.prefix, this.suffix, "always", 0, Lists.newArrayList((Object[]) new String[]{this.user}), 0, 0));
        SU.tp.sendPacket(plr, PacketOutType.ScoreboardScore.newPacket(this.user, this.bar.barname, this.number, ScoreboardAPI.setScore));
    }

    public void hide(Player plr) {
        if (this.hide) {
            return;
        }
        SU.tp.sendPacket(plr, PacketOutType.ScoreboardTeam.newPacket(this.bar.teamNamePrefix + this.uniqueChar, this.bar.teamNamePrefix + this.uniqueChar, this.prefix, this.suffix, "always", 0, Lists.newArrayList((Object[]) new String[]{this.user}), 1, 0));
        SU.tp.sendPacket(plr, PacketOutType.ScoreboardScore.newPacket(this.user, this.bar.barname, this.number, ScoreboardAPI.removeScore));
    }

    public void updateNumber(Player plr) {
        SU.tp.sendPacket(plr, PacketOutType.ScoreboardScore.newPacket(this.user, this.bar.barname, this.number, ScoreboardAPI.setScore));
    }

    public void update(Player plr) {
        SU.tp.sendPacket(plr, PacketOutType.ScoreboardTeam.newPacket(this.bar.teamNamePrefix + this.uniqueChar, this.bar.teamNamePrefix + this.uniqueChar, this.prefix, this.suffix, "always", 0, Lists.newArrayList((Object[]) new String[]{this.user}), 2, 0));
        if (!this.user.equals(this.oldUser)) {
            SU.tp.sendPacket(plr, PacketOutType.ScoreboardTeam.newPacket(this.bar.teamNamePrefix + this.uniqueChar, this.bar.teamNamePrefix + this.uniqueChar, this.prefix, this.suffix, "always", 0, Lists.newArrayList((Object[]) new String[]{this.user}), 3, 0));
            SU.tp.sendPacket(plr, PacketOutType.ScoreboardScore.newPacket(this.oldUser, this.bar.barname, this.number, ScoreboardAPI.removeScore));
            SU.tp.sendPacket(plr, PacketOutType.ScoreboardScore.newPacket(this.user, this.bar.barname, this.number, ScoreboardAPI.setScore));
            SU.tp.sendPacket(plr, PacketOutType.ScoreboardTeam.newPacket(this.bar.teamNamePrefix + this.uniqueChar, this.bar.teamNamePrefix + this.uniqueChar, this.prefix, this.suffix, "always", 0, Lists.newArrayList((Object[]) new String[]{this.oldUser}), 4, 0));
        }
    }

    public void setText(String text) {
        this.oldUser = this.user;
        String[] set = ScoreboardAPI.specialSplit(text, this.uniqueChar);
        this.prefix = set[0];
        this.user = set[1];
        this.suffix = set[2];
    }
}

