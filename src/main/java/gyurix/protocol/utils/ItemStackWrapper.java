package gyurix.protocol.utils;

import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.nbt.NBTCompound;
import gyurix.nbt.NBTPrimitive;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static gyurix.protocol.Reflection.*;

public class ItemStackWrapper implements WrappedData, StringSerializable {
    public static final Constructor bukkitStack;
    public static final Object cmnObj;
    public static final Method createStack, getType, nmsCopy, saveStack, getItem, getID;
    public static final Field itemName;
    public static final HashMap<Integer, String> itemNames = new HashMap<>();

    static {
        Class nms = getNMSClass("ItemStack");
        Class nmsItem = getNMSClass("Item");
        Class nbt = getNMSClass("NBTTagCompound");
        Class obc = Reflection.getOBCClass("inventory.CraftItemStack");
        Class cmn = Reflection.getOBCClass("util.CraftMagicNumbers");
        cmnObj = getFieldData(cmn, "INSTANCE");
        createStack = Reflection.getMethod(nms, "createStack", nbt);
        saveStack = Reflection.getMethod(nms, "save", nbt);
        nmsCopy = Reflection.getMethod(obc, "asNMSCopy", ItemStack.class);
        bukkitStack = Reflection.getConstructor(obc, nms);
        getType = Reflection.getMethod(cmn, "getMaterialFromInternalName", String.class);
        getItem = Reflection.getMethod(cmn, "getItem", Material.class);
        getID = Reflection.getMethod(nmsItem, "getId", nmsItem);
        for (Map.Entry<?, ?> e : ((Map<?, ?>) getFieldData(getNMSClass("RegistryMaterials"), "b", getFieldData(nmsItem, "REGISTRY"))).entrySet()) {
            try {
                itemNames.put((Integer) getID.invoke(null, e.getKey()), e.getValue().toString());
            } catch (Throwable err) {
                SU.error(SU.cs, err, "SpigotLib", "gyurix");
            }
        }

        itemName = Reflection.getField(nmsItem, "name");
    }

    public NBTCompound nbtData = new NBTCompound();

    public ItemStackWrapper() {
    }

    public ItemStackWrapper(ItemStack is) {
        loadFromBukkitStack(is);
    }

    public byte getCount() {
        return (byte) ((NBTPrimitive) nbtData.map.get("Count")).data;
    }

    public ItemStackWrapper(Object vanillaStack) {
        loadFromVanillaStack(vanillaStack);
    }

    public void setCount(byte count) {
        nbtData.map.put("Count", new NBTPrimitive(count));
    }

    public short getDamage() {
        try {
            return (Short) ((NBTPrimitive) nbtData.map.get("Damage")).data;
        } catch (Throwable e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void setDamage(short damage) {
        nbtData.map.put("Damage", new NBTPrimitive(damage));
    }

    public String getId() {
        if (nbtData.map.get("id") == null)
            return "minecraft:air";
        return (String) ((NBTPrimitive) nbtData.map.get("id")).data;
    }

    public void setId(String newId) {
        nbtData.map.put("id", new NBTPrimitive(newId));
    }

    public int getNumericId() {
        return getType().getId();
    }

    public void setNumericId(int newId) {
        try {
            nbtData.map.put("id", new NBTPrimitive(itemNames.get(newId)));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public Material getType() {
        try {
            return (Material) getType.invoke(cmnObj, getId());
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return Material.AIR;
        }
    }

    public void loadFromBukkitStack(ItemStack is) {
        try {
            if (is != null) {
                Object nms = nmsCopy.invoke(null, is);
                if (nms != null)
                    nbtData.loadFromNMS(saveStack.invoke(nms, new NBTCompound().toNMS()));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void loadFromVanillaStack(Object is) {
        try {
            if (is != null)
                nbtData.loadFromNMS(saveStack.invoke(is, new NBTCompound().toNMS()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public boolean hasMetaData() {
        return nbtData.map.containsKey("tag");
    }

    public boolean isUnbreakable() {
        return getMetaData().getBoolean("Unbreakable");
    }

    public NBTCompound getMetaData() {
        return nbtData.getCompound("tag");
    }

    public void setUnbreakable(boolean unbreakable) {
        if (unbreakable) {
            getMetaData().map.put("Unbreakable", new NBTPrimitive(Byte.valueOf((byte) 1)));
        } else {
            getMetaData().map.remove("Unbreakable");
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
            return createStack.invoke(null, nbtData.toNMS());
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return null;
        }
    }

    @Override
    public String toString() {
        return nbtData.toString();
    }
}

