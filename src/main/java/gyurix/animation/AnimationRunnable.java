package gyurix.animation;

import gyurix.api.VariableAPI;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by GyuriX on 2015.05.23..
 */
public class AnimationRunnable implements Runnable{
    public final Animation a;
    public String name;
    public Iterator<Frame> frames;
    public Iterator<Long> delays,repeats;
    public Frame f;
    public long repeat,delay;
    public Player plr;
    public Object obj;
    public ScheduledFuture running;
    protected AnimationRunnable(Animation anim,String name,Player player,Object object){
        a=anim;
        this.name = name;
        frames =a.frames.iterator();
        plr=player;
        obj=object;
    }
    public void run() {
        if (repeat==0){
            if (delays==null||!delays.hasNext()){
                if (!frames.hasNext())
                    frames =a.frames.iterator();
                f= frames.next();
                if (f.delays==null){
                    delay=a.frameTime;
                    repeat=1;
                    delays=null;
                }
                else{
                    delays=f.delays.iterator();
                    repeats=f.repeats.iterator();
                    delay=delays.next();
                    repeat=repeats.next();
                }
            }
            else{
                delay=delays.next();
                repeat=repeats.next();
            }
        }
        AnimationUpdateEvent e=new AnimationUpdateEvent(this, VariableAPI.fillVariables(f.text, plr, obj));
        SU.pm.callEvent(e);
        --repeat;
        running=AnimationAPI.sch.schedule(this, delay, TimeUnit.MILLISECONDS);
    }
}
