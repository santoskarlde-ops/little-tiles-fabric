package team.creative.littletiles.common.gui.tool;

import net.minecraft.core.component.PatchedDataComponentMap;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.common.grid.LittleGrid;

public abstract class GuiConfigureTool extends GuiConfigure {
    
    public GuiConfigureTool(boolean client, String name, int width, int height, ContainerSlotView tool) {
        super(client, name, width, height, tool);
    }
    
    public GuiConfigureTool(boolean client, String name, ContainerSlotView tool) {
        super(client, name, tool);
    }
    
    public LittleGrid getGrid() {
        return ((ILittleTool) tool.get().getItem()).getPositionGrid(getPlayer(), tool.get());
    }
    
    @Override
    protected boolean save(PatchedDataComponentMap data) {
        if (!(tool.get().getItem() instanceof ILittleTool))
            return false;
        return super.save(data);
    }
    
}
