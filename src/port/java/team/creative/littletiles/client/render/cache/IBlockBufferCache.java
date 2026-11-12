package team.creative.littletiles.client.render.cache;

import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;

public interface IBlockBufferCache {
    
    public boolean has(ChunkSectionLayer layer);
    
    public BufferCache getIncludingAdditional(ChunkSectionLayer layer);
    
    public void upload(@Nullable Function<ChunkSectionLayer, ChunkBufferUploader> builderSupplier, Function<ChunkSectionLayer, BufferCollection> bufferSupplier) throws VertexFormatMismatchException;
    
    public BufferCache extract(ChunkSectionLayer layer, int toExtract);
    
}
