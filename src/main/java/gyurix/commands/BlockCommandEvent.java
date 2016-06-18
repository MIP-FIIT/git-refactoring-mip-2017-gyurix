package gyurix.commands;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BlockCommandEvent
        extends Event implements Cancellable {
    private static final HandlerList hl = new HandlerList();
    private final BlockCommandSender sender;
    private boolean cancel;
    private String command;

    public BlockCommandEvent(BlockCommandSender sender, String command) {
        this.sender = sender;
        this.command = command;
    }

    public static HandlerList getHandlerList() {
        return hl;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public HandlerList getHandlers() {
        return hl;
    }

    public BlockCommandSender getSender() {
        return sender;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
