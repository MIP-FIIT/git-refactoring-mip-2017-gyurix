package gyurix.economy;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * An event for detecting and cancelling player balance changes
 */
public final class BalanceUpdateEvent extends Event implements Cancellable {
    private boolean cancel;
    private final UUID target;
    private final BigDecimal before;

    private BigDecimal after;
    private final EconomyAPI.BalanceData balanceType;

    BalanceUpdateEvent(UUID target, BigDecimal before, BigDecimal after, EconomyAPI.BalanceData balanceType) {
        this.target = target;
        this.before = before;

        this.after = after;
        this.balanceType = balanceType;
    }

    /**
     * The UUID of the player whos balance have been changed
     * @return The players UUID
     */
    public UUID getTarget() {
        return target;
    }

    /**
     * The balance of the player before this balance change
     * @return The balance before change
     */
    public BigDecimal getBefore() {
        return before;
    }

    /**
     * The balance of the player after this balance change
     * @return The balance after change
     */
    public BigDecimal getAfter() {
        return after;
    }


    /**
     * Sets the amount of the players should be after this change
     * @param after The players balance after this change
     */
    public void setAfter(BigDecimal after) {
        this.after = after;
    }

    /**
     * The type of the changed balance
     * @return The type of the changed balance
     */
    public EconomyAPI.BalanceData getBalanceType() {
        return balanceType;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }
    @Override
    public void setCancelled(boolean cancel) {
        this.cancel=cancel;
    }


    private static final HandlerList handlers = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
