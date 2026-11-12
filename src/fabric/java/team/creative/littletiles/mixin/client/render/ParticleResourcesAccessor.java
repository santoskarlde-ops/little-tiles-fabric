package team.creative.littletiles.mixin.client.render;

import java.util.Map;

import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ParticleResources.class)
public interface ParticleResourcesAccessor {
    @Accessor("spriteSets")
    Map<Identifier, ? extends SpriteSet> littletiles$getSpriteSets();
}
