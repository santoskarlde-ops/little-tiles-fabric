package team.creative.littletiles.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import team.creative.creativecore.client.gui.GuiClientControl;
import team.creative.creativecore.client.gui.registry.GuiClientRegistry;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.littletiles.common.gui.control.GuiActionDisplay;
import team.creative.littletiles.common.gui.control.GuiActionDisplay.ActionMessage;

@Environment(EnvType.CLIENT)
public final class GuiClientActionDisplay extends GuiClientControl<GuiActionDisplay> {
    public static void register() {
        GuiClientRegistry.register(GuiActionDisplay.class, GuiClientActionDisplay::new);
    }
    public GuiClientActionDisplay(GuiActionDisplay control) { super(control); }
    @Override protected ControlFormatting defaultFormatting() { return ControlFormatting.TRANSPARENT; }
    @Override protected void flowX(int width, int preferred) { control.flowX(width, preferred); }
    @Override protected void flowY(int width, int height, int preferred) {}
    @Override protected int minWidth(int available) { return 10; }
    @Override protected int minHeight(int width, int available) { return Minecraft.getInstance().font.lineHeight; }
    @Override protected int preferredWidth(int available) { return control.preferredWidth(available); }
    @Override protected int preferredHeight(int width, int available) { return control.preferredHeight(width, available); }
    @Override protected void renderContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        var pose = graphics.pose();
        pose.pushMatrix();
        int i = 0;
        while (i < control.messages.size()) {
            ActionMessage message = control.messages.get(i);
            long elapsed = System.currentTimeMillis() - message.timestamp;
            if (elapsed >= control.totalTime()) {
                control.removeMessage(i);
            } else {
                float alpha = elapsed > control.displayTime()
                    ? 1 - Math.max(0F, elapsed - control.displayTime()) / control.fadeTime() : 1;
                int opacity = Math.round(Math.max(0.05F, Math.min(1F, alpha)) * 255);
                message.setDefaultColor((opacity << 24) | 0xFFFFFF);
                message.render(graphics);
                pose.translate(0, message.getTotalHeight());
                i++;
            }
        }
        pose.popMatrix();
    }
}
