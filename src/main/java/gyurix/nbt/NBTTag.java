package gyurix.nbt;

import gyurix.configfile.ConfigData;
import gyurix.configfile.ConfigSerialization;
import gyurix.protocol.utils.WrappedData;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Type;

public interface NBTTag extends WrappedData {
    void write(ByteBuf buf);

    class NBTSerializer implements ConfigSerialization.Serializer {
        @Override
        public Object fromData(ConfigData data, Class paramClass, Type... paramVarArgs) {
            String d[] = data.stringData.split(" ", 2);
            data.stringData = d[1];
            NBTTagType type = NBTTagType.values()[Integer.valueOf(d[0])];
            return data.deserialize(type.getDataClass());
        }

        @Override
        public ConfigData toData(Object obj, Type... paramVarArgs) {
            Object o = obj instanceof NBTPrimitive ? ((NBTPrimitive) obj).getData() : obj;
            ConfigData cd = ConfigData.serializeObject(o);
            cd.stringData = NBTTagType.of(o).ordinal() + " " + cd.stringData;
            return cd;
        }
    }
}

