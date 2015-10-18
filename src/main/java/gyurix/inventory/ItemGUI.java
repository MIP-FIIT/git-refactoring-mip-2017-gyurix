package gyurix.inventory;

import gyurix.animation.Animation;
import gyurix.configfile.ConfigSerialization;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

/**
 * Created by GyuriX on 2015.10.04..
 */
public class ItemGUI{
    @ConfigSerialization.ConfigOptions(serialize = false)
    Plugin pl;
    String id;
    Animation name;
    int size;
    HashMap<Integer,ItemGUI.SlotData> slots;

    public ItemGUIView getView(Player plr) {
        return new ItemGUIView(this,plr);
    }
    public static class SlotData {
        public Animation item;
        public Animation commands;
    }
}
