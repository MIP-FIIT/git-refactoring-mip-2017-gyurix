package gyurix.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Created by GyuriX on 2016.03.11..
 */
public abstract class InventoryClickHandler {
    public abstract void onClick(ItemGUI itemGUI, Player plr, String item, InventoryClickEvent event);
}
