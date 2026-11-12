package team.creative.littletiles.common.level.handler;

import java.util.HashMap;
import java.util.function.Function;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.util.type.itr.FilterIterator;

public class LevelHandlers<T extends LevelHandler> {
    
    public static boolean isInvalidLevel(LevelAccessor level) {
        return level instanceof IOrientatedLevel;
    }
    
    protected final Function<Level, T> factory;
    private HashMap<Level, T> handlers = new HashMap<>();
    
    public LevelHandlers() {
        this.factory = createFactory();
        ServerLevelEvents.UNLOAD.register((server, level) -> unloadLevel(level));
    }
    
    public LevelHandlers(Function<Level, T> factory) {
        this.factory = factory;
        ServerLevelEvents.UNLOAD.register((server, level) -> unloadLevel(level));
    }
    
    protected Function<Level, T> createFactory() {
        return null;
    }
    
    public Iterable<T> handlers(boolean client) {
        return new FilterIterator<>(handlers.values(), x -> x.level.isClientSide() == client);
    }
    
    public Iterable<T> handlers() {
        return handlers.values();
    }
    
    public T getWithoutCreate(Level level) {
        if (level instanceof ISubLevel sub)
            level = sub.getRealLevel();
        return handlers.get(level);
    }
    
    public T get(Level level) {
        if (level instanceof ISubLevel sub)
            level = sub.getRealLevel();
        T handler = handlers.get(level);
        if (handler == null) {
            handler = factory.apply(level);
            if (handler == null)
                return null;
            handlers.put(level, handler);
        }
        return handler;
    }
    
    public void unloadLevel(Level level) {
        if (level == null || isInvalidLevel(level))
            return;
        
        T handler = handlers.remove(level);
        if (handler != null)
            unloadHandler(handler);
    }
    
    protected void unloadHandler(T handler) {
        handler.unload();
    }
    
}
