package gyurix.inventory;

import gyurix.animation.Animation;
import gyurix.animation.AnimationAPI;
import gyurix.animation.AnimationRunnable;
import gyurix.animation.AnimationUpdateEvent;
import gyurix.api.VariableAPI;
import gyurix.commands.Command;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by GyuriX on 2015.10.04..
 */
public class ItemGUIView implements InventoryHolder, Listener {
    private Inventory inv;
    private static long guiIdCounter;
    private long guiId;
    private final ItemGUI gui;
    private final Player plr;
    private boolean noCloseHandle;
    String title;
    AnimationRunnable titleR;
    HashMap<Integer,SlotData> slots=new HashMap<>();

    public ItemGUIView(ItemGUI itemGUI, Player plr) {
        gui=itemGUI;
        SU.pm.registerEvents(this, gui.pl);
        this.plr=plr;
        guiId=++guiIdCounter;
        String pref="ItemGUI-"+guiId+"-";
        titleR=AnimationAPI.runAnimation(gui.name,pref+"title",plr,plr);
        for (Map.Entry<Integer,ItemGUI.SlotData> e:gui.slots.entrySet()){
            slots.put(e.getKey(),new SlotData(pref,e.getKey(),plr,e.getValue()));
        }
        prepareInv();
    }
    @EventHandler
    public void onInvClose(InventoryCloseEvent e){
        if (noCloseHandle) {
            System.out.println("No close handle.");
            return;
        }
        if (equals(e.getView().getTopInventory().getHolder())){
            for (SlotData sd:slots.values()){
                AnimationAPI.removeAnimationRunnable(sd.commandsRunnable);
                AnimationAPI.removeAnimationRunnable(sd.itemRunnable);
            }
            AnimationAPI.removeAnimationRunnable(titleR);
            System.out.println("Inventory closed.");
        }
        else{
            System.out.println("Other inventory?");
        }
    }
    @EventHandler
    public void onAnimationUpdate(final AnimationUpdateEvent e){
        AnimationRunnable ar=e.getRunnable();
        if (!ar.name.startsWith("ItemGUI-"+guiId+"-"))
            return;
        final String[] d=ar.name.split("-");
        SU.sch.scheduleSyncDelayedTask(gui.pl, new Runnable() {
            @Override
            public void run() {
                if (d[2].equals("title")) {
                    title = e.getText();
                    prepareInv();
                } else if (d[2].equals("close")) {

                } else if (d[2].equals("item")) {
                    inv.setItem(Integer.valueOf(d[3]), SU.stringToItemStack(e.getText()));
                }
            }
        });
    }
    @EventHandler
    public void onInvClick(InventoryClickEvent e){
        if (equals(e.getView().getTopInventory().getHolder())){
            e.setCancelled(true);
            SlotData sd=slots.get(e.getSlot());
            if (sd==null)
                return;
            for (String s:VariableAPI.fillVariables(sd.commandsRunnable.f.text,plr,plr).split("\n")){
                new Command(s).execute(plr);
            }
        }
    }

    private void prepareInv() {
        inv= Bukkit.createInventory(this, gui.size,title==null?"Untitled":title);
        for (Map.Entry<Integer,SlotData> e:slots.entrySet()){
            inv.setItem(e.getKey(), SU.stringToItemStack(VariableAPI.fillVariables(e.getValue().itemRunnable.f.text,plr,plr)));
        }
        noCloseHandle=true;
        plr.openInventory(inv);
        noCloseHandle=false;
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

}
