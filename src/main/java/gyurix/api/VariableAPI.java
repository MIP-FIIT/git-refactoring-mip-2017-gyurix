package gyurix.api;

import gyurix.spigotlib.Main;
import gyurix.spigotlib.PHAHook;
import gyurix.spigotlib.SU;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * VariableAPI - Used for advanced placeholder filling in Strings
 */
public class VariableAPI {
    private static final HashSet<String> errorVars = new HashSet<>();
    private static final HashSet<String> missingHandlers = new HashSet<>();
    private static final ArrayList<Object> emptyList = new ArrayList<>();
    public static final HashMap<String, VariableHandler> handlers = new HashMap();
    public static boolean phaHook;

    /**
     * Fills variables in the given message from the given character <b>recursively</b>.
     *
     * @param msg     - The message containing placeholders
     * @param from    - Index of character from which the variables should be filled
     * @param plr     - The Player whose data should be used in the message filling
     * @param extArgs - External object arguments
     * @return The first element is always the position of the last read character, the rest are
     * the objects generated by the VariableHandlers for the placeholders found in the message
     */
    private static ArrayList<Object> fill(String msg, int from, Player plr, Object[] extArgs) {
        int msgLength = msg.length();
        int placeholderStart = from;
        ArrayList<Object> out = new ArrayList<>();

        /*  Loop through every character of the message from the given one
            until reaching the end of the message */
        for (int charId = from; charId < msgLength; ++charId) {
            char curChar = msg.charAt(charId);
            switch (curChar) {
                //Check if we found a placeholder beginning
                case '<': {
                    /* Add the substring of the input message from the last ended placeholder
                    *  until the current position for making sure that we don't lose that information.
                    *  If we don't have any characters between these two positions, then we are
                    *  not adding an empty string to the output for boosting client softwares performance
                    */
                    if (placeholderStart < charId)
                        out.add(msg.substring(placeholderStart, charId));

                    //Call this fill method recursively to find inner placeholders of this placeholder
                    ArrayList<Object> d = fill(msg, charId + 1, plr, extArgs);

                    //Skip checking characters already checked by calling the fill method recursively
                    charId = (Integer) d.get(0);
                    placeholderStart = charId + 1;

                    //Remove the current position data from the recursive calls output
                    d.remove(0);

                    //Fill the found placeholders using VariableHandlers and add them to the output
                    out.add(fillVar(plr, d, extArgs));
                    continue;
                }

                //Check if we found a placeholder ending
                case '>': {
                    //Add the placeholders text to the output
                    if (placeholderStart < charId)
                        out.add(msg.substring(placeholderStart, charId));

                    //Make sure the first element of the output will be the position of the last read character
                    out.add(0, charId);
                    return out;
                }
            }
        }
        //Add remaining text to the output
        if (placeholderStart < msg.length())
            out.add(msg.substring(placeholderStart, msg.length()));

        //Make sure the first element of the output will be the index of the last character of the message
        out.add(0, msg.length() - 1);
        return out;
    }

    /**
     * Fills variables in the given list of objects.
     *
     * @param plr     - The player whose data should be used in the variable filling
     * @param inside  - List of objects were the variables should be filled
     * @param extArgs - External arguments for the VariableHandler
     * @return The variable filled object
     */
    private static Object fillVar(Player plr, List<Object> inside, Object[] extArgs) {
        //StringBuilder for calculating the placeholders full name
        StringBuilder nameBuilder = new StringBuilder();
        int listSize = inside.size();

        //Loop through the whole list of objects
        for (int readPos = 0; readPos < listSize; ++readPos) {
            String placeholder = String.valueOf(inside.get(readPos));
            int nameParamsSep = placeholder.indexOf(':');

            //Check if we found a parameterized placeholder in the readingPosition
            if (nameParamsSep != -1) {

                //Split it to placeholder name and placeholder parameters
                nameBuilder.append(placeholder.substring(0, nameParamsSep));
                ArrayList<Object> parameters = new ArrayList<>(inside.subList(readPos + 1, listSize));
                parameters.add(0, placeholder.substring(nameParamsSep + 1));

                //Calculate the value of the placeholder
                return handle(nameBuilder.toString(), plr, parameters, extArgs);
            }
            //If not, then then our placeholders name will be longer
            nameBuilder.append(placeholder);
        }

        //If our placeholder was not a parametered one, calculate it's value without using parameters
        return handle(nameBuilder.toString(), plr, emptyList, extArgs);
    }

    /**
     * @param msg
     * @param plr
     * @param extArgs
     * @return
     */
    public static String fillVariables(String msg, Player plr, Object... extArgs) {
        if (phaHook)
            msg = PlaceholderAPI.setPlaceholders(plr, msg);
        ArrayList<Object> out = fill(msg.replace("\\<", "\u0000").replace("\\>", "\u0001"), 0, plr, extArgs);
        out.remove(0);
        String s = StringUtils.join(out, "").replace('\u0000', '<').replace('\u0001', '>');
        return s;
    }

    /**
     * @param var
     * @param plr
     * @param inside
     * @param extArgs
     * @return
     */
    private static Object handle(String var, Player plr, ArrayList<Object> inside, Object[] extArgs) {
        VariableHandler vh = handlers.get(var);
        if (vh == null) {
            if (missingHandlers.add(var))
                SU.log(Main.pl, "§cMissing handler for variable §f" + var + "§c!");
            return "<" + var + ">";
        }
        try {
            return vh.getValue(plr, inside, extArgs);
        } catch (Throwable e) {
            if (errorVars.add(var)) {
                SU.log(Main.pl, "§cError on calculating variable §f" + var + "§c!");
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
            }
            return '<' + var + '>';
        }
    }

    /**
     * Initializes the VariableAPI. This method is coded for <b>internal usage</b>, it's only public
     * for being callable from the plugins core
     */
    public static void init() {
        if (VariableAPI.phaHook)
            new PHAHook();
    }

    /**
     * Custom VariableHandlers should implement this interface and add themselves to the handlers map, for
     * being used during the variable filling process
     */
    public interface VariableHandler {
        /**
         * Method for processing variable filling requests
         *
         * @param plr   - The player which data should be used for the filling
         * @param args  - Arguments being inside the placeholder
         * @param eArgs - External arguments
         * @return The proper replacement of the fillable placeholder
         */
        Object getValue(Player plr, ArrayList<Object> args, Object[] eArgs);
    }

}

