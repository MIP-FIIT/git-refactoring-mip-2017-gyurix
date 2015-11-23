package gyurix.animation.effects;

import gyurix.animation.CustomEffect;

import java.util.HashSet;
import java.util.Iterator;

public class ScrollerEffect
        implements CustomEffect {
    public int start;
    public int max = 80;
    public int size = 16;
    public int speed = 1;
    public String text;
    public boolean reversed;
    public boolean skipColors = true;
    public char fill = 32;

    public ScrollerEffect() {
    }

    public ScrollerEffect(int max, int size, String text) {
        this(max, size, 1, 0, false, true, ' ', text);
    }

    public ScrollerEffect(int max, int size, int speed, int start, boolean reversed, boolean skipColors, char fill, String text) {
        this.max = max;
        this.size = size;
        this.speed = speed;
        this.start = start;
        this.reversed = reversed;
        this.skipColors = skipColors;
        this.fill = fill;
        this.text = text;
    }

    @Override
    public String next(String text) {
        char c;
        int i;
        int i2;
        int id;
        StringBuilder sb = new StringBuilder(this.size);
        HashSet<Character> formats = new HashSet<Character>();
        int inc = this.speed;
        char colorPrefix = ' ';
        char[] chars = text.toCharArray();
        for (int i3 = 1; i3 < this.max; ++i3) {
            int id2 = (this.start + this.max - i3) % this.max;
            if (chars.length <= id2 || chars[id2] != '\u00a7' || chars.length <= (id2 = (id2 + 1) % this.max)) continue;
            c = chars[id2];
            if (c < 'k' || c > 'o') {
                colorPrefix = c;
                break;
            }
            formats.add(Character.valueOf(c));
        }
        int n = i2 = chars.length > (id = (this.start + this.max - 1) % this.max) && chars[id] == '\u00a7' ? 1 : 0;
        while (i2 < this.size && chars.length > (id = (this.start + i2) % this.max) && chars[id] == '\u00a7') {
            id = (this.start + i2 + 1) % this.max;
            if (chars.length > id) {
                c = chars[id];
                if (this.skipColors) {
                    inc += 2;
                }
                if (c < 'k' || c > 'o') {
                    formats.clear();
                    colorPrefix = c;
                } else {
                    formats.add(Character.valueOf(c));
                }
            }
            i2 += 2;
        }
        if (colorPrefix != ' ') {
            sb.append('\u00a7').append(colorPrefix);
        }
        Iterator i$ = formats.iterator();
        while (i$.hasNext()) {
            c = ((Character) i$.next()).charValue();
            sb.append('\u00a7').append(c);
        }
        id = (this.start + this.max - 1) % this.max;
        int n2 = i = chars.length > id && chars[id] == '\u00a7' ? 1 : 0;
        while (sb.length() < this.size) {
            id = (this.start + i) % this.max;
            sb.append(chars.length > id ? chars[id] : this.fill);
            ++i;
        }
        if (sb.charAt(this.size - 1) == '\u00a7') {
            sb.setCharAt(this.size - 1, this.fill);
        }
        if (this.reversed) {
            this.start -= inc;
            if (this.start < 0) {
                this.start = this.max - inc;
            }
        } else {
            this.start += inc;
            if (this.start >= this.max) {
                this.start = inc - 1;
            }
        }
        return sb.toString();
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public CustomEffect clone() {
        return new ScrollerEffect(this.max, this.size, this.speed, this.start, this.reversed, this.skipColors, this.fill, this.text);
    }
}

