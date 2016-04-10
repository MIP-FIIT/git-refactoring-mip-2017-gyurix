package gyurix.protocol.manager;

import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.Main;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by GyuriX on 2016.01.11..
 */
public class PacketCapture {
    public static final ScheduledExecutorService io = Executors.newSingleThreadScheduledExecutor();
    public FileWriter fw;
    public HashSet<String> noIn = new HashSet<>();
    public HashSet<String> noOut = new HashSet<>();
    public ScheduledFuture task;

    public PacketCapture(String fn) {
        File f = new File(Main.pl.getDataFolder().getAbsoluteFile().getParentFile().getParentFile() + File.separator + "packetcap");
        f.mkdirs();
        f = new File(f + File.separator + fn + ".yml");
        try {
            f.createNewFile();
            fw = new FileWriter(f, true);
            task = io.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        fw.flush();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }, 5, 5, TimeUnit.SECONDS);
        } catch (Throwable e) {
        }
    }

    public void capIn(Object packetIn) {
        String type = packetIn.getClass().getSimpleName();
        if (noIn.contains(type))
            return;
        long mil = System.currentTimeMillis();
        String time = "[" + StringUtils.leftPad("" + mil % 86400000 / 3600000, 2, '0') + ":" + StringUtils.leftPad("" + mil % 3600000 / 60000, 2, '0') + ":" + StringUtils.leftPad("" + mil % 60000 / 1000, 2, '0') + "." + StringUtils.leftPad("" + mil % 1000, 3, '0') + "]";
        try {
            WrappedPacket wp = (WrappedPacket) Class.forName("gyurix.protocol.v1_8.inpackets." + type).newInstance();
            wp.loadVanillaPacket(packetIn);
            fw.append(time).append(" INW ").append(toString(wp)).append("\n");
            ;
        } catch (Throwable e) {
            try {
                fw.append(time).append(" IN ").append(toString(packetIn)).append("\n");
                ;
            } catch (Throwable err) {
                err.printStackTrace();
            }
        }
    }

    public void capOut(Object packetOut) {
        String type = packetOut.getClass().getSimpleName();
        if (noOut.contains(type))
            return;
        long mil = System.currentTimeMillis();
        String time = "[" + StringUtils.leftPad("" + mil % 86400000 / 3600000, 2, '0') + ":" + StringUtils.leftPad("" + mil % 3600000 / 60000, 2, '0') + ":" + StringUtils.leftPad("" + mil % 60000 / 1000, 2, '0') + "." + StringUtils.leftPad("" + mil % 1000, 3, '0') + "]";
        try {
            WrappedPacket wp = (WrappedPacket) Class.forName("gyurix.protocol.v1_8.outpackets." + type).newInstance();
            wp.loadVanillaPacket(packetOut);
            fw.append(time).append(" OUTW ").append(toString(wp)).append("\n");
        } catch (Throwable e) {
            try {
                fw.append(time).append(" OUT ").append(toString(packetOut)).append("\n");
            } catch (Throwable err) {
                err.printStackTrace();
            }
        }
    }

    public void stop() {
        try {
            fw.flush();
            fw.close();
            task.cancel(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toString(Object o) throws Throwable {
        StringBuilder out = new StringBuilder();
        if (o == null || o.getClass().isPrimitive() || o.getClass() == String.class || o.getClass() == UUID.class) {
            return String.valueOf(o);
        }
        for (Field f : o.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            out.append(", ").append(f.getName()).append("=");
            if (Iterable.class.isAssignableFrom(f.getType())) {
                out.append("[");
                for (Object obj : (Iterable) f.get(o)) {
                    out.append("{").append(toString(obj)).append("}, ");
                }
                if (out.charAt(out.length() - 1) == ' ')
                    out.setLength(out.length() - 2);
                out.append("]");
            } else if (f.getType().isArray()) {
                out.append("[");
                for (Object obj : (Iterable) f.get(o)) {
                    out.append("{").append(toString(obj)).append("}, ");
                }
                if (out.charAt(out.length() - 1) == ' ')
                    out.setLength(out.length() - 2);
                out.append("]");
            } else {
                out.append(f.get(o));
            }
        }
        return o.getClass().getSimpleName() + " -> " + (out.length() == 0 ? "" : out.substring(2));
    }
}