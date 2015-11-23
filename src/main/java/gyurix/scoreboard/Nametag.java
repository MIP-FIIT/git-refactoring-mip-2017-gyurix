package gyurix.scoreboard;

import com.google.common.collect.Lists;
import gyurix.protocol.PacketOutType;
import org.bukkit.entity.Player;

public class Nametag {
    public boolean hide;
    public int number;
    public String name;
    public String prefix = "\u00a7e\u00a7lprefix -> \u00a7b";
    public String suffix = "\u00a7e\u00a7l <- suffix";

    public Nametag() {
    }

    public Nametag(Player plr) {
        this.number = ScoreboardAPI.id++;
        this.name = plr.getName();
    }

    public Object getTeamPacket(int action) {
        Object[] arrobject = new Object[9];
        arrobject[0] = this.name;
        arrobject[1] = this.name;
        arrobject[2] = this.prefix;
        arrobject[3] = this.suffix;
        arrobject[4] = this.hide ? "never" : "always";
        arrobject[5] = 0;
        arrobject[6] = Lists.newArrayList((Object[]) new String[]{this.name});
        arrobject[7] = action;
        arrobject[8] = 0;
        return PacketOutType.ScoreboardTeam.newPacket(arrobject);
    }

    public Object getSetScorePacket() {
        return PacketOutType.ScoreboardScore.newPacket(this.name, "SBAPI-nametag", this.number, ScoreboardAPI.setScore);
    }

    public Object getRemoveScorePacket() {
        return PacketOutType.ScoreboardScore.newPacket(this.name, "SBAPI-nametag", 0, ScoreboardAPI.removeScore);
    }
}

