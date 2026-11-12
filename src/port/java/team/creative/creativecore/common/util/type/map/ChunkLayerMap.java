package team.creative.creativecore.common.util.type.map;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;

import net.minecraft.client.renderer.rendertype.RenderType;
import team.creative.creativecore.common.util.type.itr.ComputeNextIterator;
import team.creative.creativecore.common.util.type.list.Tuple;

/**
 * Small Fabric-side replacement for CreativeCore's render-layer map.
 *
 * Minecraft 26.3 still exposes the canonical chunk render layers through
 * RenderType.CHUNK_BUFFER_LAYERS; the old CreativeCore helper was not present
 * in the staged 26.x sources.
 */
public class ChunkLayerMap<T> implements Iterable<T> {

    private static final RenderType[] LAYERS = RenderType.CHUNK_BUFFER_LAYERS.toArray(RenderType[]::new);
    public static final int LAYERS_COUNT = LAYERS.length;
    private final T[] content;

    public ChunkLayerMap() {
        content = (T[]) new Object[LAYERS_COUNT];
    }

    public ChunkLayerMap(Function<RenderType, T> factory) {
        content = (T[]) new Object[LAYERS_COUNT];
        for (int i = 0; i < content.length; i++)
            content[i] = factory.apply(LAYERS[i]);
    }

    public ChunkLayerMap(ChunkLayerMap<T> map) {
        content = Arrays.copyOf(map.content, LAYERS_COUNT);
    }

    public static RenderType layer(int index) {
        return LAYERS[index];
    }

    private int index(RenderType layer) {
        for (int i = 0; i < LAYERS.length; i++)
            if (LAYERS[i] == layer || LAYERS[i].equals(layer))
                return i;
        throw new IllegalArgumentException("Unknown chunk render layer: " + layer);
    }

    public T get(RenderType layer) {
        return content[index(layer)];
    }

    public T getByIndex(int index) {
        return content[index];
    }

    public T put(RenderType layer, T value) {
        int index = index(layer);
        T old = content[index];
        content[index] = value;
        return old;
    }

    public T remove(RenderType layer) {
        int index = index(layer);
        T old = content[index];
        content[index] = null;
        return old;
    }

    public boolean containsKey(RenderType layer) {
        return get(layer) != null;
    }

    public void clear() {
        Arrays.fill(content, null);
    }

    public int size() {
        int size = 0;
        for (T value : content)
            if (value != null)
                size++;
        return size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public Iterable<Tuple<RenderType, T>> tuples() {
        return () -> new ComputeNextIterator<>() {
            private int index;
            private final Tuple<RenderType, T> tuple = new Tuple<>(null, null);

            @Override
            protected Tuple<RenderType, T> computeNext() {
                while (index < content.length && content[index] == null)
                    index++;
                if (index >= content.length)
                    return end();
                tuple.key = LAYERS[index];
                tuple.value = content[index++];
                return tuple;
            }
        };
    }

    @Override
    public Iterator<T> iterator() {
        return new ComputeNextIterator<>() {
            private int index;

            @Override
            protected T computeNext() {
                while (index < content.length && content[index] == null)
                    index++;
                if (index >= content.length)
                    return end();
                return content[index++];
            }
        };
    }
}