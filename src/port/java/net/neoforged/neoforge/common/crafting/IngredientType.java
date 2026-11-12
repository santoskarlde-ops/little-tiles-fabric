package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.MapCodec;

/** Minimal codec holder for NeoForge custom ingredients on Fabric. */
public final class IngredientType<T extends ICustomIngredient> {

    private final MapCodec<T> codec;

    public IngredientType(MapCodec<T> codec) {
        this.codec = codec;
    }

    public MapCodec<T> codec() {
        return codec;
    }
}