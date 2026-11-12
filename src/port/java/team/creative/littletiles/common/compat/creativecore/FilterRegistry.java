package team.creative.littletiles.common.compat.creativecore;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.util.registry.exception.RegistryException;

/** Registry for the port's serializable filters, including identity-based constants. */
public final class FilterRegistry<T> {
    private final Map<String, Function<CompoundTag, ? extends T>> readers = new HashMap<>();
    private final Map<Class<?>, String> types = new HashMap<>();
    private final Map<T, String> constants = new IdentityHashMap<>();

    public <S extends T> void register(String id, Class<S> type, Function<CompoundTag, S> reader) {
        if (readers.containsKey(id) || types.containsKey(type))
            throw new IllegalArgumentException("Duplicate filter registration: " + id + " / " + type.getName());
        readers.put(id, reader);
        types.put(type, id);
    }

    public <S extends T> void registerStatic(String id, Class<S> type, S value) {
        if (readers.containsKey(id) || constants.containsKey(value))
            throw new IllegalArgumentException("Duplicate static filter registration: " + id);
        if (!type.isInstance(value))
            throw new IllegalArgumentException("Filter does not match " + type.getName());
        readers.put(id, tag -> value);
        constants.put(value, id);
    }

    public String getId(T filter) throws RegistryException {
        String id = constants.get(filter);
        if (id == null && filter != null)
            id = types.get(filter.getClass());
        if (id == null)
            throw new RegistryException("Unregistered filter: " + filter);
        return id;
    }

    public T create(String id, CompoundTag tag) throws RegistryException {
        Function<CompoundTag, ? extends T> reader = readers.get(id);
        if (reader == null)
            throw new RegistryException("Unknown filter type: " + id);
        return reader.apply(tag);
    }

    public T read(CompoundTag tag) throws RegistryException {
        return create(tag.getStringOr("t", ""), tag);
    }
}
