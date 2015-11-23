package gyurix.animation;

import gyurix.animation.effects.*;
import gyurix.api.VariableAPI;
import gyurix.configfile.ConfigSerialization;
import gyurix.spigotlib.Main;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AnimationAPI {
    public static ScheduledExecutorService sch = Executors.newScheduledThreadPool(1);
    public static HashMap<String, Class> effects = new HashMap();
    private static HashMap<Integer, AnimationRunnable> animationsRunnables = new HashMap();
    private static HashMap<Animation, ArrayList<AnimationRunnable>> runningAnimations = new HashMap();

    public static void init() {
        effects.put("scroller", ScrollerEffect.class);
        effects.put("blink", BlinkEffect.class);
        effects.put("frame", FramesEffect.class);
        effects.put("flame", FlameEffect.class);
        effects.put("rainbow", RainbowEffect.class);
        for (String key : effects.keySet()) {
            VariableAPI.handlers.put(key, new CustomEffectHandler(key));
        }
        ConfigSerialization.serializers.put(Animation.class, new Animation.AnimationSerializer());
    }

    public static void addAnimation(Animation animation) {
        runningAnimations.put(animation, new ArrayList());
    }

    public static AnimationRunnable runAnimation(Animation a, String name, Player plr, Object obj) {
        ArrayList<AnimationRunnable> list = runningAnimations.get(a);
        if (list == null) {
            throw new RuntimeException("The given animation is not registered!");
        }
        AnimationRunnable ar = new AnimationRunnable(a, name, plr, obj);
        list.add(ar);
        return ar;
    }

    public static void removeAnimationRunnable(AnimationRunnable ar) {
        if (ar == null) {
            return;
        }
        if (ar.running != null) {
            ar.running.cancel(false);
        }
        runningAnimations.get(ar.a).remove(ar);
    }

    public static boolean removeAnimation(Animation a) {
        if (a == null) {
            return false;
        }
        for (AnimationRunnable ar : runningAnimations.remove(a)) {
            if (ar.running == null) continue;
            ar.running.cancel(true);
        }
        return true;
    }

    public static class CustomEffectHandler
            implements VariableAPI.VariableHandler {
        public final String name;

        public CustomEffectHandler(String name) {
            this.name = name;
        }

        @Override
        public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
            AnimationRunnable ar = (AnimationRunnable) oArgs[0];
            String[] d = StringUtils.join(inside, "").split(":", 2);
            CustomEffect effect = ar.effects.get(this.name).get(d[0]);
            if (effect != null) {
                String text = d.length <= 1 ? effect.getText() : d[1];
                return effect.next(VariableAPI.fillVariables(text, plr, oArgs));
            }
            Main.log.severe("The given " + this.name + " name (" + d[0] + ") is invalid " + this.name + " name in animation ");
            return "?";
        }
    }

}

