package team.creative.littletiles.fabric.client;

import java.util.function.DoubleUnaryOperator;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ClientInputEvents {
    private ClientInputEvents() {}

    @FunctionalInterface
    public interface WorldScroll {
        /** Return true to consume a scroll before vanilla changes the selected slot. */
        boolean onScroll(double horizontal, double vertical);
    }

    /** Modify the raw sensitivity option before vanilla applies its turn calculation. */
    public static final Event<DoubleUnaryOperator> MOUSE_SENSITIVITY = EventFactory.createArrayBacked(DoubleUnaryOperator.class, listeners -> sensitivity -> {
        for (DoubleUnaryOperator listener : listeners)
            sensitivity = listener.applyAsDouble(sensitivity);
        return sensitivity;
    });

    public static final Event<WorldScroll> WORLD_SCROLL = EventFactory.createArrayBacked(WorldScroll.class, listeners -> (horizontal, vertical) -> {
        for (WorldScroll listener : listeners)
            if (listener.onScroll(horizontal, vertical))
                return true;
        return false;
    });
}
