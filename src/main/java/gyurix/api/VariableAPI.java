package gyurix.api;

import gyurix.spigotlib.Main;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 *
 * A really simple and easy way to share your variables with another plugins or
 * access your and other plugins variables.
 *
 * @author GyuriX
 *
 */
public class VariableAPI {
    /**
     * The map of the available variable handlers, you need to put your VariableHandler to this
     * map, using the <b>handlers.put(String name,VariableHandler variableHandler) method</b>
     */
    public static final HashMap<String, VariableHandler> handlers = new HashMap();

    /**
     * Use this method to fill the variables in a String. You can also optionally
     * add a player and an another object next to the fillable String, which may
     * help to the VariableHandler.
     *
     * @param msg input message, where the variables should be filled
     * @param plr optional player metadata for the VariableHandlers
     * @param obj optional additional object for the VariableHandlers
     * @return The input message with filled variables.
     */
    public static String fillVariables(String msg, Player plr, Object obj) {
        msg = msg.replace("\\<", "").replace("\\>", "\001");
        StringBuilder m = new StringBuilder(msg);
        for (int i = 0; i < m.length(); i++) {
            if (m.charAt(i) == '<') {
                i = replaceVariable(m, i, plr, obj);
            }
        }
        return m.toString().replace('\000', '<').replace('\001', '>');
    }

    private static int replaceVariable(StringBuilder m, int id, Player plr, Object obj) {
        int i = id + 1;
        boolean dec = true;
        while (i < m.length()) {
            if (m.charAt(i) == '<') {
                i = replaceVariable(m, i, plr, obj);
            } else if (m.charAt(i) == '>') {
                dec = false;
                break;
            }
            i++;
        }
        if ((dec) && (i != m.length()))
            i--;
        if (i >= m.length())
            i = m.length();
        String var = m.substring(id + 1, i);
        String[] v = var.split(":", 2);
        VariableHandler vh = handlers.get(v[0]);
        String out = null;
        if (vh == null) {
            Main.log.severe("The handler is missing for variable \"" + v[0] + "\"");
        } else {
            try {
                out = vh.getValue(obj, plr, v.length == 1 ? "" : v[1]);
            } catch (Throwable e) {
                Main.log.severe("Error on calculating variable \"" + v[0] + "\":");
                e.printStackTrace();
            }
            if (out != null) {
                m.replace(id, i + 1, out);
                return id + out.length() - 1;
            }
        }
        return i;
    }

    /**
     * This is the interface, which you need to implement for making a custom variable handler.
     */
    public interface VariableHandler {
        /**
         * This method is called on every variable filling request
         *
         * @param obj optional additional object, given by the message fill requester
         * @param plr optional player object, given by the message fill requester
         * @param str optional parameters given to the variable name, i.e. "asd" from variable "&lt;myvariable:asd&gt;"
         * @return The value of the variable in a form of a String.
         */
        String getValue(Object obj, Player plr, String str);
    }
}