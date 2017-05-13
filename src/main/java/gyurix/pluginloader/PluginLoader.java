package gyurix.pluginloader;

import gyurix.configfile.ConfigSerialization;
import org.bukkit.plugin.SimplePluginManager;

import java.util.HashMap;

/**
 * Created by GyuriX, on 2017. 03. 26..
 */
public class PluginLoader {
    public static HashMap<String, PluginInfo> plugins;
    @ConfigSerialization.ConfigOptions(serialize = false)
    public static SimplePluginManager pm;

    public void load(PluginInfo pi) {
        pi.load();
    }
}
