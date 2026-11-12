package team.creative.littletiles.client.render.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.MeshData.SortState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.level.little.FakeClientLevel;
import team.creative.littletiles.client.compat.sodium.SodiumManager;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;
import team.creative.littletiles.client.render.cache.build.RenderingLevelHandler;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.level.transition.LevelTransition.SectionAdditional;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.entity.animation.LittleAnimationLevel;
import team.creative.littletiles.common.compat.sable.SableManager;

@Environment(EnvType.CLIENT)
public class LittleAnimationRenderManager extends LittleEntityRenderManager<LittleAnimationEntity> implements RenderChunkExtender {
    
    public static LittleEntityRenderManager of(LittleAnimationEntity entity) {
        if (!SodiumManager.installed() || entity.getRealLevel() instanceof FakeClientLevel || SableManager.isSubLevel(entity.getRealLevel(), entity.getCenter().baseOffset))
            return new LittleAnimationRenderManager(entity);
        return SodiumManager.createRenderManager(entity);
    }
    
    protected final ChunkLayerMap<VertexBuffer> buffers = new ChunkLayerMap<>();
    protected final Set<ChunkSectionLayer> hasBlocks = new ObjectArraySet<>(ChunkSectionLayer.values().length);
    protected List<BlockEntity> renderableBlockEntities = new ArrayList<>();
    protected MeshData.SortState transparencyState;
    protected boolean needsUpdate = false;
    
    private SectionAdditional additional;
    public ChunkLayerMap<BufferCollection> lastUploaded;
    private volatile int queued;
    
    public LittleAnimationRenderManager(LittleAnimationEntity entity) {
        super(entity);
    }
    
    @Override
    public boolean isSmall() {
        return true;
    }
    
    @Override
    public RenderChunkExtender getRenderChunk(long pos) {
        return this;
    }
    
    @Override
    public SectionAdditional getAdditional() {
        return additional;
    }
    
    @Override
    public void setAdditional(SectionAdditional uploader) {
        this.additional = uploader;
    }
    
    @Override
    public ChunkLayerMap<BufferCollection> getLastUploaded() {
        return lastUploaded;
    }
    
    @Override
    public void setLastUploaded(ChunkLayerMap<BufferCollection> uploaded) {
        this.lastUploaded = uploaded;
    }
    
    @Override
    public int getQueued() {
        return queued;
    }
    
    @Override
    public void setQueued(int queued) {
        this.queued = queued;
    }
    
    @Override
    public LittleAnimationLevel getLevel() {
        return (LittleAnimationLevel) super.getLevel();
    }
    
    @Override
    public VertexBuffer getVertexBuffer(ChunkSectionLayer layer) {
        VertexBuffer buffer = this.buffers.get(layer);
        if (buffer == null)
            this.buffers.put(layer, buffer = new VertexBuffer(VertexBuffer.Usage.STATIC));
        return buffer;
    }
    
    @Override
    public void markReadyForUpdate(boolean playerChanged) {
        needsUpdate = true;
    }
    
    @Override
    public void compileSections(Camera camera) {
        if (needsUpdate) {
            needsUpdate = false;
            hasBlocks.clear();
            renderableBlockEntities.clear();
            RebuildTask rebuild = new RebuildTask();
            Vec3 cam = camera.position();
            var c = SableManager.context(entity.level(), entity.getCenter().baseOffset);
            if (c.isSubLevel())
                cam = c.toFakeWorld(cam);
            CompileResults results = rebuild.compile(cam.x, cam.y, cam.z, LittleTilesClient.ANIMATION_HANDLER.fixedBuffers);
            globalBlockEntities.clear();
            globalBlockEntities.addAll(results.globalBlockEntities);
            renderableBlockEntities = results.blockEntities;
            transparencyState = results.transparencyState;
            prepareUpload();
            for (Entry<ChunkSectionLayer, MeshData> entry : results.renderedLayers.entrySet()) {
                VertexBuffer buffer = getVertexBuffer(entry.getKey());
                if (!buffer.isInvalid()) {
                    buffer.bind();
                    buffer.upload(entry.getValue());
                    VertexBuffer.unbind();
                    
                    BufferCollection buffers = rebuild.getBuffers(entry.getKey());
                    if (buffers != null)
                        uploaded(entry.getKey(), buffers);
                    hasBlocks.add(entry.getKey());
                } else {
                    buffer.close();
                    LittleTiles.LOGGER.error("Could not upload chunk render data due to invalid buffer");
                }
            }
        }
    }
    
    @Override
    protected void renderAllBlockEntities(PoseStack pose, Frustum frustum, Vec3 cam, float frameTime, MultiBufferSource bufferSource) {
        if (renderableBlockEntities != null)
            for (BlockEntity blockEntity : renderableBlockEntities)
                renderBlockEntity(blockEntity, pose, frustum, cam, frameTime, bufferSource);
    }
    
    @Override
    public SortState getTransparencyState() {
        return transparencyState;
    }
    
    @Override
    public void setTransparencyState(SortState state) {
        this.transparencyState = state;
    }
    
    @Override
    public boolean isEmpty(ChunkSectionLayer layer) {
        return !hasBlocks.contains(layer);
    }
    
    @Override
    public void setHasBlock(ChunkSectionLayer layer) {
        hasBlocks.add(layer);
    }
    
    @Override
    public VertexSorting createVertexSorting(double x, double y, double z) {
        BlockPos chunkOffset = entity.getCenter().chunkOrigin;
        return VertexSorting.byDistance((float) x - chunkOffset.getX(), (float) y - chunkOffset.getY(), (float) z - chunkOffset.getZ());
    }
    
    @Override
    public void resortTransparency(ChunkSectionLayer layer, double x, double y, double z) {
        if (transparencyState != null && hasBlocks.contains(ChunkSectionLayer.TRANSLUCENT)) {
            ByteBufferBuilder.Result result = transparencyState.buildSortedIndexBuffer(LittleTilesClient.ANIMATION_HANDLER.fixedBuffers.buffer(ChunkSectionLayer.TRANSLUCENT),
                createVertexSorting(x, y, z));
            if (result != null) {
                VertexBuffer buffer = getVertexBuffer(layer);
                if (buffer.isInvalid()) {
                    result.close();
                } else {
                    buffer.bind();
                    buffer.uploadIndexBuffer(result);
                    VertexBuffer.unbind();
                }
            }
        }
    }
    
    @Override
    public void renderChunkLayer(ChunkSectionLayer layer, PoseStack pose, double x, double y, double z, Matrix4f projectionMatrix, Uniform offset) {
        if (hasBlocks.contains(layer)) {
            VertexBuffer vertexbuffer = buffers.get(layer);
            if (vertexbuffer == null)
                return;
            if (offset != null) {
                BlockPos chunkOffset = entity.getCenter().chunkOrigin;
                offset.set((float) (chunkOffset.getX() - x), (float) (chunkOffset.getY() - y), (float) (chunkOffset.getZ() - z));
                offset.upload();
            }
            
            vertexbuffer.bind();
            vertexbuffer.draw();
        }
    }
    
    @Override
    protected void setBlockDirty(BlockPos pos, boolean playerChanged) {
        needsUpdate = true;
    }
    
    @Override
    public void setBlocksDirty(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        needsUpdate = true;
        
    }
    
    @Override
    public void setBlockDirty(BlockPos pos, BlockState actualState, BlockState setState) {
        needsUpdate = true;
    }
    
    @Override
    protected void setSectionDirty(int x, int y, int z, boolean playerChanged) {
        needsUpdate = true;
    }
    
    @Override
    public void unload() {
        super.unload();
        this.buffers.forEach(VertexBuffer::close);
    }
    
    @Override
    public void allChanged() {
        super.allChanged();
        needsUpdate = true;
    }
    
    static final class CompileResults {
        
        public final List<BlockEntity> globalBlockEntities = new ArrayList<>();
        public final List<BlockEntity> blockEntities = new ArrayList<>();
        public final Map<ChunkSectionLayer, MeshData> renderedLayers = new Reference2ObjectArrayMap<>();
        @Nullable
        public MeshData.SortState transparencyState;
        
        public boolean isEmpty() {
            return renderedLayers.isEmpty() && globalBlockEntities.isEmpty() && blockEntities.isEmpty();
        }
    }
    
    protected class RebuildTask {
        
        private ChunkLayerMap<BufferCollection> caches;
        private SectionBufferBuilderPack pack;
        private ChunkLayerMap<BufferBuilder> builders;
        
        private CompileResults compile(double x, double y, double z, SectionBufferBuilderPack pack) {
            this.pack = pack;
            
            CompileResults results = new CompileResults();
            LittleRenderPipelineType.startCompile(LittleAnimationRenderManager.this);
            
            builders = new ChunkLayerMap();
            
            for (BETiles block : getLevel())
                handleBlockEntity(results, block);
            
            LittleRenderPipelineType.beforeCompileEnds(LittleAnimationRenderManager.this, layer -> (ChunkBufferUploader) builder(layer), layer -> getOrCreateBuffers(layer));
            
            for (Tuple<ChunkSectionLayer, BufferBuilder> entry : builders.tuples()) {
                ChunkSectionLayer layer = entry.key;
                MeshData data = entry.value.build();
                if (data != null) {
                    if (layer == ChunkSectionLayer.TRANSLUCENT)
                        results.transparencyState = data.sortQuads(pack.buffer(ChunkSectionLayer.TRANSLUCENT), createVertexSorting(x, y, z));
                    
                    results.renderedLayers.put(layer, data);
                }
            }
            
            LittleRenderPipelineType.endCompile(LittleAnimationRenderManager.this);
            this.pack = null;
            this.builders = null;
            return results;
        }
        
        private void handleBlockEntity(CompileResults results, BETiles entity) {
            LittleRenderPipelineType.compile(LittleAnimationRenderManager.this.entity.getCenter().chunkOffset.asLong(), entity, x -> (ChunkBufferUploader) builder(x),
                x -> getOrCreateBuffers(x));
            BlockEntityRenderer blockentityrenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);
            if (blockentityrenderer != null)
                if (blockentityrenderer.shouldRenderOffScreen())
                    results.globalBlockEntities.add(entity);
                else
                    results.blockEntities.add(entity); //FORGE: Fix MC-112730
        }
        
        public BufferBuilder builder(ChunkSectionLayer layer) {
            BufferBuilder builder = builders.get(layer);
            if (builder == null)
                builders.put(layer, builder = new BufferBuilder(pack.buffer(layer), VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK));
            return builder;
        }
        
        public BufferCollection getBuffers(ChunkSectionLayer layer) {
            if (caches == null)
                return null;
            return caches.get(layer);
        }
        
        public BufferCollection getOrCreateBuffers(ChunkSectionLayer layer) {
            if (caches == null)
                caches = new ChunkLayerMap<>();
            BufferCollection cache = caches.get(layer);
            if (cache == null)
                caches.put(layer, cache = new BufferCollection());
            return cache;
        }
        
    }
    
    @Override
    public boolean shouldRender(boolean sodiumLayer) {
        return !sodiumLayer;
    }
    
    @Override
    public RenderingLevelHandler renderingHandler() {
        return RenderingLevelHandler.ANIMATION;
    }
}
