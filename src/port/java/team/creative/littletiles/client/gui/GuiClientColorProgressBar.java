package team.creative.littletiles.client.gui;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import team.creative.creativecore.client.gui.control.simple.GuiClientProgressbar;
import team.creative.creativecore.client.gui.registry.GuiClientRegistry;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.littletiles.common.gui.control.GuiColorProgressBar;

@Environment(EnvType.CLIENT)
public class GuiClientColorProgressBar extends GuiClientProgressbar<GuiColorProgressBar> {

    public static void register() {
        GuiClientRegistry.register(GuiColorProgressBar.class, GuiClientColorProgressBar::new);
    }

    public GuiClientColorProgressBar(GuiColorProgressBar control) {
        super(control);
    }

    @Override
    protected ControlFormatting defaultFormatting() {
        return ControlFormatting.NESTED_NO_PADDING;
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(Component.translatable("gui.color.rightclick"));
    }

    @Override
    public boolean mouseClicked(double x, double y, MouseButtonInfo info) {
        if (info.button() == 1) {
            playSound(SoundEvents.UI_BUTTON_CLICK);
            return true;
        }
        return false;
    }

    @Override
    protected void renderProgress(GuiGraphicsExtractor graphics, double percent) {
        graphics.fill(0, 0, (int) (rect.getContentWidth() * percent), rect.getContentHeight(), control.color.toInt());
    }
}
