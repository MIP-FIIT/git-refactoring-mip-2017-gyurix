package gyurix.protocol.utils;

import gyurix.nbt.NBTCompound;
import gyurix.nbt.NBTPrimitive;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ItemStackWrapper implements WrappedData {
    private static final Constructor bukkitStack;
    private static final Method createStack;
    private static final Method getType;
    private static final Method nmsCopy;
    private static final Method saveStack;

    static {
        Class nms = Reflection.getNMSClass("ItemStack");
        Class nbt = Reflection.getNMSClass("NBTTagCompound");
        Class obc = Reflection.getOBCClass("inventory.CraftItemStack");
        Class cmn = Reflection.getOBCClass("util.CraftMagicNumbers");
        createStack = Reflection.getMethod(nms, "createStack", nbt);
        saveStack = Reflection.getMethod(nms, "save", nbt);
        nmsCopy = Reflection.getMethod(obc, "asNMSCopy", ItemStack.class);
        bukkitStack = Reflection.getConstructor(obc, nms);
        getType = Reflection.getMethod(cmn, "getMaterialFromInternalName", String.class);
    }

    public NBTCompound nbtData;

    public ItemStackWrapper() {
    }

    public ItemStackWrapper(ItemStack is) {
        loadFromBukkitStack(is);
    }

    public ItemStackWrapper(Object vanillaStack) {
        loadFromVanillaStack(vanillaStack);
    }

    public byte getCount() {
        return ((Byte) ((NBTPrimitive) nbtData.map.get("Count")).data).byteValue();
    }

    public void setCount(byte count) {
        ((NBTPrimitive) nbtData.map.get("Count")).data = Byte.valueOf(count);
    }

    public short getDamage() {
        return (Short) ((NBTPrimitive) nbtData.map.get("Damage")).data;
    }

    public void setDamage(short damage) {
        ((NBTPrimitive) nbtData.map.get("Damage")).data = damage;
    }

    public String getId() {
        return (String) ((NBTPrimitive) nbtData.map.get("id")).data;
    }

    public void setId(String newId) {
        ((NBTPrimitive) nbtData.map.get("id")).data = newId;
    }

    public NBTCompound getMetaData() {
        return nbtData.getCompound("tag");
    }

    public Material getType() {
        try {
            return (Material) getType.invoke(null, getId());
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean hasMetaData() {
        return nbtData.map.containsKey("tag");
    }

    public boolean isUnbreakable() {
        return getMetaData().getBoolean("Unbreakable");
    }

    public void setUnbreakable(boolean unbreakable) {
        if (unbreakable) {
            getMetaData().map.put("Unbreakable", new NBTPrimitive(Byte.valueOf((byte) 1)));
        } else {
            getMetaData().map.remove("Unbreakable");
        }
    }

    public void loadFromBukkitStack(ItemStack is) {
        nbtData = new NBTCompound();
        try {
            if (is != null) {
                Object nms = nmsCopy.invoke(null, is);
                if (nms != null)
                    nbtData.loadFromNMS(saveStack.invoke(nms, new NBTCompound().saveToNMS()));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void loadFromVanillaStack(Object is) {
        nbtData = new NBTCompound();
        try {
            if (is != null)
                nbtData.loadFromNMS(saveStack.invoke(is, new NBTCompound().saveToNMS()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void removeMetaData() {
        nbtData.map.remove("tag");
    }

    public ItemStack toBukkitStack() {
        try {
            return (ItemStack) bukkitStack.newInstance(toNMS());
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Object toNMS() {
        try {
            return createStack.invoke(null, nbtData.saveToNMS());
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return null;
        }
    }
}

