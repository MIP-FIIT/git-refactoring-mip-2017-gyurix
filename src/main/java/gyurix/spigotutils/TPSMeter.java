package gyurix.spigotutils;

import gyurix.animation.AnimationAPI;
import gyurix.configfile.ConfigSerialization.ConfigOptions;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TPSMeter implements Runnable {
    /**
     * Update the servers tps metrics result in every here configured milliseconds
     */
    public static long checkTime = 10000;
    /**
     * The tps limit, if the servers tps is bellow this value, then you will see a warning in the console
     */
    public static int limit = 15;
    /**
     * The instance of the TPS meter future, it is stopped on SpigotLib shutdown
     */
    @ConfigOptions(serialize = false)
    public static ScheduledFuture meter;
    /**
     * Ticks elapsed from the last tps metrics result update
     */
    @ConfigOptions(serialize = false)
    public static int ticks = 0;
    /**
     * The current tps value of the server
     */
    @ConfigOptions(serialize = false)
    public static double tps = 20.0;

    @Override
    public void run() {
        tps = (double) ticks * 1000.0 / (double) checkTime;
        ticks = 0;
        if (tps < (double) limit)
            SU.cs.sendMessage("§9[§b TPS Meter §9]§e The servers TPS is bellow §c" + tps + "§e, is it lagging or crashed?");
    }

    public void start() {
        meter = AnimationAPI.sch.scheduleAtFixedRate(this, checkTime, checkTime, TimeUnit.MILLISECONDS);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.pl, new Runnable() {
            @Override
            public void run() {
                ++ticks;
            }
        }, 0, 1);
    }

}

