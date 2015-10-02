package gyurix.spigotutils;

import gyurix.configfile.ConfigSerialization;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

/**
 * A simple class for representing the data of a block. It helps you to easily handle
 * block types.
 */
public class BlockData implements ConfigSerialization.StringSerializable{
    /**
     * Id of the block
     */
    public int id;
    /**
     * Subid / Data value of the block
     */
    public byte data;
    /**
     * true if the block can have any kind of subid
     */
    public boolean anydata=true;

    /**
     * Constructs the BlockData of a Block.
     * @param b target Block
     */
    public BlockData(Block b){
        id=b.getTypeId();
        data=b.getData();
        anydata=false;
    }

    /**
     * Constructs the BlockData of a BlockState.
     * @param b target BlockState
     */
    public BlockData(BlockState b){
        id=b.getTypeId();
        data=b.getRawData();
        anydata=false;
    }

    /**
     * Constructs the BlockData of its representing String.
     * @param in String representing the BlockData
     */
    public BlockData(String in){
        String[] s=in.split(":",2);
        try{
            try{
                id= Material.getMaterial(s[0].toUpperCase()).getId();
            }
            catch (Throwable e){
                id = Integer.valueOf(s[0]);
            }
        }
        catch (Throwable e){
            e.printStackTrace();
        }
        try{
            data = Byte.valueOf(s[1]);
            anydata=false;
        }
        catch (Throwable e){
        }
    }

    /**
     * Checks if the given block is matching with this BlockData
     * @param b checked block
     * @return true if the given block matches with this BlockData
     */
    public boolean isBlock(Block b){
        int bid=b.getTypeId();
        byte bdata=b.getData();
        return id==bid&&(anydata||bdata==data);
    }

    /**
     * Sets the given Blocks type and data to match with this BlockData
     * @param b target Block
     */
    public void setBlock(Block b){
        b.setTypeIdAndData(id,data,true);
    }
    /**
     * Sets the given Blocks type and data to match with this BlockData
     * without calculating physics on the target block
     * @param b target Block
     */
    public void setBlockNoPhysics(Block b){
        b.setTypeIdAndData(id,data,false);
    }

    /**
     * Converts this BlockData to it's representing String
     * @return The String representing this BlockData
     */
    @Override
    public String toString() {
        Material m=Material.getMaterial(id);
        String sid=m==null?""+id:m.name();
        return anydata?sid:sid+":"+data;
    }

    /**
     * Checks if the given BlockData matches with this one.
     * @param obj checked BlockData
     * @return True if the given BlockData is same as this one, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj==null||obj.getClass()!=getClass())
            return false;
        BlockData bd= (BlockData) obj;
        return bd.id==id&&(bd.data==data||bd.anydata||anydata);
    }

    /**
     * Calculates a hashcode for this BlockData
     * @return The hashcode of this BlockData
     */
    @Override
    public int hashCode() {
        return id*16+data;
    }
}
