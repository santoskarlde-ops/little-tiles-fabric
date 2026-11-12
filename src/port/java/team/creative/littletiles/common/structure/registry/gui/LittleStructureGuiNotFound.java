package team.creative.littletiles.common.structure.registry.gui;

import net.neoforged.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import team.creative.littletiles.common.gui.tool.blueprint.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;

@Environment(EnvType.CLIENT)
public class LittleStructureGuiNotFound extends LittleStructureGuiControl {
    
    public LittleStructureGuiNotFound(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
    }
    
    @Override
    public void create(LittleStructure structure) {}
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        if (item.group.getStructureTag() != null)
            structure.load(item.group.getStructureTag(), item.provider());
        return structure;
    }
    
}