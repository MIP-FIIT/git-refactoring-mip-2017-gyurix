package gyurix.economy;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.math.BigDecimal;
import java.util.UUID;

public final class BalanceUpdateEvent
        extends Event
        implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final EconomyAPI.BalanceData balanceType;
    private final BigDecimal before;
    private final UUID target;
    private BigDecimal after;
    private boolean cancel;

    BalanceUpdateEvent(UUID target, BigDecimal before, BigDecimal after, EconomyAPI.BalanceData balanceType) {
        this.target = target;
        this.before = before;
        this.after = after;
        this.balanceType = balanceType;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public BigDecimal getAfter() {
        return this.after;
    }

    public void setAfter(BigDecimal after) {
        this.after = after;
    }

    public EconomyAPI.BalanceData getBalanceType() {
        return this.balanceType;
    }

    public BigDecimal getBefore() {
        return this.before;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public UUID getTarget() {
        return this.target;
    }

    public boolean isCancelled() {
        return this.cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}

