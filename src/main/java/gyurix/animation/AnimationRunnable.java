package gyurix.animation;

import gyurix.animation.effects.FramesEffect;
import gyurix.api.VariableAPI;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AnimationRunnable
        implements Runnable {
    public final Animation a;
    public String name;
    public FramesEffect frames;
    public HashMap<String, HashMap<String, CustomEffect>> effects = new HashMap();
    public Player plr;
    public Object obj;
    public ScheduledFuture running;

    protected AnimationRunnable(Animation anim, String name, Player player, Object object) {
        this.a = anim;
        this.name = name;
        this.plr = player;
        this.obj = object;
        for (Map.Entry<String, HashMap<String, CustomEffect>> e : anim.effects.entrySet()) {
            HashMap<String, CustomEffect> map = new HashMap<String, CustomEffect>();
            this.effects.put(e.getKey(), map);
            for (Map.Entry<String, CustomEffect> e2 : e.getValue().entrySet()) {
                map.put(e2.getKey(), e2.getValue().clone());
            }
        }
        this.frames = (FramesEffect) this.effects.get("frame").get("main").clone();
        this.run();
    }

    @Override
    public void run() {
        AnimationUpdateEvent e = new AnimationUpdateEvent(this,
                SU.optimizeColorCodes(VariableAPI.fillVariables(this.frames.next(""), this.plr, this)));
        SU.pm.callEvent(e);
        this.running = AnimationAPI.sch.schedule(this, this.frames.delay, TimeUnit.MILLISECONDS);
    }
}

