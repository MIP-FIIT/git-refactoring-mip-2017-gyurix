package gyurix.commands;

import gyurix.protocol.Reflection;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class CommandMapReplacer
        extends SimpleCommandMap {
    public CommandMapReplacer(Server server) {
        super(server);
    }

    public static void init() {
        try {
            CommandMapReplacer cr = new CommandMapReplacer(SU.srv);
            Field commandMap = Reflection.getField(Reflection.getOBCClass("CraftServer"), "commandMap");
            Object ocr = commandMap.get(SU.srv);
            Field knownCommands = Reflection.getField(SimpleCommandMap.class, "knownCommands");
            knownCommands.set(cr, knownCommands.get(ocr));
            commandMap.set(SU.srv, cr);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public boolean dispatch(CommandSender sender, String msg) throws CommandException {
        for (char c : Config.Chat.colors.toCharArray()) {
            if (!sender.hasPermission("command.color." + c)) continue;
            msg = msg.replace(Config.Chat.colorPrefix + c, "\u00a7" + c);
        }
        if (sender.hasPermission("command.multiline")) {
            msg = msg.replace("\\n", "\n");
        }
        if (sender instanceof BlockCommandSender) {
            BlockCommandEvent event = new BlockCommandEvent((BlockCommandSender) sender, msg);
            SU.pm.callEvent(event);
            if (event.isCancelled())
                return false;
        }
        String[] d = msg.split(" ", 2);
        ArrayList<Command> commands = Config.customCommands.get(d[0] = d[0].toLowerCase());
        Block b = null;
        if (Config.commandLog) {
            if (sender instanceof Player) {
                b = ((Player) sender).getLocation().getBlock();
            } else if (sender instanceof BlockCommandSender) {
                b = ((BlockCommandSender) sender).getBlock();
            }
        }
        if (commands != null && sender.hasPermission("customcommand." + d[0])) {
            if (Config.commandLog)
                Main.log.info((b != null ? "{" + b.getWorld().getName() + "-" + b.getX() + ";" + b.getY() + ";" + b.getZ() + "}" : "") + "Custom command " + d[0] + " from " + sender.getClass().getSimpleName() + " " + sender.getName() + ": " + msg);
            try {
                for (Command c : commands) {
                    c.execute(sender, d.length == 1 ? null : d[1]);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return true;
        } else {
            if (Config.commandLog)
                Main.log.info((b != null ? "{" + b.getWorld().getName() + "-" + b.getX() + ";" + b.getY() + ";" + b.getZ() + "}" : "") + "Regular command from " + sender.getClass().getSimpleName() + " " + sender.getName() + ": " + msg);
            return super.dispatch(sender, msg);
        }
    }
}

