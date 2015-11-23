package gyurix.spigotutils;

import gyurix.animation.AnimationAPI;
import gyurix.configfile.ConfigSerialization;
import gyurix.spigotlib.Main;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TPSMeter
        implements Runnable {
    @ConfigSerialization.ConfigOptions(serialize = false)
    public static int ticks = 0;
    public static long checkTime = 2000;
    public static long startupDelay = 15000;
    public static int limit = 15;
    @ConfigSerialization.ConfigOptions(serialize = false)
    public static double tps = 20.0;
    public static boolean noreport;

    public void start() {
        AnimationAPI.sch.scheduleAtFixedRate(this, startupDelay, checkTime, TimeUnit.MILLISECONDS);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.pl, new Runnable() {

            @Override
            public void run() {
                ++TPSMeter.ticks;
            }
        }, 0, 1);
    }

    @Override
    public void run() {
        tps = (double) ticks * 1000.0 / (double) checkTime;
        ticks = 0;
        if (tps < (double) limit) {
            if (noreport) {
                return;
            }
            StringBuilder out = new StringBuilder();
            out.append("\n------> TPS is bellow ").append(limit).append("! Printing thread stacktraces <------\n");
            for (Map.Entry<Thread, StackTraceElement[]> e : Thread.getAllStackTraces().entrySet()) {
                Thread t = e.getKey();
                out.append("---------------------------------------------------------------\n").append("--> \u00a7bTHREAD ").append(t.getName()).append(", priority: ").append(t.getPriority()).append("\u00a7b, state: \u00a7f").append(t.getState()).append("\n").append("---------------------------------------------------------------\n");
                for (StackTraceElement se : e.getValue()) {
                    out.append(se.toString()).append('\n');
                }
                out.append("---------------------------------------------------------------\n");
            }
            Main.log.severe(out.substring(0, out.length() - 1));
            noreport = true;
        } else {
            noreport = false;
        }
    }

}

