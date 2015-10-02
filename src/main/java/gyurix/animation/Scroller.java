package gyurix.animation;

import java.util.HashSet;

/**
 * The implementation of the Scroller CustomEffect.
 */
public class Scroller implements CustomEffect {
    /**
     * Current starting character index of the Scroller
     */
    public int start,
    /**
     * Maximal length of the Scrolled text
     */
    max = 80,
    /**
     * Length of the output string
     */
    size = 16,
    /**
     * Scrolling speed
     */
    speed = 1;
    /**
     * Scrolled text
     */
    public String text;
    /**
     * If this field is true, then the scrolling will work to the opposite direction (not left, but right)
     */
    public boolean reversed;
    /**
     * Do not handle color codes specially
     */
    public boolean skipColors = true;
    /**
     * Character for filling the empty parts of the scrolled text
     */
    public char fill = ' ';

    /**
     * Empty constructor
     */
    public Scroller() {

    }

    /**
     * Constructs a Scroller object from the given data, with the following settings:
     * speed=1, not reversed, skip colors, fill character=' '
     *
     * @param max
     * @param size
     * @param text
     */
    public Scroller(int max, int size, String text) {
        this(max, size, 1, false, true, ' ', text);
    }

    /**
     * Constructs a Scroller object from the given data
     *
     * @param max
     * @param size
     * @param speed
     * @param reversed
     * @param skipColors
     * @param fill
     * @param text
     */
    public Scroller(int max, int size, int speed, boolean reversed, boolean skipColors, char fill, String text) {
        this.max = max;
        this.size = size;
        this.speed = speed;
        this.reversed = reversed;
        this.skipColors = skipColors;
        this.fill = fill;
        this.text = text;
    }

    public String next(String text) {
        StringBuilder sb = new StringBuilder(size);
        HashSet<Character> formats = new HashSet<Character>();
        int inc = speed;
        char colorPrefix = ' ';
        char[] chars = text.toCharArray();
        for (int i = 1; i < max; i++) {
            int id = (start + max - i) % max;
            if (chars.length > id && chars[id] == '§') {
                id = (id + 1) % max;
                if (chars.length > id) {
                    char c = chars[id];
                    if (c < 'k' || c > 'o') {
                        colorPrefix = c;
                        break;
                    } else {
                        formats.add(c);
                    }
                }
            }
        }

        int id = (start + max - 1) % max;
        for (int i = (chars.length > id && chars[id] == '§' ? 1 : 0); i < size; i += 2) {
            id = (start + i) % max;
            if (chars.length > id && chars[id] == '§') {
                id = (start + i + 1) % max;
                if (chars.length > id) {
                    char c = chars[id];
                    if (skipColors)
                        inc += 2;
                    if (c < 'k' || c > 'o') {
                        formats.clear();
                        colorPrefix = c;
                    } else {
                        formats.add(c);
                    }
                }
            } else
                break;
        }
        if (colorPrefix != ' ')
            sb.append('§').append(colorPrefix);
        for (char c : formats)
            sb.append('§').append(c);
        id = (start + max - 1) % max;
        for (int i = (chars.length > id && chars[id] == '§' ? 1 : 0); sb.length() < size; i++) {
            id = (start + i) % max;
            sb.append(chars.length > id ? chars[id] : fill);
        }
        if (sb.charAt(size - 1) == '§')
            sb.setCharAt(size - 1, fill);
        if (reversed) {
            start -= inc;
            if (start < 0)
                start = max - inc;
        } else {
            start += inc;
            if (start >= max)
                start = inc - 1;
        }
        return sb.toString();
    }

    public String getText(String in) {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
