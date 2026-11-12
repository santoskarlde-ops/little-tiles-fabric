package net.neoforged.neoforge.event.level;
import net.neoforged.bus.api.Event;
import net.minecraft.world.level.LevelAccessor;
public class LevelEvent extends Event {
    private final LevelAccessor levelAccessor;
    
    public LevelEvent() {
        this(null);
    }
    
    public LevelEvent(LevelAccessor level) {
        this.levelAccessor = level;
    }
    
    /** The level this event was created for; null for the argument-less stub constructors. */
    public LevelAccessor getLevelAccessor() {
        return levelAccessor;
    }
    
    public static class Load extends LevelEvent {
        public Load() {}
        public Load(LevelAccessor level) {
            super(level);
        }
    }
    
    public static class Unload extends LevelEvent {
        public Unload() {}
        public Unload(LevelAccessor level) {
            super(level);
        }
    }
}
