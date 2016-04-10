package gyurix.inventory;

import gyurix.configfile.ConfigSerialization;
import gyurix.configfile.PostLoadable;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by GyuriX on 2016.03.11..
 */
public class ItemGUI implements InventoryHolder, Listener, PostLoadable {

    @ConfigSerialization.ConfigOptions(serialize = false)
    public HashMap<String, InventoryClickHandler> handlers = new HashMap<>();
    @ConfigSerialization.ConfigOptions(serialize = false)
    private Inventory inv, fallBack;
    private HashMap<String, ItemStack> items;
    private ArrayList<Layer> layout;
    @ConfigSerialization.ConfigOptions(serialize = false)
    private Player plr;
    private String title;
    @ConfigSerialization.ConfigOptions(serialize = false)
    private HashMap<String, HashMap<String, Object>> vars = new HashMap<>();

    public ItemGUI clone() {
        ItemGUI out = new ItemGUI();
        out.title = title;
        out.layout = layout;
        out.items = new HashMap<>(items);
        out.handlers = new HashMap<>(handlers);
        out.vars = new HashMap<>(vars);
        out.postLoad();
        return out;
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    public String getSlot(int slot) {
        if (slot < 0 || slot >= layout.size() * 9)
            return null;
        return layout.get(slot / 9).data[slot % 9];
    }

    public ArrayList<Integer> getSlotIds(String slot) {
        ArrayList<Integer> slots = new ArrayList<>();
        int size = layout.size();
        for (int l = 0; l < size; l++) {
            for (int i = 0; i < 9; i++)
                if (layout.get(l).data[i].equals(slot))
                    slots.add(l * 9 + i);
        }
        return slots;
    }

    public ItemStack getStoredItem(String slot) {
        return items.get(slot);
    }

    public Object getVar(String item, String var) {
        HashMap<String, Object> vmap = vars.get(item);
        if (vmap == null)
            return null;
        return vmap.get(var);
    }

    public void handler(String item, InventoryClickHandler handler) {
        handlers.put(item, handler);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory clicked = e.getClickedInventory();
        if (clicked == null || clicked.getHolder() == null || clicked.getHolder() != this)
            return;
        String name = getSlot(e.getSlot());
        e.setCancelled(true);
        InventoryClickHandler handler = handlers.get(name);
        if (handler != null)
            handler.onClick(this, plr, name, e);
    }

    @EventHandler
    public void onClose(final InventoryCloseEvent e) {
        Inventory clicked = e.getInventory();
        if (clicked == null || clicked.getHolder() == null || clicked.getHolder() != this)
            return;
        HandlerList.unregisterAll(this);
        if (e.getInventory() != inv || fallBack == null)
            return;
        SU.sch.scheduleSyncDelayedTask(Main.pl, new Runnable() {
            @Override
            public void run() {
                e.getPlayer().openInventory(fallBack);
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (e.getPlayer().getName().equals(plr.getName()))
            HandlerList.unregisterAll(this);
    }

    public void openInv(Player plr, Plugin pl) {
        this.plr = plr;
        SU.pm.registerEvents(this, pl);
        plr.openInventory(inv);
    }

    public void openInv(Player plr, Plugin pl, Inventory fallBack) {
        this.fallBack = fallBack;
        openInv(plr, pl);
    }

    @Override
    public void postLoad() {
        int size = layout.size() * 9;
        HashMap<String, Object> vs = vars.get("title");
        String invtitle = SU.setLength(vs == null ? title : SU.fillVariables(title, vs), 32);
        inv = Bukkit.createInventory(this, size, invtitle);
        int i = 0;
        for (Layer l : layout) {
            for (String data : l.data) {
                inv.setItem(i++, varFill(data));
            }
        }
    }

    public void resetVars(String item) {
        vars.remove(item);
    }

    public void setItem(String slot, ItemStack is) {
        int size = layout.size();
        for (int l = 0; l < size; l++) {
            for (int i = 0; i < 9; i++)
                if (layout.get(l).data[i].equals(slot))
                    inv.setItem(l * 9 + i, is);
        }
    }

    public void setItem(String slot) {
        ItemStack is = varFill(slot);
        int size = layout.size();
        for (int l = 0; l < size; l++) {
            for (int i = 0; i < 9; i++)
                if (layout.get(l).data[i].equals(slot))
                    inv.setItem(l * 9 + i, is);
        }
    }

    public void setItem(String slot, String anotherSlot) {
        ItemStack is = varFill(anotherSlot);
        int size = layout.size();
        for (int l = 0; l < size; l++) {
            for (int i = 0; i < 9; i++)
                if (layout.get(l).data[i].equals(slot))
                    inv.setItem(l * 9 + i, is);
        }
    }

    public void setVars(String item, Object... vs) {
        HashMap<String, Object> vmap = vars.get(item);
        if (vmap == null)
            vars.put(item, vmap = new HashMap<>());
        String prev = null;
        for (Object o : vs) {
            if (prev == null)
                prev = String.valueOf(o);
            else {
                vmap.put(prev, o);
                prev = null;
            }
        }
    }

    public void storeItem(String slot, ItemStack item) {
        items.put(slot, item);
    }

    public ItemStack varFill(String id) {
        ItemStack is = items.get(id);
        if (is == null || is.getType() == Material.AIR)
            return is;
        is = is.clone();
        HashMap<String, Object> vs = vars.get(id);
        if (vs == null || !is.hasItemMeta())
            return is;
        ItemMeta im = is.getItemMeta();
        if (im.hasDisplayName()) {
            im.setDisplayName(SU.fillVariables(im.getDisplayName(), vs));
        }
        if (im.hasLore()) {
            List<String> lore = im.getLore();
            int size = lore.size();
            for (int i = 0; i < size; i++)
                lore.set(i, SU.fillVariables(lore.get(i), vs));
            im.setLore(lore);
        }
        if (im instanceof SkullMeta) {
            SkullMeta sm = ((SkullMeta) im);
            if (sm.hasOwner())
                sm.setOwner(SU.fillVariables(sm.getOwner(), vs));
        }
        is.setItemMeta(im);
        return is;
    }

    public static class Layer implements ConfigSerialization.StringSerializable {
        String[] data = new String[9];

        public Layer(String in) {
            String[] d = in.split(" +", 10);
            for (int i = 0; i < 9; i++) {
                if (d.length == i)
                    return;
                data[i] = d[i];
            }
        }

        @Override
        public String toString() {
            return StringUtils.join(data, " ");
        }
    }
}