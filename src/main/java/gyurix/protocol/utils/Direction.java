package gyurix.protocol.utils;

import gyurix.protocol.Reflection;

import java.lang.reflect.Method;

public enum Direction {
    DOWN, UP, NORTH, SOUTH, WEST, EAST;
    private static final Method valueOf;
    static {
        valueOf = Reflection.getMethod(Reflection.getNMSClass("EnumDirection"), "valueOf", String.class);
    }
    Direction() {
    }
    public static Direction get(int id) {
        if (id >= 0 && id < 6) {
            return Direction.values()[id];
        }
        return null;
    }

    public Object toVanillaDirection() {
        try {
            return valueOf.invoke(null, this.name());
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}

