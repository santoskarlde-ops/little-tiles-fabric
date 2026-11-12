package team.creative.littletiles.fabric.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.level.block.state.BlockState;

/** Called after vanilla extracts the local player's overlay state. */
public final class ClientPlayerRenderEvents {
    private ClientPlayerRenderEvents() {}

    @FunctionalInterface
    public interface Extract {
        void afterExtract(PlayerRenderState state);
    }

    @FunctionalInterface
    public interface BlockOverlay {
        boolean shouldRender(BlockState state);
    }

    /** A false result suppresses the selected view-blocking block overlay. */
    public static final Event<BlockOverlay> BLOCK_OVERLAY = EventFactory.createArrayBacked(BlockOverlay.class, listeners -> state -> {
        for (BlockOverlay listener : listeners)
            if (!listener.shouldRender(state))
                return false;
        return true;
    });

    public static final Event<Extract> AFTER_EXTRACT = EventFactory.createArrayBacked(Extract.class, listeners -> state -> {
        for (Extract listener : listeners)
            listener.afterExtract(state);
    });
}
