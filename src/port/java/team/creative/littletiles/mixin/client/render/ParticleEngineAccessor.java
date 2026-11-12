package team.creative.littletiles.mixin.client.render;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.resources.Identifier;

@Mixin(ParticleEngine.class)
public interface ParticleEngineAccessor {
    
    @Accessor
    public Map<Identifier, ? extends SpriteSet> getSpriteSets();
    
}
