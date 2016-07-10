package gyurix.spigotutils;

import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;

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

    public CuboidArea (LocationData pos1, LocationData pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public boolean contains (Location loc) {
        return pos1.world.equals(loc.getWorld().getName())
                && loc.getX() >= pos1.x && loc.getY() >= pos1.y && loc.getZ() >= pos1.z
                && loc.getX() <= pos2.x && loc.getY() <= pos2.y && loc.getZ() <= pos2.z;
    }

    public void resetOutlineWithBlock(Player plr) {
        World w = pos1.getWorld();
        ArrayList<Location> locs = new ArrayList<>();
        for (int x = (int) pos1.x + 1; x < pos2.x; x++) {
            resetOutlineBlock(w.getBlockAt(x, (int) pos1.y, (int) pos1.z), plr);
            resetOutlineBlock(w.getBlockAt(x, (int) pos1.y, (int) pos2.z), plr);
            resetOutlineBlock(w.getBlockAt(x, (int) pos2.y, (int) pos1.z), plr);
            resetOutlineBlock(w.getBlockAt(x, (int) pos2.y, (int) pos2.z), plr);
        }
        for (int y = (int) pos1.y + 1; y < pos2.y; y++) {
            resetOutlineBlock(w.getBlockAt((int) pos1.x, y, (int) pos1.z), plr);
            resetOutlineBlock(w.getBlockAt((int) pos1.x, y, (int) pos2.z), plr);
            resetOutlineBlock(w.getBlockAt((int) pos2.x, y, (int) pos1.z), plr);
            resetOutlineBlock(w.getBlockAt((int) pos2.x, y, (int) pos2.z), plr);
        }
        for (int z = (int) pos1.z; z <= pos2.z; z++) {
            resetOutlineBlock(w.getBlockAt((int) pos1.x, (int) pos1.y, z), plr);
            resetOutlineBlock(w.getBlockAt((int) pos1.x, (int) pos2.y, z), plr);
            resetOutlineBlock(w.getBlockAt((int) pos2.x, (int) pos1.y, z), plr);
            resetOutlineBlock(w.getBlockAt((int) pos2.x, (int) pos2.y, z), plr);
        }
    }

    public void resetOutlineBlock (Block block, Player plr) {
        plr.sendBlockChange(block.getLocation(), block.getTypeId(), block.getData());
    }

    public void showOutlineWithBlock(Player plr, BlockData bd) {
        World w = pos1.getWorld();
        for (int x = (int) pos1.x + 1; x < pos2.x; x++) {
            plr.sendBlockChange(new Location(w, x, pos1.y, pos1.z), bd.id, bd.data);
            plr.sendBlockChange(new Location(w, x, pos1.y, pos2.z), bd.id, bd.data);
            plr.sendBlockChange(new Location(w, x, pos2.y, pos1.z), bd.id, bd.data);
            plr.sendBlockChange(new Location(w, x, pos2.y, pos2.z), bd.id, bd.data);
        }
        for (int y = (int) pos1.y + 1; y < pos2.y; y++) {
            plr.sendBlockChange(new Location(w, pos1.x, y, pos1.z), bd.id, bd.data);
            plr.sendBlockChange(new Location(w, pos1.x, y, pos2.z), bd.id, bd.data);
            plr.sendBlockChange(new Location(w, pos2.x, y, pos1.z), bd.id, bd.data);
            plr.sendBlockChange(new Location(w, pos2.x, y, pos2.z), bd.id, bd.data);
        }
        for (int z = (int) pos1.z; z <= pos2.z; z++) {
            plr.sendBlockChange(new Location(w, pos1.x, pos1.y, z), bd.id, bd.data);
            plr.sendBlockChange(new Location(w, pos1.x, pos2.y, z), bd.id, bd.data);
            plr.sendBlockChange(new Location(w, pos2.x, pos1.y, z), bd.id, bd.data);
            plr.sendBlockChange(new Location(w, pos2.x, pos2.y, z), bd.id, bd.data);
        }
    }

    public int size () {
        return ((int) pos2.x - (int) pos1.x + 1) * ((int) pos2.y - (int) pos1.y + 1) * ((int) pos2.z - (int) pos1.z + 1);
    }
}
