package gyurix.spigotutils;

import gyurix.configfile.ConfigSerialization.StringSerializable;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

/**
 * Created by GyuriX on 2016. 08. 13..
 */
public class XZ implements StringSerializable, Comparable<XZ> {
    public final int x, z;

    public XZ(String in) {
        String[] d = in.split(" ", 2);
        x = Integer.valueOf(d[0]);
        z = Integer.valueOf(d[1]);
    }

    public XZ(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public XZ(Block bl) {
        x = bl.getX();
        z = bl.getZ();
    }

    public XZ(Chunk c) {
        x = c.getX();
        z = c.getZ();
    }

    @Override
    public int compareTo(XZ o) {
        return ((Integer) hashCode()).compareTo(o.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        XZ xz = (XZ) obj;
        return x == xz.x && z == xz.z;
    }

    @Override
    public int hashCode() {
        return x << 16 + z;
    }

    @Override
    public String toString() {
        return x + " " + z;
    }
}
