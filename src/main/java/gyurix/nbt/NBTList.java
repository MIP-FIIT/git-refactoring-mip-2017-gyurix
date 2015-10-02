package gyurix.nbt;

import gyurix.utils.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by GyuriX on 2015.07.04..
 */
public class NBTList extends NBTTag{
    public static Class nmsClass;
    public ArrayList<NBTTag> list=new ArrayList<NBTTag>();
    static Field listField,listType;
    public NBTList(){

    }
    public NBTList(Object tag) {
        loadFromNMS(tag);
    }

    @Override
    public void loadFromNMS(Object tag) {
        try {
            for (Object o:((List)listField.get(tag))){
                String cln=o.getClass().getSimpleName();
                if (cln.equals("NBTTagCompound")){
                    list.add(new NBTCompound(o));
                }
                else if (cln.equals("NBTTagList")){
                    list.add(new NBTList(o));
                }
                else{
                    list.add(new NBTPrimitive(o));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object saveToNMS() {
        try {
            Object o=nmsClass.newInstance();
            List l= new ArrayList();
            for (NBTTag t:list){
                l.add(t.saveToNMS());
            }
            listField.set(o,l);
            if (!l.isEmpty())
                listType.set(o, (byte)ArrayUtils.indexOf(NBTApi.types,l.get(0).getClass()));
            return o;
        }
        catch (Throwable e){
            e.printStackTrace();
            return null;
        }
    }
    public NBTList addAll(Collection col){
        for (Object o:col){
            if (o==null)
                continue;
            list.add(NBTTag.make(o));
        }
        return this;
    }
    public NBTList addAll(Object... col){
        for (Object o:col){
            if (o==null)
                continue;
            list.add(NBTTag.make(o));
        }
        return this;
    }
    @Override
    public String toString() {
        return "[§b"+ StringUtils.join(list,", §b")+"§b]";
    }
}
