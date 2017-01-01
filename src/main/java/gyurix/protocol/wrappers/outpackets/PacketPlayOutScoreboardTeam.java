package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.scoreboard.team.CollisionRule;
import gyurix.scoreboard.team.NameTagVisibility;
import gyurix.spigotutils.ServerVersion;

import java.util.ArrayList;
import java.util.Collection;

import static gyurix.protocol.event.PacketOutType.ScoreboardTeam;

/**
 * Created by GyuriX on 11/15/2016.
 */
public class PacketPlayOutScoreboardTeam extends WrappedPacket {
    /**
     * 0: create team
     * 1: remove team
     * 2: update team info
     * 3: add players to team
     * 4: remove players from team
     */
    public int action;
    public CollisionRule collisionRule;
    public int color;
    public String displayName = "";
    /**
     * 0x01: Allow friendly fire
     * 0x02: Can see invisible players on same team
     */
    public int friendFlags;
    public String name = "";
    public NameTagVisibility nameTagVisibility;
    public ArrayList<String> players;
    public String prefix = "";
    public String suffix = "";

    public PacketPlayOutScoreboardTeam(String name, String displayName, String prefix, String suffix, NameTagVisibility nameTagVisibility, CollisionRule collisionRule, int color, ArrayList<String> players, int action, int friendFlags) {
        this.name = name;
        this.displayName = displayName;
        this.prefix = prefix;
        this.suffix = suffix;
        this.nameTagVisibility = nameTagVisibility;
        this.collisionRule = collisionRule;
        this.color = color;
        this.players = players;
        this.action = action;
        this.friendFlags = friendFlags;
    }

    public PacketPlayOutScoreboardTeam() {

    }

    public PacketPlayOutScoreboardTeam(String name, int action) {
        this.name = name;
        this.action = action;
    }

    public PacketPlayOutScoreboardTeam(String name, int action, ArrayList<String> players) {
        this.name = name;
        this.action = action;
        this.players = players;
    }

    @Override
    public Object getVanillaPacket() {
        if (Reflection.ver.isAbove(ServerVersion.v1_9))
            return ScoreboardTeam.newPacket(name, displayName, prefix, suffix, nameTagVisibility.name(), collisionRule.name(), color, players == null ? new ArrayList<>() : new ArrayList<>(players), action, friendFlags);
        else
            return ScoreboardTeam.newPacket(name, displayName, prefix, suffix, nameTagVisibility.name(), color, players == null ? new ArrayList<>() : new ArrayList<>(players), action, friendFlags);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = ScoreboardTeam.getPacketData(packet);
        name = (String) d[0];
        displayName = (String) d[1];
        prefix = (String) d[2];
        suffix = (String) d[3];
        nameTagVisibility = NameTagVisibility.valueOf((String) d[4]);
        int from = 4;
        if (Reflection.ver.isAbove(ServerVersion.v1_9))
            collisionRule = CollisionRule.valueOf((String) d[++from]);
        color = (int) d[++from];
        players = new ArrayList<>((Collection<String>) d[++from]);
        action = (int) d[++from];
        friendFlags = (int) d[++from];
    }
}
