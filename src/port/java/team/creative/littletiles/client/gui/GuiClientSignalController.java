package team.creative.littletiles.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonInfo;
import team.creative.creativecore.client.gui.GuiClientControl;
import team.creative.creativecore.client.gui.GuiClientParent;
import team.creative.creativecore.client.gui.registry.GuiClientRegistry;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.gui.signal.GuiSignalConnection;
import team.creative.littletiles.common.gui.signal.GuiSignalController;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNode;

/** Client rendering and input for the signal controller's real grid controls. */
@Environment(EnvType.CLIENT)
public final class GuiClientSignalController extends GuiClientParent<GuiSignalController> {
    private Rect controllerRect;

    public static void register() {
        GuiClientRegistry.register(GuiSignalController.class, GuiClientSignalController::new);
    }

    public GuiClientSignalController(GuiSignalController control) {
        super(control);
    }

    @Override protected ControlFormatting defaultFormatting() { return ControlFormatting.NESTED_NO_PADDING; }
    @Override public double getOffsetX() { return -control.scrolledX.current(); }
    @Override public double getOffsetY() { return -control.scrolledY.current(); }
    @Override public void flowX(int width, int preferred) { control.flowX(width, preferred); }
    @Override public void flowY(int width, int height, int preferred) { control.flowY(width, height, preferred); }

    @Override
    protected void renderContent(GuiGraphicsExtractor graphics, Rect contentRect, Rect realContentRect,
            double scale, int mouseX, int mouseY) {
        if (realContentRect == null) return;
        control.scrolledX.tick();
        control.scrolledY.tick();
        control.zoom.tick();
        setScale(control.zoom.current());
        controllerRect = realContentRect;
        super.renderContent(graphics, contentRect, realContentRect, scale, mouseX, mouseY);
        controllerRect = null;
    }

    @Override
    protected void renderControl(GuiGraphicsExtractor graphics, GuiClientControl child, Rect childRect,
            Rect realRect, double scale, int mouseX, int mouseY, boolean hover) {
        if (child.control instanceof GuiSignalNode node && controllerRect != null && realRect != null) {
            scissor(graphics, controllerRect);
            if (node.hasUnderline()) {
                var font = Minecraft.getInstance().font;
                String text = node.getUnderline();
                graphics.text(font, text, child.rect.getWidth() / 2 - font.width(text) / 2,
                    child.rect.getHeight() + 4, ColorUtils.WHITE);
            }
            boolean highlighted = realRect.inside(mouseX, mouseY);
            float originX = child.rect.getX() - getContentOffset();
            float originY = child.rect.getY() - getContentOffset();
            for (GuiSignalConnection connection : node.toConnections()) {
                var other = connection.from();
                if (!highlighted)
                    highlighted = other.toScreenRect(new Rect(0, 0, GuiClientCompat.rect(other).getWidth(),
                        GuiClientCompat.rect(other).getHeight())).inside(mouseX, mouseY);
                float x1 = connection.fromX() - originX, y1 = connection.fromY() - originY;
                float x2 = connection.toX() - originX, y2 = connection.toY() - originY;
                float length = (float) Math.hypot(x2 - x1, y2 - y1);
                if (length > 0) {
                    var pose = graphics.pose();
                    pose.pushMatrix();
                    pose.translate(x1, y1);
                    pose.rotate((float) Math.atan2(y2 - y1, x2 - x1));
                    graphics.fill(0, -1, (int) Math.ceil(length), 1, highlighted ? ColorUtils.WHITE : ColorUtils.BLACK);
                    pose.popMatrix();
                }
            }
            scissor(graphics, realRect);
        }
        super.renderControl(graphics, child, childRect, realRect, scale, mouseX, mouseY, hover);
    }

    @Override public boolean mouseClicked(double x, double y, MouseButtonInfo info) {
        return super.mouseClicked(x, y, info) || control.mouseClicked(x, y, info.button());
    }
    @Override public boolean mouseScrolled(double x, double y, double delta) {
        return super.mouseScrolled(x, y, delta) || control.mouseScrolled(x, y, delta);
    }
    @Override public void mouseDragged(double x, double y, MouseButtonInfo info, double dx, double dy, double time) {
        super.mouseDragged(x, y, info, dx, dy, time);
        control.mouseDragged(x, y, info.button(), dx, dy, time);
    }
    @Override public void mouseMoved(double x, double y) {
        super.mouseMoved(x, y);
        control.mouseMoved(x, y);
    }
    @Override public void mouseReleased(double x, double y, MouseButtonInfo info) {
        super.mouseReleased(x, y, info);
        control.mouseReleased(x, y, info.button());
    }
}
