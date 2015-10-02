package gyurix.spigotutils;

import gyurix.configfile.ConfigData;
import gyurix.configfile.ConfigSerialization;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A simple utility class for representing a range data (a data, which has a minimal
 * and a maximal value)
 * @param <T> the Comparable type of the RangedData (usually a number)
 */
public class RangedData<T extends Comparable>{
    public Comparable min,max;

    /**
     * Constructs a new RangedData of the minimal and maximal value.
     * @param min minimal value
     * @param max maximal value
     */
    public RangedData(T min,T max){
        this.min=min;
        this.max=max;
    }

    /**
     * Checks if this RangedData contains the given value
     * @param value checkable value
     * @return true if this RangedData contains the checked value
     */
    public boolean contains(T value){
        return (min==null||min.compareTo(value)<=0)&&(max==null||max.compareTo(value)>=0);
    }

    /**
     * Serializer and deserializer for the RangedData
     */
    public static class RangedDataSerializer implements ConfigSerialization.Serializer{

        public Object fromData(ConfigData data, Class cl, Type... types) {
            String[] s=data.stringData.split(":",2);
            Type[] t=types[0] instanceof ParameterizedType?((ParameterizedType)types[0]).getActualTypeArguments():new Type[0];
            Comparable min= s[0].isEmpty()?null: (Comparable) new ConfigData(s[0]).deserialize((Class) types[0],t);
            Comparable max= s[1].isEmpty()?null: (Comparable) new ConfigData(s[1]).deserialize((Class) types[0],t);
            return new RangedData(min,max);
        }

        public ConfigData toData(Object obj, Type... types) {
            RangedData rd= (RangedData) obj;
            StringBuilder out=new StringBuilder();
            if (rd.min!=null)
                out.append(rd.min);
            out.append(':');
            if (rd.max!=null)
                out.append(rd.max);
            return new ConfigData(out.toString());
        }
    }
}
