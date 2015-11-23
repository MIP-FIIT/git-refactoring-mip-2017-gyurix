package gyurix.animation.effects;

import gyurix.animation.CustomEffect;
import gyurix.spigotlib.SU;

public class RainbowEffect
        implements CustomEffect {
    public int state = -1;
    public boolean random;

    public RainbowEffect() {
    }

    public RainbowEffect(int state, boolean random) {
        this.state = state;
        this.random = random;
    }

    @Override
    public String next(String in) {
        this.state = this.random ? SU.rand.nextInt(in.length()) : (this.state + 1) % in.length();
        return "\u00a7" + in.charAt(this.state % in.length());
    }

    @Override
    public String getText() {
        return "";
    }

    @Override
    public void setText(String newText) {
    }

    @Override
    public CustomEffect clone() {
        return new RainbowEffect(this.state, this.random);
    }
}

