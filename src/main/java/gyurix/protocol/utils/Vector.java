package gyurix.protocol.utils;

import gyurix.protocol.Reflection;

import java.lang.reflect.Field;

public class Vector {
    private static final Field xf;
    private static final Field yf;
    private static final Field zf;

    static {
        Class vec3d = Reflection.getNMSClass("Vec3D");
        Field[] f = vec3d.getFields();
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
}

