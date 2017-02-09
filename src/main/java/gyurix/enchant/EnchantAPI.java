package gyurix.enchant;

import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;
import org.bukkit.Material;
import org.bukkit.command.defaults.EnchantCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static gyurix.spigotlib.Main.pl;

/**
 * Created by GyuriX on 2016. 12. 17..
 */
public class EnchantAPI implements Listener {
    public static boolean enabled;
    public static ArrayList<String> enchantNames;
    private static HashMap<Integer, Enchantment> byId;
    private static HashMap<String, Enchantment> byName;
    private static HashSet<Integer> vanillaEnchantIds = new HashSet<>();

    public EnchantAPI() {
        try {
            enchantNames = (ArrayList<String>) Reflection.getFieldData(EnchantCommand.class, "ENCHANTMENT_NAMES");
            byId = (HashMap<Integer, Enchantment>) Reflection.getFieldData(Enchantment.class, "byId");
            vanillaEnchantIds.addAll(byId.keySet());
            byName = (HashMap<String, Enchantment>) Reflection.getFieldData(Enchantment.class, "byName");
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    public static boolean createEnchant(Enchantment enchant) {
        if (enchant == null || enchantNames.contains(enchant.getName()))
            return false;
        enchantNames.add(enchant.getName());
        byId.put(enchant.getId(), enchant);
        byName.put(enchant.getName(), enchant);
        return true;
    }

    @EventHandler
    public static void onAnvilPrepare(final PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        ItemStack is = inv.getItem(0);
        if (is == null)
            return;
        Map<Enchantment, Integer> en1 = is.getType() == Material.ENCHANTED_BOOK ? ((EnchantmentStorageMeta) is.getItemMeta()).getStoredEnchants() : is.getEnchantments();
        Map<Enchantment, Integer> en2 = is.getEnchantments();
        try {
            en2 = ((EnchantmentStorageMeta) is.getItemMeta()).getStoredEnchants();
        } catch (Throwable err) {
        }
        ItemStack result = e.getResult();
        for (Map.Entry<Enchantment, Integer> e1 : en1.entrySet()) {
            if (!vanillaEnchantIds.contains(e1.getKey().getId()))
                result.addUnsafeEnchantment(e1.getKey(), e1.getValue());
        }
        for (Map.Entry<Enchantment, Integer> e1 : en2.entrySet()) {
            if (!vanillaEnchantIds.contains(e1.getKey().getId())) {
                int old = result.getEnchantmentLevel(e1.getKey());
                result.addUnsafeEnchantment(e1.getKey(), Math.max(old + 1, e1.getValue()));
            }
        }
        e.setResult(fixItem(result));
        SU.sch.scheduleSyncDelayedTask(pl, new Runnable() {
            @Override
            public void run() {
                ((Player) e.getView().getPlayer()).updateInventory();
            }
        });
    }

    public static ItemStack fixItem(ItemStack is) {
        if (is == null || is.getType() == Material.AIR)
            return is;
        ItemMeta meta = is.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null)
            lore = new ArrayList<>();
        int addId = 0;
        for (Map.Entry<Enchantment, Integer> e : is.getEnchantments().entrySet()) {
            Enchantment ec = e.getKey();
            if (!vanillaEnchantIds.contains(ec.getId())) {
                String ll = "§7" + ec.getName() + " ";
                ArrayList<String> newLore = new ArrayList<>();
                for (String s : lore) {
                    if (!s.startsWith(ll))
                        newLore.add(s);
                }
                newLore.add(addId++, ll + RomanNumsAPI.toRoman(e.getValue()));
                lore = newLore;
            }
        }
        meta.setLore(lore);
        is.setItemMeta(meta);
        return is;
    }

    public static boolean removeEnchant(Enchantment enchantment) {
        if (enchantment == null)
            return false;
        enchantNames.remove(enchantment.getName());
        byId.remove(enchantment.getId());
        return byName.remove(enchantment.getName()) != null;
    }


}
