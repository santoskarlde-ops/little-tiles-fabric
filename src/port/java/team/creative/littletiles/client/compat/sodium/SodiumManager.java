package team.creative.littletiles.client.compat.sodium;

import team.creative.littletiles.client.render.cache.build.RenderingLevelHandler;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;

/**
 * Compile-time Fabric fallback for the optional Sodium integration.
 *
 * The real integration lives in client/mod/sodium, which is still excluded from the Fabric port (see port-excludes.txt).
 * Until it is ported this reports Sodium as not installed, so callers always take the vanilla rendering path.
 */
public final class SodiumManager {
    
    public static RenderingLevelHandler RENDERING_LEVEL;
    public static RenderingLevelHandler RENDERING_ANIMATION;
    
    private SodiumManager() {}
    
    public static boolean installed() {
        return false;
    }
    
    public static void init() {}
    
    public static LittleEntityRenderManager createRenderManager(LittleAnimationEntity entity) {
        throw new UnsupportedOperationException("The Sodium integration is not ported to Fabric yet");
    }
    
}
