package gyurix.nbt;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class NBTCompound
        extends NBTTag {
    static Class nmsClass;
    static Field mapField;
    public HashMap<String, NBTTag> map = new HashMap();

    public NBTCompound() {
    }

    public NBTCompound(Object nmsTag) {
        this.loadFromNMS(nmsTag);
    }

    @Override
    public void loadFromNMS(Object tag) {
        try {
            Map<?, ?> m = (Map) mapField.get(tag);
            for (Map.Entry<?, ?> e : m.entrySet()) {
                String cln = e.getValue().getClass().getSimpleName();
                if (cln.equals("NBTTagCompound")) {
                    this.map.put((String) e.getKey(), new NBTCompound(e.getValue()));
                    continue;
                }
                if (cln.equals("NBTTagList")) {
                    this.map.put((String) e.getKey(), new NBTList(e.getValue()));
                    continue;
                }
                this.map.put((String) e.getKey(), new NBTPrimitive(e.getValue()));
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
            for (Map.Entry<String, NBTTag> e : this.map.entrySet()) {
                m.put(e.getKey(), e.getValue().saveToNMS());
            }
            return tag;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public NBTCompound getCompound(String key) {
        NBTTag tag = this.map.get(key);
        if (tag == null || !(tag instanceof NBTCompound)) {
            tag = new NBTCompound();
            this.map.put(key, tag);
        }
        return (NBTCompound) tag;
    }

    public NBTList getList(String key) {
        NBTTag tag = this.map.get(key);
        if (tag == null || !(tag instanceof NBTList)) {
            tag = new NBTList();
            this.map.put(key, tag);
        }
        return (NBTList) tag;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, NBTTag> e : this.map.entrySet()) {
            sb.append("\n\u00a7e").append((Object) e.getKey()).append(":\u00a7b ").append(e.getValue());
        }
        return sb.length() == 0 ? "{}" : "{" + sb.substring(1) + "}";
    }

    public boolean getBoolean(String key) {
        NBTTag tag = this.map.get(key);
        return tag != null && tag instanceof NBTPrimitive && ((Byte) ((NBTPrimitive) tag).data).byteValue() == 1;
    }

    public NBTCompound set(String key, Object value) {
        if (value == null) {
            this.map.remove(key);
        } else {
            this.map.put(key, NBTTag.make(value));
        }
        return this;
    }

    public NBTCompound addAll(Map<?, ?> o) {
        for (Map.Entry e : o.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) continue;
            this.map.put(e.getKey().toString(), NBTTag.make(e.getValue()));
        }
        return this;
    }
}

