package gyurix.protocol.utils;

import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;

import java.lang.reflect.Method;

/**
 * Created by GyuriX on 2016.04.06..
 */
public enum InventoryClickType implements WrappedData {
    PICKUP,
    QUICK_MOVE,
    SWAP,
    CLONE,
    THROW,
    QUICK_CRAFT,
    PICKUP_ALL;
    Method valueOf = Reflection.getMethod(Reflection.getNMSClass("InventoryClickType"), "valueOf", String.class);

    @Override
    public Object toNMS() {
        try {
            return valueOf.invoke(null, name());
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }
}
