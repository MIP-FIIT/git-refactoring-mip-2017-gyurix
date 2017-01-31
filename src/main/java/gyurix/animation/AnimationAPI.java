package gyurix.animation;

import gyurix.animation.Animation.AnimationSerializer;
import gyurix.animation.effects.*;
import gyurix.api.VariableAPI;
import gyurix.api.VariableAPI.VariableHandler;
import gyurix.configfile.ConfigSerialization;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class AnimationAPI {
    protected static final ScheduledExecutorService pool = Executors.newScheduledThreadPool(Config.animationApiThreads);
    public static HashMap<String, Class> effects = new HashMap();
    private static HashMap<Plugin, HashMap<String, HashSet<AnimationRunnable>>> runningAnimations = new HashMap();

    public static void init() {
        effects.put("scroller", ScrollerEffect.class);
        effects.put("blink", BlinkEffect.class);
        effects.put("frame", FramesEffect.class);
        effects.put("flame", FlameEffect.class);
        effects.put("rainbow", RainbowEffect.class);
        for (String key : effects.keySet()) {
            VariableAPI.handlers.put(key, new CustomEffectHandler(key));
        }
        ConfigSerialization.serializers.put(Animation.class, new AnimationSerializer());
    }

    public static void stopRunningAnimations(Plugin pl) {
        HashMap<String, HashSet<AnimationRunnable>> map = runningAnimations.remove(pl);
        if (map == null || map.isEmpty())
            return;
        for (HashSet<AnimationRunnable> ars : map.values()) {
            for (AnimationRunnable ar : ars)
                ar.stop();
        }
    }

    public static void stopRunningAnimations(Plugin pl, Player plr) {
        HashMap<String, HashSet<AnimationRunnable>> map = runningAnimations.get(pl);
        if (map == null || map.isEmpty())
            return;
        HashSet<AnimationRunnable> ars = map.remove(plr.getName());
        if (ars != null)
            for (AnimationRunnable ar : ars)
                ar.stop();
    }

    public static void stopRunningAnimations(Player plr) {
        for (HashMap<String, HashSet<AnimationRunnable>> map : runningAnimations.values()) {
            HashSet<AnimationRunnable> ars = map.remove(plr.getName());
            if (ars != null)
                for (AnimationRunnable ar : ars)
                    if (ar.future != null)
                        ar.future.cancel(true);
        }
    }

    public static void stopRunningAnimation(AnimationRunnable ar) {
        HashMap<String, HashSet<AnimationRunnable>> map = runningAnimations.get(ar.pl);
        if (map == null || map.isEmpty())
            return;
        HashSet<AnimationRunnable> ars = map.get(ar.plr.getName());
        ars.remove(ar);
        ar.stop();
    }

    public static AnimationRunnable runAnimation(Plugin pl, Animation a, String name, Player plr, AnimationUpdateListener listener) {
        HashMap<String, HashSet<AnimationRunnable>> map = runningAnimations.get(pl);
        if (map == null)
            runningAnimations.put(pl, map = new HashMap<>());
        HashSet<AnimationRunnable> ars = map.get(plr.getName());
        if (ars == null)
            map.put(plr.getName(), ars = new HashSet<>());
        AnimationRunnable ar = new AnimationRunnable(pl, a, name, plr, listener);
        return ar;
    }

    public static class CustomEffectHandler implements VariableHandler {
        public final String name;

        public CustomEffectHandler(String name) {
            this.name = name;
        }

        @Override
        public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
            AnimationRunnable ar = (AnimationRunnable) oArgs[0];
            String[] d = StringUtils.join(inside, "").split(":", 2);
            CustomEffect effect = ar.effects.get(name).get(d[0]);
            if (effect != null) {
                String text = d.length <= 1 ? effect.getText() : d[1];
                return effect.next(VariableAPI.fillVariables(text, plr, oArgs));
            }
            SU.log(Main.pl, "The given " + name + " name (" + d[0] + ") is invalid " + name + " name in animation ");
            return "?";
        }
    }

}

