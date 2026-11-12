package team.creative.littletiles.common.gui.tool.blueprint.test;

import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.control.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.control.parent.GuiPanel;
import team.creative.creativecore.common.gui.control.parent.GuiScrollY;
import team.creative.creativecore.common.gui.control.simple.GuiButton;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.littletiles.common.gui.tool.blueprint.GuiBlueprint;

public class GuiBlueprintTest extends GuiLayer {
    
    public BlueprintTestResults results;
    public GuiBlueprint blueprint;
    
    public GuiBlueprintTest(boolean client) {
        super(client, "gui.blueprint.test", 300, 200);
        setFlow(GuiFlow.STACK_Y);
    }
    
    public void init(GuiBlueprint blueprint) {
        this.blueprint = blueprint;
        this.results = blueprint.runTest();
        clear();
        init();
    }
    
    @Override
    public void becameTopLayer() {
        if (blueprint != null)
            init(blueprint);
    }
    
    @Override
    public void create() {
        if (results == null)
            return;
        
        GuiScrollY box = new GuiScrollY(this);
        add(box.setExpandable());
        
        for (BlueprintTestError error : results) {
            GuiPanel content = new GuiPanel(this, GuiFlow.STACK_Y);
            box.add(content);
            content.add(new GuiLabel(content, "header").setTitle(error.header()));
            content.add(new GuiLabel(content, "desc").setTitle(error.description()));
            GuiParent bottomLine = new GuiParent(this, GuiFlow.STACK_X).setAlign(Align.CENTER).setVAlign(VAlign.CENTER);
            content.add(bottomLine.setExpandableX());
            error.create(blueprint, bottomLine, () -> init(blueprint));
        }
        
        GuiLeftRightBox bottom = new GuiLeftRightBox(this);
        add(bottom);
        bottom.addLeft(new GuiButton(bottom, "refresh", x -> init(blueprint)).setTranslate("gui.blueprint.test.recheck"));
        bottom.addRight(new GuiButton(bottom, "okay", x -> closeThisLayer()).setTranslate("gui.okay"));
    }
    
}
