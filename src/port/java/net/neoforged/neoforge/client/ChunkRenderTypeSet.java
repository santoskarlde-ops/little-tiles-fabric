package net.neoforged.neoforge.client;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class ChunkRenderTypeSet implements Iterable<RenderType> {
    private final Set<RenderType> values;
    private ChunkRenderTypeSet(Set<RenderType> values) { this.values = values; }
    public static ChunkRenderTypeSet of(RenderType value) { return new ChunkRenderTypeSet(Collections.singleton(value)); }
    public boolean contains(RenderType value) { return values.contains(value); }
    public boolean isEmpty() { return values.isEmpty(); }
    public Iterator<RenderType> iterator() { return values.iterator(); }
}
