package gyurix.nbt;

import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;

import static gyurix.spigotlib.SU.utf8;

public class NBTPrimitive extends NBTTag {
    private static HashMap<Class, Constructor> c = new HashMap();
    private static HashMap<Class, Field> f = new HashMap();
    public Object data;

    public NBTPrimitive() {
    }

    public NBTPrimitive(Object tag) {
        if (tag.getClass().getName().startsWith("net.minecraft.server."))
            loadFromNMS(tag);
        else
            data = tag;
    }

    @Override
    public void loadFromNMS(Object nmsTag) {
        try {
            data = f.get(nmsTag.getClass()).get(nmsTag);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object toNMS() {
        try {
            return c.get(data.getClass()).newInstance(data);
        } catch (Throwable e) {
            e.printStackTrace();
            SU.cs.sendMessage("§eError on converting " + data + " " + data.getClass() + " to NMS.");
            return null;
        }
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

    public void write(ByteBuf buf) {
        if (data instanceof byte[]) {
            byte[] d = (byte[]) data;
            buf.writeInt(d.length);
            buf.writeBytes(d);
        } else if (data instanceof int[]) {
            int[] d = (int[]) data;
            buf.writeInt(d.length);
            for (int i : d)
                buf.writeInt(i);
        } else if (data instanceof String) {
            String d = (String) data;
            byte[] bytes = d.getBytes(utf8);
            buf.writeShort(bytes.length);
            buf.writeBytes(bytes);
        } else if (data instanceof Byte)
            buf.writeByte((byte) data);
        else if (data instanceof Short)
            buf.writeShort((short) data);
        else if (data instanceof Integer)
            buf.writeInt((int) data);
        else if (data instanceof Long)
            buf.writeLong((long) data);
        else if (data instanceof Float)
            buf.writeFloat((float) data);
        else if (data instanceof Double)
            buf.writeDouble((double) data);

    }

    public NBTPrimitive setData(Object data) {
        this.data = data;
        return this;
    }

    public String toString() {
        return data.toString();
    }
}

