package gyurix.scoreboard;

import com.mojang.authlib.GameProfile;
import gyurix.protocol.event.PacketOutType;
import org.bukkit.entity.Player;

public class TabPlayer {
    public int number;
    public GameProfile profile;
    public String tabname;

    public TabPlayer(GameProfile profile) {
    }

    public TabPlayer(Player plr) {
        this.profile = new GameProfile(plr.getUniqueId(), plr.getName());
        this.tabname = "\u00a7c\u00a7lTABNAME:\u00a7e" + plr.getDisplayName();
        this.number = ScoreboardAPI.id++;
    }

    public Object getRemoveScorePacket() {
        return PacketOutType.ScoreboardScore.newPacket(this.profile.getName(), "SBAPI-tabbar", 0, ScoreboardAPI.removeScore);
    }

    public Object getSetScorePacket() {
        return PacketOutType.ScoreboardScore.newPacket(this.profile.getName(), "SBAPI-tabbar", this.number, ScoreboardAPI.setScore);
    }

    public Object getTabnameRestorePacket() {
        try {
            //return PacketOutType.PlayerInfo.newPacket(ScoreboardAPI.updateTabName, Lists.newArrayList((Object[]) new Object[]{ScoreboardAPI.tabPlayer.newInstance(null, this.profile, 0, null, null)}));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object getTabnameSetPacket() {
        try {
            //return PacketOutType.PlayerInfo.newPacket(ScoreboardAPI.updateTabName, Lists.newArrayList((Object[]) new Object[]{ScoreboardAPI.tabPlayer.newInstance(PacketOutType.PlayerInfo.newPacket(), this.profile, 0, null, ChatAPI.toICBC(ChatAPI.TextToJson(this.tabname)))}));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}

