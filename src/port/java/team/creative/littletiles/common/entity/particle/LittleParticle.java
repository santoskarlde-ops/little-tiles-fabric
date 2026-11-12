package team.creative.littletiles.common.entity.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.neoforged.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSettings;
import team.creative.littletiles.mixin.client.render.ParticleEngineResourcesAccessor;
import team.creative.littletiles.mixin.client.render.ParticleResourcesAccessor;

@Environment(EnvType.CLIENT)
public class LittleParticle extends SingleQuadParticle {
    
    private static final Minecraft mc = Minecraft.getInstance();
    
    public ParticleSettings settings;
    public SpriteSet sprites;
    private float scaleDeviation;
    
    public LittleParticle(ClientLevel level, Vec3d pos, Vec3d speed, ParticleSettings settings) {
        this(level, pos, speed, settings, getSprites(settings));
    }

    private static SpriteSet getSprites(ParticleSettings settings) {
        var resources = ((ParticleEngineResourcesAccessor) mc.particleEngine).littletiles$getResourceManager();
        var sprites = ((ParticleResourcesAccessor) resources).littletiles$getSpriteSets().get(settings.texture.particleTexture);
        return java.util.Objects.requireNonNull(sprites, "Missing particle sprite set: " + settings.texture.particleTexture);
    }

    private LittleParticle(ClientLevel level, Vec3d pos, Vec3d speed, ParticleSettings settings, SpriteSet sprites) {
        super(level, pos.x, pos.y, pos.z, sprites.first());
        this.xd = speed.x * (Math.random() * 0.1 + 0.95);
        this.yd = speed.y * (Math.random() * 0.1 + 0.95);
        this.zd = speed.z * (Math.random() * 0.1 + 0.95);
        this.lifetime = (int) (settings.lifetime + settings.lifetimeDeviation * Math.random());
        this.gravity = settings.gravity;
        this.alpha = ColorUtils.alphaF(settings.color);
        this.rCol = ColorUtils.redF(settings.color);
        this.gCol = ColorUtils.greenF(settings.color);
        this.bCol = ColorUtils.blueF(settings.color);
        if (settings.randomColor) {
            this.rCol *= Math.random();
            this.gCol *= Math.random();
            this.bCol *= Math.random();
        }
        this.hasPhysics = settings.collision;
        this.settings = settings;
        this.sprites = sprites;
        this.scaleDeviation = (float) (Math.random() * settings.sizeDeviation);
        settings.texture.init(this);
        settings.texture.tick(this);
        this.setSize(0.2F * settings.startSize, 0.2F * settings.startSize);
    }
    public void tick() {
        settings.texture.tick(this);
        float lifePercentage = getAge() / (float) getMaxAge();
        this.quadSize = scaleDeviation + lifePercentage * (settings.endSize - settings.startSize) + settings.startSize;
        super.tick();
    }
    
    public int getAge() {
        return age;
    }
    
    public int getMaxAge() {
        return lifetime;
    }
    
    public void pickSprite(SpriteSet set) {
        setSprite(set.get(random));
    }

    public void setSpriteFirst(SpriteSet set) {
        this.setSprite(set.get(0, this.lifetime));
    }
    
    public void setSpriteFromAgeReverse(SpriteSet set) {
        if (!this.removed)
            this.setSprite(set.get(this.lifetime - this.age, this.lifetime));
    }
    @Override
    public ParticleRenderType getGroup() {
        return settings.texture.type.toVanilla();
    }

    @Override
    protected Layer getLayer() {
        return settings.texture.type.toLayer();
    }
    
}
