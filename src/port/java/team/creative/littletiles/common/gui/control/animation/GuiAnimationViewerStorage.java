package team.creative.littletiles.common.gui.control.animation;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.gui.AnimationPreview;
import team.creative.littletiles.mixin.client.render.LightTextureAccessor;

public interface GuiAnimationViewerStorage {
    
    public static final Camera FAKE_CAMERA = new Camera();
    
    public static int[][] makeLightBright() {
        int[][] pixels = new int[16][16];
        LightTextureAccessor texture = (LightTextureAccessor) Minecraft.getInstance().gameRenderer.lightTexture();
        for (int x = 0; x < 16; x++)
            for (int y = 0; y < 16; y++) {
                pixels[x][y] = texture.getLightPixels().getPixelRGBA(x, y);
                texture.getLightPixels().setPixelRGBA(x, y, ColorUtils.WHITE);
            }
        
        texture.getLightTexture().upload();
        return pixels;
    }
    
    public static void resetLight(int[][] pixels) {
        LightTextureAccessor texture = (LightTextureAccessor) Minecraft.getInstance().gameRenderer.lightTexture();
        for (int x = 0; x < 16; x++)
            for (int y = 0; y < 16; y++)
                texture.getLightPixels().setPixelRGBA(x, y, pixels[x][y]);
        texture.getLightTexture().upload();
    }
    
    public boolean isReady();
    
    public double longestSide();
    
    public AABB overall();
    
    public Vec3d center();
    
    public boolean highlightSelected();
    
    public void highlightSelected(boolean value);
    
    @Environment(EnvType.CLIENT)
    public Iterable<AnimationPreview> previewsToRender();
    
    @Environment(EnvType.CLIENT)
    public default void renderAll(PoseStack pose, Matrix4f projection, Minecraft mc) {
        int[][] pixels = makeLightBright();
        
        for (AnimationPreview preview : previewsToRender())
            renderPreviewInternal(pose, projection, preview, mc);
        
        resetLight(pixels);
    }
    
    @Environment(EnvType.CLIENT)
    public default void renderPreviewInternal(PoseStack pose, Matrix4f projection, AnimationPreview preview, Minecraft mc) {
        preview.setupRendering(pose);
        
        preview.animation.getRenderManager().setupRender(FAKE_CAMERA, null, false, false);
        preview.animation.getRenderManager().compileSections(FAKE_CAMERA);
        
        // 26.3 merged the cutout-mipped layer into CUTOUT, so the mipmap-blur workaround around it is gone too.
        renderChunkLayer(preview, ChunkSectionLayer.SOLID, pose, projection);
        renderChunkLayer(preview, ChunkSectionLayer.CUTOUT, pose, projection);
        
        renderChunkLayer(preview, ChunkSectionLayer.TRANSLUCENT, pose, projection);
    }
    
    @Environment(EnvType.CLIENT)
    public default void renderPreview(PoseStack pose, Matrix4f projection, AnimationPreview preview, Minecraft mc) {
        int[][] pixels = makeLightBright();
        renderPreviewInternal(pose, projection, preview, mc);
        resetLight(pixels);
    }
    
    public default void renderChunkLayer(AnimationPreview preview, ChunkSectionLayer layer, PoseStack pose, Matrix4f matrix) {
        layer.setupRenderState();
        ShaderInstance shaderinstance = RenderSystem.getShader();
        RenderSystem.setupShaderLights(shaderinstance);
        shaderinstance.setDefaultUniforms(Mode.QUADS, pose.last().pose(), matrix, Minecraft.getInstance().getWindow());
        shaderinstance.apply();
        
        preview.animation.getRenderManager().renderChunkLayer(layer, pose, 0, 0, 0, matrix, RenderSystem.getShader().CHUNK_OFFSET);
        shaderinstance.clear();
        VertexBuffer.unbind();
        layer.clearRenderState();
    }
    
}