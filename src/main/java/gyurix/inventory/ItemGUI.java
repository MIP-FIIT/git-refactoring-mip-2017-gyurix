package gyurix.inventory;

import gyurix.animation.Animation;
import gyurix.configfile.ConfigSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

public class ItemGUI {
    @ConfigSerialization.ConfigOptions(serialize = false)
    Plugin pl;
    String id;
    Animation name;
    int size;
    HashMap<Integer, SlotData> slots;

    public ItemGUIView getView(Player plr) {
        return new ItemGUIView(this, plr);
    }

    public static class SlotData {
        public Animation item;
        public Animation commands;
    }

}

