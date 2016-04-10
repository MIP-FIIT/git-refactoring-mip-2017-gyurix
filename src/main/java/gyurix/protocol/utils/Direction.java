package gyurix.protocol.utils;

import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;

import java.lang.reflect.Method;

public enum Direction implements WrappedData {
    DOWN, UP, NORTH, SOUTH, WEST, EAST;
    private static final Method valueOf = Reflection.getMethod(Reflection.getNMSClass("EnumDirection"), "valueOf", String.class);
    Direction() {
    }

    public static Direction get(int id) {
        if (id >= 0 && id < 6) {
            return Direction.values()[id];
        }
        return null;
    }

    @Override
    public Object toNMS() {
        try {
            return valueOf.invoke(null, this.name());
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return null;
        }
    }
}

