package net.neoforged.fml.loading;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.api.distmarker.Dist;

/** Small compatibility bridge for legacy distribution checks. */
public final class FMLLoader {

    private FMLLoader() {
    }

    public static Dist getDist() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Dist.CLIENT : Dist.DEDICATED_SERVER;
    }
}