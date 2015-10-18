package gyurix.protocol;

import gyurix.spigotlib.Config;
import gyurix.spigotlib.Main;
import gyurix.utils.Primitives;
import org.bukkit.Bukkit;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class Reflection {
    public static String version = null;
    public static final HashMap<String, String> nmsRenames = new HashMap();

    public static void init() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        version = name.substring(name.lastIndexOf('.') + 1);
        HashMap<String, String> mapping = Config.packetMapping.get(version);
        if (mapping == null) {
            Main.log.severe("The packet name mapping is missing for your server version!");
        } else {
            nmsRenames.putAll(mapping);
        }
        version += ".";
    }

    public static Class getInnerClass(Class cl, String name) {
        try {
            name = cl.getName() + "$" + name;
            for (Class c : cl.getDeclaredClasses()) {
                if (c.getName().equals(name))
                    return c;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Inner class not found: " + cl.getName() + "$" + name);
    }

    public static Class getClass(String className) {
        try {
            String[] classNames = className.split("\\$");
            Class c = Class.forName(classNames[0]);
            for (int i = 1; i < classNames.length; i++) {
                c = getInnerClass(c, classNames[i]);
            }
            return c;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("Class not found: " + className);
        }
    }

    public static Class getNMSClass(String className) {
        String newName = nmsRenames.get(className);
        if (newName != null)
            className = newName;
        try {
            return getClass("net.minecraft.server." + version + className);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("NMS class not found: " + className);
        }
    }

    public static Class getOBCClass(String className) {
        try {
            return getClass("org.bukkit.craftbukkit." + version + className);
        } catch (Throwable e) {
            throw new RuntimeException("OBC class not found: " + className);
        }
    }

    public static Object getEnum(Class enumType, String value) {
        try {
            return enumType.getMethod("valueOf", String.class).invoke(value);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Field getField(Class clazz, String name) {
        try {
            return setFieldAccessible(clazz.getDeclaredField(name));
        } catch (Throwable e) {
            throw new RuntimeException("Can't get field " + name + " from class " + clazz.getName());
        }
    }

    public static Field getFirstFieldOfType(Class clazz, Class type) {
        try {
            for (Field f : clazz.getDeclaredFields()) {
                if (f.getType().equals(type)) {
                    return setFieldAccessible(f);
                }
            }
            throw new RuntimeException(type.getName() + " typed field was not found in class " + clazz);
        } catch (Throwable e) {
            throw new RuntimeException("Error in analyzing " + type.getName() + " fields in class " + clazz);
        }
    }

    public static Field setFieldAccessible(Field f) {
        try {
            f.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            int modifiers = modifiersField.getInt(f);
            modifiers &= 0xFFFFFFEF;
            modifiersField.setInt(f, modifiers);
            return f;
        } catch (Throwable e) {
            throw new RuntimeException("Can't set field " + f + " accessible!");
        }

    }

    public static Field getLastFieldOfType(Class clazz, Class type) {
        Field field = null;
        for (Field f : clazz.getDeclaredFields()) {
            if (f.getType().equals(type)) {
                field = f;
            }
        }
        return setFieldAccessible(field);
    }

    public static Method getMethod(Class cl, String name, Class... args) {
        while (cl!=null){
            if (args.length == 0) {
                for (Method m : cl.getDeclaredMethods()) {
                    if (m.getParameterTypes().length==0&&m.getName().equals(name)) {
                        m.setAccessible(true);
                        return m;
                    }
                }
                for (Method m : cl.getDeclaredMethods()) {
                    if (m.getParameterTypes().length==0&&m.getName().equalsIgnoreCase(name)) {
                        m.setAccessible(true);
                        return m;
                    }
                }
            } else {
                for (Method m : cl.getDeclaredMethods()) {
                    if (m.getName().equals(name) && classArrayCompare(args, m.getParameterTypes())) {
                        m.setAccessible(true);
                        return m;
                    }
                }
                for (Method m : cl.getDeclaredMethods()) {
                    if (m.getName().equals(name) && classArrayCompareLight(args, m.getParameterTypes())) {
                        m.setAccessible(true);
                        return m;
                    }
                }
                for (Method m : cl.getDeclaredMethods()) {
                    if (m.getName().equalsIgnoreCase(name) && classArrayCompare(args, m.getParameterTypes())) {
                        m.setAccessible(true);
                        return m;
                    }
                }
                for (Method m : cl.getDeclaredMethods()) {
                    if (m.getName().equalsIgnoreCase(name) && classArrayCompareLight(args, m.getParameterTypes())) {
                        m.setAccessible(true);
                        return m;
                    }
                }
            }
            cl=cl.getSuperclass();
        }
        throw new RuntimeException("Can't find method " + name + " in class " + cl + "!");
    }

    public static boolean classArrayCompare(Class[] l1, Class[] l2) {
        if (l1.length != l2.length) {
            return false;
        }
        for (int i = 0; i < l1.length; i++) {
            if (l1[i] != l2[i])
                return false;
        }
        return true;
    }
    public static boolean classArrayCompareLight(Class[] l1, Class[] l2) {
        if (l1.length != l2.length) {
            return false;
        }
        for (int i = 0; i < l1.length; i++) {
            if (!l2[i].isAssignableFrom(l1[i]))
                return false;
        }
        return true;
    }

    public static Constructor getConstructor(Class cl, Class... classes) {
        try {
            Constructor c = cl.getDeclaredConstructor(classes);
            c.setAccessible(true);
            return c;
        } catch (Throwable e) {
            throw new RuntimeException("Can't find constructor!");
        }
    }

    public static Field getFirstFieldOfType(Class cl, Class returnType, String... matches) {
        for (Field f : cl.getDeclaredFields()) {
            if (f.getType() == returnType) {
                if (f.getGenericType() instanceof ParameterizedType) {
                    ParameterizedType type = (ParameterizedType) f.getGenericType();
                    Type[] types = type.getActualTypeArguments();
                    if (matches.length == types.length) {
                        boolean match = true;
                        for (int i = 0; i < matches.length; i++) {
                            if (!((Class) types[i]).getName().matches(matches[i])) {
                                match = false;
                                break;
                            }
                        }
                        if (match) {
                            return setFieldAccessible(f);
                        }
                    }
                }
            }
        }
        throw new RuntimeException("Can't find field!");
    }

    public static Object getData(Object obj, List<Object> data) {
        try {
            Class[] ecd=new Class[0];
            Class[] cd=ecd;
            Object[] eod=new Object[0];
            Object[] od=eod;
            for (Object o:data) {
                Class oc=o.getClass();
                if (oc.isArray()) {
                    od=(Object[])o;
                    cd = new Class[od.length];
                    for (int j = 0; j < cd.length; j++) {
                        cd[j] = od[j].getClass();
                    }
                }
                else{
                    for (String s:((String)o).split("\\.")){
                        Class objc=obj.getClass();
                        try {
                            obj= getMethod(objc,"get" + s,cd).invoke(obj,od);
                        } catch (RuntimeException e) {
                            try {
                                obj = getMethod(objc,s,cd).invoke(obj, od);
                            } catch (RuntimeException e2) {
                                obj = getField(objc, s).get(obj);
                            }
                        }
                        cd=ecd;
                        od=eod;
                    }
                }
            }
        } catch (Throwable e) {
            if (Config.debug)
                e.printStackTrace();
            System.out.println("§cReflection ERROR: §fFailed to handle " + data + " request.");
            return null;
        }
        return obj;
    }
}