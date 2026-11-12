package net.neoforged.neoforge.common.util;

/**
 * Small NeoForge event-result compatibility enum used by the Fabric port.
 */
public enum TriState {
    TRUE,
    FALSE,
    DEFAULT;

    public boolean isTrue() {
        return this == TRUE;
    }

    public boolean isFalse() {
        return this == FALSE;
    }

    public boolean isDefault() {
        return this == DEFAULT;
    }
}