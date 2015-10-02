package gyurix.spigotlib;

import gyurix.api.VariableAPI;
import gyurix.configfile.ConfigData;
import gyurix.configfile.ConfigSerialization;
import gyurix.economy.EconomyAPI;
import gyurix.protocol.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.Type;

/**
 * The SpigotLibs hook for the configuration system to register the only Spigot
 * based variables and serializers
 */
public class ConfigHook {
    /**
     * Registers the only Spigot based serializers
     */
    public static void registerSerializers() {
        ConfigSerialization.errors = Config.errors;
        ConfigSerialization.serializers.put(Vector.class, new ConfigSerialization.Serializer() {
            public Object fromData(ConfigData data, Class paramClass, Type... paramVarArgs) {
                String[] s = data.stringData.split(" ", 3);
                return new Vector(Double.valueOf(s[0]), Double.valueOf(s[1]), Double.valueOf(s[2]));
            }

            public ConfigData toData(Object obj, Type... paramVarArgs) {
                Vector v = (Vector) obj;
                return new ConfigData(v.getX() + " " + v.getY() + " " + v.getZ());
            }
        });
        ConfigSerialization.serializers.put(Location.class, new ConfigSerialization.Serializer() {
            public Object fromData(ConfigData data, Class paramClass, Type... paramVarArgs) {
                String[] s = data.stringData.split(" ", 6);
                if (s.length == 4)
                    return new Location(Bukkit.getWorld(s[0]), Double.valueOf(s[1]), Double.valueOf(s[2]), Double.valueOf(s[3]));
                return new Location(Bukkit.getWorld(s[0]), Double.valueOf(s[1]), Double.valueOf(s[2]), Double.valueOf(s[3]),
                        Float.valueOf(s[4]), Float.valueOf(s[5]));
            }

            public ConfigData toData(Object obj, Type... paramVarArgs) {
                Location loc = (Location) obj;
                if (loc.getPitch() == 0.0F && loc.getYaw() == 0.0F) {
                    return new ConfigData(loc.getWorld().getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ());
                }
                return new ConfigData(loc.getWorld().getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " " + loc.getYaw() + " " + loc.getPitch());
            }
        });
        ConfigSerialization.serializers.put(ItemStack.class, new ItemSerializer());
    }
    /**
     * Registers the only Spigot based variables
     */
    public static void registerVariables() {
        VariableAPI.handlers.put("eval", new VariableAPI.VariableHandler() {
            public String getValue(Object obj, Player plr, String str) {
                try {
                    return "" + SU.js.eval(str);
                } catch (Throwable e) {
                    return "<eval:" + str + ">";
                }
            }
        });
        VariableAPI.handlers.put("toint", new VariableAPI.VariableHandler() {
            public String getValue(Object obj, Player plr, String str) {
                try {
                    return "" + (long) (double) Double.valueOf(str);
                } catch (Throwable e) {
                    return "<toint:" + str + ">";
                }
            }
        });
        VariableAPI.handlers.put("lang", new VariableAPI.VariableHandler() {
            public String getValue(Object obj, Player plr, String str) {
                String lang = SU.getPlayerConfig(plr).getString("lang");
                return lang.isEmpty()?Config.defaultLang:lang;
            }
        });
        VariableAPI.handlers.put("args", new VariableAPI.VariableHandler() {
            public String getValue(Object obj, Player plr, String str) {
                return ""+((Object[])obj)[Integer.valueOf(str)];
            }
        });
        VariableAPI.handlers.put("plr", new VariableAPI.VariableHandler() {
            @Override
            public String getValue(Object obj, Player plr, String str) {
                return ""+ Reflection.getData(plr, str);
            }
        });
        VariableAPI.handlers.put("obj", new VariableAPI.VariableHandler() {
            @Override
            public String getValue(Object obj, Player plr, String str) {
                return ""+ Reflection.getData(obj, str);
            }
        });
        VariableAPI.handlers.put("balf", new VariableAPI.VariableHandler() {
            public String getValue(Object obj, Player plr, String str) {
                if (str.isEmpty())
                    return EconomyAPI.balanceTypes.get("default").format(EconomyAPI.getBalance(plr.getUniqueId()));
                else
                    return EconomyAPI.balanceTypes.get(str).format(EconomyAPI.getBalance(plr.getUniqueId(), str));
            }
        });
        VariableAPI.handlers.put("pname", new VariableAPI.VariableHandler() {
            public String getValue(Object obj, Player plr, String str) {
                return plr.getName();
            }
        });
    }
    /**
     * Serializer and deserializer class for ItemStacks
     */
    public static class ItemSerializer implements ConfigSerialization.Serializer {
        public Object fromData(ConfigData data, Class cl, Type... paramVarArgs) {
            return SU.stringToItemStack(data.stringData);
        }

        public ConfigData toData(Object is, Type... paramVarArgs) {
            return new ConfigData(SU.itemToString((ItemStack) is));
        }
    }
}
