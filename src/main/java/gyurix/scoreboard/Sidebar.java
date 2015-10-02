package gyurix.scoreboard;

import gyurix.protocol.PacketOutType;
import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

/**
 * Created by GyuriX on 2015.07.20..
 */
public class Sidebar extends ScoreboardBar{
    public ArrayList<SidebarLine> lines=new ArrayList<SidebarLine>();
    public Sidebar(){
        super("SBAPI-sidebar",1);
        for (int i=1;i<16;i++){
            SidebarLine line=new SidebarLine((char)(280+i));
            lines.add(line);
            line.setText("§6§lLine §e§l"+i);
            line.number=100-i;
        }
    }
    public void showLine(int line){
        SidebarLine sl=lines.get(line-1);
        if (sl.hide) {
            sl.hide = false;
            Iterator<UUID> it=viewers.iterator();
            while (it.hasNext()){
                Player p= Bukkit.getPlayer(it.next());
                if (p==null)
                    it.remove();
                else{
                    sl.show(p);
                }
            }
        }
    }
    public void hideLine(int line){
        SidebarLine sl=lines.get(line-1);
        if (!sl.hide) {
            Iterator<UUID> it=viewers.iterator();
            while (it.hasNext()){
                Player p= Bukkit.getPlayer(it.next());
                if (p==null)
                    it.remove();
                else
                    sl.hide(p);
            }
            sl.hide = true;
        }
    }
    public void setLine(int line,String text){
        SidebarLine sl=lines.get(line-1);
        sl.setText(text);
        Iterator<UUID> it=viewers.iterator();
        while (it.hasNext()){
            Player p= Bukkit.getPlayer(it.next());
            if (p==null)
                it.remove();
            else
                sl.update(p);
        }
    }
    public void setNumber(int line,int number){
        SidebarLine sl=lines.get(line-1);
        sl.number=number;
        Iterator<UUID> it=viewers.iterator();
        while (it.hasNext()){
            Player p= Bukkit.getPlayer(it.next());
            if (p==null)
                it.remove();
            else
                sl.updateNumber(p);
        }
    }

    @Override
    public void addViewer(Player plr) {
        viewers.add(plr.getUniqueId());
        sendUpdatePacket(plr);
        for (SidebarLine line:lines){
            if (!line.hide){
                line.show(plr);
            }
        }
        SU.tp.sendPacket(plr,showPacket);
    }

    @Override
    public void addViewerFirstBar(Player plr) {
        System.out.println("First bar: "+plr.getName());
        viewers.add(plr.getUniqueId());
        sendCreatePacket(plr);
        for (SidebarLine line:lines){
            if (!line.hide){
                line.show(plr);
            }
        }
        SU.tp.sendPacket(plr,showPacket);
    }

    @Override
    public void moveViewer(ScoreboardBar oldBar, Player plr) {
        System.out.println("Move: "+plr.getName());
        UUID uuid=plr.getUniqueId();
        oldBar.viewers.remove(uuid);
        viewers.add(uuid);
        Sidebar old= (Sidebar) oldBar;
        for (int i=0;i<15;i++){
            old.lines.get(i).hide(plr);
            lines.get(i).show(plr);
        }
        sendUpdatePacket(plr);
    }

    @Override
    public void removeViewer(Player plr) {
        if (!viewers.remove(plr.getUniqueId()))
            return;
        if (!plr.isOnline())
            return;
        SU.tp.sendPacket(plr,hidePacket);
        for (SidebarLine line:lines){
            line.hide(plr);
        }
    }
}
