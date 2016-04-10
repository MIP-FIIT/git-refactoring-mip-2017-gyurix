package gyurix.protocol.utils;

import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class BlockLocation implements WrappedData {
    private static final Class cl = Reflection.getNMSClass("BaseBlockPosition");
    private static final Constructor con = Reflection.getConstructor(cl, int.class, int.class, int.class);
    private static final Method getX = Reflection.getMethod(cl, "getX");
    private static final Method getY = Reflection.getMethod(cl, "getY");
    private static final Method getZ = Reflection.getMethod(cl, "getZ");
    public int x;
    public int y;
    public int z;

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

    public BlockLocation(Object nmsData) {
        try {
            x = (int) getX.invoke(nmsData);
            y = (int) getY.invoke(nmsData);
            z = (int) getZ.invoke(nmsData);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    @Override
    public Object toNMS() {
        try {
            return con.newInstance(x, y, z);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }
}

