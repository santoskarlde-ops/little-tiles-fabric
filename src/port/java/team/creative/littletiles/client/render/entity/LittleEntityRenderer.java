package team.creative.littletiles.client.render.entity;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import team.creative.littletiles.common.entity.LittleEntity;

public class LittleEntityRenderer extends EntityRenderer<LittleEntity, EntityRenderState> {
    
    public LittleEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    @Override
    public boolean shouldRender(LittleEntity animation, Frustum frustum, double camX, double camY, double camZ, float partialTick) {
        return isVisible(animation, frustum, camX, camY, camZ);
    }
    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
    
    public static boolean isVisible(LittleEntity animation, Frustum frustum, double camX, double camY, double camZ) {
        if (!animation.hasLoaded())
            return false;
        if (animation.getRenderManager().isInSight == null)
            animation.getRenderManager().isInSight = animation.shouldRender(camX, camY, camZ) && frustum.isVisible(animation.getBoundingBox().inflate(0.5D));
        return animation.getRenderManager().isInSight;
    }
    
}
