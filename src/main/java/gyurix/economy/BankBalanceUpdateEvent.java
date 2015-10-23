package gyurix.economy;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * An event for detecting and cancelling bank balance changes
 */
public final class BankBalanceUpdateEvent extends Event implements Cancellable {
    private boolean cancel;
    private final String target;
    private final BigDecimal before;

    private BigDecimal after;
    private final EconomyAPI.BalanceData balanceType;

    BankBalanceUpdateEvent(String target, BigDecimal before, BigDecimal after, EconomyAPI.BalanceData balanceType) {
        this.target = target;
        this.before = before;

        this.after = after;
        this.balanceType = balanceType;
    }

    /**
     * The name of the bank whos balance has been changed
     * @return The banks UUID
     */
    public String getTarget() {
        return target;
    }

    /**
     * The balance of the bank before this balance change
     * @return The balance before change
     */
    public BigDecimal getBefore() {
        return before;
    }

    /**
     * The balance of the bank after this balance change
     * @return The balance after change
     */
    public BigDecimal getAfter() {
        return after;
    }


    /**
     * Sets the amount of the banks should be after this change
     * @param after The banks balance after this change
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
