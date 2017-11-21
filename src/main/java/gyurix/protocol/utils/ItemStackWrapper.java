package gyurix.protocol.utils;

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

import static gyurix.configfile.ConfigSerialization.ConfigOptions;
import static gyurix.nbt.NBTTagType.tag;
import static gyurix.protocol.Reflection.getFieldData;
import static gyurix.protocol.Reflection.getNMSClass;

public class ItemStackWrapper implements WrappedData {
    @ConfigOptions(serialize = false)
    public static final HashMap<Integer, String> itemNames = new HashMap<>();
    private static final Constructor bukkitStack;
    @ConfigOptions(serialize = false)
    private static final Object cmnObj;
    private static final Method getType, nmsCopy, saveStack, getItem, getID, convertStack;
    private static final Class nmsClass;
    private static final Field itemName;
    private static Constructor createStack;

    static {
        Class nmsItem = getNMSClass("Item");
        nmsClass = getNMSClass("ItemStack");
        Class nbt = getNMSClass("NBTTagCompound");
        Class obc = Reflection.getOBCClass("inventory.CraftItemStack");
        Class cmn = Reflection.getOBCClass("util.CraftMagicNumbers");
        cmnObj = getFieldData(cmn, "INSTANCE");
        createStack = Reflection.getConstructor(nmsClass, nbt);
        convertStack = Reflection.getMethod(nmsClass, "convertStack");
        saveStack = Reflection.getMethod(nmsClass, "save", nbt);
        nmsCopy = Reflection.getMethod(obc, "asNMSCopy", ItemStack.class);
        bukkitStack = Reflection.getConstructor(obc, nmsClass);
        getType = Reflection.getMethod(cmn, "getMaterialFromInternalName", String.class);
        getItem = Reflection.getMethod(cmn, "getItem", Material.class);
        getID = Reflection.getMethod(nmsItem, "getId", nmsItem);
        try {
            for (Map.Entry<?, ?> e : ((Map<?, ?>) getFieldData(getNMSClass("RegistryMaterials"), "b", getFieldData(nmsItem, "REGISTRY"))).entrySet()) {
                itemNames.put((Integer) getID.invoke(null, e.getKey()), e.getValue().toString());
            }
        } catch (Throwable err) {
            SU.error(SU.cs, err, "SpigotLib", "gyurix");
        }

        itemName = Reflection.getField(nmsItem, "name");
    }

    public NBTCompound nbtData = new NBTCompound();

    public ItemStackWrapper() {
    }

    public ItemStackWrapper(NBTCompound nbt) {
        this.nbtData = nbt;
    }

    public ItemStackWrapper(ItemStack is) {
        loadFromBukkitStack(is);
    }

    public ItemStackWrapper(Object vanillaStack) {
        loadFromVanillaStack(vanillaStack);
    }

    public byte getCount() {
        return (byte) ((NBTPrimitive) nbtData.get("Count")).getData();
    }

    public void setCount(byte count) {
        nbtData.put("Count", tag(count));
    }

    public short getDamage() {
        try {
            return (Short) ((NBTPrimitive) nbtData.get("Damage")).getData();
        } catch (Throwable e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void setDamage(short damage) {
        nbtData.put("Damage", tag(damage));
    }

    public String getId() {
        if (nbtData.get("id") == null)
            return "minecraft:air";
        return (String) ((NBTPrimitive) nbtData.get("id")).getData();
    }

    public void setId(String newId) {
        nbtData.put("id", tag(newId));
    }

    public NBTCompound getMetaData() {
        return nbtData.getCompound("tag");
    }

    public int getNumericId() {
        return getType().getId();
    }

    public boolean hasMetaData() {
        return nbtData.containsKey("tag");
    }

    public Material getType() {
        try {
            return (Material) getType.invoke(cmnObj, getId());
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return Material.AIR;
        }
    }

    public boolean isUnbreakable() {
        return getMetaData().getBoolean("Unbreakable");
    }

    public void setUnbreakable(boolean unbreakable) {
        if (unbreakable) {
            getMetaData().put("Unbreakable", tag((byte) 1));
        } else {
            getMetaData().remove("Unbreakable");
        }
    }

    public void loadFromBukkitStack(ItemStack is) {
        try {
            if (is != null) {
                Object nms = nmsCopy.invoke(null, is);
                if (nms != null)
                    nbtData = (NBTCompound) tag(saveStack.invoke(nms, new NBTCompound().toNMS()));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void loadFromVanillaStack(Object is) {
        try {
            if (is != null)
                tag(saveStack.invoke(is, new NBTCompound().toNMS()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void removeMetaData() {
        nbtData.remove("tag");
    }

    public void setNumericId(int newId) {
        try {
            nbtData.put("id", tag(itemNames.get(newId)));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public ItemStack toBukkitStack() {
        try {
            Object nms = toNMS();
            return (ItemStack) bukkitStack.newInstance(nms);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Object toNMS() {
        try {
            Object nms = createStack.newInstance(nbtData.toNMS());
            convertStack.invoke(nms);
            return nms;
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

