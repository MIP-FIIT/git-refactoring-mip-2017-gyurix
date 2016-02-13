package gyurix.protocol.utils;

import gyurix.protocol.Reflection;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class BlockLocation {
    private static Constructor blockPositionConstructor;
    private static Method getX;
    private static Method getY;
    private static Method getZ;

    static {
        try {
            blockPositionConstructor = Reflection.getNMSClass("BlockPosition").getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        Class cl = Reflection.getNMSClass("BaseBlockPosition");
        try {
            getX = cl.getMethod("getX");
            getY = cl.getMethod("getY");
            getZ = cl.getMethod("getZ");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public int x;
    public int y;
    public int z;

    public BlockLocation(Object vanillaBlockLocation) {
        try {
            this.x = (Integer) getX.invoke(vanillaBlockLocation);
            this.y = (Integer) getY.invoke(vanillaBlockLocation);
            this.z = (Integer) getZ.invoke(vanillaBlockLocation);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public BlockLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockLocation(Block bl) {
        this(bl.getX(), bl.getY(), bl.getZ());
    }

    public BlockLocation(Location loc) {
        this(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public Object toVanillaBlockPosition() {
        try {
            return blockPositionConstructor.newInstance(this.x, this.y, this.z);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}

