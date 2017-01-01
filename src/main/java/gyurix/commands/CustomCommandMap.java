package gyurix.commands;

import gyurix.commands.event.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static gyurix.protocol.Reflection.getField;
import static gyurix.spigotlib.SU.*;

/**
 * Created by GyuriX on 2016.08.23..
 */
public class CustomCommandMap extends SimpleCommandMap {
    private static final Field cmdMapF1 = getField(srv.getClass(), "commandMap");
    private static final Field cmdMapF2 = getField(pm.getClass(), "commandMap");
    private static final Field knownCmdsF = getField(SimpleCommandMap.class, "knownCommands");
    public static SimpleCommandMap backend;
    public static Map<String, Command> knownCommands;

    public CustomCommandMap() {
        super(srv);
        try {
            backend = (SimpleCommandMap) cmdMapF1.get(srv);
            cmdMapF1.set(srv, this);
            cmdMapF2.set(pm, this);
            knownCommands = (Map<String, Command>) knownCmdsF.get(backend);
            knownCmdsF.set(this, knownCommands);
        } catch (Throwable e) {
            cs.sendMessage("§2[§aStartup§2]§c Failed to initialize CustomCommandMap :(");
            error(cs, e, "SpigotLib", "gyurix");
        }
    }

    public static void unhook() {
        try {
            cmdMapF1.set(pm, backend);
            cmdMapF2.set(srv, backend);
        } catch (Throwable e) {
        }
    }

    @Override
    public void clearCommands() {
        backend.clearCommands();
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
            error(sender, err.getCause(), "SpigotLib", "gyurix");
            return true;
        }
    }

    @Override
    public Command getCommand(String name) {
        return backend.getCommand(name);
    }

    public Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(knownCommands.values());
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
    public void registerAll(String fallbackPrefix, List<Command> commands) {
        backend.registerAll(fallbackPrefix, commands);
    }

    public void registerServerAliases() {
        backend.registerServerAliases();
    }

    public void setFallbackCommands() {
        backend.setFallbackCommands();
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
}
