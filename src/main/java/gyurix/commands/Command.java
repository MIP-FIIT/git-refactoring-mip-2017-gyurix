package gyurix.commands;

import gyurix.api.TitleAPI;
import gyurix.api.VariableAPI;
import gyurix.configfile.ConfigSerialization;
import gyurix.economy.EconomyAPI;
import gyurix.inventory.InventoryAPI;
import gyurix.spigotlib.ChatAPI;
import gyurix.spigotlib.SU;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * CommandAPI
 * This is a class representing a command. It's useful for simulating specialized commands.
 * This API also supports filling variables from the VariableAPI.
 */
public class Command implements ConfigSerialization.StringSerializable{
    /**
     * Type of a command
     */
    public enum CommandType{NOCMD,NORMAL,OP,CONSOLE,LOG,ABM,MSG,TS,TITLE,SUBTITLE,ARGS,ECON,ECONSET,XP,XPLEVEL,HP,MAXHP,
        SOUND,PARTICLE,NOTE,GSOUND,GPARTICLE,GNOTE,OPENINV,CUSTOM}

    /**
     * Map of the CustomCommandHandlers. You need to use the Command.customCommands.put(customCommandName,customCommandHandler)
     * method to register your own custom command handler
     */
    public static HashMap<String,CustomCommandHandler> customCommands= new HashMap<String, CustomCommandHandler>();
    CommandType type=CommandType.LOG;
    String cmd;
    String customCMD;

    /**
     * Simple constructor of the command.
     * @param in input string of the constructor, it must have the "CommandType:CommandDescription" format
     */
    public Command(String in){
        String[] s=in.split(":",2);
        try{
            if (s.length==1){
                cmd=in;
            }
            else{
                cmd=s[1];
                type=CommandType.valueOf(s[0]);
            }
        }
        catch (Throwable e){
            type=CommandType.CUSTOM;
            customCMD=s[0];
        }
    }

    /**
     * Executes this command
     *
     * @param sender CommandSender sender of the command.
     * @param args Optional execution arguments
     * @return true if the command execution was successfull, false otherwise
     */
    public boolean execute(CommandSender sender,Object... args){
        try {
            Player plr= sender instanceof Player? (Player) sender :null;
            String text=VariableAPI.fillVariables(cmd,plr,args);
            switch (type){
                case NOCMD:
                    return true;
                case NORMAL:
                    plr.chat(text);
                    return true;
                case OP:
                    boolean wasOp=plr.isOp();
                    plr.setOp(true);
                    plr.chat(text);
                    plr.setOp(wasOp);
                    return true;
                case CONSOLE:
                    SU.srv.dispatchCommand(SU.cs,text);
                    return true;
                case LOG:
                    System.out.println(text);
                    return true;
                case ABM:
                    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR,text,plr);
                    return true;
                case MSG:
                    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM,text,plr);
                    return true;
                case TS:
                    String[] times=text.split(" ",3);
                    TitleAPI.setShowTime(Integer.valueOf(times[0]),Integer.valueOf(times[1]),Integer.valueOf(times[2]),plr);
                    return true;
                case TITLE:
                    TitleAPI.setTitle(text,plr);
                    return true;
                case SUBTITLE:
                    TitleAPI.setSubTitle(text, plr);
                    return true;
                case ECON:
                    String[] data=text.split(" ",2);
                    switch (data.length){
                        case 0:return false;
                        case 1:return EconomyAPI.addBalance(plr.getUniqueId(),new BigDecimal(data[0]));
                        default:
                            return EconomyAPI.addBalance(plr.getUniqueId(),data[0],new BigDecimal(data[1]));
                    }
                case ECONSET:
                    data=text.split(" ",2);
                    switch (data.length){
                        case 0:return false;
                        case 1:EconomyAPI.setBalance(plr.getUniqueId(),new BigDecimal(data[0]));
                        default:
                            EconomyAPI.setBalance(plr.getUniqueId(),data[0],new BigDecimal(data[1]));
                    }
                case XP:
                    if (plr!=null)
                        plr.setExp(Integer.valueOf(text));
                    return true;
                case XPLEVEL:
                    if (plr!=null)
                        plr.setLevel(Integer.valueOf(text));
                    return true;
                case HP:
                    if (plr!=null)
                        plr.setHealth(Integer.valueOf(text));
                    return true;
                case MAXHP:
                    if (plr!=null)
                        plr.setMaxHealth(Integer.valueOf(text));
                    return true;
                case PARTICLE:
                    data=text.split(" ",5);
                    Location loc=new Location(plr.getWorld(),Double.valueOf(data[0]),Double.valueOf(data[1]),Double.valueOf(data[2]));
                    plr.playEffect(loc, Effect.valueOf(data[3]),data.length==4?0:Integer.valueOf(data[4]));
                    return true;
                case SOUND:
                    data=text.split(" ",6);
                    loc=new Location(plr.getWorld(),Double.valueOf(data[0]),Double.valueOf(data[1]),Double.valueOf(data[2]));
                    plr.playSound(loc,data[3],Float.valueOf(data[4]),Float.valueOf(data[5]));
                    return true;
                case NOTE:
                    data=text.split(" ",4);
                    loc=new Location(plr.getWorld(),Double.valueOf(data[0]),Double.valueOf(data[1]),Double.valueOf(data[2]));
                    Instrument i=Instrument.valueOf(data[0]);
                    int octave=Integer.valueOf(data[2]);
                    Note.Tone t= Note.Tone.valueOf(data[3]);
                    switch(data[1]){
                        case "flat":
                            plr.playNote(loc, i, Note.flat(octave,t));
                            return true;
                        case "sharp":
                            plr.playNote(loc, i, Note.sharp(octave, t));
                            return true;
                        case "natural":
                            plr.playNote(loc, i, Note.natural(octave, t));
                            return true;
                        default:
                            System.err.println("Invalid node type: "+data[1]);
                    }
                    return true;
                case GPARTICLE:
                    data=text.split(" ", 5);
                    World w=plr.getWorld();
                    loc=new Location(w,Double.valueOf(data[0]),Double.valueOf(data[1]),Double.valueOf(data[2]));
                    w.playEffect(loc, Effect.valueOf(data[3]), data.length == 4 ? 0 : Integer.valueOf(data[4]));
                    return true;
                case GSOUND:
                    data=text.split(" ", 6);
                    w=plr.getWorld();
                    loc=new Location(w,Double.valueOf(data[0]),Double.valueOf(data[1]),Double.valueOf(data[2]));
                    float v=Float.valueOf(data[4]);
                    float v1=Float.valueOf(data[5]);
                    for (Player p:w.getPlayers())
                        p.playSound(loc,data[3],v,v1);
                    return true;
                case GNOTE:
                    data=text.split(" ", 4);
                    w=plr.getWorld();
                    loc=new Location(w,Double.valueOf(data[0]),Double.valueOf(data[1]),Double.valueOf(data[2]));
                    i=Instrument.valueOf(data[0]);
                    octave=Integer.valueOf(data[2]);
                    t= Note.Tone.valueOf(data[3]);
                    Note n;
                    switch(data[1]){
                        case "flat":
                            n=Note.flat(octave,t);
                            break;
                        case "sharp":
                            n=Note.sharp(octave, t);
                            break;
                        case "natural":
                            n=Note.natural(octave, t);
                            break;
                        default:
                            System.err.println("Invalid node type: "+data[1]);
                            return true;
                    }
                    for (Player p:w.getPlayers())
                        p.playNote(loc, i, n);
                    return true;
                case OPENINV:
                    data=text.split(" ", 2);
                    InventoryAPI.getView(data[0],data.length==1?plr:SU.getPlayer(data[1]));
                    System.out.println("INV OPENED");
                    return true;
                case CUSTOM:
                    return customCommands.get(customCMD).handle(sender, customCMD, text.split(" "), args);
                case ARGS:
                    if (args.length<=Integer.valueOf(text)){
                        throw new RuntimeException();
                    }
                default:
                    return false;
            }
        }
        catch (RuntimeException e){
            throw e;
        }
        catch (Throwable e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Converts the command to it's representing string.
     * @return the conversion output
     */
    @Override
    public String toString() {
        return type==CommandType.CUSTOM?customCMD+":"+cmd:type.name()+":"+cmd;
    }
}
