package gyurix.animation;

import gyurix.configfile.ConfigSerialization;

import java.util.ArrayList;

public class Frame implements ConfigSerialization.StringSerializable {
    public ArrayList<Long> delays;
    public ArrayList<Long> repeats;
    public String text;

    public Frame(String in) {
        if (in.startsWith("{")) {
            this.delays = new ArrayList();
            this.repeats = new ArrayList();
            int id = in.indexOf("}");
            for (String s : in.substring(1, id).split("(,+| +) *")) {
                String[] d2 = s.split(":", 2);
                if (d2.length == 2) {
                    this.delays.add(Long.valueOf(d2[1]));
                    this.repeats.add(Long.valueOf(d2[0]));
                    continue;
                }
                this.delays.add(Long.valueOf(d2[0]));
                this.repeats.add(1L);
            }
            this.text = in.substring(id + 1);
        } else {
            this.text = in;
        }
    }

    @Override
    public String toString() {
        if (this.delays == null || this.delays.isEmpty()) {
            return this.text;
        }
        StringBuilder out = new StringBuilder();
        out.append('{');
        for (int i = 0; i < this.delays.size(); ++i) {
            long repeat = this.repeats.get(i);
            long delay = this.delays.get(i);
            if (repeat == 1) {
                out.append(delay);
            } else {
                out.append(repeat).append(':').append(delay);
            }
            out.append(",");
        }
        out.setCharAt(out.length() - 1, '}');
        out.append(this.text);
        return out.toString();
    }
}

