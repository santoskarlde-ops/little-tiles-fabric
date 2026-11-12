package net.neoforged.neoforge.client.event;

import net.neoforged.bus.api.Event;

public class ModelEvent extends Event {
    public static class RegisterAdditional extends ModelEvent { public <T> void register(T id) {} }
    public static class RegisterGeometryLoaders extends ModelEvent { public <T> void register(T id, Object loader) {} }
}
