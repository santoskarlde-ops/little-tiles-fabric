package net.neoforged.neoforge.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Minimal event payload used by the port's block-place event bridge.
 */
public class BlockSnapshot {
    public final ResourceKey<Level> dimension;
    public final Level level;
    public final BlockPos pos;

    protected BlockSnapshot(ResourceKey<Level> dimension, Level level, BlockPos pos) {
        this.dimension = dimension;
        this.level = level;
        this.pos = pos;
    }

    public static BlockSnapshot create(ResourceKey<Level> dimension, Level level, BlockPos pos) {
        return new BlockSnapshot(dimension, level, pos);
    }
}