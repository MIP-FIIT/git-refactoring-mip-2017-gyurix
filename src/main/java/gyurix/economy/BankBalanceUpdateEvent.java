package gyurix.economy;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.math.BigDecimal;

public final class BankBalanceUpdateEvent
        extends Event
        implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final String target;
    private final BigDecimal before;
    private final EconomyAPI.BalanceData balanceType;
    private boolean cancel;
    private BigDecimal after;

    BankBalanceUpdateEvent(String target, BigDecimal before, BigDecimal after, EconomyAPI.BalanceData balanceType) {
        this.target = target;
        this.before = before;
        this.after = after;
        this.balanceType = balanceType;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public String getTarget() {
        return this.target;
    }

    public BigDecimal getBefore() {
        return this.before;
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

    public boolean isCancelled() {
        return this.cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}

