package team.creative.littletiles.common.entity.particle;

import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle.Layer;
import net.neoforged.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public enum LittleParticleRenderType {
    
    TERRAIN_SHEET,
    PARTICLE_SHEET_OPAQUE,
    PARTICLE_SHEET_TRANSLUCENT,
    PARTICLE_SHEET_LIT,
    CUSTOM,
    NO_RENDER;
    
    @Environment(EnvType.CLIENT)
    public ParticleRenderType toVanilla() {
        return switch (this) {
            case NO_RENDER -> ParticleRenderType.NO_RENDER;
            case PARTICLE_SHEET_OPAQUE, PARTICLE_SHEET_TRANSLUCENT, TERRAIN_SHEET -> ParticleRenderType.SINGLE_QUADS;
            case PARTICLE_SHEET_LIT, CUSTOM -> throw new UnsupportedOperationException("Particle render mode is not ported: " + this);
        };
    }

    @Environment(EnvType.CLIENT)
    public Layer toLayer() {
        return switch (this) {
            case PARTICLE_SHEET_OPAQUE -> Layer.OPAQUE;
            case PARTICLE_SHEET_TRANSLUCENT -> Layer.TRANSLUCENT;
            case TERRAIN_SHEET -> Layer.OPAQUE_TERRAIN;
            case PARTICLE_SHEET_LIT, CUSTOM, NO_RENDER -> throw new UnsupportedOperationException("Particle layer is not available: " + this);
        };
    }

}
