package gyurix.spigotlib;

import gyurix.api.VariableAPI;
import gyurix.configfile.ConfigData;
import gyurix.configfile.ConfigSerialization;
import gyurix.economy.EconomyAPI;
import gyurix.protocol.Reflection;
import gyurix.spigotutils.TPSMeter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.script.ScriptException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class ConfigHook {
    public static HashMap<String, Object> data = new HashMap();

    public static void registerSerializers() {
        ConfigSerialization.errors = Config.errors;
        ConfigSerialization.serializers.put(Vector.class, new ConfigSerialization.Serializer() {

            @Override
            public Object fromData(ConfigData data, Class paramClass, Type... paramVarArgs) {
                String[] s = data.stringData.split(" ", 3);
                return new Vector(Double.valueOf(s[0]).doubleValue(), Double.valueOf(s[1]).doubleValue(), Double.valueOf(s[2]).doubleValue());
            }

            @Override
            public ConfigData toData(Object obj, Type... paramVarArgs) {
                Vector v = (Vector) obj;
                return new ConfigData("" + v.getX() + " " + v.getY() + " " + v.getZ());
            }
        });
        ConfigSerialization.serializers.put(Location.class, new ConfigSerialization.Serializer() {

            @Override
            public Object fromData(ConfigData data, Class paramClass, Type... paramVarArgs) {
                String[] s = data.stringData.split(" ", 6);
                if (s.length == 4) {
                    return new Location(Bukkit.getWorld(s[0]), Double.valueOf(s[1]).doubleValue(), Double.valueOf(s[2]).doubleValue(), Double.valueOf(s[3]).doubleValue());
                }
                return new Location(Bukkit.getWorld(s[0]), Double.valueOf(s[1]).doubleValue(), Double.valueOf(s[2]).doubleValue(), Double.valueOf(s[3]).doubleValue(), Float.valueOf(s[4]).floatValue(), Float.valueOf(s[5]).floatValue());
            }

            @Override
            public ConfigData toData(Object obj, Type... paramVarArgs) {
                Location loc = (Location) obj;
                if (loc.getPitch() == 0.0f && loc.getYaw() == 0.0f) {
                    return new ConfigData(loc.getWorld().getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ());
                }
                return new ConfigData(loc.getWorld().getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " " + loc.getYaw() + " " + loc.getPitch());
            }
        });
        ConfigSerialization.serializers.put(ItemStack.class, new ItemSerializer());
    }

    public static void registerVariables() {
        VariableAPI.handlers.put("eval", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                String s = StringUtils.join(inside, "");
                try {
                    return SU.js.eval(s);
                } catch (ScriptException e) {
                    return "<eval:" + s + ">";
                }
            }
        });
        VariableAPI.handlers.put("tobool", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return Boolean.valueOf(StringUtils.join(inside, ""));
            }
        });
        VariableAPI.handlers.put("tobyte", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return Byte.valueOf((byte) Double.valueOf(StringUtils.join(inside, "")).doubleValue());
            }
        });
        VariableAPI.handlers.put("toshort", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return (short) Double.valueOf(StringUtils.join(inside, "")).doubleValue();
            }
        });
        VariableAPI.handlers.put("toint", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return (int) Double.valueOf(StringUtils.join(inside, "")).doubleValue();
            }
        });
        VariableAPI.handlers.put("tolong", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return (long) Double.valueOf(StringUtils.join(inside, "")).doubleValue();
            }
        });
        VariableAPI.handlers.put("tofloat", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return Float.valueOf(StringUtils.join(inside, ""));
            }
        });
        VariableAPI.handlers.put("todouble", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return Double.valueOf(StringUtils.join(inside, ""));
            }
        });
        VariableAPI.handlers.put("tostr", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return StringUtils.join(inside, "");
            }
        });
        VariableAPI.handlers.put("toarray", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return inside.toArray();
            }
        });
        VariableAPI.handlers.put("splits", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return StringUtils.join(inside, "").split(" ");
            }
        });
        VariableAPI.handlers.put("noout", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return "";
            }
        });
        VariableAPI.handlers.put("lang", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                String lang = SU.getPlayerConfig(plr).getString("lang");
                return lang.isEmpty() ? Config.defaultLang : lang;
            }
        });
        VariableAPI.handlers.put("args", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                int id = Integer.valueOf(StringUtils.join(inside, ""));
                return oArgs[id];
            }
        });
        VariableAPI.handlers.put("len", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                Object o = inside.get(0);
                return o.getClass().isArray() ? Array.getLength(o) : ((Collection) o).size();
            }
        });
        VariableAPI.handlers.put("iarg", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                int id = Integer.valueOf(inside.get(0).toString());
                return inside.get(id);
            }
        });
        VariableAPI.handlers.put("plr", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return Reflection.getData(plr, inside);
            }
        });
        VariableAPI.handlers.put("obj", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return Reflection.getData(oArgs[0], inside);
            }
        });
        VariableAPI.handlers.put("dstore", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                if (inside.size() == 1) {
                    String[] s = StringUtils.join(inside, "").split(" ", 2);
                    return ConfigHook.data.put(s[0], s[1]);
                }
                return ConfigHook.data.put(inside.get(0).toString(), inside.get(1));
            }
        });
        VariableAPI.handlers.put("dget", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return ConfigHook.data.get(StringUtils.join(inside, ""));
            }
        });
        VariableAPI.handlers.put("tps", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return TPSMeter.tps;
            }
        });
        VariableAPI.handlers.put("realtime", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return System.currentTimeMillis();
            }
        });
        VariableAPI.handlers.put("formattime", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                String str = StringUtils.join(inside, "");
                int id = str.indexOf(32);
                Long time = Long.valueOf(str.substring(0, id));
                String format = str.substring(id + 1);
                return new SimpleDateFormat(format).format(time);
            }
        });
        VariableAPI.handlers.put("balf", new VariableAPI.VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                if (inside == null) {
                    return EconomyAPI.balanceTypes.get("default").format(EconomyAPI.getBalance(plr.getUniqueId()));
                }
                String str = StringUtils.join(inside, "");
                return EconomyAPI.balanceTypes.get(str).format(EconomyAPI.getBalance(plr.getUniqueId(), str));
            }
        });
    }

    public static class ItemSerializer
            implements ConfigSerialization.Serializer {
        @Override
        public Object fromData(ConfigData data, Class cl, Type... paramVarArgs) {
            return SU.stringToItemStack(data.stringData);
        }

        @Override
        public ConfigData toData(Object is, Type... paramVarArgs) {
            return new ConfigData(SU.itemToString((ItemStack) is));
        }
    }

}

