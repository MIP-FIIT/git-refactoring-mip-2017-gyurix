package gyurix.commands;

import gyurix.api.BungeeAPI;
import gyurix.api.TitleAPI;
import gyurix.api.VariableAPI;
import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.economy.EconomyAPI;
import gyurix.spigotlib.ChatAPI;
import gyurix.spigotlib.ChatAPI.ChatMessageType;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import static gyurix.api.BungeeAPI.executeBungeeCommands;
import static gyurix.api.BungeeAPI.executeServerCommands;

public class Command implements StringSerializable {
    public static HashMap<String, CustomCommandHandler> customCommands = new HashMap();

    static {
        customCommands.put("ABM", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    ChatAPI.sendJsonMsg(ChatMessageType.ACTION_BAR, text, plr);
                } else {
                    cs.sendMessage("§cABM:§f " + text);
                }
                return true;
            }
        });
        customCommands.put("BUNGEE", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    executeBungeeCommands(new String[]{text}, plr.getName());
                }
                return true;
            }
        });
        customCommands.put("CHANCE", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                return SU.rand.nextDouble() < Double.valueOf(text);
            }
        });
        customCommands.put("CLOSEINV", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    ((Player) cs).closeInventory();
                    return true;
                }
                return false;
            }
        });
        customCommands.put("CONSOLE", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                SU.srv.dispatchCommand(SU.cs, text);
                return true;
            }
        });
        customCommands.put("DAMAGE", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    plr.damage(Double.valueOf(text));
                    return true;
                }
                return false;
            }
        });
        customCommands.put("ECON", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    String[] data = text.split(" ", 2);
                    return data.length != 0 && (data.length == 1 ? EconomyAPI.addBalance(plr.getUniqueId(), new BigDecimal(data[0])) :
                            EconomyAPI.addBalance(plr.getUniqueId(), data[0], new BigDecimal(data[1])));
                }
                return false;
            }
        });
        customCommands.put("ECONSET", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    String[] data = text.split(" ", 2);
                    return data.length != 0 && (data.length == 1 ? EconomyAPI.setBalance(plr.getUniqueId(), new BigDecimal(data[0])) :
                            EconomyAPI.setBalance(plr.getUniqueId(), data[0], new BigDecimal(data[1])));
                }
                return false;
            }
        });
        customCommands.put("EXPLOSION", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    Location loc = plr.getLocation();
                    plr.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), Float.valueOf(text), false, false);
                }
                return false;
            }
        });
        customCommands.put("GNOTE", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                String[] data = text.split(" ");
                Location loc = null;
                int firstId = 1;
                if (!data[0].equals("*")) {
                    firstId = 4;
                    loc = new Location(Bukkit.getWorld(data[0]), Double.valueOf(data[1]), Double.valueOf(data[2]), Double.valueOf(data[3]));
                }
                int length = data.length;
                for (int id = firstId; id < length; id++) {
                    String[] d = data[id].split(":");
                    Instrument instrument = Instrument.valueOf(d[0]);
                    Note note = new Note(Integer.valueOf(d[1]));
                    for (Player p : loc == null ? Bukkit.getOnlinePlayers() : loc.getWorld().getPlayers())
                        p.playNote(loc == null ? p.getLocation() : loc, instrument, note);
                }
                return true;
            }
        });
        customCommands.put("GPARTICLE", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                String[] data = text.split(" ");
                Location loc = new Location(Bukkit.getWorld(data[0]), Double.valueOf(data[1]), Double.valueOf(data[2]), Double.valueOf(data[3]));
                loc.getWorld().spigot().playEffect(loc, Effect.valueOf(data[4]), Integer.valueOf(data[5]), Integer.valueOf(data[6]),
                        Float.valueOf(data[7]), Float.valueOf(data[8]), Float.valueOf(data[9]), Float.valueOf(data[10]),
                        Integer.valueOf(data[11]), Integer.valueOf(data[12]));
                return true;
            }
        });
        customCommands.put("GSOUND", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                String[] data = text.split(" ", 7);
                if (data.length == 6) {
                    Location loc = new Location(Bukkit.getWorld(data[0]), Double.valueOf(data[1]), Double.valueOf(data[2]), Double.valueOf(data[3]));
                    loc.getWorld().playSound(loc, data[4], Float.valueOf(data[5]), Float.valueOf(data[6]));
                } else if (data.length == 3) {
                    Location loc = ((Entity) cs).getLocation();
                    loc.getWorld().playSound(loc, data[0], Float.valueOf(data[1]), Float.valueOf(data[2]));
                }
                return true;
            }
        });
        customCommands.put("HP", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    plr.setHealth(Math.min(Double.valueOf(text), plr.getMaxHealth()));
                    return true;
                }
                return false;
            }
        });
        customCommands.put("KICK", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    ((Player) cs).kickPlayer(text);
                    return true;
                }
                return false;
            }
        });
        customCommands.put("LOG", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                SU.cs.sendMessage(text);
                return true;
            }
        });
        customCommands.put("MAXHP", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    plr.setMaxHealth(Double.valueOf(text));
                    return true;
                }
                return false;
            }
        });
        customCommands.put("MSG", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    ChatAPI.sendJsonMsg(ChatMessageType.SYSTEM, text, plr);
                } else {
                    cs.sendMessage(text);
                }
                return true;
            }
        });
        customCommands.put("NOCMD", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                return true;
            }
        });
        customCommands.put("NORMAL", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    plr.chat(text);
                    return true;
                }
                return false;
            }
        });
        customCommands.put("NOTE", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    String[] data = text.split(" ");
                    Location loc = plr.getLocation();
                    int firstId = 1;
                    if (!data[0].equals("*")) {
                        firstId = 4;
                        loc = new Location(Bukkit.getWorld(data[0]), Double.valueOf(data[1]), Double.valueOf(data[2]), Double.valueOf(data[3]));
                    }
                    int length = data.length;
                    for (int id = firstId; id < length; id++) {
                        String[] d = data[id].split(":");
                        Instrument instrument = Instrument.valueOf(d[0]);
                        Note note = new Note(Integer.valueOf(d[1]));
                        plr.playNote(loc, instrument, note);
                    }
                    return true;
                }
                return false;
            }
        });
        customCommands.put("OP", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    boolean wasOp = plr.isOp();
                    plr.setOp(true);
                    plr.chat(text);
                    plr.setOp(wasOp);
                    return true;
                }
                return false;
            }
        });
        customCommands.put("PARTICLE", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    String[] data = text.split(" ");
                    Location loc = new Location(plr.getWorld(), Double.valueOf(data[0]), Double.valueOf(data[1]), Double.valueOf(data[2]));
                    plr.spigot().playEffect(loc, Effect.valueOf(data[3]), Integer.valueOf(data[4]), Integer.valueOf(data[5]),
                            Float.valueOf(data[6]), Float.valueOf(data[7]), Float.valueOf(data[8]), Float.valueOf(data[9]),
                            Integer.valueOf(data[10]), Integer.valueOf(data[11]));
                    return true;
                }
                return false;
            }
        });
        customCommands.put("PERMADD", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                return cs instanceof Player && SU.perm.playerAdd((Player) cs, text);
            }
        });
        customCommands.put("PERMREM", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                return cs instanceof Player && SU.perm.playerRemove((Player) cs, text);
            }
        });

        customCommands.put("POTION", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof LivingEntity) {
                    LivingEntity ent = (LivingEntity) cs;
                    ArrayList<PotionEffect> effects = new ArrayList<>();
                    for (String s : text.split(" ")) {
                        try {
                            String[] d = s.split(":");
                            PotionEffectType type = PotionEffectType.getByName(d[0]);
                            boolean particles = !(d[d.length - 1].equals("NP") || d[d.length - 2].equals("NP"));
                            boolean ambient = !(d[d.length - 1].equals("NA") || d[d.length - 2].equals("NA"));
                            int c = (particles ? 0 : 1) + (ambient ? 0 : 1) + 1;
                            int duration = Integer.valueOf(d[d.length - c]);
                            int level = d.length == c + 2 ? 0 : Integer.valueOf(d[1]);
                            effects.add(new PotionEffect(type, duration, level, ambient, particles));
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                    ent.addPotionEffects(effects);
                    return true;
                }
                return false;
            }
        });
        customCommands.put("SEND", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    BungeeAPI.send(text, plr);
                    return true;
                }
                return false;
            }
        });
        customCommands.put("SERVER", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    String[] d = text.split(" ", 2);
                    String[] servers = d[0].split("[,;]");
                    executeServerCommands(d[1].split(";"), servers);
                }
                return true;
            }
        });
        customCommands.put("SETITEM", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    PlayerInventory pi = ((Player) cs).getInventory();
                    String[] d = text.split(" ", 2);
                    pi.setItem(Integer.valueOf(d[0]), SU.stringToItemStack(d[1]));
                    return true;
                }
                return false;
            }
        });
        customCommands.put("SOUND", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    String[] data = text.split(" ", 6);
                    if (data.length == 6) {
                        Location loc = new Location(plr.getWorld(), Double.valueOf(data[0]), Double.valueOf(data[1]), Double.valueOf(data[2]));
                        plr.playSound(loc, data[3], Float.valueOf(data[4]), Float.valueOf(data[5]));
                    } else if (data.length == 3) {
                        plr.playSound(plr.getLocation(), data[0], Float.valueOf(data[1]), Float.valueOf(data[2]));
                    }
                    return true;
                }
                return false;
            }
        });
        customCommands.put("SUBTITLE", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    TitleAPI.setSubTitle(text, plr);
                } else {
                    cs.sendMessage("§bSUBTITLE:§f " + text);
                }
                return true;
            }
        });
        customCommands.put("TS", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    String[] times = text.split(" ", 3);
                    TitleAPI.setShowTime(Integer.valueOf(times[0]), Integer.valueOf(times[1]), Integer.valueOf(times[2]), plr);
                    return true;
                }
                return false;
            }
        });

        customCommands.put("TITLE", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    TitleAPI.setTitle(text, plr);
                } else {
                    cs.sendMessage("§eTITLE:§f " + text);
                }
                return true;
            }
        });
        customCommands.put("XPLEVEL", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    plr.setLevel(Integer.valueOf(text));
                    return true;
                }
                return false;
            }
        });
        customCommands.put("XP", new CustomCommandHandler() {
            @Override
            public boolean handle(CommandSender cs, String text, Object... args) {
                if (cs instanceof Player) {
                    Player plr = (Player) cs;
                    plr.setExp(Math.max(Math.min(Float.valueOf(text), 1), 0));
                    return true;
                }
                return false;
            }
        });
    }

    public String cmd;
    public int delay = -1;
    public String type = "CONSOLE";

    public Command(String in) {
        if (in.startsWith("{")) {
            int id = in.indexOf('}');
            delay = Integer.valueOf(in.substring(1, id));
            in = in.substring(id + 1);
        }
        String[] s = in.split(":", 2);
        if (s.length == 1) {
            cmd = in;
        } else {
            cmd = s[1];
            type = s[0];
        }
    }

    public static boolean executeAll(CommandSender sender, ArrayList<Command> list, Object... args) {
        if (list == null)
            return false;
        for (Command c : list)
            if (!c.execute(sender, args))
                return false;
        return true;
    }

    public static boolean executeAll(Iterable<Player> pls, Player plr, ArrayList<Command> list, Object... args) {
        if (list == null || pls == null)
            return false;
        for (Player p : pls) {
            if (p != plr)
                for (Command c : list)
                    c.execute(p, args);
        }
        return true;
    }

    public boolean execute(CommandSender sender, Object... args) {
        if (delay < 0)
            return executeNow(sender, args);
        SU.sch.scheduleSyncDelayedTask(Main.pl, new DelayedCommandExecutor(this, sender, args), delay);
        return true;
    }

    public boolean executeNow(CommandSender sender, Object... args) {
        Player plr = sender instanceof Player ? (Player) sender : null;
        String text = VariableAPI.fillVariables(cmd, plr, args);
        try {
            CustomCommandHandler h = customCommands.get(type);
            if (h == null) {
                SU.cs.sendMessage("§cCommandAPI: §eHandler for command \"§f" + type + "§e\" was not found.");
                return false;
            }
            return h.handle(sender, text, args);
        } catch (Throwable e) {
            SU.cs.sendMessage("§cCommandAPI: §eError on executing command \"§b" + type + ":§f" + text + "§e\".");
            if (Config.debug)
                SU.error(sender, e, "SpigotLib", "gyurix");
            return false;
        }
    }

    @Override
    public String toString() {
        return (delay > -1 ? "{" + delay + '}' + type : type) + ':' + cmd;
    }
}

