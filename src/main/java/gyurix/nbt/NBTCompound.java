package gyurix.nbt;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class NBTCompound
        extends NBTTag {
    static Field mapField;
    static Class nmsClass;
    public HashMap<String, NBTTag> map = new HashMap<>();

    public NBTCompound() {
    }

    public NBTCompound(Object nmsTag) {
        loadFromNMS(nmsTag);
    }

    public NBTCompound addAll(Map<?, ?> o) {
        for (Entry e : o.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) continue;
            map.put(e.getKey().toString(), NBTTag.make(e.getValue()));
        }
        return this;
    }

    public boolean getBoolean(String key) {
        NBTTag tag = map.get(key);
        return tag != null && tag instanceof NBTPrimitive && (Byte) ((NBTPrimitive) tag).data == 1;
    }

    public NBTCompound getCompound(String key) {
        NBTTag tag = map.get(key);
        if (tag == null || !(tag instanceof NBTCompound)) {
            tag = new NBTCompound();
            map.put(key, tag);
        }
        return (NBTCompound) tag;
    }

    public NBTList getList(String key) {
        NBTTag tag = map.get(key);
        if (tag == null || !(tag instanceof NBTList)) {
            tag = new NBTList();
            map.put(key, tag);
        }
        return (NBTList) tag;
    }

    @Override
    public void loadFromNMS(Object tag) {
        try {
            Map<?, ?> m = (Map) mapField.get(tag);
            for (Entry<?, ?> e : m.entrySet()) {
                String cln = e.getValue().getClass().getSimpleName();
                if (cln.equals("NBTTagCompound")) {
                    map.put((String) e.getKey(), new NBTCompound(e.getValue()));
                    continue;
                }
                if (cln.equals("NBTTagList")) {
                    map.put((String) e.getKey(), new NBTList(e.getValue()));
                    continue;
                }
                map.put((String) e.getKey(), new NBTPrimitive(e.getValue()));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object saveToNMS() {
        try {
            Object tag = nmsClass.newInstance();
            Map m = (Map) mapField.get(tag);
            for (Entry<String, NBTTag> e : map.entrySet()) {
                m.put(e.getKey(), e.getValue().saveToNMS());
            }
            return tag;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public NBTCompound set(String key, Object value) {
        if (value == null) {
            map.remove(key);
        } else {
            map.put(key, NBTTag.make(value));
        }
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, NBTTag> e : map.entrySet()) {
            sb.append("\n\u00a7e").append((Object) e.getKey()).append(":\u00a7b ").append(e.getValue());
        }
        return sb.length() == 0 ? "{}" : "{" + sb.substring(1) + "}";
    }
}

