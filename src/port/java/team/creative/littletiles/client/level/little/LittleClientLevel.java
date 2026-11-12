package team.creative.littletiles.client.level.little;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import net.neoforged.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.creativecore.common.util.unsafe.CreativeHackery;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.level.ClientLevelExtender;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.client.render.entity.LittleLevelRenderManager;
import team.creative.littletiles.common.level.little.LevelBlockChangeListener;
import team.creative.littletiles.common.level.little.LittleLevel;

@Environment(EnvType.CLIENT)
public abstract class LittleClientLevel extends ClientLevel implements LittleLevel {
    
    public Entity holder;
    public IVecOrigin origin;
    
    public boolean preventNeighborUpdate = false;
    
    public LittleLevelRenderManager renderManager;
    private final List<LevelBlockChangeListener> blockChangeListeners = new ArrayList<>();
    
    protected LittleClientLevel(ClientLevelData data, ResourceKey<Level> dimension, boolean debug, long seed, int seaLevel, RegistryAccess.Frozen access) {
        super(FakeClientPacketListener.get(access), data, dimension, access.lookupOrThrow(Registries.DIMENSION_TYPE).getOrThrow(
            LittleTilesRegistry.FAKE_DIMENSION), 3, 3, Minecraft.getInstance().levelExtractor, debug, seed, seaLevel);
    }
    public void removeEntityById(int id, RemovalReason reason) {
        removeEntity(id, reason);
    }
    public void sendPacketToServer(Packet packet) {
        LittleTilesClient.PLAYER_CONNECTION.send((LittleLevel) this, packet);
    }
    @Environment(EnvType.CLIENT)
    public LittleEntityRenderManager getRenderManager() {
        return renderManager;
    }
    public abstract RecipeAccess recipeAccess();
    public abstract FeatureFlagSet enabledFeatures();
    public LevelEntityGetter<Entity> getEntities() {
        return super.getEntities();
    }
    @Override
    public void disconnect(Component reason) {
        Minecraft.getInstance().level.disconnect(reason);
    }
    public void registerBlockChangeListener(LevelBlockChangeListener listener) {
        blockChangeListeners.add(listener);
    }
    public void sendBlockUpdated(BlockPos pos, BlockState actualState, BlockState setState, int p_104688_) {
        this.renderManager.blockChanged(this, pos, actualState, setState, p_104688_);
    }
    public void setBlocksDirty(BlockPos pos, BlockState actualState, BlockState setState) {
        this.renderManager.setBlockDirty(pos, actualState, setState);
        blockChangeListeners.forEach(x -> x.blockChanged(pos, setState));
    }
    public void setSectionDirtyWithNeighbors(int x, int y, int z) {
        this.renderManager.setSectionDirtyWithNeighbors(x, y, z);
    }
    
    public void setSectionDirty(int x, int y, int z) {
        this.renderManager.setSectionDirty(x, y, z);
    }
    
    public TransientEntitySectionManager<Entity> getEStorage() {
        return ((ClientLevelExtender) this).getEntityStorage();
    }
    
    public void onChunkLoaded(LevelChunk chunk) {
        this.getEStorage().startTicking(chunk.getPos());
        chunk.setLightCorrect(true);
        LevelChunkSection[] section = chunk.getSections();
        for (int i = 0; i < section.length; i++)
            if (!section[i].hasOnlyAir())
                this.renderManager.setSectionDirty(chunk.getPos().x(), chunk.getSectionYFromSectionIndex(i), chunk.getPos().z());
    }
    public Entity getHolder() {
        return holder;
    }
    public void setHolder(Entity entity) {
        this.holder = entity;
    }
    public void neighborChanged(BlockPos pos, Block block, Orientation orientation) {
        if (preventNeighborUpdate)
            return;
        if (this.isClientSide()) {
            BlockState blockstate = this.getBlockState(pos);
            
            try {
                blockstate.handleNeighborChanged(this, pos, block, orientation, false);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception while updating neighbours");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Block being updated");
                crashreportcategory.setDetail("Source block type", () -> {
                    try {
                        return String.format("ID #%s (%s // %s)", BuiltInRegistries.BLOCK.getKey(block), block.getDescriptionId(), block.getClass().getCanonicalName());
                    } catch (Throwable throwable1) {
                        return "ID #" + BuiltInRegistries.BLOCK.getKey(block);
                    }
                });
                CrashReportCategory.populateBlockDetails(crashreportcategory, this, pos, blockstate);
                throw new ReportedException(crashreport);
            }
        } else
            super.neighborChanged(pos, block, orientation);
    }
    public void updateNeighborsAtExceptFromFacing(BlockPos pos, Block block, Direction facing, Orientation orientation) {
        if (preventNeighborUpdate)
            return;
        super.updateNeighborsAtExceptFromFacing(pos, block, facing, orientation);
    }
    public void updateNeighborsAt(BlockPos pos, Block block) {
        if (preventNeighborUpdate)
            return;
        super.updateNeighborsAt(pos, block);
    }
    public LittleClientChunkCache getChunkSource() {
        return (LittleClientChunkCache) super.getChunkSource();
    }
    public void unload(LevelChunk chunk) {
        chunk.clearAllBlockEntities();
        this.getChunkSource().getLightEngine().setLightEnabled(chunk.getPos(), false);
    }
    public void unload() {
        if (renderManager != null)
            renderManager.unload();
    }
    public MapId getFreeMapId() {
        return new MapId(0);
    }
    public void destroyBlockProgress(int id, BlockPos pos, int progress) {
        renderManager.destroyBlockProgress(id, pos, progress);
    }
    public LevelTickAccess<Block> getBlockTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }
    public LevelTickAccess<Fluid> getFluidTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }
    public void gameEvent(Holder<GameEvent> event, Vec3 pos, Context context) {}
    public Iterable<Entity> entities() {
        return getEntities().getAll();
    }
    public Iterable<? extends ChunkAccess> chunks() {
        return getChunkSource().all();
    }
    public void tick() {
        tickBlockEntities();
    }
    
    private static class FakeClientPacketListener extends ClientPacketListener {
        
        private static final FakeClientPacketListener INSTANCE = CreativeHackery.allocateInstance(FakeClientPacketListener.class);
        
        public static FakeClientPacketListener get(RegistryAccess.Frozen access) {
            INSTANCE.access = access;
            return INSTANCE;
        }
        
        private RegistryAccess.Frozen access;
        
        public FakeClientPacketListener(Minecraft mc, Connection con, CommonListenerCookie cookie) {
            super(mc, con, cookie);
        }
        public RegistryAccess.Frozen registryAccess() {
            return this.access;
        }
        
    }
}
