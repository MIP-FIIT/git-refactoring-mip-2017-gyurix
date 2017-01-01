package gyurix.spigotutils;

import gyurix.protocol.utils.Direction;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import static java.lang.Math.*;

/**
 * Created by GyuriX on 2016. 07. 13..
 */
public class BlockUtils {
    public static Vector getDirection(float yaw, float pitch) {
        double xz = cos(toRadians(pitch));
        return new Vector(-xz * sin(toRadians(yaw)), -sin(toRadians(pitch)), xz * cos(toRadians(yaw)));
    }

    public static float getPitch(Vector vec) {
        double x = vec.getX();
        double z = vec.getZ();
        if (x == 0 && z == 0)
            return vec.getY() == 0 ? 0 : vec.getY() > 0 ? 90 : -90;
        return (float) toDegrees(atan(-vec.getY() / sqrt(x * x + z * z)));
    }

    public static byte getSignDurability(float yaw) {
        yaw -= 168.75f;
        while (yaw < 0)
            yaw += 360;
        return (byte) (yaw / 22.5f);
    }

    public static Direction getSimpleDirection(float yaw, float pitch) {
        while (yaw < 45)
            yaw += 360;
        yaw -= 45;
        return pitch > 45 ? Direction.DOWN : pitch < -45 ? Direction.UP : Direction.values()[(int) (yaw / 90 + 2)];
    }

    public static Location getYMax(World world, int minx, int minz) {
        for (int y = 255; y > 0; y--) {
            Block b = world.getBlockAt(minx, y, minz);
            if (b.getType().isSolid())
                return b.getLocation();
        }
        return new Location(world, minx, 1, minz);
    }

    public static float getYaw(Vector vec) {
        return (float) ((toDegrees(atan2(-vec.getX(), vec.getZ())) + 720) % 360);
    }
}
