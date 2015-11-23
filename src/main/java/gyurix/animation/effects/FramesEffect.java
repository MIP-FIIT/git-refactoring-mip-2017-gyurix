package gyurix.animation.effects;

import gyurix.animation.CustomEffect;
import gyurix.animation.Frame;
import gyurix.configfile.ConfigSerialization;
import gyurix.spigotlib.SU;

import java.util.ArrayList;
import java.util.Iterator;

public class FramesEffect
        implements CustomEffect {
    public ArrayList<Frame> frames = new ArrayList();
    public boolean random;
    @ConfigSerialization.ConfigOptions(defaultValue = "9223372036854775807")
    public long frameTime = Long.MAX_VALUE;
    @ConfigSerialization.ConfigOptions(serialize = false)
    public int state = -1;
    @ConfigSerialization.ConfigOptions(serialize = false)
    public Iterator<Long> delays;
    @ConfigSerialization.ConfigOptions(serialize = false)
    public Iterator<Long> repeats;
    @ConfigSerialization.ConfigOptions(serialize = false)
    public long repeat;
    @ConfigSerialization.ConfigOptions(serialize = false)
    public long delay;
    @ConfigSerialization.ConfigOptions(serialize = false)
    public Frame f;

    @Override
    public String next(String in) {
        if (this.repeat == 0) {
            if (this.delays == null || !this.delays.hasNext()) {
                this.state = this.random ? SU.rand.nextInt(this.frames.size()) : (this.state + 1) % this.frames.size();
                this.f = this.frames.get(this.state);
                if (this.f.delays == null) {
                    this.delay = this.frameTime;
                    this.repeat = 1;
                    this.delays = null;
                } else {
                    this.delays = this.f.delays.iterator();
                    this.repeats = this.f.repeats.iterator();
                    this.delay = this.delays.next();
                    this.repeat = this.repeats.next();
                }
            } else {
                this.delay = this.delays.next();
                this.repeat = this.repeats.next();
            }
        }
        --this.repeat;
        return this.f.text;
    }

    @Override
    public String getText() {
        return this.f != null ? this.f.text : "";
    }

    @Override
    public void setText(String newText) {
    }

    @Override
    public CustomEffect clone() {
        FramesEffect fe = new FramesEffect();
        fe.frameTime = this.frameTime;
        fe.state = this.state;
        fe.frames = this.frames;
        fe.random = this.random;
        fe.f = this.f;
        fe.delay = this.delay;
        fe.delays = this.delays;
        fe.repeats = this.repeats;
        fe.repeat = this.repeat;
        return fe;
    }
}

