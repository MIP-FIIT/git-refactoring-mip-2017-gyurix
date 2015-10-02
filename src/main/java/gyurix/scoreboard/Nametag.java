package gyurix.scoreboard;

import com.google.common.collect.Lists;
import gyurix.protocol.PacketOutType;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

/**
 * Created by GyuriX on 2015.07.22..
 */
public class Nametag {
    public boolean hide;
    public int number;
    public String name;
    public String prefix="§e§lprefix -> §b";
    public String suffix="§e§l <- suffix";
    public Nametag(){

    }
    public Nametag(Player plr){
        number=ScoreboardAPI.id++;
        name=plr.getName();
    }
    public Object getTeamPacket(int action){
        return PacketOutType.ScoreboardTeam.newPacket(
                name, name, //name, displayname
                prefix,suffix, //prefix, suffix
                hide?"never":"always", 0, //nametag visibility, color
                Lists.newArrayList(name), action, 0); //users, action, flags
    }
    public Object getSetScorePacket() {
        return PacketOutType.ScoreboardScore.newPacket(name, "SBAPI-nametag", number, ScoreboardAPI.setScore);
    }
    public Object getRemoveScorePacket() {
        return PacketOutType.ScoreboardScore.newPacket(name, "SBAPI-nametag", 0, ScoreboardAPI.removeScore);
    }
}
