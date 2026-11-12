package team.creative.littletiles.client.level.little;

import java.util.HashMap;
import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import team.creative.littletiles.mixin.client.level.ClientChunkCacheAccessor;

public class LittleClientChunkCache extends ClientChunkCache {
    
    private HashMap<Long, LevelChunk> chunks;
    
    public LittleClientChunkCache(ClientLevel level, int distance) {
        super(level, distance);
    }
    
    public void init(LittleClientLevel level) {
        ((ClientChunkCacheAccessor) this).setLevel(level);
        ((ClientChunkCacheAccessor) this).setLightEngine(new LevelLightEngine(this, true, level.dimensionType().hasSkyLight()));
        this.chunks = new HashMap<>();
    }
    
    public void addLoadedChunk(LevelChunk chunk) {
        chunks.put(chunk.getPos().pack(), chunk);
        getLevel().onChunkLoaded(chunk);
    }
    
    public Iterable<LevelChunk> all() {
        return chunks.values();
    }
    public void drop(ChunkPos pos) {}
    @Nullable
    public LevelChunk getChunk(int x, int z, ChunkStatus status, boolean create) {
        LevelChunk chunk = chunks.get(ChunkPos.pack(x, z));
        if (chunk == null && create)
            chunks.put(ChunkPos.pack(x, z), chunk = new LevelChunk(getLevel(), new ChunkPos(x, z)));
        return chunk;
    }
    @Override
    public LevelChunk replaceWithPacketData(int x, int z, ClientboundLevelChunkPacketData data) {
        LevelChunk chunk = getChunk(x, z, ChunkStatus.FULL, true);
        chunk.replaceWithPacketData(x, z, data);
        getLevel().onChunkLoaded(chunk);
        ClientChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(getLevel(), chunk);
        return chunk;
    }
    public LittleClientLevel getLevel() {
        return (LittleClientLevel) super.getLevel();
    }
    public void tick(BooleanSupplier running, boolean chunks) {}
    public void updateViewCenter(int x, int z) {}
    public void updateViewRadius(int distance) {}
    public String gatherStats() {
        return "" + this.getLoadedChunksCount();
    }
    public int getLoadedChunksCount() {
        return this.chunks.size();
    }
    public void onLightUpdate(LightLayer layer, SectionPos pos) {
        getLevel().renderManager.setSectionDirty(pos.x(), pos.y(), pos.z());
    }
    
    public void addEntity(Entity entity) {}
    
    public void removeEntity(Entity entity) {}
    
}