package gyurix.inventory;

import gyurix.animation.AnimationAPI;
import gyurix.animation.AnimationRunnable;
import gyurix.animation.AnimationUpdateEvent;
import gyurix.api.VariableAPI;
import gyurix.commands.Command;
import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public class ItemGUIView
        implements InventoryHolder,
        Listener {
    private static long guiIdCounter;
    private final ItemGUI gui;
    private final Player plr;
    String title;
    AnimationRunnable titleR;
    HashMap<Integer, SlotData> slots = new HashMap();
    private Inventory inv;
    private long guiId;
    private boolean noCloseHandle;

    public ItemGUIView(ItemGUI itemGUI, Player plr) {
        this.gui = itemGUI;
        SU.pm.registerEvents(this, this.gui.pl);
        this.plr = plr;
        this.guiId = ++guiIdCounter;
        String pref = "ItemGUI-" + this.guiId + "-";
        this.titleR = AnimationAPI.runAnimation(this.gui.name, pref + "title", plr, plr);
        for (Map.Entry<Integer, ItemGUI.SlotData> e : this.gui.slots.entrySet()) {
            this.slots.put(e.getKey(), new SlotData(pref, e.getKey(), plr, e.getValue()));
        }
        this.prepareInv();
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        if (this.noCloseHandle) {
            System.out.println("No close handle.");
            return;
        }
        if (this.equals(e.getView().getTopInventory().getHolder())) {
            for (SlotData sd : this.slots.values()) {
                AnimationAPI.removeAnimationRunnable(sd.commandsRunnable);
                AnimationAPI.removeAnimationRunnable(sd.itemRunnable);
            }
            AnimationAPI.removeAnimationRunnable(this.titleR);
            System.out.println("Inventory closed.");
        } else {
            System.out.println("Other inventory?");
        }
    }

    @EventHandler
    public void onAnimationUpdate(final AnimationUpdateEvent e) {
        AnimationRunnable ar = e.getRunnable();
        if (!ar.name.startsWith("ItemGUI-" + this.guiId + "-")) {
            return;
        }
        final String[] d = ar.name.split("-");
        SU.sch.scheduleSyncDelayedTask(this.gui.pl, new Runnable() {

            @Override
            public void run() {
                if (d[2].equals("title")) {
                    ItemGUIView.this.title = e.getText();
                    ItemGUIView.this.prepareInv();
                } else if (!d[2].equals("close") && d[2].equals("item")) {
                    ItemGUIView.this.inv.setItem(Integer.valueOf(d[3]).intValue(), SU.stringToItemStack(e.getText()));
                }
            }
        });
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (this.equals(e.getView().getTopInventory().getHolder())) {
            e.setCancelled(true);
            SlotData sd = this.slots.get(e.getSlot());
            if (sd == null) {
                return;
            }
            for (String s : VariableAPI.fillVariables(sd.commandsRunnable.frames.f.text, this.plr, new Object[]{this.plr}).split("\n")) {
                new Command(s).execute(this.plr);
            }
        }
    }

    private void prepareInv() {
        this.inv = Bukkit.createInventory(this, this.gui.size, this.title == null ? "Untitled" : this.title);
        for (Map.Entry<Integer, SlotData> e : this.slots.entrySet()) {
            this.inv.setItem(e.getKey().intValue(), SU.stringToItemStack(VariableAPI.fillVariables(e.getValue().itemRunnable.frames.f.text, this.plr, this.plr)));
        }
        this.noCloseHandle = true;
        this.plr.openInventory(this.inv);
        this.noCloseHandle = false;
    }

    public Inventory getInventory() {
        return this.inv;
    }

}

