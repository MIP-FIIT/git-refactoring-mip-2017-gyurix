package gyurix.animation;

import gyurix.configfile.ConfigSerialization;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * The implementation of the Blink CustomEffect.
 */
public class Blink implements ConfigSerialization.StringSerializable,CustomEffect{
    private boolean active=true;
    private ArrayList<Long> repeat=new ArrayList<Long>();
    private long remaining;
    private Iterator<Long> data;
    private String text;

    public Blink(String in){
        if (in.startsWith("{")){
            for (String s:in.substring(1,in.indexOf("}")).split(" ")){
                if (s.equals("A"))
                    active=false;
                else
                    repeat.add(Long.valueOf(s));
            }
        }
        else{
            repeat.add(1L);
            text=in;
        }
        data=repeat.iterator();
        remaining=data.next();
    }

    public Blink() {

    }

    public String next(String in){
        --remaining;
        if (remaining==0){
            if (!data.hasNext())
                data=repeat.iterator();
            remaining=data.next();
            active=!active;
        }
        return active?in:in.replaceAll("."," ");
    }

    public String getText(String newText) {
        return newText.isEmpty()?text:newText;
    }

    public void setText(String newText) {
        text=newText;
    }

    @Override
    public String toString() {
        StringBuilder out=new StringBuilder();
        out.append("{");
        if (!active)
            out.append("A ");
        for (Long r:repeat){
            out.append(r).append(' ');
        }
        out.setCharAt(out.length()-1,'}');
        return out.append(text).toString();
    }

    @Override
    public CustomEffect clone() {
        return new Blink(toString());
    }
}
