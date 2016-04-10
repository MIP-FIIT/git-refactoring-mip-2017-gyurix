package gyurix.spigotutils;

import gyurix.configfile.ConfigSerialization;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class BlockData
        implements ConfigSerialization.StringSerializable {
    public boolean anydata = true;
    public byte data;
    public int id;

    public BlockData(Block b) {
        this.id = b.getTypeId();
        this.data = b.getData();
        this.anydata = false;
    }

    public BlockData(BlockState b) {
        this.id = b.getTypeId();
        this.data = b.getRawData();
        this.anydata = false;
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
            this.data = Byte.valueOf(s[1]).byteValue();
            this.anydata = false;
        } catch (Throwable e) {
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

    @Override
    public String toString() {
        Material m = Material.getMaterial(this.id);
        String sid = m == null ? "" + this.id : m.name();
        return this.anydata ? sid : sid + ":" + this.data;
    }
}

