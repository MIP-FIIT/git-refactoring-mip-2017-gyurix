package gyurix.scoreboard;

import gyurix.protocol.PacketOutType;
import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by GyuriX on 2015.07.22..
 */
public abstract class ScoreboardBar {
    LinkedList<UUID> viewers=new LinkedList<UUID>();
    public final String barname;
    private String title;
    public final Object showPacket,hidePacket;
    private ScoreboardAPI.ScoreboardDisplayMode displayMode= ScoreboardAPI.ScoreboardDisplayMode.INTEGER;
    public ScoreboardBar(String barname, int displaySlot) {
        this.barname = barname;
        title=barname;
        showPacket = PacketOutType.ScoreboardDisplayObjective.newPacket(displaySlot, barname);
        hidePacket = PacketOutType.ScoreboardDisplayObjective.newPacket(displaySlot, "");
    }
    public void sendUpdatePacket(Player plr){
        SU.tp.sendPacket(plr,PacketOutType.ScoreboardObjective.newPacket(barname, title, displayMode.nmsEnum, 2));
    }
    public void sendCreatePacket(Player plr){
        SU.tp.sendPacket(plr,PacketOutType.ScoreboardObjective.newPacket(barname, title, displayMode.nmsEnum, 0));
    }
    public abstract void addViewer(Player plr);
    public abstract void addViewerFirstBar(Player plr);
    public abstract void moveViewer(ScoreboardBar oldBar,Player plr);
    public abstract void removeViewer(Player plr);
    public ScoreboardAPI.ScoreboardDisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(ScoreboardAPI.ScoreboardDisplayMode mode){
        if (displayMode==mode)
            return;
        displayMode=mode;
        sendPackets(PacketOutType.ScoreboardObjective.newPacket(barname, title,
                displayMode.nmsEnum, 2));
    }
    public void setTitle(String newtitle){
        title=SU.setLength(newtitle,32);
        sendPackets(PacketOutType.ScoreboardObjective.newPacket(barname, title,
                displayMode.nmsEnum, 2));
    }
    public void sendPackets(Object... packets){
        for (Object p:packets){
            Iterator<UUID> it=viewers.iterator();
            while (it.hasNext()){
                Player plr=Bukkit.getPlayer(it.next());
                if (plr==null||!plr.isOnline())
                    it.remove();
                else
                    SU.tp.sendPacket(plr,p);
            }
        }
    }
}
