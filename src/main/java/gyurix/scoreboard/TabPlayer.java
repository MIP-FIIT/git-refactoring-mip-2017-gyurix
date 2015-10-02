package gyurix.scoreboard;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import gyurix.protocol.PacketOutType;
import gyurix.spigotlib.ChatAPI;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

/**
 * Created by GyuriX on 2015.07.21..
 */
public class TabPlayer {
    public String tabname;
    public GameProfile profile;
    public int number;
    public TabPlayer(GameProfile profile){

    }
    public TabPlayer(Player plr) {
        profile=new GameProfile(plr.getUniqueId(), plr.getName());
        tabname="§c§lTABNAME:§e"+plr.getDisplayName();
        number=ScoreboardAPI.id++;
    }
    public Object getTabnameSetPacket(){
        try {
            return PacketOutType.PlayerInfo.newPacket(ScoreboardAPI.updateTabName, Lists.newArrayList(ScoreboardAPI.tabPlayer.newInstance(
                    PacketOutType.PlayerInfo.newPacket(), profile, 0, null, ChatAPI.toICBC(ChatAPI.TextToJson(tabname)))));
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
    public Object getTabnameRestorePacket(){
        try {
            return PacketOutType.PlayerInfo.newPacket(ScoreboardAPI.updateTabName, Lists.newArrayList(ScoreboardAPI.tabPlayer.newInstance(
                    null, profile, 0, null, null)));
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object getSetScorePacket() {
        return PacketOutType.ScoreboardScore.newPacket(profile.getName(), "SBAPI-tabbar", number, ScoreboardAPI.setScore);
    }
    public Object getRemoveScorePacket() {
        return PacketOutType.ScoreboardScore.newPacket(profile.getName(), "SBAPI-tabbar", 0, ScoreboardAPI.removeScore);
    }
}
