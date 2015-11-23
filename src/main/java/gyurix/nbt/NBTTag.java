package gyurix.nbt;

import java.util.Collection;
import java.util.Map;

public abstract class NBTTag {
    public static NBTTag make(Object o) {
        if (o instanceof NBTTag) {
            return (NBTTag) o;
        }
        if (o instanceof Collection) {
            return new NBTList().addAll((Collection) o);
        }
        if (o.getClass().isArray()) {
            return new NBTList().addAll((Object[]) o);
        }
        if (o instanceof Map) {
            return new NBTCompound().addAll((Map) o);
        }
        return new NBTPrimitive().setData(o);
    }

    public abstract void loadFromNMS(Object var1);

    public abstract Object saveToNMS();
}

