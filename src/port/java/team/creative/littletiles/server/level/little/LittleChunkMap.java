package team.creative.littletiles.server.level.little;

import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.IntSupplier;

import com.mojang.datafixers.DataFixer;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ChunkTaskPriorityQueue;
import net.minecraft.server.level.ChunkTaskDispatcher;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.util.thread.ConsecutiveExecutor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import team.creative.creativecore.common.util.unsafe.CreativeHackery;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.level.little.LittleChunkSerializer;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.packet.entity.LittleVanillaPacket;

public class LittleChunkMap extends ChunkMap {
    
    private LittleServerChunkCache chunkCache;
    private ChunkScanAccess fakeAccess;
    private DistanceManager distanceManager;
    private ThreadedLevelLightEngine lightEngine;
    private ChunkTaskDispatcher lightTaskDispatcher;
    
    public LittleChunkMap(ServerLevel level, LevelStorageSource.LevelStorageAccess access, DataFixer fixer, StructureTemplateManager templateManager, Executor exe, BlockableEventLoop<Runnable> loop, LightChunkGetter lightGetter, ChunkGenerator generator, ChunkStatusUpdateListener status, TicketStorage tickets, int viewDistance, boolean sync) {
        super(level, access, fixer, templateManager, exe, loop, lightGetter, generator, status, tickets, viewDistance, sync);
    }
    
    public void init(ServerLevel level, LittleServerChunkCache chunkCache, LightChunkGetter lightGetter, Executor exe) {
        this.chunkCache = chunkCache;
        this.fakeAccess = (pos, visitor) -> CompletableFuture.runAsync(() -> {});
        ConsecutiveExecutor mailLight = new ConsecutiveExecutor(exe, "light");
        this.lightTaskDispatcher = new ChunkTaskDispatcher(mailLight, exe);
        this.lightEngine = new ThreadedLevelLightEngine(lightGetter, this, level.dimensionType().hasSkyLight(), mailLight, lightTaskDispatcher);
        this.distanceManager = CreativeHackery.allocateInstance(LittleDistanceManager.class);
    }
    
    public LittleChunkHolder addChunkLevel(LittleChunkHolder holder) {
        holder.chunk.setLoaded(true);
        holder.chunk.registerAllBlockEntitiesAfterLevelLoad();
        holder.chunk.registerTickContainerInLevel((ServerLevel) this.chunkCache.getLevel());
        return holder;
    }
    
    public LittleChunkHolder createHolder(ChunkPos pos) {
        return addChunkLevel(new LittleChunkHolder((ServerLevel) chunkCache.getLevel(), pos, this.lightEngine));
    }
    
    public LittleChunkHolder createHolder(ChunkPos pos, CompoundTag nbt) {
        return addChunkLevel(new LittleChunkHolder(LittleChunkSerializer.read((LittleLevel) chunkCache.getLevel(), nbt), this.lightEngine));
    }
    
    public ClientboundLevelChunkWithLightPacket createPacket(LevelChunk chunk) {
        return new ClientboundLevelChunkWithLightPacket(chunk, this.lightEngine, (BitSet) null, (BitSet) null);
    }
    public ChunkHolder getUpdatingChunkIfPresent(long pos) {
        throw new UnsupportedOperationException();
    }
    public ChunkHolder getVisibleChunkIfPresent(long pos) {
        throw new UnsupportedOperationException();
    }
    protected IntSupplier getChunkQueueLevel(long pos) {
        return () -> ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1;
    }
    public ThreadedLevelLightEngine getLightEngine() {
        return lightEngine;
    }
    public String getChunkDebugData(ChunkPos pos) {
        return "";
    }
    public ReportedException debugFuturesAndCreateReportedException(IllegalStateException exception, String message) {
        CrashReport crashreport = CrashReport.forThrowable(exception, "Chunk loading");
        CrashReportCategory crashreportcategory = crashreport.addCategory("Chunk loading");
        crashreportcategory.setDetail("Details", message);
        return new ReportedException(crashreport);
    }
    public void close() throws IOException {}
    public boolean hasWork() {
        return false;
    }
    /** the holder class has no use in this scenario, so all methods related to it are empty */
    public CompletableFuture<ChunkResult<LevelChunk>> prepareEntityTickingChunk(ChunkHolder holder) {
        return null;
    }
    public CompletableFuture<ChunkAccess> applyStep(GenerationChunkHolder holder, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache) {
        return null;
    }
    /** the holder class has no use in this scenario, so all methods related to it are empty */
    public CompletableFuture<ChunkResult<LevelChunk>> prepareTickingChunk(ChunkHolder holder) {
        return null;
    }
    /** the holder class has no use in this scenario, so all methods related to it are empty */
    public CompletableFuture<ChunkResult<LevelChunk>> prepareAccessibleChunk(ChunkHolder p_143110_) {
        return null;
    }
    public int getTickingGenerated() {
        return 0;
    }
    public int size() {
        return chunkCache.getLoadedChunksCount();
    }
    public DistanceManager getDistanceManager() {
        return this.distanceManager;
    }
    public List<ServerPlayer> getPlayersCloseForSpawning(ChunkPos pos) {
        return Collections.EMPTY_LIST;
    }
    public void move(ServerPlayer player) {}
    public List<ServerPlayer> getPlayers(ChunkPos pos, boolean all) {
        return Collections.EMPTY_LIST;
    }
    public String getStorageName() {
        return "";
    }
    public boolean isOldChunkAround(ChunkPos pos, int range) {
        return false;
    }
    public CompletableFuture<Optional<CompoundTag>> read(ChunkPos pos) {
        return null;
    }
    public CompletableFuture<Void> write(ChunkPos pos, CompoundTag tag) {
        return null;
    }
    public void flushWorker() {}
    public ChunkScanAccess chunkScanner() {
        return fakeAccess;
    }
    public void tick() {}
    public void removeEntity(Entity entity) {}
    public void addEntity(Entity entity) {}
    protected void broadcastAndSend(Entity entity, Packet<?> packet) {
        LittleLevel level = (LittleLevel) chunkCache.getLevel();
        LittleTiles.NETWORK.sendToClientTrackingAndSelf(new LittleVanillaPacket(level, packet), level.getHolder());
    }
    public void broadcast(Entity entity, Packet<?> packet) {
        LittleLevel level = (LittleLevel) chunkCache.getLevel();
        LittleTiles.NETWORK.sendToClientTracking(new LittleVanillaPacket(level, packet), level.getHolder());
    }
    
}
