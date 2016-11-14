package gyurix.protocol.utils;

import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class BlockLocation implements WrappedData, StringSerializable {
    public static final BlockLocation notDefined = new BlockLocation(0, 0, 0);
    private static final Class cl = Reflection.getNMSClass("BaseBlockPosition");
    private static final Constructor con = Reflection.getConstructor(cl, int.class, int.class, int.class);
    private static final Method getX = Reflection.getMethod(cl, "getX");
    private static final Method getY = Reflection.getMethod(cl, "getY");
    private static final Method getZ = Reflection.getMethod(cl, "getZ");
    public int x;
    public int y;
    public int z;

    public BlockLocation() {

    }

    public BlockLocation(String in) {
        String[] d = in.split(" ", 3);
        x = Integer.valueOf(d[0]);
        y = Integer.valueOf(d[1]);
        z = Integer.valueOf(d[2]);
    }

    public BlockLocation(Block bl) {
        this(bl.getX(), bl.getY(), bl.getZ());
    }

    public BlockLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    public BlockLocation clone() {
        return new BlockLocation(x, y, z);
    }

    @Override
    public String toString() {
        return x + " " + y + ' ' + z;
    }

    public Block getBlock(World w) {
        return w.getBlockAt(x, y, z);
    }

    public Location getLocation(World w) {
        return new Location(w, x, y, z);
    }

    public boolean isDefined() {
        return x != 0 || y != 0 || z != 0;
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

