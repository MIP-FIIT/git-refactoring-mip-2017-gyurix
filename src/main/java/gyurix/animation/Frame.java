package gyurix.animation;

import gyurix.configfile.ConfigSerialization;
import java.util.ArrayList;

/**
 * The implementation of a Frame in the animations
 */
public class Frame implements ConfigSerialization.StringSerializable{
    /**
     * Frame timing settings
     */
    public ArrayList<Long> delays,repeats;
    /**
     * Text of the frame
     */
    public String text;

    /**
     * Constructs a Frame object from it's string representation. Used for deserializing Frames.
     * @param in the string format of a Frame
     */
    public Frame(String in){
        if (in.startsWith("{")){
            delays=new ArrayList<Long>();
            repeats=new ArrayList<Long>();
            int id=in.indexOf("}");
            for (String s:in.substring(1,id).split("(,+| +) *")){
                String[] d2=s.split(":",2);
                if (d2.length==2){
                    delays.add(Long.valueOf(d2[1]));
                    repeats.add(Long.valueOf(d2[0]));
                }
                else{
                    delays.add(Long.valueOf(d2[0]));
                    repeats.add(1L);
                }
            }
            text=in.substring(id+1);
        }
        else{
            text=in;
        }
    }

    /**
     * Serializes the Frame to its string representation. Used for serializing Frames.
     * @return the string representation of the Frame
     */
    @Override
    public String toString() {
        if (delays==null||delays.isEmpty())
            return text;
        StringBuilder out=new StringBuilder();
        out.append('{');
        for (int i=0;i<delays.size();i++){
            long repeat=repeats.get(i);
            long delay=delays.get(i);
            if (repeat==1)
                out.append(delay);
            else
                out.append(repeat).append(':').append(delay);
            out.append(",");
        }
        out.setCharAt(out.length()-1,'}');
        out.append(text);
        return out.toString();
    }
}
