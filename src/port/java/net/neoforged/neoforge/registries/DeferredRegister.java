package net.neoforged.neoforge.registries;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

/**
 * Eager Fabric-side stand-in for the small part of NeoForge's
 * {@code DeferredRegister} used by LittleTiles.
 */
public final class DeferredRegister<T> {

    private final Registry<T> registry;
    private final String namespace;

    private DeferredRegister(Object registryOrKey, String namespace) {
        this.registry = registryOrKey instanceof Registry<?> value ? cast(value) : null;
        this.namespace = namespace;
    }

    @SuppressWarnings("unchecked")
    private static <T> Registry<T> cast(Registry<?> registry) {
        return (Registry<T>) registry;
    }

    /**
     * Accepts both a vanilla Registry and a ResourceKey/custom registry key.
     * The latter is intentionally unregistered; ingredient types are local
     * codec metadata in the Fabric port.
     */
    public static <T> DeferredRegister<T> create(Object registryOrKey, String namespace) {
        return new DeferredRegister<>(registryOrKey, namespace);
    }

    public <V extends T> DeferredHolder<T, V> register(String name, Supplier<? extends V> supplier) {
        return registerValue(name, supplier.get());
    }

    public <V extends T> DeferredHolder<T, V> register(String name, Function<String, ? extends V> factory) {
        return registerValue(name, factory.apply(name));
    }

    private <V extends T> DeferredHolder<T, V> registerValue(String name, V value) {
        if (registry != null)
            Registry.register(registry, Identifier.tryBuild(namespace, name), value);
        return new DeferredHolder<>(value);
    }

    /**
     * NeoForge normally attaches the register to an event bus. Entries are
     * already registered above, so this is intentionally a no-op.
     */
    public void register(Object ignoredEventBus) {
    }
}