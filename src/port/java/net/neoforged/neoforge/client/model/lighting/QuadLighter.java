package net.neoforged.neoforge.client.model.lighting;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface QuadLighter {
    default void setup(Level level, BlockPos pos, BlockState state) {}
    default void process(BufferBuilder builder, PoseStack.Pose pose, BakedQuad quad, int overlay) {}
    default void reset() {}
}
