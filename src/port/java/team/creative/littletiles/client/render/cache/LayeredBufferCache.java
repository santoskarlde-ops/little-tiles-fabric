package team.creative.littletiles.client.render.cache;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;

public class LayeredBufferCache extends ChunkLayerMap<BufferCache> {
    
    public int length(ChunkSectionLayer type) {
        BufferCache cache = get(type);
        if (cache != null)
            return cache.lengthToUpload();
        return 0;
    }
    
    public int length(ChunkSectionLayer type, int facing) {
        BufferCache cache = get(type);
        if (cache != null)
            return cache.lengthToUpload(facing);
        return 0;
    }
    
    public LayeredBufferCache copy() {
        LayeredBufferCache cache = new LayeredBufferCache();
        for (Tuple<ChunkSectionLayer, BufferCache> t : this.tuples())
            cache.put(t.key, t.value.copy());
        return cache;
    }
    
}
