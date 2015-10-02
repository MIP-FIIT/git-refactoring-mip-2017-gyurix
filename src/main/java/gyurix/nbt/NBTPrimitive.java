package gyurix.nbt;

import gyurix.protocol.Reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by GyuriX on 2015.07.04..
 */
public class NBTPrimitive extends NBTTag{
    public Object data;
    private static HashMap<Class,Field> f=new HashMap<Class, Field>();
    private static HashMap<Class,Constructor> c=new HashMap<Class, Constructor>();

    static void init(){
        NBTApi.types[0]=Reflection.getNMSClass("NBTTagEnd");
        Class cl=Reflection.getNMSClass("NBTTagByte");
        c.put(Byte.class, Reflection.getConstructor(NBTApi.types[1]= cl, byte.class));
        f.put(cl,Reflection.getField(cl, "data"));
        c.put(Short.class,Reflection.getConstructor(NBTApi.types[2] = cl = Reflection.getNMSClass("NBTTagShort"), short.class));
        f.put(cl,Reflection.getField(cl, "data"));
        c.put(Integer.class,Reflection.getConstructor(NBTApi.types[3] = cl = Reflection.getNMSClass("NBTTagInt"), int.class));
        f.put(cl,Reflection.getField(cl, "data"));
        c.put(Long.class,Reflection.getConstructor(NBTApi.types[4] = cl = Reflection.getNMSClass("NBTTagLong"), long.class));
        f.put(cl,Reflection.getField(cl, "data"));
        c.put(Float.class,Reflection.getConstructor(NBTApi.types[5] = cl = Reflection.getNMSClass("NBTTagFloat"), float.class));
        f.put(cl,Reflection.getField(cl, "data"));
        c.put(Double.class,Reflection.getConstructor(NBTApi.types[6] = cl = Reflection.getNMSClass("NBTTagDouble"), double.class));
        f.put(cl,Reflection.getField(cl, "data"));
        c.put(String.class,Reflection.getConstructor(NBTApi.types[7] = cl = Reflection.getNMSClass("NBTTagString"), String.class));
        f.put(cl,Reflection.getField(cl, "data"));
        c.put(byte[].class, Reflection.getConstructor(NBTApi.types[8] = cl = Reflection.getNMSClass("NBTTagByteArray"), byte[].class));
        f.put(cl,Reflection.getField(cl, "data"));
        c.put(int[].class, Reflection.getConstructor(NBTApi.types[11]= cl=Reflection.getNMSClass("NBTTagIntArray"), int[].class));
        f.put(cl,Reflection.getField(cl,"data"));
    }
    public NBTPrimitive() {

    }
    public NBTPrimitive(Object tag) {
        loadFromNMS(tag);
    }

    @Override
    public void loadFromNMS(Object nmsTag) {
        try {
            data=f.get(nmsTag.getClass()).get(nmsTag);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object saveToNMS() {
        try {
            return c.get(data.getClass()).newInstance(data);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public NBTPrimitive setData(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
