package team.creative.littletiles.fabric.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/** Hooks at the beginning of render-state update and after frame rendering. */
public final class ClientFrameEvents {
    private ClientFrameEvents() {}

    public static final Event<Runnable> START = EventFactory.createArrayBacked(Runnable.class, listeners -> () -> {
        for (Runnable listener : listeners)
            listener.run();
    });
    public static final Event<Runnable> END = EventFactory.createArrayBacked(Runnable.class, listeners -> () -> {
        for (Runnable listener : listeners)
            listener.run();
    });
}
