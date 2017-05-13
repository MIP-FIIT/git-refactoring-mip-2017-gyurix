package gyurix.pluginloader;

import gyurix.configfile.ConfigSerialization;

/**
 * Created by GyuriX, on 2017. 03. 26..
 */
public class PluginInfo implements ConfigSerialization.StringSerializable {
    public String name, version, user, license;

    public PluginInfo(String in) {

    }

    public PluginInfo(String name, String version, String user, String license) {
        this.name = name;
        this.version = version;
        this.user = user;
        this.license = license;
    }

    public void load() {

    }

    @Override
    public String toString() {
        return name + " " + version + " " + user + " " + license;
    }
}
