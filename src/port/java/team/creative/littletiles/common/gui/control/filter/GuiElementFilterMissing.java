package team.creative.littletiles.common.gui.control.filter;

import team.creative.creativecore.common.gui.IGuiParent;
import team.creative.creativecore.common.gui.control.collection.GuiStackSelector;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.filter.TileFilters;

public class GuiElementFilterMissing extends GuiElementFilter {
    
    protected GuiStackSelector selector;
    
    public GuiElementFilterMissing(IGuiParent parent) {
        super(parent);
        add(new GuiLabel(this, "missing").setTranslate("gui.filter.missing"));
    }
    
    @Override
    public BiFilter<IParentCollection, LittleTile> get() {
        return TileFilters.missing();
    }
    
}
