package gyurix.spigotlib;

import gyurix.configfile.ConfigSerialization.ConfigOptions;
import gyurix.configfile.PostLoadable;
import gyurix.spigotutils.BlockData;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Created by GyuriX on 2016. 09. 16..
 */
public class Items implements PostLoadable {
    @ConfigOptions(serialize = false)
    public static HashMap<String, Enchantment> enchantAliases = new HashMap<>();
    public static HashMap<String, ArrayList<String>> enchants = new HashMap<>();
    @ConfigOptions(serialize = false)
    public static HashMap<String, BlockData> nameAliases = new HashMap<>();
    public static HashMap<BlockData, ArrayList<String>> names = new HashMap<>();

    @Override
    public void postLoad() {
        enchantAliases.clear();
        for (Entry<String, ArrayList<String>> e : enchants.entrySet()) {
            Enchantment ec = Enchantment.getByName(e.getKey());
            for (String s : e.getValue()) {
                enchantAliases.put(s, ec);
            }
        }
        nameAliases.clear();
        for (Entry<BlockData, ArrayList<String>> e : names.entrySet()) {
            BlockData bd = e.getKey();
            for (String s : e.getValue()) {
                nameAliases.put(s, bd);
            }
        }
    }
}
