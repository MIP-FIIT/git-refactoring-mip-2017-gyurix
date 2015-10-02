package gyurix.nbt;

import java.util.Collection;
import java.util.Map;

/**
 * The abstract class, represanting an NBTTag
 */
public abstract class NBTTag {
    /**
     * Load an NMS NBTTag to this tag
     * @param nmsTag NMS NBTTag to be loaded to this tag
     */
    public abstract void loadFromNMS(Object nmsTag);

    /**
     * Save this NBTTag to a NMS NBTTag
     * @return The NMS NBTTag containing the data of this tag.
     */
    public abstract Object saveToNMS();

    /**
     * A method for creating an NBTTag from an NBT compatible object.
     * @param o object to be converted to the NBTTag
     * @return The NBTTag made from the given object
     */
    public static NBTTag make(Object o) {
        if (o instanceof NBTTag){
            return (NBTTag) o;
        }
        else if (o instanceof Collection){
            return new NBTList().addAll((Collection)o);
        }
        else if (o.getClass().isArray()){
            return new NBTList().addAll((Object[])o);
        }
        else if (o instanceof Map){
            return new NBTCompound().addAll((Map)o);
        }
        return new NBTPrimitive().setData(o);
    }
}
