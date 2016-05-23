package gyurix.spigotutils;

import gyurix.configfile.ConfigSerialization;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.SU;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

/**
 * Class used for storing the data of a block
 */
public class BlockData implements ConfigSerialization.StringSerializable {
    public boolean anydata = true;
    public byte data;
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

    public BlockData(int id, byte data) {
        this.id = id;
        this.data = data;
        anydata = false;
    }
    public BlockData(String in) {
        String[] s = in.split(":", 2);
        try {
            try {
                this.id = Material.getMaterial(s[0].toUpperCase()).getId();
            } catch (Throwable e) {
                this.id = Integer.valueOf(s[0]);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            this.data = Byte.valueOf(s[1]);
            this.anydata = false;
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        BlockData bd = (BlockData) obj;
        return bd.id == this.id && (bd.data == this.data || bd.anydata || this.anydata);
    }

    public int hashCode() {
        return this.id * 16 + this.data;
    }

    public boolean isBlock(Block b) {
        int bid = b.getTypeId();
        byte bdata = b.getData();
        return this.id == bid && (this.anydata || bdata == this.data);
    }

    public void setBlock(Block b) {
        b.setTypeIdAndData(this.id, this.data, true);
    }

    public void setBlockNoPhysics(Block b) {
        b.setTypeIdAndData(this.id, this.data, false);
    }

    public ItemStack toItem() {
        ItemStack is = Config.blocks.get(this);
        if (is == null)
            is = Config.blocks.get(new BlockData(id));
        if (is == null)
            is = new ItemStack(id, 1, data);
        return is;
    }

    @Override
    public String toString() {
        Material m = Material.getMaterial(this.id);
        String sid = m == null ? "" + this.id : m.name();
        return this.anydata ? sid : sid + ":" + this.data;
    }
}

