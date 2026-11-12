package team.creative.littletiles.common.entity;

import net.minecraft.world.entity.EntityDimensions;

/** Computes the dimensions formerly supplied by the loader's entity-size event. */
public final class EntitySizeHandler {
    private EntitySizeHandler() {}

    public static EntityDimensions dimensions(PrimedSizedTnt tnt) {
        return EntityDimensions.scalable((float) tnt.size.getPosX(tnt.grid), (float) tnt.size.getPosY(tnt.grid));
    }
}
