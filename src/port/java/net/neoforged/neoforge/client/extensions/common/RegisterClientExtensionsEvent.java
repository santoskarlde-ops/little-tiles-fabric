package net.neoforged.neoforge.client.extensions.common;

import net.neoforged.bus.api.Event;

public class RegisterClientExtensionsEvent extends Event {
    public <T> void register(IClientBlockExtensions extension, T... blocks) {}
}
