package gyurix.nbt;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper for vanilla NBTCompounds.
 */
public class NBTCompound extends NBTTag{
    /**
     * The map of every NBTTag contained by this compound tag.
     */
    public HashMap<String,NBTTag> map=new HashMap<String, NBTTag>();
    static Class nmsClass;
    static Field mapField;

    /**
     * Empty constructor
     */
    public NBTCompound(){}

    /**
     * Contstructor from an NMS NBTCompound object
     * @param nmsTag NMS NBTCompound tag
     */
    public NBTCompound(Object nmsTag){
        loadFromNMS(nmsTag);
    }

    @Override
    public void loadFromNMS(Object tag) {
        try {
            Map m= (Map) mapField.get(tag);
            for (Map.Entry<String,Object> e:((Map<String,Object>)m).entrySet()){
                String cln=e.getValue().getClass().getSimpleName();
                if (cln.equals("NBTTagCompound")){
                    map.put(e.getKey(),new NBTCompound(e.getValue()));
                }
                else if (cln.equals("NBTTagList")){
                    map.put(e.getKey(),new NBTList(e.getValue()));
                }
                else{
                    map.put(e.getKey(),new NBTPrimitive(e.getValue()));
                }
            }
        }
        catch (Throwable e){
            e.printStackTrace();
        }
    }

    @Override
    public Object saveToNMS() {
        try {
            Object tag=nmsClass.newInstance();
            Map m= (Map) mapField.get(tag);
            for (Map.Entry<String,NBTTag> e:map.entrySet()){
                m.put(e.getKey(),e.getValue().saveToNMS());
            }
            return tag;
        }
        catch (Throwable e){
            e.printStackTrace();
            return null;
        }
    }
    public NBTCompound getCompound(String key){
        NBTTag tag=map.get(key);
        if (tag==null||!(tag instanceof NBTCompound)){
            map.put(key,tag=new NBTCompound());
        }
        return (NBTCompound)tag;
    }

    public NBTList getList(String key){
        NBTTag tag=map.get(key);
        if (tag==null||!(tag instanceof NBTList)){
            map.put(key,tag=new NBTList());
        }
        return (NBTList)tag;
    }

    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        for (Map.Entry e:map.entrySet()){
            sb.append("\n§e").append(e.getKey()).append(":§b ").append(e.getValue());
        }
        return sb.length()==0?"{}":"{"+sb.substring(1)+"}";
    }

    /**
     * Get the boolean value of a tag contained by this compound.
     * @param key the String key of the byte tag containing a boolean value
     * @return The boolean value of the given byte tag
     */
    public boolean getBoolean(String key) {
        NBTTag tag = map.get(key);
        return !(tag == null || !(tag instanceof NBTPrimitive)) && ((Byte) ((NBTPrimitive) tag).data) == 1;
    }

    /**
     *
     * Add or set the value of a key
     *
     * @param key the String key of the tag
     * @param value the value of this key, it could be a NbtTag or just a normal NBT compatible object
     * @return this NBTCompound
     */
    public NBTCompound set(String key, Object value) {
        if (value==null)
            map.remove(key);
        else
            map.put(key, NBTTag.make(value));
        return this;
    }

    /**
     * Add several key value pairs to this Compound
     *
     * @param o map containing the addable NbtTags or the NBT compatible objects
     * @return this NBTCompound
     */
    public NBTCompound addAll(Map<?,?> o) {
        for (Map.Entry<?,?> e:o.entrySet()){
            if (e.getKey()==null||e.getValue()==null)
                continue;
            map.put(e.getKey().toString(), NBTTag.make(e.getValue()));
        }
        return this;
    }
}