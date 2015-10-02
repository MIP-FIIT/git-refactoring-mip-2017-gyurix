package gyurix.nbt;

import gyurix.protocol.Reflection;
import org.bukkit.entity.Entity;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Have you ever dreamed about changing the RAW NBT values? With this API you can without using any kind of
 * per version different NMS class.
 */
public class NBTApi {
    static Method getEntityHandle, getEntityNBTTag, entityFillNBTTag, setEntityNBTData;
    static Class nmsEntityClass;
    /**
     * NMS classes of every NBTTag type. Do NOT change this field.
     */
    public static Class[] types=new Class[12];

    /**
     * Used for initializing the NBTApi. You should NEVER call this method.
     */
    public static void init(){
        getEntityHandle = Reflection.getMethod(Reflection.getOBCClass("entity.CraftEntity"), "getHandle");
        getEntityNBTTag =Reflection.getMethod(nmsEntityClass = Reflection.getNMSClass("Entity"), "getNBTTag");
        types[9]=NBTCompound.nmsClass=Reflection.getNMSClass("NBTTagCompound");
        NBTCompound.mapField=Reflection.getFirstFieldOfType(NBTCompound.nmsClass, Map.class);
        types[10]=NBTList.nmsClass=Reflection.getNMSClass("NBTTagList");
        NBTList.listField=Reflection.getField(NBTList.nmsClass, "list");
        NBTList.listType=Reflection.getField(NBTList.nmsClass, "type");
        NBTPrimitive.init();
        entityFillNBTTag = Reflection.getMethod(nmsEntityClass,"c", NBTCompound.nmsClass);
        setEntityNBTData = Reflection.getMethod(nmsEntityClass,"f", NBTCompound.nmsClass);
    }

    /**
     * Get the NBT tags of a Bukkit Entity.
     *
     * @param ent Bukkit entity
     * @return The NBTCompound containing the NBT tags of the given entity.
     */
    public static NBTCompound getNbtData(Entity ent){
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

    /**
     * Set the NBT tags of a Bukkit Entity.
     *
     * @param ent Bukkit entity
     * @param data The NBTCompound containing the NBT tags of the given entity.
     */
    public static void setNbtData(Entity ent, NBTCompound data){
        try{
            Object nmsEntity = getEntityHandle.invoke(ent);
            setEntityNBTData.invoke(nmsEntity, data.saveToNMS());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
