package gyurix.api;

import gyurix.spigotlib.Config;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VariableAPI {
    public static final HashMap<String, VariableHandler> handlers = new HashMap();
    public static boolean phaHook;

    public static ArrayList<Object> fill(String msg, int from, Player plr, Object[] oArgs) {
        int l = msg.length();
        int sid = from;
        ArrayList<Object> out = new ArrayList<Object>();
        for (int i = from; i < l; ++i) {
            char c = msg.charAt(i);
            if (c == '<') {
                if (sid < i) {
                    out.add(msg.substring(sid, i));
                }
                ArrayList<Object> d = fill(msg, i + 1, plr, oArgs);
                i = (Integer) d.get(0);
                sid = i + 1;
                d.remove(0);
                out.add(fillVar(plr, d, oArgs));
                continue;
            }
            if (c != '>') continue;
            if (sid < i) {
                out.add(msg.substring(sid, i));
            }
            out.add(0, i);
            return out;
        }
        if (sid < msg.length()) {
            out.add(msg.substring(sid, msg.length()));
        }
        out.add(0, msg.length() - 1);
        return out;
    }

    public static Object fillVar(Player plr, List<Object> inside, Object[] oArgs) {
        String s = "";
        int l = inside.size();
        for (int c = 0; c < l; ++c) {
            String os = String.valueOf(inside.get(c));
            int id = os.indexOf(58);
            if (id != -1) {
                s = s + os.substring(0, id);
                ArrayList<Object> list = new ArrayList<Object>(inside.subList(c + 1, l));
                if (id != os.length() - 1) {
                    list.add(0, os.substring(id + 1));
                }
                return handle(s, plr, list, oArgs);
            }
            s = s + os;
        }
        return handle(s, plr, new ArrayList<Object>(), oArgs);
    }

    public static String fillVariables(String msg, Player plr, Object... oArgs) {
        ArrayList<Object> out = fill(msg.replace("\\<", "\u0000").replace("\\>", "\u0001"), 0, plr, oArgs);
        out.remove(0);
        String s = StringUtils.join(out, "").replace('\u0000', '<').replace('\u0001', '>');
        if (phaHook) {
            if (Config.debug)
                SU.log(Main.pl, "§ePHA Fill variables - \"§f" + msg + "§e\" = \"§f" + PlaceholderAPI.setPlaceholders(plr, s) + "§e\"");
            return PlaceholderAPI.setPlaceholders(plr, s);
        }
        return s;
    }

    private static Object handle(String var, Player plr, ArrayList<Object> inside, Object[] oArgs) {
        VariableHandler vh = handlers.get(var);
        if (vh == null) {
            SU.log(Main.pl, "§cMissing handler for variable §f" + var + "§c!");
        }
        try {
            return vh.getValue(plr, inside, oArgs);
        } catch (Throwable e) {
            SU.log(Main.pl, "§cError on calculating variable §f" + var + "§c!");
            if (Config.debug) {
                e.printStackTrace();
            }
            return "<" + var + ">";
        }
    }

    public interface VariableHandler {
        Object getValue(Player plr, ArrayList<Object> args, Object[] eArgs);
    }

}

