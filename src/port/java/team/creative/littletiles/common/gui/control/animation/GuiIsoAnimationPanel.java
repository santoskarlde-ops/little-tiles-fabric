package team.creative.littletiles.common.gui.control.animation;

import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.simple.GuiButtonIcon;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.Icon;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.blueprint.GuiTreeItemStructure;
import team.creative.littletiles.common.math.box.LittleBox;

public class GuiIsoAnimationPanel extends GuiParent {
    
    public GuiIsoAnimationPanel(team.creative.creativecore.common.gui.IGuiParent parent, GuiTreeItemStructure item, LittleBox box, LittleGrid grid, boolean even) {
        super(parent);
        setFlow(GuiFlow.STACK_Y);
        setAlign(Align.STRETCH);
        GuiIsoAnimationViewer viewer = new GuiIsoAnimationViewer(this, "viewer", item, box, grid, even);
        add(viewer);
        GuiParent buttons = new GuiParent(this, GuiFlow.STACK_X).setAlign(Align.CENTER);
        add(buttons);
        buttons.add(new GuiButtonIcon(buttons, "reset", Icon.HOUSE, x -> viewer.resetView()));
        buttons.add(new GuiButtonIcon(buttons, "axis", Icon.COORDS, x -> viewer.nextAxis()));
        buttons.add(new GuiButtonIcon(buttons, "flip", Icon.MIRROR, x -> viewer.mirrorView()));
    }
    
    public GuiIsoAnimationPanel setVisibleAxis(boolean visible) {
        get("viewer", GuiIsoAnimationViewer.class).visibleAxis = visible;
        return this;
    }
    
    public GuiIsoAnimationPanel setViewerDim(int width, int height) {
        get("viewer", GuiIsoAnimationViewer.class).setDim(width, height);
        return this;
    }
    
}
