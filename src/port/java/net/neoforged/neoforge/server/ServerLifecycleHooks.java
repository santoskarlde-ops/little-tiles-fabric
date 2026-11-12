package net.neoforged.neoforge.server;

import net.minecraft.server.MinecraftServer;

/**
 * Compatibility boundary for the NeoForge server singleton.
 */
public final class ServerLifecycleHooks {
    private ServerLifecycleHooks() {}

    public static MinecraftServer getCurrentServer() {
        return null;
    }
}