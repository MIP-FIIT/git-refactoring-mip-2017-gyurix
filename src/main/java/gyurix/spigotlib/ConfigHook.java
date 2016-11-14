package gyurix.spigotlib;

import gyurix.api.VariableAPI;
import gyurix.api.VariableAPI.VariableHandler;
import gyurix.configfile.ConfigData;
import gyurix.configfile.ConfigSerialization;
import gyurix.configfile.ConfigSerialization.Serializer;
import gyurix.economy.EconomyAPI;
import gyurix.protocol.Reflection;
import gyurix.sign.SignConfig;
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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class ConfigHook {
    public static HashMap<String, Object> data = new HashMap();

    public static void registerSerializers() {
        ConfigSerialization.serializers.put(SignConfig.class, new Serializer() {
            @Override
            public Object fromData(ConfigData cd, Class cl, Type... paramVarArgs) {
                SignConfig sc = new SignConfig();
                for (int i = 0; i < 4; i++) {
                    String s = cd.listData.get(i).stringData;
                    sc.lines.add(s.equals(" ") ? "" : s);
                }
                return sc;
            }

            @Override
            public ConfigData toData(Object sco, Type... paramVarArgs) {
                SignConfig sc = (SignConfig) sco;
                ConfigData cd = new ConfigData();
                cd.listData = new ArrayList<>();
                for (int i = 0; i < 4; i++)
                    cd.listData.add(new ConfigData(sc.lines.get(i)));
                return cd;
            }
        });
        ConfigSerialization.serializers.put(Vector.class, new Serializer() {
            @Override
            public Object fromData(ConfigData data, Class paramClass, Type... paramVarArgs) {
                String[] s = data.stringData.split(" ", 3);
                return new Vector(Double.valueOf(s[0]), Double.valueOf(s[1]), Double.valueOf(s[2]));
            }

            @Override
            public ConfigData toData(Object obj, Type... paramVarArgs) {
                Vector v = (Vector) obj;
                return new ConfigData(String.valueOf(v.getX()) + ' ' + v.getY() + ' ' + v.getZ());
            }
        });
        ConfigSerialization.serializers.put(Location.class, new Serializer() {
            @Override
            public Object fromData(ConfigData data, Class paramClass, Type... paramVarArgs) {
                String[] s = data.stringData.split(" ", 6);
                if (s.length == 4) {
                    return new Location(Bukkit.getWorld(s[0]), Double.valueOf(s[1]), Double.valueOf(s[2]), Double.valueOf(s[3]));
                }
                return new Location(Bukkit.getWorld(s[0]), Double.valueOf(s[1]), Double.valueOf(s[2]), Double.valueOf(s[3]), Float.valueOf(s[4]), Float.valueOf(s[5]));
            }

            @Override
            public ConfigData toData(Object obj, Type... paramVarArgs) {
                Location loc = (Location) obj;
                if (loc.getPitch() == 0.0f && loc.getYaw() == 0.0f) {
                    return new ConfigData(loc.getWorld().getName() + ' ' + loc.getX() + ' ' + loc.getY() + ' ' + loc.getZ());
                }
                return new ConfigData(loc.getWorld().getName() + ' ' + loc.getX() + ' ' + loc.getY() + ' ' + loc.getZ() + ' ' + loc.getYaw() + ' ' + loc.getPitch());
            }
        });
        ConfigSerialization.serializers.put(ItemStack.class, new ItemSerializer());
    }

    public static void registerVariables() {
        VariableAPI.handlers.put("eval", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                String s = StringUtils.join(inside, "");
                try {
                    return SU.js.eval(s);
                } catch (ScriptException e) {
                    return "<eval:" + s + '>';
                }
            }
        });
        VariableAPI.handlers.put("tobool", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return Boolean.valueOf(StringUtils.join(inside, ""));
            }
        });
        VariableAPI.handlers.put("tobyte", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return (byte) Double.valueOf(StringUtils.join(inside, "")).doubleValue();
            }
        });
        VariableAPI.handlers.put("toshort", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return (short) Double.valueOf(StringUtils.join(inside, "")).doubleValue();
            }
        });
        VariableAPI.handlers.put("toint", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return (int) Double.valueOf(StringUtils.join(inside, "")).doubleValue();
            }
        });
        VariableAPI.handlers.put("tolong", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return (long) Double.valueOf(StringUtils.join(inside, "")).doubleValue();
            }
        });
        VariableAPI.handlers.put("tofloat", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return Float.valueOf(StringUtils.join(inside, ""));
            }
        });
        VariableAPI.handlers.put("todouble", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return Double.valueOf(StringUtils.join(inside, ""));
            }
        });
        VariableAPI.handlers.put("tostr", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return StringUtils.join(inside, "");
            }
        });
        VariableAPI.handlers.put("toarray", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return inside.toArray();
            }
        });
        VariableAPI.handlers.put("substr", new VariableHandler() {
            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                String[] s = StringUtils.join(inside, "").split(" ", 3);
                int from = Integer.valueOf(s[0]);
                int to = Integer.valueOf(s[1]);
                return s[2].substring(from < 0 ? s[2].length() + from : from, to < 0 ? s[2].length() + to : to);
            }
        });
        VariableAPI.handlers.put("splits", new VariableHandler() {
            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return StringUtils.join(inside, "").split(" ");
            }
        });
        VariableAPI.handlers.put("splitlen", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                String[] s = StringUtils.join(inside, "").split(" ", 3);
                Integer max = Integer.valueOf(s[0]);
                String pref = SU.unescapeText(s[1]);
                String text = s[2];
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < text.length(); i += max) {
                    sb.append('\n').append(pref).append(text.substring(i, Math.min(text.length(), i + max)));
                }
                return sb.length() == 0 ? "" : sb.substring(1);
            }
        });
        VariableAPI.handlers.put("noout", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return "";
            }
        });
        VariableAPI.handlers.put("lang", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                String s = StringUtils.join(inside, "");
                return GlobalLangFile.get(SU.getPlayerConfig(plr).getString("lang"), s);
            }
        });
        VariableAPI.handlers.put("booltest", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                String[] s = StringUtils.join(inside, "").split(";");
                return Boolean.valueOf(s[0]) ? s[1] : s[2];
            }
        });
        VariableAPI.handlers.put("args", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                int id = Integer.valueOf(StringUtils.join(inside, ""));
                return oArgs[id];
            }
        });
        VariableAPI.handlers.put("len", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                Object o = inside.get(0);
                return o.getClass().isArray() ? Array.getLength(o) : ((Collection) o).size();
            }
        });
        VariableAPI.handlers.put("iarg", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                int id = Integer.valueOf(inside.get(0).toString());
                return inside.get(id);
            }
        });
        VariableAPI.handlers.put("plr", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return Reflection.getData(plr, inside);
            }
        });
        VariableAPI.handlers.put("obj", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return Reflection.getData(oArgs[0], inside);
            }
        });
        VariableAPI.handlers.put("iobj", new VariableHandler() {
            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                Object obj = inside.remove(0);
                return Reflection.getData(obj, inside);
            }
        });
        VariableAPI.handlers.put("dstore", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                if (inside.size() == 1) {
                    String[] s = StringUtils.join(inside, "").split(" ", 2);
                    return data.put(s[0], s[1]);
                }
                return data.put(inside.get(0).toString(), inside.get(1));
            }
        });
        VariableAPI.handlers.put("dget", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return data.get(StringUtils.join(inside, ""));
            }
        });
        VariableAPI.handlers.put("tps", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return TPSMeter.tps;
            }
        });
        VariableAPI.handlers.put("real", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                return System.currentTimeMillis();
            }
        });
        VariableAPI.handlers.put("formattime", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                String str = StringUtils.join(inside, "");
                int id = str.indexOf(' ');
                long time = Long.valueOf(str.substring(0, id));
                String format = str.substring(id + 1);
                return new SimpleDateFormat(format).format(time);
            }
        });
        VariableAPI.handlers.put("balf", new VariableHandler() {

            @Override
            public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
                if (inside == null || inside.isEmpty()) {
                    return EconomyAPI.balanceTypes.get("default").format(EconomyAPI.getBalance(plr.getUniqueId()).setScale(2, BigDecimal.ROUND_HALF_UP));
                }
                String str = StringUtils.join(inside, "");
                return EconomyAPI.balanceTypes.get(str).format(EconomyAPI.getBalance(plr.getUniqueId(), str).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
        });
    }

    public static class ItemSerializer
            implements Serializer {
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

