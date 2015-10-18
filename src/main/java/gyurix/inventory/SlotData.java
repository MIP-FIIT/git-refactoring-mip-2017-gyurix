package gyurix.inventory;

import gyurix.animation.Animation;
import gyurix.animation.AnimationAPI;
import gyurix.animation.AnimationRunnable;
import gyurix.api.VariableAPI;
import gyurix.commands.Command;
import org.bukkit.entity.Player;

/**
 * Created by GyuriX on 2015.10.04..
 */
public class SlotData extends ItemGUI.SlotData{
    public AnimationRunnable itemRunnable;
    public AnimationRunnable commandsRunnable;
    public Player plr;
    public SlotData(String name,int i,Player plr,ItemGUI.SlotData old){
        item=old.item;
        commands=old.commands;
        itemRunnable= AnimationAPI.runAnimation(old.item,name+"item-"+i,plr,plr);
        commandsRunnable= AnimationAPI.runAnimation(old.commands,name+"cmd-"+i,plr,plr);
    }
}
