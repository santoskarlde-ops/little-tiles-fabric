package team.creative.creativecore.common.util.type.map;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import team.creative.creativecore.common.util.type.itr.ComputeNextIterator;
import team.creative.creativecore.common.util.type.list.Tuple;

/**
 * Render-layer map whose values are lists, matching the small CreativeCore
 * API used by LittleTiles' box collection and upload paths.
 */
public class ChunkLayerMapList<T> implements Iterable<T> {

    private final List<T>[] content;

    public ChunkLayerMapList() {
        content = (List<T>[]) new List<?>[ChunkLayerMap.LAYERS_COUNT];
    }

    private int index(ChunkSectionLayer layer) {
        for (int i = 0; i < ChunkLayerMap.LAYERS_COUNT; i++)
            if (ChunkLayerMap.layer(i) == layer || ChunkLayerMap.layer(i).equals(layer))
                return i;
        throw new IllegalArgumentException("Unknown chunk render layer: " + layer);
    }

    public List<T> get(ChunkSectionLayer layer) {
        return content[index(layer)];
    }

    public List<T> getOrCreate(ChunkSectionLayer layer) {
        int index = index(layer);
        if (content[index] == null)
            content[index] = new ArrayList<>();
        return content[index];
    }

    public void add(ChunkSectionLayer layer, T value) {
        getOrCreate(layer).add(value);
    }

    public void consumeEachLayer(BiConsumer<ChunkSectionLayer, List<T>> consumer) {
        for (int i = 0; i < ChunkLayerMap.LAYERS_COUNT; i++)
            consumer.accept(ChunkLayerMap.layer(i), getOrCreate(ChunkLayerMap.layer(i)));
    }

    public boolean isEmpty() {
        for (List<T> list : content)
            if (list != null && !list.isEmpty())
                return false;
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return new ComputeNextIterator<>() {
            private int layer;
            private Iterator<T> current;

            @Override
            protected T computeNext() {
                while (current == null || !current.hasNext()) {
                    if (layer >= content.length)
                        return end();
                    List<T> list = content[layer++];
                    current = list == null ? null : list.iterator();
                }
                return current.next();
            }
        };
    }

    public Iterable<Tuple<ChunkSectionLayer, List<T>>> tuples() {
        return () -> new ComputeNextIterator<>() {
            private int index;
            private final Tuple<ChunkSectionLayer, List<T>> tuple = new Tuple<>(null, null);

            @Override
            protected Tuple<ChunkSectionLayer, List<T>> computeNext() {
                while (index < content.length && (content[index] == null || content[index].isEmpty()))
                    index++;
                if (index >= content.length)
                    return end();
                tuple.key = ChunkLayerMap.layer(index);
                tuple.value = content[index++];
                return tuple;
            }
        };
    }
}