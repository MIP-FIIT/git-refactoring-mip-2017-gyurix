package gyurix.scoreboard;

import com.google.common.collect.Lists;
import gyurix.protocol.PacketOutType;
import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by GyuriX on 2015.07.20..
 */
public class SidebarLine {
    public boolean hide;
    public int number;
    public final char uniqueChar;
    public String oldUser;
    public String user,prefix="",suffix="";
    public SidebarLine(char ch){
        uniqueChar=ch;
        oldUser=user="§"+uniqueChar;
    }
    public void show(Player plr){
        if (hide)
            return;
        SU.tp.sendPacket(plr, PacketOutType.ScoreboardTeam.newPacket(
                "SBT" + uniqueChar,"SBT" + uniqueChar, //name, displayname
                prefix,suffix, //prefix, suffix
                "always",0, //nametag visibility, color
                Lists.newArrayList(user),0,0 //users, action, flags
        ));
        SU.tp.sendPacket(plr, PacketOutType.ScoreboardScore.newPacket(user,"SBAPI-sidebar",number,ScoreboardAPI.setScore));
    }
    public void hide(Player plr){
        if (hide)
            return;
        SU.tp.sendPacket(plr, PacketOutType.ScoreboardTeam.newPacket(
                "SBT" + uniqueChar,"SBT" + uniqueChar, //name, displayname
                prefix,suffix, //prefix, suffix
                "always",0, //nametag visibility, color
                Lists.newArrayList(user),1,0 //users, action, flags
        ));
        SU.tp.sendPacket(plr, PacketOutType.ScoreboardScore.newPacket(user,"SBAPI-sidebar",number,ScoreboardAPI.removeScore));
    }
    public void updateNumber(Player plr){
        SU.tp.sendPacket(plr, PacketOutType.ScoreboardScore.newPacket(user,"SBAPI-sidebar",number,ScoreboardAPI.setScore));
    }
    public void update(Player plr){
        SU.tp.sendPacket(plr, PacketOutType.ScoreboardTeam.newPacket(
                "SBT" + uniqueChar,"SBT" + uniqueChar, //name, displayname
                prefix,suffix, //prefix, suffix
                "always",0, //nametag visibility, color
                Lists.newArrayList(user),2,0 //users, action, flags
        ));
        if (!user.equals(oldUser)){
            SU.tp.sendPacket(plr, PacketOutType.ScoreboardTeam.newPacket(
                    "SBT" + uniqueChar,"SBT" + uniqueChar, //name, displayname
                    prefix,suffix, //prefix, suffix
                    "always",0, //nametag visibility, color
                    Lists.newArrayList(user),3,0 //users, action, flags
            ));
            SU.tp.sendPacket(plr, PacketOutType.ScoreboardScore.newPacket(oldUser,"SBAPI-sidebar",number,ScoreboardAPI.removeScore));
            SU.tp.sendPacket(plr, PacketOutType.ScoreboardScore.newPacket(user,"SBAPI-sidebar",number,ScoreboardAPI.setScore));
            SU.tp.sendPacket(plr, PacketOutType.ScoreboardTeam.newPacket(
                    "SBT" + uniqueChar,"SBT" + uniqueChar, //name, displayname
                    prefix,suffix, //prefix, suffix
                    "always",0, //nametag visibility, color
                    Lists.newArrayList(oldUser),4,0 //users, action, flags
            ));
        }

    }
    public void setText(String text){
        oldUser=user;
        String[] set=ScoreboardAPI.specialSplit(text,uniqueChar);
        prefix=set[0];
        user=set[1];
        suffix=set[2];
    }
}
