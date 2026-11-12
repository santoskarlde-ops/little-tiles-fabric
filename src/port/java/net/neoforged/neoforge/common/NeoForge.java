package net.neoforged.neoforge.common;

import java.util.function.Consumer;

import net.neoforged.bus.api.EventPriority;

/**
 * Compile-time event-bus bridge. Fabric-side event wiring is added in the
 * corresponding port handlers; legacy NeoForge registrations are harmless
 * no-ops until then.
 */
public final class NeoForge {

    public static final EventBus EVENT_BUS = new EventBus();

    private NeoForge() {
    }

    public static final class EventBus {
        public void register(Object listener) {
        }

        public void unregister(Object listener) {
        }

        public void addListener(Object listener) {
        }

        public void addListener(EventPriority priority, Object listener) {
        }

        public <T> void addListener(Consumer<T> listener) {
        }

        public <T> void addListener(EventPriority priority, Consumer<T> listener) {
        }

        public <T> T post(T event) {
            return event;
        }
    }
}