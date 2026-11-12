package net.neoforged.neoforge.client.settings;

public enum KeyModifier {
    NONE,
    SHIFT,
    CONTROL,
    ALT;
    public boolean matches(KeyModifier other) { return this == other; }
}
