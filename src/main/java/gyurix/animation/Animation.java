package gyurix.animation;

import gyurix.configfile.ConfigData;
import gyurix.configfile.ConfigSerialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author GyuriX
 *
 * A representation of an animation
 */
public class Animation {
    /**
     *  The map of the effect names and their implementation
     */
    public HashMap<String,HashMap<String,CustomEffect>> effects=new HashMap<String, HashMap<String, CustomEffect>>();
    /**
     * The frame list of the Animation
     */
    public ArrayList<Frame> frames=new ArrayList<Frame>();
    @ConfigSerialization.ConfigOptions(serialize = false)
    /**
     * The identifical code of the animation
     */
    protected int id;
    /**
     * Time of the not specialized timed frames in milliseconds
     */
    public long frameTime=1000;

    /**
     * The serializer and deserializer of the animations to be able to store them in the config in a special form
     */
    public static class AnimationSerializer implements ConfigSerialization.Serializer{

        public Object fromData(ConfigData data, Class cl, Type... args) {
            Animation anim=new Animation();
            for (Map.Entry<ConfigData,ConfigData> e:data.mapData.entrySet()){
                String key=e.getKey().stringData;
                if (key.equals("frameTime")){
                    anim.frameTime=Long.valueOf(e.getValue().stringData);
                }
                else if (key.equals("frames")){
                    anim.frames= (ArrayList<Frame>) e.getValue().deserialize(ArrayList.class,Frame.class);
                }
                else if (key.endsWith("s")){
                    key=key.substring(0,key.length()-1);
                    if (AnimationAPI.effects.keySet().contains(key)){
                        anim.effects.put(key, (HashMap<String, CustomEffect>) e.getValue()
                                .deserialize(HashMap.class, String.class, AnimationAPI.effects.get(key)));
                    }
                }
            }
            AnimationAPI.addAnimation(anim);
            return anim;
        }

        public ConfigData toData(Object obj, Type... types) {
            Animation anim= (Animation) obj;
            ConfigData out=new ConfigData();
            out.mapData=new LinkedHashMap<ConfigData, ConfigData>();
            out.mapData.put(new ConfigData("frameTime"),new ConfigData(""+anim.frameTime));
            out.mapData.put(new ConfigData("frames"), ConfigData.serializeObject(anim.frames,Frame.class));
            for (Map.Entry<String,HashMap<String,CustomEffect>> e:anim.effects.entrySet()){
                Class cl=AnimationAPI.effects.get(e.getKey());
                if (cl==null){
                    System.err.println("Unregistered effect type "+e.getKey()+" can't be saved.");
                    continue;
                }
                out.mapData.put(new ConfigData(e.getKey()+"s"),ConfigData.serializeObject(e.getValue(),String.class,cl));
            }
            return out;
        }
    }
}
