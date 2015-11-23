package gyurix.nbt;

import gyurix.protocol.Reflection;
import org.bukkit.entity.Entity;

import java.lang.reflect.Method;
import java.util.Map;

public class NBTApi {
    public static Class[] types;
    static Method getEntityHandle;
    static Method getEntityNBTTag;
    static Method entityFillNBTTag;
    static Method setEntityNBTData;
    static Class nmsEntityClass;

    static {
        types = new Class[12];
    }

    public static void init() {
        getEntityHandle = Reflection.getMethod(Reflection.getOBCClass("entity.CraftEntity"), "getHandle");
        nmsEntityClass = Reflection.getNMSClass("Entity");
        getEntityNBTTag = Reflection.getMethod(nmsEntityClass, "getNBTTag");
        NBTApi.types[9] = NBTCompound.nmsClass = Reflection.getNMSClass("NBTTagCompound");
        NBTCompound.mapField = Reflection.getFirstFieldOfType(NBTCompound.nmsClass, Map.class);
        NBTApi.types[10] = NBTList.nmsClass = Reflection.getNMSClass("NBTTagList");
        NBTList.listField = Reflection.getField(NBTList.nmsClass, "list");
        NBTList.listType = Reflection.getField(NBTList.nmsClass, "type");
        NBTPrimitive.init();
        entityFillNBTTag = Reflection.getMethod(nmsEntityClass, "c", NBTCompound.nmsClass);
        setEntityNBTData = Reflection.getMethod(nmsEntityClass, "f", NBTCompound.nmsClass);
    }

    public static NBTCompound getNbtData(Entity ent) {
        try {
            Object nmsEntity = getEntityHandle.invoke(ent);
            Object tag = getEntityNBTTag.invoke(nmsEntity);
            if (tag == null) {
                tag = NBTCompound.nmsClass.newInstance();
            }
            entityFillNBTTag.invoke(nmsEntity, tag);
            return new NBTCompound(tag);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setNbtData(Entity ent, NBTCompound data) {
        try {
            Object nmsEntity = getEntityHandle.invoke(ent);
            setEntityNBTData.invoke(nmsEntity, data.saveToNMS());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

