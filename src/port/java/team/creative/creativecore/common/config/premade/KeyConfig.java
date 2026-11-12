package team.creative.creativecore.common.config.premade;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.settings.KeyModifier;

/**
 * Small Fabric-side compatibility copy of CreativeCore's Forge key setting.
 *
 * <p>The port only needs key matching and display text here.  Configuration
 * persistence remains owned by CreativeCore when that module is present.</p>
 */
public class KeyConfig {

    public static final KeyConfig UNBOUND = new KeyConfig(InputConstants.Type.KEYBOARD.ordinal(), -1, KeyModifier.NONE.ordinal());

    public final int type;
    public final int key;
    public final int modifier;

    public KeyConfig(int type, int key, int modifier) {
        this.type = type;
        this.key = key;
        this.modifier = modifier;
    }

    public KeyConfig(int keyCode, KeyModifier modifier) {
        this(InputConstants.Type.KEYBOARD, keyCode, modifier);
    }

    public KeyConfig(InputConstants.Type type, int code, KeyModifier modifier) {
        this(type.ordinal(), code, modifier.ordinal());
    }

    public KeyConfig(InputConstants.Key key, KeyModifier modifier) {
        this(key.getType(), key.getValue(), modifier);
    }

    public KeyModifier getKeyModifier() {
        KeyModifier[] values = KeyModifier.values();
        return modifier >= 0 && modifier < values.length ? values[modifier] : KeyModifier.NONE;
    }

    public KeyModifier getModifier() {
        return getKeyModifier();
    }

    public InputConstants.Key getKey() {
        if (isUnbound())
            return InputConstants.UNKNOWN;
        return InputConstants.Type.values()[type].getOrCreate(key);
    }

    public boolean isUnbound() {
        return key == -1;
    }

    public Component getTranslatedKeyMessage() {
        InputConstants.Key value = getKey();
        return value.getDisplayName();
    }

    public boolean matchesPress(int key, int action) {
        return this.key == key && action == InputConstants.PRESS;
    }

    public boolean matches(int key) {
        return this.key == key;
    }

    public boolean matches(KeyEvent event) {
        return this.type == InputConstants.Type.KEYBOARD.ordinal() && this.key == event.key();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof KeyConfig other && other.type == type && other.key == key && other.modifier == modifier;
    }
}