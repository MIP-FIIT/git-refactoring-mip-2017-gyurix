package gyurix.spigotlib.features;

import gyurix.protocol.PacketInEvent;
import gyurix.protocol.Protocol;
import gyurix.protocol.utils.ItemStackWrapper;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by gyurix on 24/11/2015.
 */
public class AntiCreativeItemHack implements Protocol.PacketInListener {
    @Override
    public void onPacketIN(PacketInEvent e) {
        Player plr = e.getPlayer();
        if (!Config.AntiItemHack.enabled || (plr.hasPermission("antiitemhack.bypass")))
            return;
        try {
            int slotid = 0;
            ItemStack is = null;
            for (Object o : e.getPacketData()) {
                if (o != null && o.getClass() == Integer.class) {
                    slotid = (Integer) o;
                    continue;
                }
                if (o == null) continue;
                is = new ItemStackWrapper(o).toBukkitStack();
            }
            String result = creativeSlotCheck(plr, slotid, is);
            if (result != null) {
                Main.lang.msg(Main.lang.get(plr, "itemhack"), plr, result);
                e.setCancelled(true);
                plr.updateInventory();
            }
        } catch (Throwable err) {
            err.printStackTrace();
        }
    }

    public String creativeSlotCheck(Player plr, int slot, ItemStack is) {
        int invid = this.convertPacketSlot(slot);
        if (is == null || is.getType() == Material.AIR) {
            try {
                SU.getPlayerConfig(plr).setObject("antiitemhack.lastitem", plr.getInventory().getItem(invid));
            } catch (NullPointerException var5_5) {
                // empty catch block
            }
            return null;
        }
        switch (invid) {
            case -1: {
                return null;
            }
            case -2: {
                return "itemhack.craftitem";
            }
        }
        String id = String.valueOf(is.getTypeId());
        switch (slot) {
            case 5: {
                return Config.AntiItemHack.helmets.contains(id) ? null : "itemhack.helmet";
            }
            case 6: {
                return Config.AntiItemHack.chestplates.contains(id) ? null : "itemhack.chestplate";
            }
            case 7: {
                return Config.AntiItemHack.leggings.contains(id) ? null : "itemhack.leggings";
            }
            case 8: {
                return Config.AntiItemHack.boots.contains(id) ? null : "itemhack.boots";
            }
        }
        if (SU.itemEqual((ItemStack) SU.getPlayerConfig(plr).get("antiitemhack.lastitem", ItemStack.class), is)) {
            return null;
        }
        if (is.getAmount() > is.getType().getMaxStackSize()) {
            return "itemhack.stacksize";
        }
        return SU.containsItem(Config.AntiItemHack.whitelist, is) || SU.containsItem(plr.getInventory(), is) ? null : "itemhack.invaliditem";
    }

    public int convertPacketSlot(int slot) {
        if (slot > 8 && slot < 36) {
            return slot;
        }
        if (slot > 35 && slot < 45) {
            return slot - 36;
        }
        if (slot > 44 || slot < 0) {
            return -1;
        }
        if (slot < 5) {
            return -2;
        }
        return 31 + slot;
    }
}
