package team.creative.littletiles.common.gui.control.animation;

import team.creative.creativecore.common.gui.IGuiParent;
import team.creative.littletiles.client.gui.GuiClientCompat;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.neoforged.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.neoforged.neoforge.client.ClientHooks;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.math.vec.SmoothValue;
import team.creative.creativecore.common.util.math.vec.Vec3d;

public class GuiAnimationViewer extends GuiControl {
    
    public SmoothValue offX = new SmoothValue(200);
    public SmoothValue offY = new SmoothValue(200);
    public SmoothValue offZ = new SmoothValue(200);
    
    public SmoothValue rotX = new SmoothValue(200);
    public SmoothValue rotY = new SmoothValue(200);
    public SmoothValue rotZ = new SmoothValue(200);
    public SmoothValue distance = new SmoothValue(200);
    
    public ViewerDragMode grabMode = ViewerDragMode.NONE;
    private ProjectionMode projection = ProjectionMode.SHOWCASE;
    public double grabX;
    public double grabY;
    public GuiAnimationViewerStorage storage;
    private boolean initialized = false;
    
    private boolean forward;
    private boolean backward;
    private boolean left;
    private boolean right;
    private boolean up;
    private boolean down;
    
    public GuiAnimationViewer(IGuiParent parent, String name, GuiAnimationViewerStorage storage) {
        super(parent, name);
        this.storage = storage;
    }
    public ControlFormatting getControlFormatting() {
        return ControlFormatting.NESTED_NO_PADDING;
    }
    public void mouseMoved(double x, double y) {
        if (grabMode == ViewerDragMode.NONE)
            return;
        
        switch (grabMode) {
            case LEFT -> {
                int scale = Math.min(GuiClientCompat.rect(this).getWidth(), GuiClientCompat.rect(this).getHeight());
                rotY.setStart(rotY.aimed() + (x - grabX) / scale * 180);
                rotX.setStart(rotX.aimed() + (y - grabY) / scale * 180);
            }
            default -> projection.dragMouse(this, x - grabX, y - grabY);
        }
        grabX = x;
        grabY = y;
    }
    public boolean mouseClicked(double x, double y, int button) {
        if (grabMode == ViewerDragMode.NONE) {
            grabMode = ViewerDragMode.of(button);
            grabX = x;
            grabY = y;
            return true;
        }
        return false;
    }
    public void mouseReleased(double x, double y, int button) {
        if (button == grabMode.ordinal() - 1)
            grabMode = ViewerDragMode.NONE;
    }
    public boolean mouseScrolled(double x, double y, double delta) {
        distance.set(Math.max(distance.aimed() + delta * -(Minecraft.getInstance().hasControlDown() ? 5 : 1), 0));
        return true;
    }
    
    public void nextProjection() {
        setProjection(projection.next());
    }
    
    public void setProjection(ProjectionMode mode) {
        this.projection = mode;
        initialized = false;
    }
    
    public static Matrix4f getProjectionMatrix(Minecraft mc, double fov, float width, float height) {
        return new Matrix4f().perspective((float) (fov * Math.PI / 180F), width / height, 0.05F, mc.gameRenderer.getDepthFar());
    }
    public boolean keyPressed(KeyEvent event) {
        Options options = Minecraft.getInstance().options;
        if (options.keyUp.matches(event))
            forward = true;
        if (options.keyDown.matches(event))
            backward = true;
        if (options.keyRight.matches(event))
            left = true;
        if (options.keyLeft.matches(event))
            right = true;
        if (options.keyJump.matches(event))
            up = true;
        if (options.keyShift.matches(event))
            down = true;
        return false;
    }
    public boolean keyReleased(KeyEvent event) {
        Options options = Minecraft.getInstance().options;
        if (options.keyUp.matches(event))
            forward = false;
        if (options.keyDown.matches(event))
            backward = false;
        if (options.keyRight.matches(event))
            left = false;
        if (options.keyLeft.matches(event))
            right = false;
        if (options.keyJump.matches(event))
            up = false;
        if (options.keyShift.matches(event))
            down = false;
        return false;
    }
    
    public void resetView() {
        offX.set(0);
        offY.set(0);
        offZ.set(0);
        rotX.set(0);
        rotY.set(0);
        rotZ.set(0);
        initialized = false;
    }
    @Environment(EnvType.CLIENT)
    protected void renderContent(GuiGraphicsExtractor graphics, Rect controlRect, Rect realRect, double controlScale, int mouseX, int mouseY) {
        if (!storage.isReady())
            return;
        
        if (!initialized) {
            resetView();
            projection.init(this);
            initialized = true;
        }
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        
        rotX.tick();
        rotY.tick();
        rotZ.tick();
        distance.tick();
        offX.tick();
        offY.tick();
        offZ.tick();
        
        float amount = mc.getDeltaTracker().getGameTimeDeltaTicks() * 2;
        if (Minecraft.getInstance().hasControlDown())
            amount *= 4;
        if (forward)
            projection.forward(amount, this);
        if (backward)
            projection.forward(-amount, this);
        if (left)
            projection.lateral(amount, this);
        if (right)
            projection.lateral(-amount, this);
        if (up)
            projection.up(amount, this);
        if (down)
            projection.up(-amount, this);
        
        var view = RenderSystem.getModelViewStack();
        view.pushMatrix();
        view.identity();
        RenderSystem.applyModelViewMatrix();
        
        PoseStack pose = new PoseStack();
        
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        double scale = window.getGuiScale();
        int height = (int) (realRect.getHeight() * scale);
        RenderSystem.viewport((int) (realRect.minX * scale), (int) (window.getHeight() - realRect.minY * scale - height), (int) (realRect.getWidth() * scale), height);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        Matrix4f projection = getProjectionMatrix(mc, 70, GuiClientCompat.rect(this).getContentWidth(), GuiClientCompat.rect(this).getContentHeight());
        RenderSystem.setProjectionMatrix(projection, VertexSorting.DISTANCE_TO_ORIGIN);
        
        RenderSystem.enableDepthTest();
        
        Vec3d center = storage.center();
        this.projection.prepareRendering(pose, center, this);
        
        storage.renderAll(pose, projection, mc);
        
        RenderSystem.viewport(0, 0, window.getWidth(), window.getHeight());
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        
        RenderSystem.setProjectionMatrix(new Matrix4f().ortho(0.0F, (float) (window.getWidth() / window.getGuiScale()), (float) (window.getHeight() / window.getGuiScale()), 0.0F,
            1000.0F, ClientHooks.getGuiFarPlane()), VertexSorting.ORTHOGRAPHIC_Z);
        
        Lighting.setupFor3DItems();
        RenderSystem.disableDepthTest();
        view.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }
    @Environment(EnvType.CLIENT)
    protected void renderContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}
    public void closed() {}
    public void init() {}
    public void tick() {}
    public void flowX(int width, int preferred) {}
    public void flowY(int width, int height, int preferred) {}
    protected int preferredWidth(int availableWidth) {
        return 10;
    }
    protected int preferredHeight(int width, int availableHeight) {
        return 10;
    }
    
    protected static enum ViewerDragMode {
        
        NONE,
        LEFT,
        RIGHT,
        MIDDLE;
        
        public static ViewerDragMode of(int button) {
            return switch (button) {
                case 0 -> LEFT;
                case 1 -> RIGHT;
                case 2 -> MIDDLE;
                default -> NONE;
            };
        }
        
    }
    
    public static enum ProjectionMode {
        
        SHOWCASE {
            public void init(GuiAnimationViewer viewer) {
                viewer.distance.set(viewer.storage.longestSide() / 2D + 2);
            }
            public void dragMouse(GuiAnimationViewer viewer, double x, double y) {
                double grabOffset = 0.01;
                switch (viewer.grabMode) {
                    case RIGHT -> {
                        Vector3f offset = new Vector3f((float) (x * grabOffset), 0, (float) (y * grabOffset));
                        apply(offset, viewer);
                    }
                    case MIDDLE -> {
                        Vector3f offset = new Vector3f((float) (x * grabOffset), (float) (y * -grabOffset), 0);
                        apply(offset, viewer);
                    }
                }
            }
            public void forward(float amount, GuiAnimationViewer viewer) {
                viewer.rotX.add(amount);
            }
            public void lateral(float amount, GuiAnimationViewer viewer) {
                viewer.rotY.add(-amount);
            }
            
            public void apply(Vector3f vec, GuiAnimationViewer viewer) {
                vec.rotate(Axis.XP.rotationDegrees((float) viewer.rotX.current()));
                vec.rotate(Axis.YP.rotationDegrees((float) viewer.rotY.current()));
                vec.rotate(Axis.ZP.rotationDegrees((float) viewer.rotZ.current()));
                viewer.offX.set(viewer.offX.aimed() + vec.x);
                viewer.offY.set(viewer.offY.aimed() + vec.y);
                viewer.offZ.set(viewer.offZ.aimed() + vec.z);
            }
            public void prepareRendering(PoseStack pose, Vec3d center, GuiAnimationViewer viewer) {
                
                pose.translate(viewer.offX.current(), viewer.offY.current(), viewer.offZ.current() - viewer.distance.current());
                
                pose.mulPose(new org.joml.Matrix4f().rotation(Axis.XP.rotationDegrees((float) viewer.rotX.current())));
                pose.mulPose(new org.joml.Matrix4f().rotation(Axis.YP.rotationDegrees((float) viewer.rotY.current())));
                pose.mulPose(new org.joml.Matrix4f().rotation(Axis.ZP.rotationDegrees((float) viewer.rotZ.current())));
                
                pose.translate(-center.x, -center.y, -center.z);
            }
            public ProjectionMode next() {
                return PLAYER;
            }
            public void up(float amount, GuiAnimationViewer viewer) {
                viewer.offZ.add(amount * 0.05F);
            }
        },
        PLAYER {
            public void init(GuiAnimationViewer viewer) {
                viewer.offZ.set(viewer.storage.longestSide() / 2D);
                viewer.offY.set(-viewer.storage.overall().getSize());
                viewer.rotX.set(45);
                viewer.rotY.set(180);
            }
            public void dragMouse(GuiAnimationViewer viewer, double x, double y) {}
            public void forward(float amount, GuiAnimationViewer viewer) {
                amount *= 0.1;
                viewer.offX.add(amount * (Math.sin(-viewer.rotY.aimed() * Math.PI / 180)));
                viewer.offZ.add(amount * (Math.cos(viewer.rotY.aimed() * Math.PI / 180)));
            }
            public void lateral(float amount, GuiAnimationViewer viewer) {
                amount *= -0.1;
                viewer.offX.add(amount * (Math.cos(-viewer.rotY.aimed() * Math.PI / 180)));
                viewer.offZ.add(amount * (Math.sin(viewer.rotY.aimed() * Math.PI / 180)));
            }
            public void prepareRendering(PoseStack pose, Vec3d center, GuiAnimationViewer viewer) {
                pose.mulPose(new org.joml.Matrix4f().rotation(Axis.XP.rotationDegrees((float) viewer.rotX.current())));
                pose.mulPose(new org.joml.Matrix4f().rotation(Axis.YP.rotationDegrees((float) viewer.rotY.current())));
                pose.mulPose(new org.joml.Matrix4f().rotation(Axis.ZP.rotationDegrees((float) viewer.rotZ.current())));
                
                pose.translate(viewer.offX.current(), viewer.offY.current(), viewer.offZ.current());
                pose.translate(-center.x, -center.y, -center.z);
                
            }
            public ProjectionMode next() {
                return SHOWCASE;
            }
            public void up(float amount, GuiAnimationViewer viewer) {
                viewer.offY.add(amount * -0.1);
            }
        };
        
        public abstract ProjectionMode next();
        
        public abstract void init(GuiAnimationViewer viewer);
        
        public abstract void prepareRendering(PoseStack pose, Vec3d center, GuiAnimationViewer viewer);
        
        public abstract void dragMouse(GuiAnimationViewer viewer, double x, double y);
        
        public abstract void forward(float amount, GuiAnimationViewer viewer);
        
        public abstract void lateral(float amount, GuiAnimationViewer viewer);
        
        public abstract void up(float amount, GuiAnimationViewer viewer);
        
    }
}