package team.creative.littletiles.client.compat.iris;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Compile-time Fabric fallback for the optional Iris integration.
 *
 * The real integration lives in client/mod/iris, which is still excluded from the Fabric port (see port-excludes.txt).
 * Until it is ported this reports shaders as inactive and does nothing.
 */
public final class IrisManager {
    
    private IrisManager() {}
    
    public static boolean installed() {
        return false;
    }
    
    public static void init() {}
    
    public static boolean isShaders() {
        return false;
    }
    
    public static void beginBlock(Object buffers, BlockState state, BlockPos pos) {}
    
    public static void resetBlockContext(Object buffers) {}
    
}
