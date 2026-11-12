package net.neoforged.fml;

/**
 * Fabric-side loader compatibility boundary.
 *
 * <p>Optional NeoForge integrations are deliberately reported as absent
 * until they receive a real Fabric implementation.</p>
 */
public final class ModList {
    private static final ModList INSTANCE = new ModList();

    private ModList() {}

    public static ModList get() {
        return INSTANCE;
    }

    public boolean isLoaded(String modId) {
        return false;
    }
}