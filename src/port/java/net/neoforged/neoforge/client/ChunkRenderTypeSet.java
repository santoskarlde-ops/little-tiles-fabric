package net.neoforged.neoforge.client;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public final class ChunkRenderTypeSet implements Iterable<ChunkSectionLayer> {
    private final Set<ChunkSectionLayer> values;
    private ChunkRenderTypeSet(Set<ChunkSectionLayer> values) { this.values = values; }
    public static ChunkRenderTypeSet of(ChunkSectionLayer value) { return new ChunkRenderTypeSet(Collections.singleton(value)); }
    public boolean contains(ChunkSectionLayer value) { return values.contains(value); }
    public boolean isEmpty() { return values.isEmpty(); }
    public Iterator<ChunkSectionLayer> iterator() { return values.iterator(); }
}
