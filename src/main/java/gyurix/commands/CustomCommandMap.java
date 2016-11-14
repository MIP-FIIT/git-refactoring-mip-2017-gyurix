package gyurix.commands;

import gyurix.commands.event.*;
import gyurix.spigotlib.SU;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static gyurix.protocol.Reflection.getField;
import static gyurix.spigotlib.SU.pm;

/**
 * Created by GyuriX on 2016.08.23..
 */
public class CustomCommandMap extends SimpleCommandMap {
    private static final Field cmdMapF = getField(SimplePluginManager.class, "commandMap");
    private static final Field knownCmdsF = getField(SimpleCommandMap.class, "knownCommands");
    public static SimpleCommandMap backend;
    public static Map<String, Command> knownCommands;

    public CustomCommandMap() {
        super(SU.srv);
        try {
            backend = (SimpleCommandMap) cmdMapF.get(pm);
            cmdMapF.set(pm, this);
            knownCommands = (Map<String, Command>) knownCmdsF.get(backend);
            knownCmdsF.set(this, knownCommands);
        } catch (Throwable e) {
            SU.cs.sendMessage("§2[§aStartup§2]§c Failed to initialize CustomCommandMap :(");
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    public static void unhook() {
        try {
            cmdMapF.set(pm, backend);
        } catch (Throwable e) {
        }
    }

    public void setFallbackCommands() {
        backend.setFallbackCommands();
    }

    @Override
    public void registerAll(String fallbackPrefix, List<Command> commands) {
        backend.registerAll(fallbackPrefix, commands);
    }

    @Override
    public boolean register(String fallbackPrefix, Command command) {
        if (backend == null)
            return false;
        return backend.register(fallbackPrefix, command);
    }

    @Override
    public boolean register(String label, String fallbackPrefix, Command command) {
        return backend.register(label, fallbackPrefix, command);
    }

    @Override
    public boolean dispatch(CommandSender sender, String cmd) throws CommandException {
        {
            CommandExecuteEvent e = new CommandExecuteEvent(sender, cmd);
            pm.callEvent(e);
            if (e.isCancelled())
                return true;
            sender = e.getSender();
            cmd = e.getCommand();
        }
        try {
            boolean unknown = !backend.dispatch(sender, cmd);
            if (unknown) {
                CommandUnknownEvent e = new CommandUnknownEvent(sender, cmd);
                pm.callEvent(e);
                if (e.isCancelled())
                    return true;
            }
            return !unknown;
        } catch (CommandException err) {
            CommandErrorEvent e = new CommandErrorEvent(sender, cmd, err);
            pm.callEvent(e);
            if (e.isCancelled())
                return true;
            sender.sendMessage("§c[CommandAPI] §cError on executing command§e " + cmd);
            SU.error(sender, err.getCause(), "SpigotLib", "gyurix");
            return true;
        }
    }

    @Override
    public void clearCommands() {
        backend.clearCommands();
    }

    @Override
    public Command getCommand(String name) {
        return backend.getCommand(name);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String cmdLine) throws IllegalArgumentException {
        {
            PreTabCompleteEvent e = new PreTabCompleteEvent(sender, cmdLine);
            pm.callEvent(e);
            if (e.isCancelled())
                return e.getResult();
        }
        List<String> list = backend.tabComplete(sender, cmdLine);
        PostTabCompleteEvent e = new PostTabCompleteEvent(sender, cmdLine, list);
        pm.callEvent(e);
        if (e.isCancelled())
            return e.getResult();
        return list;
    }

    public Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(knownCommands.values());
    }

    public void registerServerAliases() {
        backend.registerServerAliases();
    }
}
