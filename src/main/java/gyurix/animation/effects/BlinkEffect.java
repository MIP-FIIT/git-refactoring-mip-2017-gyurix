package gyurix.animation.effects;

import gyurix.animation.CustomEffect;
import gyurix.configfile.ConfigSerialization;

import java.util.ArrayList;
import java.util.Iterator;

public class BlinkEffect
        implements ConfigSerialization.StringSerializable,
        CustomEffect {
    private boolean active = true;
    private Iterator<Long> data;
    private long remaining;
    private ArrayList<Long> repeat = new ArrayList();
    private String text;

    public BlinkEffect(String in) {
        if (in.startsWith("{")) {
            for (String s : in.substring(1, in.indexOf("}")).split(" ")) {
                if (s.equals("A")) {
                    this.active = false;
                    continue;
                }
                this.repeat.add(Long.valueOf(s));
            }
        } else {
            this.repeat.add(1L);
            this.text = in;
        }
        this.data = this.repeat.iterator();
        this.remaining = this.data.next();
    }

    public BlinkEffect() {
    }

    @Override
    public CustomEffect clone() {
        BlinkEffect be = new BlinkEffect();
        be.active = this.active;
        be.data = this.data;
        be.remaining = this.remaining;
        be.repeat = this.repeat;
        be.text = this.text;
        return be;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public void setText(String newText) {
        this.text = newText;
    }

    @Override
    public String next(String in) {
        --this.remaining;
        if (this.remaining == 0) {
            if (!this.data.hasNext()) {
                this.data = this.repeat.iterator();
            }
            this.remaining = this.data.next();
            this.active = !this.active;
        }
        return this.active ? in : in.replaceAll(".", " ");
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("{");
        if (!this.active) {
            out.append("A ");
        }
        for (Long r : this.repeat) {
            out.append(r).append(' ');
        }
        out.setCharAt(out.length() - 1, '}');
        return out.append(this.text).toString();
    }
}

