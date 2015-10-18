package gyurix.inventory;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InventoryAPI {
    static final HashMap<String,ItemGUI> itemGUIs=new HashMap<>();
    static final HashMap<Plugin,ArrayList<ItemGUI>> pluginItemGUIs=new HashMap<>();

    public static ItemGUIView getView(String name,Player plr){
        return itemGUIs.get(name).getView(plr);
    }
    public static boolean register(Plugin p,ItemGUI ig){
        if (itemGUIs.containsKey(ig.id))
            return false;
        ig.pl=p;
        itemGUIs.put(ig.id, ig);
        ArrayList<ItemGUI> list=pluginItemGUIs.get(p);
        if (list==null){
            pluginItemGUIs.put(p,list= new ArrayList<>());
        }
        list.add(ig);
        return true;
    }
    public static boolean unregister(ItemGUI ig){
        try {
            pluginItemGUIs.get(ig.pl).remove(ig);
            return itemGUIs.remove(ig.id)!=null;
        }
        catch (Throwable e){
            e.printStackTrace();
            return false;
        }
    }
    public static boolean unregister(Plugin p){
        ArrayList<ItemGUI> list=pluginItemGUIs.remove(p);
        if (list==null||list.isEmpty())
            return false;
        for (ItemGUI ig:list){
            itemGUIs.remove(ig.id);
        }
        return true;
    }

}
