package gyurix.spigotutils;

import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.SU;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

/**
 * Class used for storing the data of a block
 */
public class BlockData implements StringSerializable, Comparable<BlockData> {
    public boolean anydata = true;
    public short data;
    public int id;

    public BlockData(Block b) {
        id = b.getTypeId();
        data = b.getData();
        anydata = false;
    }

    public BlockData(BlockState b) {
        id = b.getTypeId();
        data = b.getRawData();
        anydata = false;
    }

    public BlockData(int id) {
        this.id = id;
    }

    public BlockData(int id, short data) {
        this.id = id;
        this.data = data;
        anydata = false;
    }

    public BlockData(String in) {
        String[] s = in.split(":", 2);
        try {
            try {
                id = Material.getMaterial(s[0].toUpperCase()).getId();
            } catch (Throwable e) {
                id = Integer.valueOf(s[0]);
            }
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        if (s.length == 2)
            try {
                data = Byte.valueOf(s[1]);
                anydata = false;
            } catch (Throwable e) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
            }
    }

    @Override
    public int compareTo(BlockData o) {
        return ((Integer) hashCode()).compareTo(o.hashCode());
    }

    public int hashCode() {
        return id * 16 + data;
    }

    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        BlockData bd = (BlockData) obj;
        return bd.id == id && (bd.data == data || bd.anydata || anydata);
    }

    public BlockData clone() {
        return anydata ? new BlockData(id) : new BlockData(id, data);
    }

    @Override
    public String toString() {
        Material m = Material.getMaterial(id);
        String sid = m == null ? "" + id : m.name();
        return anydata ? sid : sid + ':' + data;
    }

    public boolean isBlock(Block b) {
        int bid = b.getTypeId();
        byte bdata = b.getData();
        return id == bid && (anydata || bdata == data);
    }

    public void setBlock(Block b) {
        b.setTypeIdAndData(id, (byte) data, true);
    }

    public void setBlockNoPhysics(Block b) {
        b.setTypeIdAndData(id, (byte) data, false);
    }

    public ItemStack toItem() {
        ItemStack is = Config.blocks.get(this);
        if (is == null)
            is = Config.blocks.get(new BlockData(id));
        if (is == null)
            is = new ItemStack(id, 1, data);
        return is;
    }
}

