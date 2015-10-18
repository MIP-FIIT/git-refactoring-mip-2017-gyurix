package gyurix.api;

import gyurix.spigotlib.Config;
import gyurix.spigotlib.Main;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A really simple and easy way to share your variables with another plugins or
 * access your and other plugins variables.
 *
 * @author GyuriX
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
     * @param msg   input message, where the variables should be filled
     * @param plr   optional player metadata for the VariableHandlers
     * @param oArgs optional additional objects for the VariableHandlers
     * @return The input message with filled variables.
     */
    public static String fillVariables(String msg, Player plr, Object... oArgs) {
        ArrayList<Object> out=fill(msg.replace("\\<", "\000").replace("\\>", "\001"),0,plr,oArgs);
        out.remove(0);
        return StringUtils.join(out, "").replace('\000', '<').replace('\001', '>');
    }
    private static Object handle(String var,Player plr,ArrayList<Object> inside,Object[] oArgs){
        VariableHandler vh=handlers.get(var);
        if (vh==null){
            Main.log.severe("Missing handler for variable "+var+"!");
        }
        try{
            return vh.getValue(plr, inside,oArgs);
        }
        catch (Throwable e){
            Main.log.severe("Error on calculating variable "+var+"!");
            if (Config.debug)
                e.printStackTrace();
        }
        return "<"+var+">";
    }
    public static Object fillVar(Player plr,List<Object> inside,Object[] oArgs){
        String s="";
        int c;
        int l=inside.size();
        for (c=0;c<l;c++){
            String os=String.valueOf(inside.get(c));
            int id=os.indexOf(':');
            if (id!=-1){
                s+=os.substring(0,id);
                ArrayList<Object> list=new ArrayList<>(inside.subList(c+1,l));
                if (id!=os.length()-1)
                    list.add(0,os.substring(id+1));
                return handle(s,plr,list,oArgs);
            }
            s+=os;
        }
        return handle(s,plr,new ArrayList<Object>(),oArgs);
    }
    public static ArrayList<Object> fill(String msg, int from, Player plr, Object[] oArgs) {
        int l=msg.length();
        int sid=from;
        ArrayList<Object> out=new ArrayList<>();
        for (int i=from;i<l;i++){
            char c=msg.charAt(i);
            if (c=='<'){
                if (sid<i){
                    out.add(msg.substring(sid,i));
                }
                ArrayList<Object> d=fill(msg, i+1, plr, oArgs);
                i=(Integer)d.get(0);
                sid=i+1;
                d.remove(0);
                out.add(fillVar(plr,d,oArgs));
            }
            else if (c=='>'){
                if (sid<i){
                    out.add(msg.substring(sid,i));
                }
                out.add(0,i);
                return out;
            }
        }
        if (sid<msg.length()){
            out.add(msg.substring(sid,msg.length()));
        }
        out.add(0,msg.length()-1);
        return out;
    }

    /**
     * This is the interface, which you need to implement for making a custom variable handler.
     */
    public interface VariableHandler {
        /**
         * This method is called on every variable filling request
         *
         * @param oArgs  optional additional objects, given by the message fill requester
         * @param plr    optional player object, given by the message fill requester
         * @param inside calculated objects by other handlers inside this handler
         * @return The value of the variable in a form of a String.
         */
        Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs);
    }
}