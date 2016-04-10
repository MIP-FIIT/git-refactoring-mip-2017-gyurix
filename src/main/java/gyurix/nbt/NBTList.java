package gyurix.nbt;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NBTList
        extends NBTTag {
    public static Class nmsClass;
    static Field listField;
    static Field listType;
    public ArrayList<NBTTag> list = new ArrayList();

    public NBTList() {
    }

    public NBTList(Object tag) {
        this.loadFromNMS(tag);
    }

    public NBTList addAll(Collection col) {
        for (Object o : col) {
            if (o == null) continue;
            this.list.add(NBTTag.make(o));
        }
        return this;
    }

    public NBTList addAll(Object... col) {
        for (Object o : col) {
            if (o == null) continue;
            this.list.add(NBTTag.make(o));
        }
        return this;
    }

    @Override
    public void loadFromNMS(Object tag) {
        try {
            for (Object o : (List) listField.get(tag)) {
                String cln = o.getClass().getSimpleName();
                if (cln.equals("NBTTagCompound")) {
                    this.list.add(new NBTCompound(o));
                    continue;
                }
                if (cln.equals("NBTTagList")) {
                    this.list.add(new NBTList(o));
                    continue;
                }
                this.list.add(new NBTPrimitive(o));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object saveToNMS() {
        try {
            Object o = nmsClass.newInstance();
            ArrayList<Object> l = new ArrayList<Object>();
            for (NBTTag t : this.list) {
                l.add(t.saveToNMS());
            }
            listField.set(o, l);
            if (!l.isEmpty()) {
                listType.set(o, Byte.valueOf((byte) ArrayUtils.indexOf(NBTApi.types, l.get(0).getClass())));
            }
            return o;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public String toString() {
        return "[\u00a7b" + StringUtils.join(this.list, ", \u00a7b") + "\u00a7b]";
    }
}

