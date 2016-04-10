package gyurix.protocol.utils;

import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class Vector implements WrappedData {
    private static final Class cl = Reflection.getNMSClass("Vec3D");
    private static final Constructor con = Reflection.getConstructor(cl, double.class, double.class, double.class);
    private static final Field xf;
    private static final Field yf;
    private static final Field zf;

    static {
        Field[] f = cl.getFields();
        xf = f[0];
        yf = f[1];
        zf = f[2];
    }

    public double x;
    public double y;
    public double z;

    public Vector(Object vanillaVector) {
        try {
            this.x = (Double) xf.get(vanillaVector);
            this.y = (Double) yf.get(vanillaVector);
            this.z = (Double) zf.get(vanillaVector);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object toNMS() {
        try {
            return con.newInstance(x, y, z);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return null;
        }
    }
}

