package gyurix.inventory;

import gyurix.animation.AnimationAPI;
import gyurix.animation.AnimationRunnable;
import org.bukkit.entity.Player;

public class SlotData
        extends ItemGUI.SlotData {
    public AnimationRunnable itemRunnable;
    public AnimationRunnable commandsRunnable;
    public Player plr;

    public SlotData(String name, int i, Player plr, ItemGUI.SlotData old) {
        this.item = old.item;
        this.commands = old.commands;
        this.itemRunnable = AnimationAPI.runAnimation(old.item, name + "item-" + i, plr, plr);
        this.commandsRunnable = AnimationAPI.runAnimation(old.commands, name + "cmd-" + i, plr, plr);
    }
}

