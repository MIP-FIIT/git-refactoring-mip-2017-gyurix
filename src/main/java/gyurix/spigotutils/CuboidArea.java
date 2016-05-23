package gyurix.spigotutils;

import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Location;

/**
 * Created by GyuriX on 2016.05.15..
 */
public class CuboidArea {
    public LocationData pos1, pos2;

    public CuboidArea() {
    }

    public CuboidArea(Selection sel) {
        pos1 = new LocationData(sel.getMinimumPoint());
        pos2 = new LocationData(sel.getMaximumPoint());
        fix();
    }

    public CuboidArea(LocationData pos1, LocationData pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public void fix() {
        double tmp;
        if (pos1.x > pos2.x) {
            tmp = pos1.x;
            pos1.x = pos2.x;
            pos2.x = tmp;
        }
        if (pos1.y > pos2.y) {
            tmp = pos1.y;
            pos1.y = pos2.y;
            pos2.y = tmp;
        }
        if (pos1.z > pos2.z) {
            tmp = pos1.z;
            pos1.z = pos2.z;
            pos2.z = tmp;
        }
    }

    public boolean contains(Location loc) {
        return pos1.world.equals(loc.getWorld().getName())
                && loc.getX() >= pos1.x && loc.getY() >= pos1.y && loc.getZ() >= pos1.z
                && loc.getX() <= pos2.x && loc.getY() <= pos2.y && loc.getZ() <= pos2.z;
    }
}
