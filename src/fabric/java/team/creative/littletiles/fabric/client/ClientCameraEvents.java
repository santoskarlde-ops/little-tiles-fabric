package team.creative.littletiles.fabric.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ClientCameraEvents {
    private ClientCameraEvents() {}

    @FunctionalInterface
    public interface WorldFov {
        float modify(float fov);
    }

    public static final Event<WorldFov> WORLD_FOV = EventFactory.createArrayBacked(WorldFov.class, listeners -> fov -> {
        for (WorldFov listener : listeners)
            fov = listener.modify(fov);
        return fov;
    });
}
