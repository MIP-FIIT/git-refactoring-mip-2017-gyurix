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
        profile = new GameProfile(plr.getUniqueId(), plr.getName());
        tabname = "\u00a7c\u00a7lTABNAME:\u00a7e" + plr.getDisplayName();
        number = ScoreboardAPI.id++;
    }

    public Object getRemoveScorePacket() {
        return PacketOutType.ScoreboardScore.newPacket(profile.getName(), "SBAPI-tabbar", 0, ScoreboardAPI.removeScore);
    }

    public Object getSetScorePacket() {
        return PacketOutType.ScoreboardScore.newPacket(profile.getName(), "SBAPI-tabbar", number, ScoreboardAPI.setScore);
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

