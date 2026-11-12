package team.creative.littletiles.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import team.creative.littletiles.common.entity.EntitySit;

public class LittleSitRenderer extends EntityRenderer<EntitySit, EntityRenderState> {
    
    public LittleSitRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
    
}
