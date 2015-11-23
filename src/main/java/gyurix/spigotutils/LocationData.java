package gyurix.spigotutils;

import gyurix.configfile.ConfigSerialization;
import gyurix.protocol.utils.BlockLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

public class LocationData
        implements ConfigSerialization.StringSerializable {
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public String world;

    public LocationData(String in) {
        String[] d = in.split(" ");
        switch (d.length) {
            case 3: {
                this.x = Double.valueOf(d[0]);
                this.y = Double.valueOf(d[1]);
                this.z = Double.valueOf(d[2]);
                return;
            }
            case 4: {
                this.world = d[0];
                this.x = Double.valueOf(d[1]);
                this.y = Double.valueOf(d[2]);
                this.z = Double.valueOf(d[3]);
                return;
            }
            case 5: {
                this.x = Double.valueOf(d[0]);
                this.y = Double.valueOf(d[1]);
                this.z = Double.valueOf(d[2]);
                this.yaw = Float.valueOf(d[3]).floatValue();
                this.pitch = Float.valueOf(d[4]).floatValue();
                return;
            }
            case 6: {
                this.world = d[0];
                this.x = Double.valueOf(d[1]);
                this.y = Double.valueOf(d[2]);
                this.z = Double.valueOf(d[3]);
                this.yaw = Float.valueOf(d[4]).floatValue();
                this.pitch = Float.valueOf(d[5]).floatValue();
                return;
            }
        }
    }

    public LocationData() {
    }

    public LocationData(LocationData ld) {
        this(ld.world, ld.x, ld.y, ld.z, ld.yaw, ld.pitch);
    }

    public LocationData(BlockLocation bl) {
        this(bl.x, bl.y, bl.z);
    }

    public LocationData(Vector v) {
        this(v.getX(), v.getY(), v.getZ());
    }

    public LocationData(double x, double y, double z) {
        this(null, x, y, z, 0.0f, 0.0f);
    }

    public LocationData(String world, double x, double y, double z) {
        this(world, x, y, z, 0.0f, 0.0f);
    }

    public LocationData(double x, double y, double z, float yaw, float pitch) {
        this(null, x, y, z, yaw, pitch);
    }

    public LocationData(Block bl) {
        this(bl.getWorld().getName(), bl.getX(), bl.getY(), bl.getZ(), 0.0f, 0.0f);
    }

    public LocationData(Location loc) {
        this(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public LocationData(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Block getBlock() {
        if (!this.isAvailable()) {
            return null;
        }
        return Bukkit.getWorld(this.world).getBlockAt((int) this.x, (int) this.y, (int) this.z);
    }

    public BlockLocation getBlockLocation() {
        return new BlockLocation((int) this.x, (int) this.y, (int) this.z);
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z, this.yaw, this.pitch);
    }

    public Vector toVector() {
        return new Vector(this.x, this.y, this.z);
    }

    public boolean isAvailable() {
        return this.world != null && Bukkit.getWorld(this.world) != null;
    }

    public LocationData add(LocationData ld) {
        return ld == null ? this : this.add(ld.x, ld.y, ld.z);
    }

    public LocationData subtract(LocationData ld) {
        return ld == null ? this : this.subtract(ld.x, ld.y, ld.z);
    }

    public LocationData multiple(LocationData ld) {
        return ld == null ? this : this.multiple(ld.x, ld.y, ld.z);
    }

    public LocationData add(double num) {
        this.x += num;
        this.y += num;
        this.z += num;
        return this;
    }

    public LocationData subtract(double num) {
        this.x -= num;
        this.y -= num;
        this.z -= num;
        return this;
    }

    public LocationData multiple(double num) {
        this.x *= num;
        this.y *= num;
        this.z *= num;
        return this;
    }

    public LocationData add(double nx, double ny, double nz) {
        this.x += nx;
        this.y += ny;
        this.z += nz;
        return this;
    }

    public LocationData subtract(double nx, double ny, double nz) {
        this.x -= nx;
        this.y -= ny;
        this.z -= nz;
        return this;
    }

    public LocationData multiple(double nx, double ny, double nz) {
        this.x *= nx;
        this.y *= ny;
        this.z *= nz;
        return this;
    }

    public Vector getDirection() {
        Vector vector = new Vector();
        vector.setY(-Math.sin(Math.toRadians(this.pitch)));
        double xz = Math.cos(Math.toRadians(this.pitch));
        vector.setX((-xz) * Math.sin(Math.toRadians(this.yaw)));
        vector.setZ(xz * Math.cos(Math.toRadians(this.yaw)));
        return vector;
    }

    public LocationData setDirection(Vector vector) {
        double x = vector.getX();
        double z = vector.getZ();
        if (x == 0.0 && z == 0.0) {
            this.pitch = vector.getY() > 0.0 ? -90 : 90;
            return this;
        }
        double theta = Math.atan2(-x, z);
        this.yaw = (float) Math.toDegrees((theta + 6.283185307179586) % 6.283185307179586);
        double x2 = NumberConversions.square(x);
        double z2 = NumberConversions.square(z);
        double xz = Math.sqrt(x2 + z2);
        this.pitch = (float) Math.toDegrees(Math.atan((-vector.getY()) / xz));
        return this;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        if (this.world != null) {
            out.append(' ').append(this.world);
        }
        out.append(' ').append(this.x).append(' ').append(this.y).append(' ').append(this.z);
        if (this.yaw != 0.0f || this.pitch != 0.0f) {
            out.append(' ').append(this.yaw).append(' ').append(this.pitch);
        }
        return out.substring(1);
    }

    public World getWorld() {
        return Bukkit.getWorld(this.world);
    }

    public LocationData clone() {
        return new LocationData(this);
    }

    public int hashCode() {
        int hash = 57 + (this.world != null ? this.world.hashCode() : 0);
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.y) ^ Double.doubleToLongBits(this.y) >>> 32);
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.z) ^ Double.doubleToLongBits(this.z) >>> 32);
        hash = 19 * hash + Float.floatToIntBits(this.pitch);
        hash = 19 * hash + Float.floatToIntBits(this.yaw);
        return hash;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof LocationData)) {
            return false;
        }
        LocationData ld = (LocationData) obj;
        return (this.world == null && ld.world == null || this.world != null && ld.world != null && this.world.equals(ld.world)) && this.x == ld.x && this.y == ld.y && this.z == ld.z && this.yaw == ld.yaw && this.pitch == ld.pitch;
    }
}

