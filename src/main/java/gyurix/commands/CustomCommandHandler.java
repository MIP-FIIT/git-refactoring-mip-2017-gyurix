package gyurix.commands;

import org.bukkit.command.CommandSender;

public interface CustomCommandHandler {
    /* varargs */ boolean handle(CommandSender var1, String var2, String[] var3, Object... var4);
}

