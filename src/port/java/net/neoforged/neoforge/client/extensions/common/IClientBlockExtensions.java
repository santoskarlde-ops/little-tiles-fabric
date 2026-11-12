package net.neoforged.neoforge.client.extensions.common;

import org.joml.Vector3d;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface IClientBlockExtensions {
    IClientBlockExtensions DEFAULT = new IClientBlockExtensions() {};
    static IClientBlockExtensions of(BlockState state) { return DEFAULT; }
    default boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, Object manager) { return false; }
    default boolean addHitEffects(BlockState state, Level level, Object target, Object manager) { return false; }
    default Vector3d getFogColor(BlockState state, LevelReader level, BlockPos pos, Entity entity, Vector3d originalColor, float partialTicks) { return originalColor; }
}
