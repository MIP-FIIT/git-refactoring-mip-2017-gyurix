package gyurix.scoreboard;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import gyurix.protocol.PacketOutType;
import gyurix.spigotlib.ChatAPI;
import org.bukkit.entity.Player;

public class TabPlayer {
    public String tabname;
    public GameProfile profile;
    public int number;

    public TabPlayer(GameProfile profile) {
    }

    public TabPlayer(Player plr) {
        this.profile = new GameProfile(plr.getUniqueId(), plr.getName());
        this.tabname = "\u00a7c\u00a7lTABNAME:\u00a7e" + plr.getDisplayName();
        this.number = ScoreboardAPI.id++;
    }

    public Object getTabnameSetPacket() {
        try {
            return PacketOutType.PlayerInfo.newPacket(ScoreboardAPI.updateTabName, Lists.newArrayList((Object[]) new Object[]{ScoreboardAPI.tabPlayer.newInstance(PacketOutType.PlayerInfo.newPacket(), this.profile, 0, null, ChatAPI.toICBC(ChatAPI.TextToJson(this.tabname)))}));
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object getTabnameRestorePacket() {
        try {
            return PacketOutType.PlayerInfo.newPacket(ScoreboardAPI.updateTabName, Lists.newArrayList((Object[]) new Object[]{ScoreboardAPI.tabPlayer.newInstance(null, this.profile, 0, null, null)}));
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object getSetScorePacket() {
        return PacketOutType.ScoreboardScore.newPacket(this.profile.getName(), "SBAPI-tabbar", this.number, ScoreboardAPI.setScore);
    }

    public Object getRemoveScorePacket() {
        return PacketOutType.ScoreboardScore.newPacket(this.profile.getName(), "SBAPI-tabbar", 0, ScoreboardAPI.removeScore);
    }
}

