package gyurix.scoreboard;

import gyurix.protocol.PacketOutType;
import gyurix.spigotlib.ChatAPI;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by GyuriX on 2015.07.21..
 */
public class Tabbar extends ScoreboardBar{
    public String header="§b§lTest header",footer="§c§l--------------------\nTabbar, made by gyuriX\n§c§l--------------------";
    public ArrayList<UUID> viewers=new ArrayList<UUID>();
    public HashMap<UUID,TabPlayer> pls=new HashMap<UUID, TabPlayer>();
    public boolean hide;
    public Tabbar(){
        super("SBAPI-tabbar",0);
    }
    public void addPlayer(UUID uuid,TabPlayer tabPlayer){
        pls.put(uuid, tabPlayer);
        sendPackets(tabPlayer.getTabnameSetPacket(),tabPlayer.getSetScorePacket());
    }
    public void removePlayer(UUID uuid){
        TabPlayer tp=pls.remove(uuid);
        if (tp!=null)
            sendPackets(tp.getRemoveScorePacket(),tp.getTabnameRestorePacket());
    }
    public void setHeaderFooter(String header,String footer){
        sendPackets(PacketOutType.PlayerListHeaderFooter.newPacket(
                ChatAPI.toICBC(ChatAPI.TextToJson(this.header = header)), ChatAPI.toICBC(ChatAPI.TextToJson(this.footer = footer))));
    }
    public void setTabName(UUID plID,String name){
        TabPlayer tp=pls.get(plID);
        tp.tabname=name;
        sendPackets(tp.getTabnameSetPacket());
    }
    public void hide(){
        if (hide) {
            System.err.println("§eHIDING ALREADY SHOWED TABBAR?");
            return;
        }
        sendPackets(hidePacket);
        hide=true;
    }
    public void show(){
        if (!hide) {
            System.err.println("§eSHOWING ALREADY SHOWED TABBAR?");
            return;
        }
        sendPackets(showPacket);
        hide=false;
    }
    public void setNumber(Player plr,int number){
        TabPlayer sp=pls.get(plr.getUniqueId());
        sp.number=number;
        sendPackets(sp.getSetScorePacket(),showPacket);
    }
    @Override
    public void addViewer(Player plr) {
        System.out.println("§cTB: §aAdd viewer: "+plr.getName());
        viewers.add(plr.getUniqueId());
        sendUpdatePacket(plr);
        for (TabPlayer p:pls.values()){
            SU.tp.sendPacket(plr,p.getSetScorePacket());
            SU.tp.sendPacket(plr,p.getTabnameSetPacket());
        }
        if (!hide)
            SU.tp.sendPacket(plr,showPacket);
        SU.tp.sendPacket(plr,PacketOutType.PlayerListHeaderFooter.newPacket(
                ChatAPI.toICBC(ChatAPI.TextToJson(header)),ChatAPI.toICBC(ChatAPI.TextToJson(footer))));
    }

    @Override
    public void addViewerFirstBar(Player plr) {
        System.out.println("§cTB: §2Add viewer FIRST BAR: "+plr.getName());
        viewers.add(plr.getUniqueId());
        sendCreatePacket(plr);
        for (TabPlayer p:pls.values()){
            SU.tp.sendPacket(plr,p.getSetScorePacket());
            SU.tp.sendPacket(plr,p.getTabnameSetPacket());
        }
        if (!hide)
            SU.tp.sendPacket(plr,showPacket);
        SU.tp.sendPacket(plr,PacketOutType.PlayerListHeaderFooter.newPacket(
                ChatAPI.toICBC(ChatAPI.TextToJson(header)),ChatAPI.toICBC(ChatAPI.TextToJson(footer))));
    }

    @Override
    public void moveViewer(ScoreboardBar oldBar, Player plr) {
        System.out.println("§cTB: §eMove viewer: "+plr.getName());
        Tabbar old= (Tabbar) oldBar;
        viewers.add(plr.getUniqueId());
        sendUpdatePacket(plr);
        for (TabPlayer p:pls.values()){
            TabPlayer op=old.pls.get(p.profile.getId());
            if (op==null||op.number!=p.number)
                SU.tp.sendPacket(plr,p.getSetScorePacket());
            if (op==null||!op.tabname.equals(p.tabname))
                SU.tp.sendPacket(plr,p.getTabnameSetPacket());
        }
        for (TabPlayer p:old.pls.values()){
            if (!pls.containsKey(p.profile.getId())){
                SU.tp.sendPacket(plr,p.getRemoveScorePacket());
                SU.tp.sendPacket(plr,p.getTabnameRestorePacket());
            }
        }
        if (old.hide&&!hide)
            SU.tp.sendPacket(plr,showPacket);
        if (hide&&!old.hide)
            SU.tp.sendPacket(plr,hidePacket);
        if (!(old.header.equals(header)&&old.footer.equals(footer))){
            SU.tp.sendPacket(plr,PacketOutType.PlayerListHeaderFooter.newPacket(
                    ChatAPI.toICBC(ChatAPI.TextToJson(header)),ChatAPI.toICBC(ChatAPI.TextToJson(footer))));
        }
    }

    @Override
    public void removeViewer(Player plr) {
        System.out.println("§cTB: §cRemove viewer: "+plr.getName());
        if (!viewers.remove(plr.getUniqueId()))
            return;
        if (!plr.isOnline())
            return;
        for (TabPlayer p : pls.values()) {
            SU.tp.sendPacket(plr, p.getRemoveScorePacket());
            SU.tp.sendPacket(plr, p.getTabnameRestorePacket());
        }
        SU.tp.sendPacket(plr, PacketOutType.PlayerListHeaderFooter.newPacket(
                ChatAPI.toICBC(ChatAPI.TextToJson("")), ChatAPI.toICBC(ChatAPI.TextToJson(""))));
        if (!hide)
            SU.tp.sendPacket(plr, hidePacket);

    }
}
