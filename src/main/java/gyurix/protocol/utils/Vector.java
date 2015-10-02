package gyurix.protocol.utils;

import gyurix.protocol.Reflection;

import java.lang.reflect.Field;

public class Vector {
    public double x, y, z;
    private static final Field xf,yf,zf;
    static {
        Class vec3d=Reflection.getNMSClass("Vec3D");
        Field[] f=vec3d.getFields();
        xf=f[0];
        yf=f[1];
        zf=f[2];
    }
    public Vector(Object vanillaVector){
        try{
            x=(Double)xf.get(vanillaVector);
            y=(Double)yf.get(vanillaVector);
            z=(Double)zf.get(vanillaVector);
        }
        catch (Throwable e){
            e.printStackTrace();
        }
    }
}