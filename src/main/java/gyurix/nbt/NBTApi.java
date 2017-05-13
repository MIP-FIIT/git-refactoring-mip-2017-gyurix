package gyurix.nbt;

import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.bukkit.entity.Entity;

import java.io.DataInputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static gyurix.spigotlib.SU.utf8;

public class NBTApi {
    public static HashMap<Class, Integer> typeMap = new HashMap<>();
    public static Class[] types;
    static Method entityFillNBTTag;
    static Method getEntityHandle;
    static Class nmsEntityClass;
    static Method setEntityNBTData;

    static {
        types = new Class[12];
        typeMap.put(Byte.class, 1);
        typeMap.put(Short.class, 2);
        typeMap.put(Integer.class, 3);
        typeMap.put(Long.class, 4);
        typeMap.put(Float.class, 5);
        typeMap.put(Double.class, 6);
        typeMap.put(byte[].class, 7);
        typeMap.put(String.class, 8);
        typeMap.put(NBTList.class, 9);
        typeMap.put(NBTCompound.class, 10);
        typeMap.put(int[].class, 11);
    }

    public static NBTCompound getNbtData(Entity ent) {
        try {
            Object nmsEntity = getEntityHandle.invoke(ent);
            Object tag = NBTCompound.nmsClass.newInstance();
            entityFillNBTTag.invoke(nmsEntity, tag);
            return new NBTCompound(tag);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return null;
        }
    }

    public static int getType(NBTTag value) {
        return typeMap.get(value instanceof NBTPrimitive ? ((NBTPrimitive) value).data.getClass() : value.getClass());
    }

    public static void init() {
        getEntityHandle = Reflection.getMethod(Reflection.getOBCClass("entity.CraftEntity"), "getHandle");
        nmsEntityClass = Reflection.getNMSClass("Entity");
        types[9] = NBTCompound.nmsClass = Reflection.getNMSClass("NBTTagCompound");
        NBTCompound.mapField = Reflection.getFirstFieldOfType(NBTCompound.nmsClass, Map.class);
        types[10] = NBTList.nmsClass = Reflection.getNMSClass("NBTTagList");
        NBTList.listField = Reflection.getField(NBTList.nmsClass, "list");
        NBTList.listType = Reflection.getField(NBTList.nmsClass, "type");
        NBTPrimitive.init();
        entityFillNBTTag = Reflection.getMethod(nmsEntityClass, "c", NBTCompound.nmsClass);
        setEntityNBTData = Reflection.getMethod(nmsEntityClass, "f", NBTCompound.nmsClass);
    }

    public static String readString(DataInputStream bis) throws Throwable {
        short s = bis.readShort();
        byte[] d = new byte[s];
        bis.read(d);
        return new String(d, utf8);
    }

    public static NBTTag readTag(DataInputStream bis, byte type) throws Throwable {
        switch (type) {
            case 1:
                return new NBTPrimitive(bis.readByte());
            case 2:
                return new NBTPrimitive(bis.readShort());
            case 3:
                return new NBTPrimitive(bis.readInt());
            case 4:
                return new NBTPrimitive(bis.readLong());
            case 5:
                return new NBTPrimitive(bis.readFloat());
            case 6:
                return new NBTPrimitive(bis.readDouble());
            case 7: {
                int len = bis.readInt();
                byte[] ar = new byte[len];
                bis.read(ar);
                return new NBTPrimitive(ar);
            }
            case 8:
                return new NBTPrimitive(readString(bis));
            case 9: {
                byte listType = bis.readByte();
                int len = bis.readInt();
                NBTList out = new NBTList();
                if (listType == 0 || len == 0) {
                    return out;
                }
                for (int i = 0; i < len; ++i)
                    out.list.add(readTag(bis, listType));
                return out;
            }
            case 10: {
                NBTCompound out = new NBTCompound();
                while (true) {
                    byte compType = bis.readByte();
                    if (compType == 0) {
                        return out;
                    }
                    out.map.put(readString(bis), readTag(bis, compType));
                }
            }
            case 11: {
                int len = bis.readInt();
                int[] ar = new int[len];
                for (int i = 0; i < len; ++i)
                    ar[i] = bis.readInt();
                return new NBTPrimitive(ar);
            }
        }
        throw new RuntimeException("§cUnknown NBT tag type - " + type);
    }

    public static ByteBuf save(String title, NBTCompound comp) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.heapBuffer();
        buf.writeByte(10);
        buf.writeShort(title.length());
        buf.writeBytes(title.getBytes());
        comp.write(buf);
        return buf;
    }

    public static void setNbtData(Entity ent, NBTCompound data) {
        try {
            Object nmsEntity = getEntityHandle.invoke(ent);
            setEntityNBTData.invoke(nmsEntity, data.toNMS());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

