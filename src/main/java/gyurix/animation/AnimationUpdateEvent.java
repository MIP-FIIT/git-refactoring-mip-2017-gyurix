package gyurix.animation;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author GyuriX
 *
 * The event called on every animation update. If you use this event, you need to be threadsafe,
 * because this event is called asynchronously.
 */
public class AnimationUpdateEvent extends Event {
    private static final HandlerList hl=new HandlerList();
    private final AnimationRunnable runnable;
    private String text;

    public AnimationUpdateEvent(AnimationRunnable runnable, String text) {
        this.runnable = runnable;
        this.text = text;
    }

    @Override
    public HandlerList getHandlers() {
        return hl;
    }

    public static HandlerList getHandlerList() {
        return hl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public AnimationRunnable getRunnable() {
        return runnable;
    }
}
