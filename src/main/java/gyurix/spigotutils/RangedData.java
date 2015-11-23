package gyurix.spigotutils;

import gyurix.configfile.ConfigData;
import gyurix.configfile.ConfigSerialization;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class RangedData<T extends Comparable> {
    public Comparable min;
    public Comparable max;

    public RangedData(T min, T max) {
        this.min = min;
        this.max = max;
    }

    public boolean contains(T value) {
        return !(this.min != null && this.min.compareTo(value) > 0 || this.max != null && this.max.compareTo(value) < 0);
    }

    public static class RangedDataSerializer
            implements ConfigSerialization.Serializer {
        @Override
        public /* varargs */ Object fromData(ConfigData data, Class cl, Type... types) {
            String[] s = data.stringData.split(":", 2);
            Type[] t = types[0] instanceof ParameterizedType ? ((ParameterizedType) types[0]).getActualTypeArguments() : new Type[]{};
            Comparable min = s[0].isEmpty() ? null : (Comparable) new ConfigData(s[0]).deserialize((Class) types[0], t);
            Comparable max = s[1].isEmpty() ? null : (Comparable) new ConfigData(s[1]).deserialize((Class) types[0], t);
            return new RangedData(min, max);
        }

        @Override
        public /* varargs */ ConfigData toData(Object obj, Type... types) {
            RangedData rd = (RangedData) obj;
            StringBuilder out = new StringBuilder();
            if (rd.min != null) {
                out.append(rd.min);
            }
            out.append(':');
            if (rd.max != null) {
                out.append(rd.max);
            }
            return new ConfigData(out.toString());
        }
    }

}

