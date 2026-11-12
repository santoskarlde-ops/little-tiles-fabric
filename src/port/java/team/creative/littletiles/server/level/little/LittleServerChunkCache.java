package team.creative.littletiles.server.level.little;

import net.minecraft.util.profiling.Profiler;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import com.mojang.datafixers.DataFixer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import team.creative.creativecore.common.util.type.itr.FunctionIterator;

public class LittleServerChunkCache extends ServerChunkCache implements Iterable<LevelChunk> {
    
    private final HashMap<Long, LittleChunkHolder> chunks = new HashMap<>();
    private final MainThreadExecutor mainThreadProcessor;
    private final LittleServerLevel littleLevel;
    
    public LittleServerChunkCache(LittleServerLevel level, LevelStorageAccess storageAccess, DataFixer dataFixer, StructureTemplateManager structureTemplate, Executor exe, ChunkGenerator generator, int viewDistance, int simulationDistance, boolean sync, ChunkStatusUpdateListener status) {
        super(level, storageAccess, dataFixer, structureTemplate, exe, generator, viewDistance, simulationDistance, sync, status);
        littleLevel = level;
        mainThreadProcessor = new MainThreadExecutor(level);
    }
    
    private ChunkResult<ChunkAccess> getChunkMainThread(int x, int z, boolean create) {
        LittleChunkHolder holder = chunks.get(ChunkPos.pack(x, z));
        if (holder == null && create)
            chunks.put(ChunkPos.pack(x, z), holder = ((LittleChunkMap) chunkMap).createHolder(new ChunkPos(x, z)));
        return ChunkResult.of(holder.chunk);
    }
    
    @Override
    @Nullable
    public ChunkAccess getChunk(int x, int z, ChunkStatus status, boolean create) {
        if (Thread.currentThread() != littleLevel.getServer().getRunningThread())
            return CompletableFuture.supplyAsync(() -> this.getChunk(x, z, status, create), this.mainThreadProcessor).join();
        LittleChunkHolder holder = chunks.get(ChunkPos.pack(x, z));
        if (holder == null && create)
            chunks.put(ChunkPos.pack(x, z), holder = ((LittleChunkMap) chunkMap).createHolder(new ChunkPos(x, z)));
        return holder == null ? null : holder.chunk;
    }
    
    @Override
    @Nullable
    public LevelChunk getChunkNow(int x, int z) {
        if (Thread.currentThread() != littleLevel.getServer().getRunningThread())
            return null;
        return getChunk(x, z, true);
    }
    
    @Override
    public CompletableFuture<ChunkResult<ChunkAccess>> getChunkFuture(int x, int z, ChunkStatus status, boolean create) {
        CompletableFuture<ChunkResult<ChunkAccess>> completablefuture;
        if (Thread.currentThread() == littleLevel.getServer().getRunningThread()) {
            completablefuture = CompletableFuture.supplyAsync(() -> getChunkMainThread(x, z, create));
            this.mainThreadProcessor.managedBlock(completablefuture::isDone);
        } else
            completablefuture = CompletableFuture.supplyAsync(() -> getChunkMainThread(x, z, create), this.mainThreadProcessor);
        return completablefuture;
    }
    
    @Override
    public boolean hasChunk(int x, int z) {
        return chunks.containsKey(ChunkPos.pack(x, z));
    }
    
    @Override
    public LightChunk getChunkForLighting(int x, int z) {
        return getChunk(x, z, ChunkStatus.EMPTY, false);
    }
    
    @Override
    public boolean pollTask() {
        return mainThreadProcessor.pollTask();
    }
    
    @Override
    public boolean isPositionTicking(long pos) {
        if (!littleLevel.shouldTickBlocksAt(pos))
            return false;
        return chunks.containsKey(pos);
    }
    
    @Override
    public void save(boolean all) {}
    
    @Override
    public void close() throws IOException {}
    
    @Override
    public void tick(BooleanSupplier running, boolean chunks) {
        Profiler.get().push("purge");
        this.runDistanceManagerUpdates2();
        Profiler.get().popPush("chunks");
        if (chunks)
            this.tickChunks();
        Profiler.get().pop();
    }
    
    private void tickChunks() {
        if (this.littleLevel.isDebug()) {
            ((LittleChunkMap) chunkMap).tick();
            return;
        }
        
        ProfilerFiller profilerfiller = Profiler.get();
        profilerfiller.push("pollingChunks");
        int k = this.littleLevel.getGameRules().get(GameRules.RANDOM_TICK_SPEED);
        
        profilerfiller.popPush("tick");
        for (Entry<Long, LittleChunkHolder> entry : chunks.entrySet())
            if (this.littleLevel.shouldTickBlocksAt(entry.getKey()))
                this.littleLevel.tickChunk(entry.getValue().chunk, k);
            
        for (LittleChunkHolder holder : chunks.values())
            holder.broadcastChanges();
        profilerfiller.pop();
        ((LittleChunkMap) chunkMap).tick();
    }
    
    @Override
    public int getPendingTasksCount() {
        return this.mainThreadProcessor.getPendingTasksCount();
    }
    
    @Override
    public ChunkGenerator getGenerator() {
        return null;
    }
    
    @Override
    public RandomState randomState() {
        return null;
    }
    
    @Override
    public int getLoadedChunksCount() {
        return chunks.size();
    }
    
    @Override
    public void blockChanged(BlockPos pos) {
        LittleChunkHolder chunkholder = chunks.get(ChunkPos.pack(pos));
        if (chunkholder != null)
            chunkholder.blockChanged(pos);
    }
    
    @Override
    public void onLightUpdate(LightLayer layer, SectionPos pos) {
        this.mainThreadProcessor.execute(() -> {
            LittleChunkHolder chunkholder = chunks.get(ChunkPos.pack(pos.x(), pos.z()));
            if (chunkholder != null)
                chunkholder.sectionLightChanged(layer, pos.y());
        });
    }
    
    @Override
    public boolean updateChunkForced(ChunkPos pos, boolean added) {
        return false;
    }
    
    @Override
    public void move(ServerPlayer player) {
        ((LittleChunkMap) chunkMap).move(player);
    }
    
    @Override
    public void removeEntity(Entity entity) {
        ((LittleChunkMap) chunkMap).removeEntity(entity);
    }
    
    @Override
    public void addEntity(Entity entity) {
        ((LittleChunkMap) chunkMap).addEntity(entity);
    }
    
    public void broadcastAndSend(Entity entity, Packet<?> packet) {
        ((LittleChunkMap) chunkMap).broadcastAndSend(entity, packet);
    }
    
    public void broadcast(Entity entity, Packet<?> packet) {
        ((LittleChunkMap) chunkMap).broadcast(entity, packet);
    }
    
    @Override
    public void setViewDistance(int distance) {}
    
    @Override
    public void setSimulationDistance(int distance) {}
    
    @Override
    public void setSpawnSettings(boolean spawnEnemies) {}
    
    @Override
    public String getChunkDebugData(ChunkPos pos) {
        return "";
    }
    
    @Override
    public ChunkScanAccess chunkScanner() {
        return chunkMap.chunkScanner();
    }
    
    @Override
    @Nullable
    @VisibleForDebug
    public NaturalSpawner.SpawnState getLastSpawnState() {
        return null;
    }
    
    public void loadLevelChunk(ChunkPos pos, CompoundTag tag) {
        chunks.put(pos.pack(), ((LittleChunkMap) chunkMap).createHolder(pos, tag));
    }
    
    public Iterable<LevelChunk> all() {
        return this;
    }
    
    @Override
    public Iterator<LevelChunk> iterator() {
        return new FunctionIterator<>(chunks.values().iterator(), x -> x.chunk);
    }
    
    public boolean runDistanceManagerUpdates2() {
        this.chunkMap.getDistanceManager().runAllUpdates(this.chunkMap);
        return false;
    }
    
    @Override
    public ChunkGeneratorStructureState getGeneratorState() {
        return null;
    }
    
    public final class MainThreadExecutor extends BlockableEventLoop<Runnable> {
        
        public MainThreadExecutor(Level level) {
            super("Chunk source main thread executor for " + level.dimension().identifier(), false);
        }
        
        @Override
        public Runnable wrapRunnable(Runnable p_8506_) {
            return p_8506_;
        }
        
        @Override
        protected boolean shouldRun(Runnable p_8504_) {
            return true;
        }
        
        @Override
        protected boolean scheduleExecutables() {
            return true;
        }
        
        @Override
        protected Thread getRunningThread() {
            return LittleServerChunkCache.this.littleLevel.getServer().getRunningThread();
        }
        
        @Override
        protected void doRunTask(Runnable run) {
            Profiler.get().incrementCounter("runTask");
            super.doRunTask(run);
        }
        
        @Override
        public boolean pollTask() {
            if (LittleServerChunkCache.this.runDistanceManagerUpdates2())
                return true;
            LittleServerChunkCache.this.getLightEngine().tryScheduleUpdate();
            return super.pollTask();
        }
    }
    
}
