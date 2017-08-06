package gyurix.spigotlib;

import gyurix.api.VariableAPI;
import me.clip.placeholderapi.*;
import org.bukkit.entity.Player;

public class PHAHook extends PlaceholderHook {
    public PHAHook() {
        PlaceholderAPI.registerPlaceholderHook("sl", this);
    }

    @Override
    public String onPlaceholderRequest(Player plr, String msg) {
        return VariableAPI.fillVariables(msg, plr);
    }
}
