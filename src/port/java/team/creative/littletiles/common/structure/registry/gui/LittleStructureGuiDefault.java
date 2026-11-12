package team.creative.littletiles.common.structure.registry.gui;

import net.neoforged.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import team.creative.littletiles.common.gui.tool.blueprint.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;

@Environment(EnvType.CLIENT)
public class LittleStructureGuiDefault extends LittleStructureGuiControl {
    
    public LittleStructureGuiDefault(LittleStructureGui type, GuiTreeItemStructure item) {
        super(type, item);
    }
    
    @Override
    public void create(LittleStructure structure) {}
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        return structure;
    }
    
}