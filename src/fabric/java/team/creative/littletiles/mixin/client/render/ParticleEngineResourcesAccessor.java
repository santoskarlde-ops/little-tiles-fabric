package team.creative.littletiles.mixin.client.render;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ParticleEngine.class)
public interface ParticleEngineResourcesAccessor {
    @Accessor("resourceManager")
    ParticleResources littletiles$getResourceManager();
}
