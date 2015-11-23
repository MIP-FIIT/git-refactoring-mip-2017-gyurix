package gyurix.nbt;

import gyurix.protocol.Reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;

public class NBTPrimitive
        extends NBTTag {
    private static HashMap<Class, Field> f = new HashMap();
    private static HashMap<Class, Constructor> c = new HashMap();
    public Object data;

    public NBTPrimitive() {
    }

    public NBTPrimitive(Object tag) {
        this.loadFromNMS(tag);
    }

    static void init() {
        Class cl;
        NBTApi.types[0] = Reflection.getNMSClass("NBTTagEnd");
        NBTApi.types[1] = cl = Reflection.getNMSClass("NBTTagByte");
        c.put(Byte.class, Reflection.getConstructor(NBTApi.types[1], Byte.TYPE));
        f.put(cl, Reflection.getField(cl, "data"));
        NBTApi.types[2] = cl = Reflection.getNMSClass("NBTTagShort");
        c.put(Short.class, Reflection.getConstructor(cl, Short.TYPE));
        f.put(cl, Reflection.getField(cl, "data"));
        NBTApi.types[3] = cl = Reflection.getNMSClass("NBTTagInt");
        c.put(Integer.class, Reflection.getConstructor(cl, Integer.TYPE));
        f.put(cl, Reflection.getField(cl, "data"));
        NBTApi.types[4] = cl = Reflection.getNMSClass("NBTTagLong");
        c.put(Long.class, Reflection.getConstructor(cl, Long.TYPE));
        f.put(cl, Reflection.getField(cl, "data"));
        NBTApi.types[5] = cl = Reflection.getNMSClass("NBTTagFloat");
        c.put(Float.class, Reflection.getConstructor(cl, Float.TYPE));
        f.put(cl, Reflection.getField(cl, "data"));
        NBTApi.types[6] = cl = Reflection.getNMSClass("NBTTagDouble");
        c.put(Double.class, Reflection.getConstructor(cl, Double.TYPE));
        f.put(cl, Reflection.getField(cl, "data"));
        NBTApi.types[7] = cl = Reflection.getNMSClass("NBTTagString");
        c.put(String.class, Reflection.getConstructor(cl, String.class));
        f.put(cl, Reflection.getField(cl, "data"));
        NBTApi.types[8] = cl = Reflection.getNMSClass("NBTTagByteArray");
        c.put(byte[].class, Reflection.getConstructor(cl, byte[].class));
        f.put(cl, Reflection.getField(cl, "data"));
        NBTApi.types[11] = cl = Reflection.getNMSClass("NBTTagIntArray");
        c.put(int[].class, Reflection.getConstructor(cl, int[].class));
        f.put(cl, Reflection.getField(cl, "data"));
    }

    @Override
    public void loadFromNMS(Object nmsTag) {
        try {
            this.data = f.get(nmsTag.getClass()).get(nmsTag);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object saveToNMS() {
        try {
            return c.get(this.data.getClass()).newInstance(this.data);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public NBTPrimitive setData(Object data) {
        this.data = data;
        return this;
    }

    public String toString() {
        return this.data.toString();
    }
}

