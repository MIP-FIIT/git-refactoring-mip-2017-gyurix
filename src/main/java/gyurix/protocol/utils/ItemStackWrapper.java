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
    private static final Method createStack;
    private static final Method saveStack;
    private static final Field handle;
    private static final Constructor bukkitStack;

    static {
        Class nms = Reflection.getNMSClass("ItemStack");
        Class nbt = Reflection.getNMSClass("NBTTagCompound");
        Class obc = Reflection.getOBCClass("inventory.CraftItemStack");
        createStack = Reflection.getMethod(nms, "createStack", nbt);
        saveStack = Reflection.getMethod(nms, "save", nbt);
        handle = Reflection.getFirstFieldOfType(obc, nms);
        bukkitStack = Reflection.getConstructor(obc, nms);
    }

    public NBTCompound nbtData;

    public ItemStackWrapper() {
    }

    public ItemStackWrapper(ItemStack is) {
        this.loadFromBukkitStack(is);
    }

    public ItemStackWrapper(Object vanillaStack) {
        this.loadFromVanillaStack(vanillaStack);
    }

    public Material getType() {
        return Material.getMaterial((int) this.getId());
    }

    public void setType(Material type) {
        if (type == null) {
            type = Material.AIR;
        }
        this.setId((short) type.getId());
    }

    public short getId() {
        return (Short) ((NBTPrimitive) this.nbtData.map.get("id")).data;
    }

    public void setId(short newId) {
        ((NBTPrimitive) this.nbtData.map.get("id")).data = newId;
    }

    public short getDamage() {
        return (Short) ((NBTPrimitive) this.nbtData.map.get("Damage")).data;
    }

    public void setDamage(short damage) {
        ((NBTPrimitive) this.nbtData.map.get("Damage")).data = damage;
    }

    public byte getCount() {
        return ((Byte) ((NBTPrimitive) this.nbtData.map.get("Count")).data).byteValue();
    }

    public void setCount(byte count) {
        ((NBTPrimitive) this.nbtData.map.get("Count")).data = Byte.valueOf(count);
    }

    public void removeMetaData() {
        this.nbtData.map.remove("tag");
    }

    public NBTCompound getMetaData() {
        return this.nbtData.getCompound("tag");
    }

    public boolean isUnbreakable() {
        return this.getMetaData().getBoolean("Unbreakable");
    }

    public void setUnbreakable(boolean unbreakable) {
        if (unbreakable) {
            this.getMetaData().map.put("Unbreakable", new NBTPrimitive(Byte.valueOf((byte) 1)));
        } else {
            this.getMetaData().map.remove("Unbreakable");
        }
    }

    public void loadFromVanillaStack(Object is) {
        this.nbtData = new NBTCompound();
        try {
            this.nbtData.loadFromNMS(saveStack.invoke(is, new NBTCompound().saveToNMS()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void loadFromBukkitStack(ItemStack is) {
        this.nbtData = new NBTCompound();
        try {
            this.nbtData.loadFromNMS(saveStack.invoke(handle.get(is), new NBTCompound().saveToNMS()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public Object toVanillaStack() {
        try {
            return createStack.invoke(null, this.nbtData.saveToNMS());
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public ItemStack toBukkitStack() {
        try {
            return (ItemStack) bukkitStack.newInstance(this.toVanillaStack());
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}

