package team.creative.littletiles.common.gui.control;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import team.creative.creativecore.common.gui.IGuiParent;
import team.creative.creativecore.common.gui.control.simple.GuiProgressbar;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.client.gui.GuiClientColorProgressBar;

public class GuiColorProgressBar extends GuiProgressbar {

    static {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
            GuiClientColorProgressBar.register();
    }

    public Color color;

    public GuiColorProgressBar(IGuiParent parent, String name, double pos, double max, Color color) {
        super(parent, name, pos, max);
        this.color = color;
    }
}
