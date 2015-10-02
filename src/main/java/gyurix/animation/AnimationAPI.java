package gyurix.animation;

import gyurix.api.VariableAPI;
import gyurix.configfile.ConfigSerialization;
import gyurix.spigotlib.Main;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author GyuriX
 *
 * An API for managing both simple and complex text based animations. It currently has
 * two built in effect types, the Scroller and the Blink, but you can also make your
 * custom effect types using this API. The animations are executed Async, and they can
 * contain variables, which are filled using the VariableAPI.
 */
public class AnimationAPI {
    /**
     * The instance of the animation thread pool.
     */
    public static ScheduledExecutorService sch= Executors.newScheduledThreadPool(1);
    private static HashMap<Integer,Animation> animations=new HashMap<Integer, Animation>();
    private static HashMap<Animation,ArrayList<AnimationRunnable>> runningAnimations=new HashMap<Animation, ArrayList<AnimationRunnable>>();
    /**
     * Map of the custom effects. You should put your custom effect types to this map, using method
     * effects.put(String customEffectName,Class classImplementingTheCustomEffectsInterface);
     */
    public static HashMap<String,Class> effects=new HashMap<String, Class>();
    private static int id=0;

    /**
     * Initializer of the AnimationAPI. You should NOT use this method.
     */
    public static void init(){
        effects.put("scroller",Scroller.class);
        effects.put("blink",Blink.class);
        for (String key:effects.keySet()){
            VariableAPI.handlers.put(key, new CustomEffectHandler(key));
        }
        ConfigSerialization.serializers.put(Animation.class,new Animation.AnimationSerializer());
    }

    /**
     * Add Animation to the AnimationAPI. You don't need to use this method, if your animation was
     * loaded from the config.
     *
     * @param animation Animation to be added.
     */
    public static void addAnimation(Animation animation){
        animations.put(++id,animation);
        for (Frame f:animation.frames){
            for (String effect:animation.effects.keySet()){
                f.text=f.text.replace("<"+effect,"<"+effect+":"+id);
            }
        }
        for (String effectName:animation.effects.keySet()){
            for (HashMap<String,CustomEffect> list:animation.effects.values()){
                for (CustomEffect effect:list.values()){
                    effect.setText(effect.getText("").replace("<"+effectName,"<"+effectName+":"+id));
                }
            }
        }
        runningAnimations.put(animation, new ArrayList<AnimationRunnable>());
    }

    /**
     *
     * Use this method to start an Animation
     *
     * @param a animation, which will be started
     * @param name name of the AnimationRunnable for easy identify by the AnimationUpdateEvent listeners
     * @param plr optional additional player object, for better identify by the AnimationUpdateEvent listeners
     * @param obj optional additional object, for better identify by the AnimationUpdateEvent listeners
     * @return The created and started AnimationRunnable
     */
    public static AnimationRunnable runAnimation(Animation a,String name,Player plr,Object obj){
        ArrayList<AnimationRunnable> list=runningAnimations.get(a);
        if (list==null)
            throw new RuntimeException("The given gyurix.animation is not registred!");
        AnimationRunnable ar=new AnimationRunnable(a,name,plr,obj);
        ar.run();
        list.add(ar);
        return ar;
    }

    /**
     * Stops and removes an AnimationRunnable.
     *
     * @param ar removeable and stopable AnimationRunnable
     */
    public static void removeAnimationRunnable(AnimationRunnable ar){
        if (ar==null)
            return;
        if (ar.running!=null)
            ar.running.cancel(false);
        runningAnimations.get(ar.a).remove(ar);
    }

    /**
     * Removes an Animation and stops+removes its every AnimationRunnable.
     *
     * @param a removeable Animation
     * @return The success of the remove process, it's false, if the Animation is null or not registered.
     */
    public static boolean removeAnimation(Animation a){
        if (a==null)
            return false;
        Animation anim=animations.remove(a.id);
        if (anim==null)
            return false;
        for (AnimationRunnable ar:runningAnimations.get(anim)){
            if (ar.running!=null)
                ar.running.cancel(true);
        }
        runningAnimations.remove(anim);
        return true;
    }

    /**
     * A special VariableHandler class for handling custom effect based variables
     */
    public static class CustomEffectHandler implements VariableAPI.VariableHandler {
        public final String name;

        public CustomEffectHandler(String name) {
            this.name = name;
        }

        public String getValue(Object obj, Player plr, String data) {
            String[] d=data.split(":",3);
            String text=d.length<=2?"":d[2];
            CustomEffect effect=animations.get(Integer.valueOf(d[0])).effects.get(name).get(d[1]);
            if (effect!=null){
                return effect.next(VariableAPI.fillVariables(effect.getText(text),plr,obj));
            }
            else
                Main.log.severe("Invalid "+name+" name in gyurix.animation "+d[0]);
            return "?";
        }
    }
}
