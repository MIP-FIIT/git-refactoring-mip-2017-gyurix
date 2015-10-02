package gyurix.protocol.utils;

import gyurix.protocol.Reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class BlockLocation {
    public int x,y,z;
    private static Constructor blockPositionConstructor;
    private static Method getX,getY,getZ;
    static {
        try {
            blockPositionConstructor= Reflection.getNMSClass("BlockPosition").getConstructor(int.class,int.class,int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        Class cl=Reflection.getNMSClass("BaseBlockPosition");
        try {
            getX=cl.getMethod("getX");
            getY=cl.getMethod("getY");
            getZ=cl.getMethod("getZ");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    public BlockLocation(Object vanillaBlockLocation){
        try {
            x=(Integer)getX.invoke(vanillaBlockLocation);
            y=(Integer)getY.invoke(vanillaBlockLocation);
            z=(Integer)getZ.invoke(vanillaBlockLocation);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    public BlockLocation(int x,int y,int z){
        this.x=x;
        this.y=y;
        this.z=z;
    }
    public Object toVanillaBlockPosition(){
        try {
            return blockPositionConstructor.newInstance(x,y,z);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}
