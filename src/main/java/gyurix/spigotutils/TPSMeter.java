package gyurix.spigotutils;

import gyurix.animation.AnimationAPI;
import gyurix.configfile.ConfigSerialization;
import gyurix.spigotlib.Main;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by GyuriX on 2015.10.12..
 */
public class TPSMeter implements Runnable{
    @ConfigSerialization.ConfigOptions(serialize = false)
    public static int ticks=0;
    public static long checkTime=2000;
    public static long startupDelay=15000;
    public static int limit=15;
    @ConfigSerialization.ConfigOptions(serialize = false)
    public static double tps=20;
    public TPSMeter(){

    }
    public void start(){
        AnimationAPI.sch.scheduleAtFixedRate(this, startupDelay, checkTime, TimeUnit.MILLISECONDS);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.pl, new Runnable() {
            @Override
            public void run() {
                ticks++;
            }
        }, 0, 1);
    }
    @Override
    public void run() {
        tps=ticks*1000.0/checkTime;
        //Main.log.info("§eTPS: §b"+tps);
        if (tps<limit){
            StringBuilder out=new StringBuilder();
            out.append("\n§cTPS is bellow " + limit + "! Printing thread stacktraces:\n");
            for (Map.Entry<Thread,StackTraceElement[]> e:Thread.getAllStackTraces().entrySet()){
                Thread t=e.getKey();
                out.append("§e---------------------------------------------------------------\n")
                        .append("§e--> §bTHREAD §f").append(t.getName()).append("§b, priority: §f")
                        .append(t.getPriority()).append("§b, state: §f").append(t.getState()).append("\n")
                        .append("§e---------------------------------------------------------------\n§f");
                for (StackTraceElement se:e.getValue()){
                    out.append(se.toString()).append('\n');
                }
                out.append("§e---------------------------------------------------------------\n");
            }
            Main.log.severe(out.substring(0,out.length()-1));
        }
        ticks=0;
    }
}
