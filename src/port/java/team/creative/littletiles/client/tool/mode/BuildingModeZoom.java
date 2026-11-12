package team.creative.littletiles.client.tool.mode;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.util.math.vec.SmoothValue;
import team.creative.littletiles.client.render.overlay.OverlayRenderer.OverlayGuiLayer;
import team.creative.littletiles.fabric.client.ClientCameraEvents;
import team.creative.littletiles.fabric.client.ClientInputEvents;
import team.creative.littletiles.client.tool.LittleTool;

public class BuildingModeZoom extends BuildingModeFeature {
    
    private static final Minecraft MC = Minecraft.getInstance();
    private static final double MIN_ZOOM = 1;
    private static final double MAX_ZOOM = 16;
    private boolean active;
    
    private SmoothValue zoom = new SmoothValue(200, 1);
    private double scrollPosition;
    private int level = 0;
    
    @CreativeConfig
    private double scrollSpeed = 0.25;
    
    @CreativeConfig
    public List<Double> levels = Arrays.asList(1D, 2D, 4D, 8D, 16D);
    
    public BuildingModeZoom() {
        ClientCameraEvents.WORLD_FOV.register(this::fov);
        ClientInputEvents.MOUSE_SENSITIVITY.register(this::sensitivity);
        ClientInputEvents.WORLD_SCROLL.register(this::scroll);
    }
    
    @Override
    public void create(OverlayGuiLayer gui, LittleTool tool, List<BuildingModeFeature> allFeatures) {
        active = true;
    }
    
    @Override
    public void remove(OverlayGuiLayer gui) {
        active = false;
    }
    
    private boolean active() {
        return active && MC.player != null && MC.gui.screen() == null;
    }
    
    private double zoomLevel() {
        double zoom;
        if (level >= levels.size())
            if (levels.isEmpty())
                zoom = 1;
            else
                zoom = levels.getLast();
        else
            zoom = levels.get(level);
        return Mth.clamp(zoom, MIN_ZOOM, MAX_ZOOM);
    }
    
    private double zoom() {
        return zoom.current();
    }
    
    private float fov(float fov) {
        if (!active())
            return fov;
        zoom.tick();
        return (float) Math.toDegrees(2 * Math.atan(Math.tan(Math.toRadians(fov / 2)) / zoom()));
    }

    private double sensitivity(double sensitivity) {
        return active() ? sensitivity / zoom() : sensitivity;
    }

    public boolean scroll(double horizontal, double vertical) {
        if (!active() || BuildingModeFeatures.GRID.isKeyDown())
            return false;
        
        scrollPosition = Math.clamp(scrollPosition + vertical * scrollSpeed, 0, levels.size());
        int levelBefore = level;
        level = (int) scrollPosition;
        
        if (levelBefore != level) {
            this.zoom.set(zoomLevel());
            MC.player.sendOverlayMessage(Component.translatable("building.zoom.message", zoomLevel()));
        }
        return true;
    }
    
}
