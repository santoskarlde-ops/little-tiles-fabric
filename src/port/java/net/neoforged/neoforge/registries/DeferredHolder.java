package net.neoforged.neoforge.registries;

import java.util.function.Supplier;

import net.minecraft.core.Holder;

/**
 * Small Fabric-port compatibility wrapper for NeoForge's deferred holders.
 *
 * <p>The port registers entries eagerly, so there is no event-time resolution
 * to perform.  Keeping the wrapper preserves the source API while exposing
 * both NeoForge-style {@code get()} and the older {@code value()} calls used
 * by the LittleTiles sources.</p>
 */
public final class DeferredHolder<R, T> implements Supplier<T> {

    private final T value;

    DeferredHolder(T value) {
        this.value = value;
    }

    public T value() {
        return value;
    }

    @Override
    public T get() {
        return value;
    }

    public Holder<T> holder() {
        return Holder.direct(value);
    }
}