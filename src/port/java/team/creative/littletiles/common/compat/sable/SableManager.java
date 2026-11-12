package team.creative.littletiles.common.compat.sable;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.littletiles.common.level.context.ILittleLevelContext;

/**
 * Compile-time Fabric fallback for the optional Sable integration.
 *
 * The NeoForge/Sable implementation is excluded from the Fabric port. Keeping
 * the boundary here makes callers deterministic: without Sable, all lookups
 * use the normal LittleTiles context and origins are returned unchanged.
 */
public final class SableManager {

    public static final boolean INSTALLED = false;

    private SableManager() {}

    public static @Nullable ILittleLevelContext context(LevelAccessor level, BlockPos pos) {
        return ILittleLevelContext.STANDARD;
    }

    public static @Nullable ILittleLevelContext context(LevelAccessor level, ChunkPos pos) {
        return ILittleLevelContext.STANDARD;
    }

    public static @Nullable ILittleLevelContext context(LevelAccessor level, Vec3 pos) {
        return ILittleLevelContext.STANDARD;
    }

    public static boolean isSubLevel(LevelAccessor level, BlockPos pos) {
        return false;
    }

    public static void markDirty(LevelAccessor level, BlockPos pos) {}

    public static IVecOrigin originWrapper(LevelAccessor level, Vec3 pos, IVecOrigin origin) {
        return origin;
    }
}