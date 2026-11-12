package team.creative.littletiles.client.level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.world.level.Level;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.fabric.client.ClientFrameEvents;
import team.creative.littletiles.client.render.cache.build.RenderingThread;
import team.creative.littletiles.common.level.handler.LevelHandler;
import team.creative.littletiles.common.level.handler.LevelHandlers;

public class LevelHandlersClient {
    
    private List<LevelAwareHandler> awareHandlers = new ArrayList<>();
    private List<LevelHandlerClient> handlers = new ArrayList<>();
    
    private ClientLevel currentLevel;

    public LevelHandlersClient() {
        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, level) -> changeLevel(level));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> changeLevel(null));
        ClientTickEvents.START_LEVEL_TICK.register(level -> LittleTiles.ANIMATION_HANDLERS.tickLevel(level));
        ClientTickEvents.START_CLIENT_TICK.register(client -> LittleTiles.TICKERS.clientTick(client));
        ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> LittleTiles.ANIMATION_HANDLERS.entityLoaded(entity));
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> LittleTiles.ANIMATION_HANDLERS.entityUnloaded(entity));
        ClientFrameEvents.END.register(() -> {
            if (LittleTilesClient.ANIMATION_HANDLER != null)
                LittleTilesClient.ANIMATION_HANDLER.renderEnd();
        });
    }
    
    public void register(LevelAwareHandler handler) {
        awareHandlers.add(handler);
    }
    
    public <T extends LevelHandler> void register(Function<Level, T> function, Consumer<T> consumer) {
        handlers.add(new LevelHandlerClient<>(function, consumer));
    }
    
    private void changeLevel(ClientLevel next) {
        if (currentLevel == next)
            return;
        if (currentLevel != null) {
            for (int i = 0; i < handlers.size(); i++)
                handlers.get(i).unload();
            awareHandlers.forEach(LevelAwareHandler::unload);
            LittleTiles.ANIMATION_HANDLERS.unloadLevel(currentLevel);
            LittleTiles.TICKERS.unloadLevel(currentLevel);
            RenderingThread.unload();
        }
        currentLevel = next;
        if (next == null || LevelHandlers.isInvalidLevel(next))
            return;
        for (int i = 0; i < handlers.size(); i++)
            handlers.get(i).load(next);
    }

    private static record LevelHandlerClient<T extends LevelHandler>(Function<Level, T> factory, Consumer<T> consumer) {
        
        public void load(Level level) {
            consumer.accept(factory.apply(level));
        }
        
        public void unload() {
            consumer.accept(null);
        }
    }
}
