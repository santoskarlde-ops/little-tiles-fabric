package team.creative.littletiles.client.render.overlay;

import team.creative.littletiles.client.gui.GuiClientCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.joml.Vector4f;

import com.google.common.base.Strings;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.fabric.client.ClientPlayerRenderEvents;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiControlDistHandler;
import team.creative.creativecore.common.gui.manager.GuiManager;
import team.creative.creativecore.common.gui.manager.GuiManagerDist;
import team.creative.creativecore.client.gui.GuiControlRect;
import team.creative.creativecore.client.gui.GuiClientLayer;
import team.creative.creativecore.client.gui.IGuiClientIntegratedParent;
import team.creative.creativecore.client.gui.registry.GuiClientRegistry;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.integration.IGuiIntegratedParent;
import team.creative.creativecore.client.gui.integration.ScreenEventListener;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.math.vec.Vec2d;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.list.TupleList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.level.LevelAwareHandler;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.gui.control.GuiActionDisplay;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;

public class OverlayRenderer implements IGuiClientIntegratedParent, LevelAwareHandler {
    
    private static final Minecraft MC = Minecraft.getInstance();
    
    private boolean doneInit = false;
    private final OverlayGuiLayer transparentLayer = new OverlayGuiLayer(true, "overlay");
    
    private final SingletonList<GuiLayer> layers = new SingletonList<>(transparentLayer);
    private final Screen screen = new Screen(Component.literal("overlay")) {};
    private final ScreenEventListener listener = new ScreenEventListener(this, screen);
    private int lastWidth = -1;
    private int lastHeight = -1;
    private List<OverlayText> scheduled = new ArrayList<>();
    
    public OverlayRenderer() {
        HudElementRegistry.addLast(Identifier.tryBuild(LittleTiles.MODID, "overlay"), (graphics, tickDelta) -> renderPost(graphics));
        ClientPlayerRenderEvents.BLOCK_OVERLAY.register(state -> !(state.getBlock() instanceof BlockTile));
        transparentLayer.setParent(this);
    }
    
    public OverlayGuiLayer gui() {
        return this.transparentLayer;
    }
    
    public void displayActionMessage(List<Component> message) {
        transparentLayer.addMessage(message);
    }
    
    private Vec2d projectToScreen(Vec3 cam, Vec3d pos) {
        var camera = MC.gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
        if (camera == null || !camera.initialized || camera.projectionMatrix == null || camera.viewRotationMatrix == null)
            return null;
        Vector4f clip = new Vector4f((float) (pos.x - cam.x), (float) (pos.y - cam.y), (float) (pos.z - cam.z), 1);
        clip.mul(camera.viewRotationMatrix).mul(camera.projectionMatrix);
        if (clip.w <= 0)
            return null;
        float x = (clip.x / clip.w + 1) * MC.getWindow().getGuiScaledWidth() / 2;
        float y = (1 - clip.y / clip.w) * MC.getWindow().getGuiScaledHeight() / 2;
        return new Vec2d(x, y);
    }
    
    public void renderLabel(Vec3 cam, Vec3d vec, Component component, int color) {
        Vec2d position = projectToScreen(cam, vec);
        if (position != null)
            scheduled.add(new OverlayText(position, component, color));
        
    }
    
    public void renderPost(GuiGraphicsExtractor graphics) {
        Player player = MC.player;
        Font font = MC.font;
        if (player != null && !MC.gui.hud.isHidden()) {
            if (!doneInit) {
                transparentLayer.init();
                doneInit = true;
            }
            
            screen.width = MC.getWindow().getGuiScaledWidth();
            screen.height = MC.getWindow().getGuiScaledHeight();
            if (screen.width != lastWidth || screen.height != lastHeight) {
                transparentLayer.reflow();
                lastWidth = screen.width;
                lastHeight = screen.height;
            }
            
            IGuiClientIntegratedParent.render(this, graphics, screen, listener, 0, 0);
            
            for (OverlayText text : scheduled)
                graphics.text(MC.font, text.component, (int) text.pos.x, (int) text.pos.y, text.color);
            scheduled.clear();
            
            Component tooltip = null;
            if (LittleTilesClient.PREVIEW_RENDERER.tool() != null)
                tooltip = LittleTilesClient.PREVIEW_RENDERER.tool().tooltip();
            
            if (tooltip == null && LittleTiles.CONFIG.rendering.showTooltip && player.getMainHandItem().getItem() instanceof IItemTooltip item) {
                ItemStack stack = player.getMainHandItem();
                String tooltipKey = stack.getItem().builtInRegistryHolder().key().identifier().getNamespace() + "." + stack.getItem().builtInRegistryHolder().key().identifier()
                        .getPath() + ".tooltip";
                tooltipKey = item.tooltipTranslateKey(stack, tooltipKey);
                if (LanguageUtils.can(tooltipKey))
                    tooltip = Component.translatable(tooltipKey, item.tooltipData(stack));
            }
            
            if (tooltip != null) {
                String[] lines = tooltip.getString().split("\\n");
                
                int y = MC.getWindow().getGuiScaledHeight() - 2;
                for (int i = lines.length - 1; i >= 0; i--) {
                    String s = lines[i];
                    
                    if (!Strings.isNullOrEmpty(s)) {
                        y -= font.lineHeight;
                        int k = font.width(s);
                        int i1 = 2 + y;
                        graphics.fill(1, i1 - 1, 2 + k + 1, i1 + font.lineHeight - 1, -1873784752);
                        graphics.text(font, s, 2, i1, 14737632);
                    }
                }
            }
        }
    }
    public boolean isContainer() {
        return false;
    }
    public boolean isClient() {
        return true;
    }
    public Player getPlayer() {
        return MC.player;
    }
    public void closeTopLayer() {}
    public void closeLayer(GuiLayer layer) {}
    public List<GuiLayer> getLayers() {
        return layers;
    }
    public GuiLayer getTopLayer() {
        return transparentLayer;
    }
    public void openLayer(GuiLayer layer) {}
    public void closeLayer(int layer) {}
    public void send(CreativePacket message) {}
    public void unload() {
        transparentLayer.clearMessages();
    }
    public Provider provider() {
        return MC.level.registryAccess();
    }
    @Override public GuiControlDistHandler createDist(GuiControl control) { return GuiClientRegistry.create(control); }
    @Override public <T extends GuiManagerDist> T createDist(GuiManager<T> manager) { return (T) GuiClientRegistry.create(manager); }
    
    public static class OverlayGuiLayer extends GuiLayer {
        static { GuiClientRegistry.register(OverlayGuiLayer.class, GuiClientOverlayLayer::new); }
        
        private final GuiActionDisplay actionDisplay = new GuiActionDisplay(this, "action").setMessageCount(1);
        
        public OverlayGuiLayer(boolean client, String name) {
            super(client, name);
        }
        
        private TupleList<GuiControl, OverlayPosition> positions = new TupleList<>();
        public void create() {
            addOverlayControl(actionDisplay, OverlayPosition.ACTION_BAR);
        }
        
        public void addOverlayControl(GuiControl control, OverlayPosition position) {
            super.add(control);
            positions.add(control, position);
        }
        public boolean hasMinimumOuterSpacing() {
            return false;
        }
        @Deprecated
        public GuiParent add(boolean conditional, Supplier<GuiControl> controlSupplier) {
            throw new UnsupportedOperationException();
        }
        @Deprecated
        public GuiParent add(GuiControl control) {
            throw new UnsupportedOperationException();
        }
        @Deprecated
        public GuiParent add(GuiControl... controls) {
            throw new UnsupportedOperationException();
        }
        void positionControls(int width, int height) {
            for (Tuple<GuiControl, OverlayPosition> tuple : positions)
                tuple.value.positionControl(GuiClientCompat.rect(tuple.key), width, height);
        }

        public void clearMessages() {
            actionDisplay.clearMessages();
        }
        
        public void addMessage(List<Component> message) {
            actionDisplay.addMessage(message);
        }
    }
    
    private static class GuiClientOverlayLayer extends GuiClientLayer {
        GuiClientOverlayLayer(OverlayGuiLayer layer) { super(layer); }
        @Override public ControlFormatting getControlFormatting() { return ControlFormatting.TRANSPARENT; }
        @Override public boolean hasGrayBackground() { return false; }
        @Override public boolean isExpandableX() { return true; }
        @Override public boolean isExpandableY() { return true; }
        @Override public void flowY(int width, int height, int preferred) {
            super.flowY(width, height, preferred);
            ((OverlayGuiLayer) control).positionControls(width, height);
        }
    }

    public static enum OverlayPosition {
        
        TOP_STRETCH {
            protected void positionControl(GuiControlRect control, int width, int height) {
                control.setX(0);
                control.setWidth(width, 0);
                control.setY(0);
            }
        },
        
        CENTER {
            protected void positionControl(GuiControlRect control, int width, int height) {
                control.setX(width / 2 - control.getWidth() / 2);
                control.setY(height / 2 - control.getHeight() / 2);
            }
        },
        ACTION_BAR {
            protected void positionControl(GuiControlRect control, int width, int height) {
                control.setX(width / 2 - control.getWidth() / 2);
                control.setY(height - control.getHeight() - 30);
            }
        };
        
        protected abstract void positionControl(GuiControlRect control, int width, int height);
    }
    
    public static record OverlayText(Vec2d pos, Component component, int color) {}
    
}
