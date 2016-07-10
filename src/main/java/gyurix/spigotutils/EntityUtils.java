package gyurix.spigotutils;

import gyurix.nbt.NBTApi;
import gyurix.nbt.NBTCompound;
import gyurix.nbt.NBTPrimitive;
import gyurix.spigotlib.SU;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static gyurix.protocol.Reflection.*;

/**
 * Created by GyuriX on 2016. 06. 09..
 */
public class EntityUtils {
    private static final Method bukkitEntity = getMethod(nmsEntity, "getBukkitEntity");
    private static final Class craftEntity = getOBCClass("entity.CraftEntity"), nmsEntity = getNMSClass("Entity"), craftWorld = getOBCClass("CraftWorld"), nmsWorld = getNMSClass("World");
    private static final Field killerField = getField(getNMSClass("EntityLiving"), "killer"), nmsEntityGet = getField(craftEntity, "entity"),
            nmsWorldGet = getField(craftWorld, "world"), craftWorldGet = getField(nmsWorld, "world");

    /**
     * Converts the given NMS entity to a Bukkit entity
     *
     * @param ent - The NMS entity
     * @return The Bukkit entity
     */
    public static Entity getBukkitEntity(Object ent) {
        try {
            return (Entity) bukkitEntity.invoke(ent);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }

    /**
     * Converts the given NMS World or WorldServer to Bukkit World
     *
     * @param world - The Bukkit world
     * @return The NMS entity
     */
    public static World getBukkitWorld (Object world) {
        try {
            return (World) craftWorldGet.get(world);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }

    /**
     * Converts the given Bukkit world to an NMS WorldServer
     *
     * @param world - The Bukkit world
     * @return The NMS entity
     */
    public static Object getNMSWorld (World world) {
        try {
            return nmsWorldGet.get(world);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }

    /**
     * Checks if the given entity is in NoAI mode
     *
     * @param ent - Target entity
     * @return The NoAI mode of the given entity
     */
    public static boolean hasNoAI(LivingEntity ent) {
        if (ent == null)
            return false;
        NBTCompound nbt = NBTApi.getNbtData(ent);
        NBTPrimitive noAI = (NBTPrimitive) nbt.map.get("NoAI");
        return noAI != null && (int) noAI.data == 1;
    }

    /**
     * Sets the killer of the given entity
     *
     * @param ent    - The entity
     * @param killer - The new killer of the entity
     */
    public static void setKiller(LivingEntity ent, Player killer) {
        Object nmsEnt = getNMSEntity(ent);
        try {
            killerField.set(nmsEnt, killer == null ? null : getNMSEntity(killer));
        } catch (IllegalAccessException e) {

        }
    }

    /**
     * Converts the given Bukkit entity to an NMS entity
     *
     * @param ent - The Bukkit entity
     * @return The NMS entity
     */
    public static Object getNMSEntity (Entity ent) {
        try {
            return nmsEntityGet.get(ent);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }

    /**
     * Sets the NoAI mode of the given entity
     *
     * @param ent  - The entity
     * @param noAi - The new NoAI mode
     */
    public static void setNoAI(LivingEntity ent, boolean noAi) {
        if (ent == null)
            return;
        NBTCompound nbt = NBTApi.getNbtData(ent);
        nbt.set("NoAI", noAi ? 1 : 0);
        NBTApi.setNbtData(ent, nbt);
    }

}
