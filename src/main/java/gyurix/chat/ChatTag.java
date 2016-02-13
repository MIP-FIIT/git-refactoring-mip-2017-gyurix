package gyurix.chat;

import com.google.common.collect.Lists;
import gyurix.json.JsonAPI;
import gyurix.json.JsonSettings;
import gyurix.spigotlib.ChatAPI;
import gyurix.spigotlib.SU;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.ArrayList;

public class ChatTag {
    public ChatTag parent;
    public String text, translate, selector, insertion;
    public ArrayList<ChatTag> with;
    public ChatScoreData score;
    public ArrayList<ChatTag> extra;
    @JsonSettings(defaultValue = "false")
    public boolean bold, italic, underlined, strikethrough, obfuscated;
    public ChatColor color;
    public ChatClickEvent clickEvent;
    public ChatHoverEvent hoverEvent;

    public ChatTag() {

    }

    public ChatTag(String in) {
        text = in;
    }

    public static ChatTag fromExtraText(String extraText) {
        String[] parts = extraText.split("\\\\\\|");
        ArrayList<ChatTag> tags = new ArrayList<>();
        for (String part : parts) {
            String[] sa = part.split("\\\\-");
            ChatTag tag = ChatTag.fromColoredText(sa[0]);
            for (int i = 1; i < sa.length; i++) {
                if (sa[i].length() > 0) {
                    tag.setExtra(sa[i].charAt(0), sa[i].substring(1));
                }
            }
            tags.add(tag);
        }
        return ChatTag.fromSeveralTag(tags);
    }

    public static ChatTag fromColoredText(String colText) {
        colText = SU.optimizeColorCodes(colText);
        ArrayList<ChatTag> ctl = new ArrayList<>();
        ChatTag ct = new ChatTag();
        boolean col = false;
        StringBuilder sb = new StringBuilder();
        for (char c : colText.toCharArray()) {
            if (col) {
                ChatColor color = ChatColor.forId(c);
                if (color == ChatColor.reset)
                    color = ChatColor.white;
                if (sb.length() != 0) {
                    ct.text = sb.toString();
                    sb.setLength(0);
                    ctl.add(ct);
                    ct = color.isFormat() ? ct.cloneFormat(new ChatTag()) : new ChatTag();
                }
                if (color.isFormat()) {
                    switch (color) {
                        case obfuscated:
                            ct.obfuscated = true;
                            break;
                        case bold:
                            ct.bold = true;
                            break;
                        case strikethrough:
                            ct.strikethrough = true;
                            break;
                        case underline:
                            ct.underlined = true;
                            break;
                        case italic:
                            ct.italic = true;
                            break;
                    }
                } else {
                    ct.color = color;
                }
                col = false;
            } else {
                if (c == '§')
                    col = true;
                else
                    sb.append(c);
            }
        }
        if (col)
            sb.append('§');
        ct.text = sb.toString();
        ctl.add(ct);
        return ChatTag.fromSeveralTag(ctl);
    }

    public static ChatTag fromSeveralTag(ArrayList<ChatTag> ctl) {
        if (ctl.size() == 1)
            return ctl.iterator().next();
        ChatTag out = new ChatTag("");
        out.extra = ctl;
        return out;
    }

    public static String stripExtras(String extraText) {
        String[] parts = extraText.split("\\\\\\|");
        StringBuilder out = new StringBuilder();
        for (String p : parts) {
            out.append(p.split("\\\\-")[0]);
        }
        return out.toString();
    }

    public static ChatTag fromBaseComponents(BaseComponent[] baseComponents) {
        StringBuilder data = new StringBuilder();
        for (BaseComponent bc : baseComponents) {
            data.append(bc.toLegacyText());
        }
        return ChatTag.fromColoredText(data.toString());
    }

    public static ChatTag fromICBC(Object icbc) {
        return (ChatTag) JsonAPI.deserialize(ChatAPI.toJson(icbc), ChatTag.class);
    }

    public boolean isSimpleText() {
        return translate == null && selector == null && insertion == null && with == null && score == null && extra == null &&
                bold == italic == underlined == strikethrough == obfuscated == false && color == null && clickEvent == null && hoverEvent == null;
    }

    public ChatTag cloneFormat(ChatTag target) {
        target.bold = bold;
        target.italic = italic;
        target.underlined = underlined;
        target.strikethrough = strikethrough;
        target.obfuscated = obfuscated;
        target.color = color;
        return target;
    }

    @Override
    public String toString() {
        return JsonAPI.serialize(this);
    }

    public String toFormatlessString() {
        ArrayList<ChatTag> tags = extra == null ? Lists.newArrayList(this) : extra;
        StringBuilder out = new StringBuilder();
        for (ChatTag tag : tags) {
            out.append(tag.text);
        }
        return out.toString();
    }

    public String toColoredString() {
        ArrayList<ChatTag> tags = extra == null ? Lists.newArrayList(this) : extra;
        StringBuilder out = new StringBuilder();
        for (ChatTag tag : tags) {
            out.append(tag.getFormatPrefix());
            out.append(tag.text);
        }
        return SU.optimizeColorCodes(out.toString());
    }

    public String getFormatPrefix() {
        StringBuilder pref = new StringBuilder();
        if (color != null)
            pref.append('§').append(color.id);
        if (obfuscated)
            pref.append("§k");
        if (bold)
            pref.append("§l");
        if (strikethrough)
            pref.append("§m");
        if (underlined)
            pref.append("§n");
        if (italic)
            pref.append("§o");
        return pref.toString();
    }

    /**
     * Sets an extra data for this ChatTag. Available extra types:
     * <p>
     * T - Hover event, show text
     * I - Hover event, show item
     * A - Hover event, show achievement
     * E - Hover event, show entity
     * S - Click event, suggest command
     * R - Click event, run command
     * U - Click event, open url
     * F - Click event, open file
     * P - Click event, change page
     * D - Click event, twitch user data
     * + - Insertion (on shift click)
     *
     * @param extraType The character representing the extra type
     * @param value     A String representing the value of this extra
     * @ - Selector
     */
    public ChatTag setExtra(char extraType, String value) {
        if (extraType == '+')
            insertion = value;
        else if (extraType == '@') {
            text = null;
            selector = value;
        } else {
            ChatHoverEventType he = ChatHoverEventType.forId(extraType);
            if (he == null) {
                ChatClickEventType ce = ChatClickEventType.forId(extraType);
                if (ce != null)
                    clickEvent = new ChatClickEvent(ce, value);
            } else {
                hoverEvent = new ChatHoverEvent(he, value);
            }
        }
        return this;
    }

    public Object toICBC() {
        return ChatAPI.toICBC(JsonAPI.serialize(this));
    }
}

