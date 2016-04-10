package gyurix.animation;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AnimationUpdateEvent
        extends Event {
    private static final HandlerList hl = new HandlerList();
    private final AnimationRunnable runnable;
    private String text;

    public AnimationUpdateEvent(AnimationRunnable runnable, String text) {
        super(false);
        this.runnable = runnable;
        this.text = text;
    }

    public static HandlerList getHandlerList() {
        return hl;
    }

    public HandlerList getHandlers() {
        return hl;
    }

    public AnimationRunnable getRunnable() {
        return this.runnable;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

}

