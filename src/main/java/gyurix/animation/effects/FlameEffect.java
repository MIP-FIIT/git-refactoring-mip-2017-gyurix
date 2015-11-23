package gyurix.animation.effects;

import gyurix.animation.CustomEffect;
import gyurix.configfile.ConfigSerialization;

import java.util.ArrayList;
import java.util.HashMap;

public class FlameEffect
        implements CustomEffect {
    public FlameInfo info;
    public int start;
    public int speed = 1;
    public HashMap<Integer, ArrayList<FlameEvent>> events = new HashMap();

    @Override
    public String next(String in) {
        StringBuilder out = new StringBuilder();
        int strstate = this.start;
        int state = 0;
        if (strstate >= in.length()) {
            this.step();
            return in;
        }
        if (strstate > 0) {
            out.append(in.substring(0, strstate));
        }
        while (strstate < in.length() && state < this.info.counts.length) {
            int count = this.info.counts[state];
            if (strstate + count > in.length()) {
                count = in.length() - strstate;
            }
            if (strstate > -1) {
                out.append(this.info.pref[state]);
                out.append(in.substring(strstate, strstate + count));
            }
            strstate += count;
            ++state;
        }
        if (strstate < in.length()) {
            out.append(in.substring(strstate));
        }
        this.step();
        return out.toString();
    }

    private void step() {
        this.start += this.speed;
        ArrayList<FlameEvent> list = this.events.get(this.start);
        if (list != null) {
            for (FlameEvent e : list) {
                e.execute(this);
            }
        }
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
        FlameEffect fe = new FlameEffect();
        fe.info = this.info;
        fe.speed = this.speed;
        fe.start = this.start;
        fe.events = this.events;
        return fe;
    }

    public static class FlameEvent
            implements ConfigSerialization.StringSerializable {
        public FlameEventType type;
        public int value;

        public FlameEvent() {
        }

        public FlameEvent(FlameEventType type, int value) {
            this.type = type;
            this.value = value;
        }

        public FlameEvent(String in) {
            String[] d = in.split(" ", 2);
            this.type = FlameEventType.valueOf(d[0].toUpperCase());
            this.value = Integer.valueOf(d[1]);
        }

        public void execute(FlameEffect fe) {
            if (this.type == FlameEventType.SPEED) {
                fe.speed = this.value;
            } else {
                fe.start = this.value;
            }
        }

        public enum FlameEventType {
            SPEED,
            START;


            FlameEventType() {
            }
        }

    }

    public static class FlameInfo
            implements ConfigSerialization.StringSerializable {
        public int[] counts;
        public String[] pref;

        public FlameInfo() {
        }

        public FlameInfo(String in) {
            String[] d = in.split(" ");
            this.counts = new int[d.length];
            this.pref = new String[d.length];
            for (int i = 0; i < d.length; ++i) {
                String[] d2 = d[i].split(":", 2);
                this.counts[i] = 1;
                try {
                    this.pref[i] = d2[0];
                    this.counts[i] = Integer.valueOf(d2[1]);
                    continue;
                } catch (Throwable var5_5) {
                    // empty catch block
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < this.counts.length; ++i) {
                out.append(' ').append(this.pref[i]).append(':').append(this.counts[i]);
            }
            return out.substring(1);
        }
    }

}

