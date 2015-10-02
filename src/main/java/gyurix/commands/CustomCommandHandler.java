package gyurix.commands;

import org.bukkit.command.CommandSender;

public interface CustomCommandHandler {
    boolean handle(CommandSender sender,String command,String[] args,Object... extArgs);
}
