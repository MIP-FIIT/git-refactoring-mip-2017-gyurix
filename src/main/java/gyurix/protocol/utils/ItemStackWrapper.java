package gyurix.protocol.utils;

import gyurix.nbt.NBTCompound;
import gyurix.nbt.NBTPrimitive;
import gyurix.protocol.Reflection;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ItemStackWrapper {
    public NBTCompound nbtData;
    private static final Method createStack,saveStack;
    private static final Field handle;
    private static final Constructor bukkitStack;
    static {
        Class nms=Reflection.getNMSClass("ItemStack");
        Class nbt=Reflection.getNMSClass("NBTTagCompound");
        Class obc=Reflection.getOBCClass("inventory.CraftItemStack");
        createStack= Reflection.getMethod(nms,"createStack", nbt);
        saveStack=Reflection.getMethod(nms,"save", nbt);
        handle=Reflection.getFirstFieldOfType(obc,nms);
        bukkitStack=Reflection.getConstructor(obc,nms);
    }
    public ItemStackWrapper(){

    }
    public ItemStackWrapper(ItemStack is) {
        loadFromBukkitStack(is);
    }
    public ItemStackWrapper(Object vanillaStack) {
        loadFromVanillaStack(vanillaStack);
    }

    public Material getType(){
        return Material.getMaterial(getId());
    }
    public void setType(Material type){
        if (type==null)
            type=Material.AIR;
        setId((short)type.getId());
    }
    public short getId(){

        return (Short)(((NBTPrimitive)nbtData.map.get("id")).data);
    }
    public void setId(short newId){
        ((NBTPrimitive)nbtData.map.get("id")).data=newId;
    }
    public short getDamage(){
        return (Short)(((NBTPrimitive)nbtData.map.get("Damage")).data);
    }
    public void setDamage(short damage){
        ((NBTPrimitive)nbtData.map.get("Damage")).data=damage;
    }
    public byte getCount(){
        return (Byte)(((NBTPrimitive)nbtData.map.get("Count")).data);
    }
    public void setCount(byte count){
        ((NBTPrimitive)nbtData.map.get("Count")).data=count;
    }
    public void removeMetaData(){
        nbtData.map.remove("tag");
    }
    public NBTCompound getMetaData(){
        return nbtData.getCompound("tag");
    }
    public boolean isUnbreakable(){
        return getMetaData().getBoolean("Unbreakable");
    }
    public void setUnbreakable(boolean unbreakable){
        if (unbreakable)
            getMetaData().map.put("Unbreakable",new NBTPrimitive((byte)1));
        else
            getMetaData().map.remove("Unbreakable");
    }
    public void loadFromVanillaStack(Object is){
        nbtData=new NBTCompound();
        try {
            nbtData.loadFromNMS(saveStack.invoke(is, new NBTCompound().saveToNMS()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    public void loadFromBukkitStack(ItemStack is){
        nbtData=new NBTCompound();
        try {
            nbtData.loadFromNMS(saveStack.invoke(handle.get(is), new NBTCompound().saveToNMS()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    public Object toVanillaStack(){
        try {
            return createStack.invoke(null,nbtData.saveToNMS());
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
    public ItemStack toBukkitStack(){
        try {
            return (ItemStack) bukkitStack.newInstance(toVanillaStack());
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

}
