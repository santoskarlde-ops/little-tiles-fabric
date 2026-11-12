package team.creative.littletiles.common.gui.control.animation;

import team.creative.creativecore.common.gui.IGuiParent;
import team.creative.littletiles.common.gui.GuiCompat;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.simple.GuiButtonIcon;
import team.creative.creativecore.common.gui.control.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.control.tree.GuiTree;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.Icon;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.common.gui.tool.blueprint.GuiBlueprintAnimationHandler;

public class GuiAnimationPanel extends GuiParent {
    
    public final GuiTree tree;
    public final GuiAnimationViewerStorage storage;
    public final boolean options;
    public final GuiBlueprintAnimationHandler animation;
    
    public GuiAnimationPanel(IGuiParent parent, GuiTree tree, GuiAnimationViewerStorage storage, boolean options, GuiBlueprintAnimationHandler animation) {
        super(parent, "animation", GuiFlow.STACK_Y);
        setExpandable();
        
        this.tree = tree;
        this.storage = storage;
        this.options = options;
        this.animation = animation;
        
        GuiAnimationViewer viewer = new GuiAnimationViewer(this, "viewer", storage);
        add(viewer.setExpandable());
        
        GuiParent animationButtons = new GuiParent(this, GuiFlow.STACK_X).setAlign(Align.CENTER);
        add(animationButtons.setExpandableX());
        
        animationButtons.add(new GuiButtonIcon(animationButtons, "perspective", Icon.CAMERA, x -> viewer.nextProjection()).setTooltip(new TextBuilder().translate("gui.blueprint.perspective")
                .build()));
        animationButtons.add(new GuiButtonIcon(animationButtons, "home", Icon.HOUSE, x -> viewer.resetView()).setTooltip(new TextBuilder().translate("gui.blueprint.home").build()));
        if (animation != null) {
            animationButtons.add(new GuiButtonIcon(animationButtons, "play", Icon.PLAY, x -> animation.play()).setTooltip(new TextBuilder().translate("gui.blueprint.play").build()));
            animationButtons.add(new GuiButtonIcon(animationButtons, "pause", Icon.PAUSE, x -> animation.pause()).setTooltip(new TextBuilder().translate("gui.blueprint.pause").build()));
            animationButtons.add(new GuiButtonIcon(animationButtons, "stop", Icon.STOP, x -> animation.stop()).setTooltip(new TextBuilder().translate("gui.blueprint.stop").build()));
        }
        
        GuiParent checkboxes = new GuiParent(this, GuiFlow.FIT_X).setAlign(Align.CENTER);
        add(checkboxes.setExpandableX());
        
        if (options) {
            checkboxes.add(new GuiCheckBox(checkboxes, "filter", tree.hasCheckboxes()).setTranslate("gui.blueprint.view.filter").consumeChanged(x -> {
                tree.setCheckboxes(x, false);
                tree.updateTree();
            }));
            
            checkboxes.add(new GuiCheckBox(checkboxes, "highlight", storage.highlightSelected()).setTranslate("gui.blueprint.view.highlight").consumeChanged(x -> storage.highlightSelected(
                x)));
        }
    }
    
    public void refresh() {
        if (options) {
            GuiCompat.setSilent(get("filter", GuiCheckBox.class), tree.hasCheckboxes());
            GuiCompat.setSilent(get("highlight", GuiCheckBox.class), storage.highlightSelected());
        }
    }
    
}
