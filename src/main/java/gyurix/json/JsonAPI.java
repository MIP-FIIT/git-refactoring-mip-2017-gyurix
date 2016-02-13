package gyurix.json;

import gyurix.configfile.ConfigSerialization;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.Config;
import gyurix.utils.Primitives;

import java.lang.reflect.*;
import java.util.*;

public class JsonAPI {
    public static final Type[] emptyTypeArray = new Type[0];

    public static String serialize(Object o) {
        StringBuilder sb = new StringBuilder();
        try {
            serialize(sb, o);
            return sb.toString();
        } catch (Throwable e) {
            System.err.println("JsonAPI: Error on serializing " + o.getClass().getName() + " object.");
            return "{}";
        }
    }

    private static void serialize(StringBuilder sb, Object o) {
        if (o == null) {
            sb.append("null");
            return;
        }
        Class cl = o.getClass();
        if (o instanceof String || o instanceof UUID || o.getClass().isEnum() || o instanceof ConfigSerialization.StringSerializable) {
            sb.append('\"').append(escape(o.toString())).append("\"");
        } else if (o instanceof Boolean || o instanceof Byte || o instanceof Short || o instanceof Integer || o instanceof Long || o instanceof Float || o instanceof Double) {
            sb.append(o);
        } else if (o instanceof Iterable || cl.isArray()) {
            sb.append('[');
            if (cl.isArray()) {
                for (Object obj : (Object[]) o) {
                    serialize(sb, obj);
                    sb.append(',');
                }
            } else {
                for (Object obj : (Iterable) o) {
                    serialize(sb, obj);
                    sb.append(',');
                }
            }
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.setCharAt(sb.length() - 1, ']');
            } else {
                sb.append(']');
            }
        } else if (o instanceof Map) {
            sb.append('{');
            for (Map.Entry<?, ?> e : ((Map<?, ?>) o).entrySet()) {
                String key = String.valueOf(e.getValue());
                sb.append('\"').append(escape(key)).append("\":");
                serialize(sb, key);
                sb.append(',');
            }
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.setCharAt(sb.length() - 1, '}');
            } else {
                sb.append('}');
            }
        } else {
            sb.append('{');
            for (Field f : cl.getDeclaredFields()) {
                try {
                    f.setAccessible(true);
                    JsonSettings settings = f.getAnnotation(JsonSettings.class);
                    String fn = f.getName();
                    boolean serialize = !(fn.equals("self") || fn.equals("parent") || fn.equals("instance"));
                    String defaultValue = "null";
                    if (settings != null) {
                        serialize = settings.serialize();
                        defaultValue = settings.defaultValue();
                    }
                    Object fo = f.get(o);
                    if (!serialize || String.valueOf(fo).equals(defaultValue)) continue;
                    sb.append('\"').append(escape(fn)).append("\":");
                    serialize(sb, fo);
                    sb.append(',');
                    continue;
                } catch (Throwable e) {
                    System.err.println("JsonAPI: Error on serializing " + f.getName() + " field in " + o.getClass().getName() + " class.");
                    if (!Config.debug) continue;
                    e.printStackTrace();
                }
            }
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.setCharAt(sb.length() - 1, '}');
            } else {
                sb.append('}');
            }
        }
    }

    public static Object deserialize(String json, Class cl, Type... params) {
        System.out.println(json);
        StringReader sr = new StringReader(json);
        try {
            return deserialize(null, sr, cl, params);
        } catch (Throwable e) {
            System.err.println(e.getMessage());
            //if (Config.debug) {
            e.printStackTrace();
            //}
            return "{}";
        }
    }

    private static Object deserialize(Object parent, StringReader in, Class cl, Type... params) throws Throwable {
        cl = Primitives.wrap(cl);
        char c = '-';
        if (in.hasNext())
            c = in.next();
        else in.id++;
        if (Map.class.isAssignableFrom(cl)) {
            if (c != '{') {
                throw new Throwable("JSONAPI: Error on deserializing Json " + new String(in.str) + ", expected {, found " + c + " (character id: " + in.id + ")");
            }
            Class keyClass = (Class) (params[0] instanceof ParameterizedType ? ((ParameterizedType) params[0]).getRawType() : params[0]);
            Type[] keyType = params[0] instanceof ParameterizedType ? ((ParameterizedType) params[0]).getActualTypeArguments() : emptyTypeArray;
            Class valueClass = (Class) (params[1] instanceof ParameterizedType ? ((ParameterizedType) params[1]).getRawType() : params[1]);
            Type[] valueType = params[1] instanceof ParameterizedType ? ((ParameterizedType) params[1]).getActualTypeArguments() : emptyTypeArray;
            Map map = cl == EnumMap.class ? new EnumMap<>(keyClass) : (Map) cl.newInstance();
            if (in.next() == '}')
                return map;
            else
                in.id -= 2;
            while (in.next() != '}') {
                Object key = deserialize(map, in, keyClass, keyType);
                if (in.next() != ':')
                    throw new Throwable("JSONAPI: Error on deserializing Json " + new String(in.str) + ", expected :, found " + in.last() + " (character id: " + (in.id - 1) + ")");
                map.put(key, deserialize(map, in, valueClass, valueType));
            }
            return map;
        } else if (Collection.class.isAssignableFrom(cl)) {
            if (c != '[') {
                throw new Throwable("JSONAPI: Error on deserializing Json " + new String(in.str) + ", expected {, found " + c + " (character id: " + in.id + ")");
            }
            Class dataClass = (Class) (params[0] instanceof ParameterizedType ? ((ParameterizedType) params[0]).getRawType() : params[0]);
            Type[] dataType = params[0] instanceof ParameterizedType ? ((ParameterizedType) params[0]).getActualTypeArguments() : emptyTypeArray;
            Collection col = (Collection) cl.newInstance();
            if (in.next() == ']')
                return col;
            else
                in.id -= 2;
            while (in.next() != ']') {
                col.add(deserialize(col, in, dataClass, dataType));
            }
            return col;
        } else if (cl.isArray()) {
            if (c != '[') {
                throw new Throwable("JSONAPI: Error on deserializing Json " + new String(in.str) + ", expected {, found " + c + " (character id: " + in.id + ")");
            }
            Class dataClass = cl.getComponentType();
            ArrayList col = new ArrayList();
            if (in.next() == ']') {
                return Array.newInstance(dataClass, 0);
            } else
                in.id -= 2;
            while (in.next() != ']') {
                col.add(deserialize(null, in, dataClass));
            }
            Object[] out = (Object[]) Array.newInstance(dataClass, col.size());
            return col.toArray(out);
        } else if (c == '{') {
            Object obj = ConfigSerialization.newInstance(cl);
            if (in.next() == '}')
                return obj;
            else
                in.id -= 2;
            while (in.next() != '}') {
                String fn = readString(in);
                if (in.next() != ':')
                    throw new Throwable("JSONAPI: Error on deserializing Json " + new String(in.str) + ", expected :, found " + in.last() + " (character id: " + (in.id - 1) + ")");
                try {
                    Field f = Reflection.getField(cl, fn);
                    Type gt = f.getGenericType();
                    f.set(obj, deserialize(obj, in, f.getType(), gt instanceof ParameterizedType ? ((ParameterizedType) gt).getActualTypeArguments() : emptyTypeArray));
                } catch (Throwable e) {
                    System.err.println("JSONAPI: Field " + fn + " is declared in json, but it is missing from class " + cl.getName() + ".");
                    e.printStackTrace();
                }
                ;
            }
            try {
                Field f = Reflection.getField(cl, "parent");
                f.set(obj, parent);
            } catch (Throwable e) {
            }
            ;
            try {
                Field f = Reflection.getField(cl, "self");
                f.set(obj, obj);
            } catch (Throwable e) {
            }
            ;
            try {
                Field f = Reflection.getField(cl, "instance");
                f.set(obj, obj);
            } catch (Throwable e) {
            }
            ;
            return obj;
        } else {
            in.id--;
            String str = readString(in);
            try {
                return Reflection.getConstructor(cl, String.class).newInstance(str);
            } catch (Throwable e) {
            }
            ;
            try {
                return Reflection.getMethod(cl, "valueOf", String.class).invoke(null, str);
            } catch (Throwable e) {
            }
            ;
            try {
                Method m = Reflection.getMethod(cl, "fromString", String.class);
                if (cl == UUID.class && !str.contains("-"))
                    str = str.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
                return m.invoke(null, str);

            } catch (Throwable e) {
                e.printStackTrace();
            }
            ;
            throw new Throwable("JSONAPI: Error on deserializing Json " + new String(in.str) + ", expected " + cl.getName() + ", found String.");
        }
    }

    public static String readString(StringReader in) {
        int start = in.id;
        int end = -1;
        boolean esc = false;
        boolean stresc = false;
        while (in.hasNext()) {
            char c = in.next();
            if (esc)
                esc = false;
            else if (c == '\\')
                esc = true;
            else if (c == '\"') {
                if (stresc) {
                    end = in.id - 1;
                    break;
                } else {
                    stresc = true;
                    start = in.id;
                }
            } else if (!stresc && (c == ']' || c == '}' || c == ',' || c == ':')) {
                in.id--;
                break;
            }
        }
        if (end == -1)
            end = in.id;
        return unescape(new String(in.str, start, end - start));
    }

    public static String unescape(String s) {
        boolean esc = false;
        int utf = -1;
        int utfc = -1;
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (esc) {
                switch (c) {
                    case 'b': {
                        out.append('\b');
                        break;
                    }
                    case 'f': {
                        out.append('\f');
                        break;
                    }
                    case 'n': {
                        out.append('\n');
                        break;
                    }
                    case 'r': {
                        out.append('\r');
                        break;
                    }
                    case 't': {
                        out.append('\t');
                        break;
                    }
                    case 'u': {
                        utf = 0;
                        utfc = 0;
                        break;
                    }
                    default: {
                        out.append(c);
                    }
                }
                esc = false;
                continue;
            }
            if (utf >= 0) {
                utf = utf * 16 + HextoDec(c);
                if (++utfc != 4) continue;
                out.append((char) utf);
                utf = -1;
                utfc = -1;
                continue;
            }
            if (c == '\\') {
                esc = true;
                continue;
            }
            out.append(c);
        }
        return out.toString();
    }

    public static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static int HextoDec(char c) {
        return c >= '0' && c <= '9' ? c - 48 : (c >= 'A' && c <= 'F' ? c - 65 : c - 97);
    }
}