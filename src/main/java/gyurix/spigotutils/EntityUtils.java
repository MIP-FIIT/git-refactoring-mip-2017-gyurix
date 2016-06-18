package gyurix.spigotutils;

import gyurix.nbt.NBTApi;
import gyurix.nbt.NBTCompound;
import gyurix.nbt.NBTPrimitive;
import gyurix.protocol.Reflection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by GyuriX on 2016. 06. 09..
 */
public class EntityUtils {
    private static final Class craftEntity = Reflection.getOBCClass("entity.CraftEntity");
    private static final Field killerField = Reflection.getField(Reflection.getNMSClass("EntityLiving"), "killer");
    private static final Class nmsEntity = Reflection.getNMSClass("Entity");
    private static final Method bukkitEntity = Reflection.getMethod(nmsEntity, "getBukkitEntity");
    private static final Field nmsEntityGet = Reflection.getField(craftEntity, "entity");

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

        }
        return null;
    }

    /**
     * Converts the given Bukkit entity to an NMS entity
     *
     * @param ent - The Bukkit entity
     * @return The NMS entity
     */
    public static Object getNMSEntity(Entity ent) {
        try {
            return nmsEntityGet.get(ent);
        } catch (IllegalAccessException e) {
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
