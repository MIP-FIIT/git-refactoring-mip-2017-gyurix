package gyurix.scoreboard;

import com.google.common.collect.Lists;
import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.spigotutils.ServerVersion;
import org.bukkit.entity.Player;

public class Nametag {
    public boolean hide;
    public String name;
    public int number;
    public String prefix = "§f";
    public String suffix = "§f";

    public Nametag() {
    }

    public Nametag(Player plr) {
        this.number = ScoreboardAPI.id++;
        this.name = plr.getName();
    }

    public Object getRemoveScorePacket() {
        return PacketOutType.ScoreboardScore.newPacket(this.name, "SBAPI-nametag", 0, ScoreboardAPI.removeScore);
    }

    public Object getSetScorePacket() {
        return PacketOutType.ScoreboardScore.newPacket(this.name, "SBAPI-nametag", this.number, ScoreboardAPI.setScore);
    }

    public Object getTeamPacket(int action) {
        Object[] args = new Object[Reflection.ver == ServerVersion.v1_8 ? 9 : 10];
        args[0] = this.name;
        args[1] = this.name;
        args[2] = this.prefix;
        args[3] = this.suffix;
        args[4] = this.hide ? "never" : "always";
        if (Reflection.ver == ServerVersion.v1_8) {
            args[5] = 0;
            args[6] = Lists.newArrayList((Object[]) new String[]{this.name});
            args[7] = action;
            args[8] = 0;
        } else {
            args[5] = "always";
            args[6] = 0;
            args[7] = Lists.newArrayList((Object[]) new String[]{this.name});
            args[8] = action;
            args[9] = 0;
        }
        return PacketOutType.ScoreboardTeam.newPacket(args);
    }
}

