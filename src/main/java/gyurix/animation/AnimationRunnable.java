package gyurix.animation;

import gyurix.animation.effects.FramesEffect;
import gyurix.api.VariableAPI;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map.Entry;

import static gyurix.spigotlib.Main.pl;

public class AnimationRunnable implements Runnable {
    public final Animation a;
    public HashMap<String, HashMap<String, CustomEffect>> effects = new HashMap();
    public FramesEffect frames;
    public String name;
    public Object obj;
    public Player plr;
    public int running = -1;

    protected AnimationRunnable(Animation anim, String name, Player player, Object object) {
        a = anim;
        this.name = name;
        plr = player;
        obj = object;
        for (Entry<String, HashMap<String, CustomEffect>> e : anim.effects.entrySet()) {
            HashMap<String, CustomEffect> map = new HashMap<String, CustomEffect>();
            effects.put(e.getKey(), map);
            for (Entry<String, CustomEffect> e2 : e.getValue().entrySet()) {
                map.put(e2.getKey(), e2.getValue().clone());
            }
        }
        frames = (FramesEffect) effects.get("frame").get("main").clone();
        run();
    }

    @Override
    public void run() {
        AnimationUpdateEvent e = new AnimationUpdateEvent(this,
                SU.optimizeColorCodes(VariableAPI.fillVariables(frames.next(""), plr, this)));
        SU.pm.callEvent(e);
        long delay = frames.delay / 50;
        if (delay > Integer.MAX_VALUE)
            return;
        if (delay < 1)
            delay = 1;
        running = SU.sch.scheduleSyncDelayedTask(pl, this, delay);
    }
}

