package gyurix.protocol.utils;


import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.json.JsonAPI;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.ChatAPI;
import gyurix.spigotlib.SU;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

public class DataWatcher implements WrappedData, StringSerializable {
    private static final Class nmsDW = Reflection.getNMSClass("DataWatcher");
    private static final Constructor con = Reflection.getConstructor(nmsDW, Reflection.getNMSClass("Entity"));
    private static final Field dwField = Reflection.getField(nmsDW, "c");
    private static final Class nmsItem = Reflection.getInnerClass(nmsDW, "Item");
    private static final Constructor itc = nmsItem.getConstructors()[0];
    private static final Field itemField = Reflection.getField(nmsItem, "b");
    private static final Constructor objcon = Reflection.getConstructor(Reflection.getNMSClass("DataWatcherObject"),
            int.class, Reflection.getNMSClass("DataWatcherSerializer"));
    private static final HashMap<Class, Object> serializers = new HashMap<>();

    static {
        Class dwr = Reflection.getNMSClass("DataWatcherRegistry");
        try {
            serializers.put(Byte.class, dwr.getField("a").get(null));
            serializers.put(Integer.class, dwr.getField("b").get(null));
            serializers.put(Float.class, dwr.getField("c").get(null));
            serializers.put(String.class, dwr.getField("d").get(null));
            serializers.put(ChatAPI.icbcClass, dwr.getField("e").get(null));
            serializers.put(Reflection.getNMSClass("ItemStack"), dwr.getField("f").get(null));
            serializers.put(Reflection.getNMSClass("IBlockData"), dwr.getField("g").get(null));
            serializers.put(Boolean.class, dwr.getField("h").get(null));
            serializers.put(Reflection.getNMSClass("Vector3f"), dwr.getField("i").get(null));
            serializers.put(Reflection.getNMSClass("BlockPosition"), dwr.getField("k").get(null));
            serializers.put(Reflection.getNMSClass("EnumDirection"), dwr.getField("l").get(null));
            serializers.put(UUID.class, dwr.getField("m").get(null));
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    public TreeMap<Integer, Object> map = new TreeMap<>();

    public DataWatcher() {
    }

    public DataWatcher(Object nmsData) {
        try {
            Map<Integer, Object> m = (Map<Integer, Object>) dwField.get(nmsData);
            for (Entry<Integer, Object> e : m.entrySet()) {
                map.put(e.getKey(), itemField.get(e.getValue()));
            }
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    public static ArrayList<Object> convertToNmsItems(ArrayList<Object> in) {
        ArrayList<Object> out = new ArrayList<>();
        int id = 0;
        for (Object wr : in) {
            try {
                Object o = WrapperFactory.unwrap(wr);
                out.add(itc.newInstance(objcon.newInstance(id++, serializers.get(o.getClass())), o));
            } catch (Throwable e) {
                SU.error(SU.cs, e, "MythaliumCore", "gyuriX");
            }
        }
        return out;
    }

    public static ArrayList<Object> wrapNMSItems(List<Object> in) {
        ArrayList<Object> out = new ArrayList<>();
        for (Object o : in) {
            try {
                out.add(WrapperFactory.wrap(itemField.get(o)));
            } catch (Throwable e) {
                SU.error(SU.cs, e, "MythaliumCore", "gyuriX");
            }
        }
        return out;
    }

    @Override
    public Object toNMS() {
        Object dw = null;
        try {
            dw = con.newInstance((Object) null);
            Map<Integer, Object> m = (Map<Integer, Object>) dwField.get(dw);
            for (Entry<Integer, Object> e : map.entrySet()) {
                Object o = WrapperFactory.unwrap(e.getValue());
                m.put(e.getKey(), itc.newInstance(objcon.newInstance(e.getKey(),
                        serializers.get(o.getClass())), o));
            }
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return dw;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (Entry<Integer, Object> e : map.entrySet()) {
            out.append("§e, §b").append(e.getKey()).append("§e: §f").append(JsonAPI.serialize(e.getValue()));
        }
        return out.length() == 0 ? "§e{}" : "§e{§b" + out.substring(6) + "§e}";
    }
}
