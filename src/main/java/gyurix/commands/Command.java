package gyurix.commands;

import gyurix.api.BungeeAPI;
import gyurix.api.TitleAPI;
import gyurix.api.VariableAPI;
import gyurix.configfile.ConfigSerialization;
import gyurix.economy.EconomyAPI;
import gyurix.inventory.InventoryAPI;
import gyurix.protocol.PacketOutType;
import gyurix.spigotlib.ChatAPI;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class Command implements ConfigSerialization.StringSerializable {
    public static HashMap<String, CustomCommandHandler> customCommands = new HashMap();
    CommandType type = CommandType.LOG;
    String cmd;
    String customCMD;
    int delay = -1;

    public Command(String in) {
        if (in.startsWith("{")) {
            int id = in.indexOf("}");
            delay = Integer.valueOf(in.substring(1, id));
            in = in.substring(id + 1);
        }
        String[] s = in.split(":", 2);
        try {
            if (s.length == 1) {
                this.cmd = in;
            } else {
                this.cmd = s[1];
                this.type = CommandType.valueOf(s[0]);
            }
        } catch (Throwable e) {
            this.type = CommandType.CUSTOM;
            this.customCMD = s[0];
        }
    }

    public boolean execute(CommandSender sender, Object... args) {
        if (delay < 0)
            return executeNow(sender, args);
        SU.sch.scheduleSyncDelayedTask(Main.pl, new DelayedCommandExecutor(this, sender, args), delay);
        return true;
    }

    public boolean executeNow(CommandSender sender, Object... args) {
        try {
            Player plr = sender instanceof Player ? (Player) sender : null;
            String text = VariableAPI.fillVariables(this.cmd, plr, args);
            switch (this.type) {
                case NOCMD: {
                    return true;
                }
                case NORMAL: {
                    plr.chat(text);
                    return true;
                }
                case OP: {
                    boolean wasOp = plr.isOp();
                    plr.setOp(true);
                    plr.chat(text);
                    plr.setOp(wasOp);
                    return true;
                }
                case POTION: {
                    ArrayList<PotionEffect> effects = new ArrayList<>();
                    for (String s : text.split(" ")) {
                        try {
                            String[] d = s.split("\\;");
                            PotionEffectType type = PotionEffectType.getByName(d[0]);
                            int level = 0;
                            int duration = 10;
                            boolean ambient = true;
                            boolean particles = true;
                            for (int i = 1; i < d.length; i++) {
                                if (d[i].startsWith("lvl:"))
                                    level = Integer.valueOf(d[i].substring(4)) - 1;
                                else if (d[i].startsWith("dur:"))
                                    duration = Integer.valueOf(d[i].substring(4));
                                else if (d[i].equals("NA"))
                                    ambient = false;
                                else if (d[i].equals("NP"))
                                    particles = false;
                            }
                            effects.add(new PotionEffect(type, duration, level, ambient, particles));
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                    plr.addPotionEffects(effects);
                    return true;
                }
                case COMPLETE: {
                    String arg = (String) args[0];
                    int id = arg.indexOf(' ');
                    arg = arg.substring(id + 1);
                    SU.tp.sendPacket(plr, PacketOutType.TabComplete.newPacket(SU.filterStart(text.split("\\|"), arg, Config.TabComplete.caseSensitive)));
                    return true;
                }
                case COMPLETENAMES: {
                    ArrayList<String> names = new ArrayList<>();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (plr.canSee(p))
                            names.add(p.getName());
                    }
                    String arg = (String) args[0];
                    int id = arg.indexOf(' ');
                    arg = arg.substring(id + 1);
                    SU.tp.sendPacket(plr, PacketOutType.TabComplete.newPacket(SU.filterStart(names, arg, Config.TabComplete.caseSensitive)));
                    return true;
                }
                case CONSOLE: {
                    SU.srv.dispatchCommand(SU.cs, text);
                    return true;
                }
                case LOG: {
                    System.out.println(text);
                    return true;
                }
                case ABM: {
                    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, text, plr);
                    return true;
                }
                case MSG: {
                    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, text, plr);
                    return true;
                }
                case TS: {
                    String[] times = text.split(" ", 3);
                    TitleAPI.setShowTime(Integer.valueOf(times[0]), Integer.valueOf(times[1]), Integer.valueOf(times[2]), plr);
                    return true;
                }
                case TITLE: {
                    TitleAPI.setTitle(text, plr);
                    return true;
                }
                case SUBTITLE: {
                    TitleAPI.setSubTitle(text, plr);
                    return true;
                }
                case ECON: {
                    String[] data = text.split(" ", 2);
                    switch (data.length) {
                        case 0: {
                            return false;
                        }
                        case 1: {
                            return EconomyAPI.addBalance(plr.getUniqueId(), new BigDecimal(data[0]));
                        }
                    }
                    return EconomyAPI.addBalance(plr.getUniqueId(), data[0], new BigDecimal(data[1]));
                }
                case ECONSET: {
                    String[] data = text.split(" ", 2);
                    switch (data.length) {
                        case 0: {
                            return false;
                        }
                        case 1: {
                            EconomyAPI.setBalance(plr.getUniqueId(), new BigDecimal(data[0]));
                        }
                    }
                    EconomyAPI.setBalance(plr.getUniqueId(), data[0], new BigDecimal(data[1]));
                }
                case XP: {
                    if (plr != null) {
                        plr.setExp(Math.max(Math.min(Float.valueOf(text), 1), 0));
                    }
                    return true;
                }
                case XPLEVEL: {
                    if (plr != null) {
                        plr.setLevel(Integer.valueOf(text));
                    }
                    return true;
                }
                case HP: {
                    if (plr != null) {
                        plr.setHealth(Double.valueOf(text));
                    }
                    return true;
                }
                case MAXHP: {
                    if (plr != null) {
                        plr.setMaxHealth(Double.valueOf(text));
                    }
                    return true;
                }
                case PARTICLE: {
                    String[] data = text.split(" ", 5);
                    Location loc = new Location(plr.getWorld(), Double.valueOf(data[0]), Double.valueOf(data[1]), Double.valueOf(data[2]));
                    plr.playEffect(loc, Effect.valueOf(data[3]), data.length == 4 ? 0 : Integer.valueOf(data[4]));
                    return true;
                }
                case SOUND: {
                    String[] data = text.split(" ", 6);
                    Location loc = new Location(plr.getWorld(), Double.valueOf(data[0]), Double.valueOf(data[1]), Double.valueOf(data[2]));
                    plr.playSound(loc, data[3], Float.valueOf(data[4]), Float.valueOf(data[5]));
                    return true;
                }
                case NOTE: {
                    String[] data = text.split(" ", 4);
                    Location loc = new Location(plr.getWorld(), Double.valueOf(data[0]), Double.valueOf(data[1]), Double.valueOf(data[2]));
                    Instrument i = Instrument.valueOf(data[0]);
                    int octave = Integer.valueOf(data[2]);
                    Note.Tone t = Note.Tone.valueOf(data[3]);
                    switch (data[1]) {
                        case "flat": {
                            plr.playNote(loc, i, Note.flat(octave, t));
                            return true;
                        }
                        case "sharp": {
                            plr.playNote(loc, i, Note.sharp(octave, t));
                            return true;
                        }
                        case "natural": {
                            plr.playNote(loc, i, Note.natural(octave, t));
                            return true;
                        }
                    }
                    System.err.println("Invalid node type: " + data[1]);
                    return true;
                }
                case GPARTICLE: {
                    String[] data = text.split(" ", 5);
                    World w = plr.getWorld();
                    Location loc = new Location(w, Double.valueOf(data[0]), Double.valueOf(data[1]), Double.valueOf(data[2]));
                    w.playEffect(loc, Effect.valueOf(data[3]), data.length == 4 ? 0 : Integer.valueOf(data[4]));
                    return true;
                }
                case GSOUND: {
                    String[] data = text.split(" ", 6);
                    World w = plr.getWorld();
                    Location loc = new Location(w, Double.valueOf(data[0]), Double.valueOf(data[1]), Double.valueOf(data[2]));
                    float v = Float.valueOf(data[4]);
                    float v1 = Float.valueOf(data[5]);
                    for (Player p : w.getPlayers()) {
                        p.playSound(loc, data[3], v, v1);
                    }
                    return true;
                }
                case GNOTE: {
                    Note n;
                    String[] data = text.split(" ", 4);
                    World w = plr.getWorld();
                    Location loc = new Location(w, Double.valueOf(data[0]), Double.valueOf(data[1]), Double.valueOf(data[2]));
                    Instrument i = Instrument.valueOf(data[0]);
                    int octave = Integer.valueOf(data[2]);
                    Note.Tone t = Note.Tone.valueOf(data[3]);
                    switch (data[1]) {
                        case "flat": {
                            n = Note.flat(octave, t);
                            break;
                        }
                        case "sharp": {
                            n = Note.sharp(octave, t);
                            break;
                        }
                        case "natural": {
                            n = Note.natural(octave, t);
                            break;
                        }
                        default: {
                            System.err.println("Invalid node type: " + data[1]);
                            return true;
                        }
                    }
                    for (Player p : w.getPlayers()) {
                        p.playNote(loc, i, n);
                    }
                    return true;
                }
                case OPENINV: {
                    String[] data = text.split(" ", 2);
                    InventoryAPI.getView(data[0], data.length == 1 ? plr : SU.getPlayer(data[1]));
                    System.out.println("INV OPENED");
                    return true;
                }
                case CUSTOM: {
                    return customCommands.get(this.customCMD).handle(sender, this.customCMD, text.split(" "), args);
                }
                case KICK: {
                    plr.kickPlayer(text);
                    return true;
                }
                case SEND: {
                    BungeeAPI.send(text, plr);
                    return true;
                }
                case ARGS: {
                    if (args.length > Integer.valueOf(text)) break;
                    throw new RuntimeException();
                }
            }
            return false;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String toString() {
        return this.type == CommandType.CUSTOM ? this.customCMD + ":" + this.cmd : this.type.name() + ":" + this.cmd;
    }
    public enum CommandType {
        NOCMD, NORMAL, OP, POTION, COMPLETE, COMPLETENAMES, CONSOLE, LOG, ABM, MSG, TS, TITLE, SUBTITLE, ARGS, ECON, ECONSET,
        XP, XPLEVEL, HP, MAXHP, SOUND, PARTICLE, NOTE, GSOUND, GPARTICLE, GNOTE, OPENINV, CUSTOM, SEND, KICK;
    }
}

