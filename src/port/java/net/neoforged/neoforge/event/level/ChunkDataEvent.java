package net.neoforged.neoforge.event.level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.neoforged.bus.api.Event;
public class ChunkDataEvent extends Event {
    private final ChunkAccess chunk;
    private final CompoundTag data;
    
    public ChunkDataEvent() {
        this(null, null);
    }
    
    public ChunkDataEvent(ChunkAccess chunk, CompoundTag data) {
        this.chunk = chunk;
        this.data = data;
    }
    
    public ChunkAccess getChunk() {
        return chunk;
    }
    
    public CompoundTag getData() {
        return data;
    }
    
    /** Compile-compatibility shape only; not dispatched by any loader. */
    public static class Load extends ChunkDataEvent {
        private final ChunkType type;
        
        public Load() {
            this.type = null;
        }
        
        public Load(ChunkAccess chunk, CompoundTag data, ChunkType type) {
            super(chunk, data);
            this.type = type;
        }
        
        public ChunkType getStatus() {
            return type;
        }
    }
    
    /** Compile-compatibility shape only; not dispatched by any loader. */
    public static class Save extends ChunkDataEvent {
        private final LevelAccessor levelAccessor;
        
        public Save() {
            this.levelAccessor = null;
        }
        
        public Save(ChunkAccess chunk, LevelAccessor level, CompoundTag data) {
            super(chunk, data);
            this.levelAccessor = level;
        }
        
        public LevelAccessor getLevelAccessor() {
            return levelAccessor;
        }
    }
}
