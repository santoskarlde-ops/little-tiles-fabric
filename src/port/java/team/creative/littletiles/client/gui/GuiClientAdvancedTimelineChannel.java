package team.creative.littletiles.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import team.creative.creativecore.client.gui.control.timeline.GuiClientTimelineChannel;
import team.creative.creativecore.client.gui.registry.GuiClientRegistry;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.littletiles.common.structure.registry.gui.LittleDoorAdvancedGui.GuiAdvancedTimelineChannel;

@Environment(EnvType.CLIENT)
public final class GuiClientAdvancedTimelineChannel extends GuiClientTimelineChannel<GuiAdvancedTimelineChannel, Double> {
    public static void register() { GuiClientRegistry.register(GuiAdvancedTimelineChannel.class, GuiClientAdvancedTimelineChannel::new); }
    public GuiClientAdvancedTimelineChannel(GuiAdvancedTimelineChannel control) { super(control); }
    @Override protected void renderContent(GuiGraphicsExtractor graphics, Rect contentRect, Rect realRect, double scale, int mouseX, int mouseY) {
        control.updateFixedKeys();
        super.renderContent(graphics, contentRect, realRect, scale, mouseX, mouseY);
    }
}
