package net.neoforged.neoforge.client.event;

import net.neoforged.bus.api.Event;

public class RegisterClientExtensionsEvent extends Event {
    public <T> void register(Object extension, T... blocks) {}
}
