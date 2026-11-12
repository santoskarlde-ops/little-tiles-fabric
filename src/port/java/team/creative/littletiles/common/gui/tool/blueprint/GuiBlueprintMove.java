package team.creative.littletiles.common.gui.tool.blueprint;

import team.creative.creativecore.common.gui.IGuiParent;
import java.util.Collections;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.control.simple.GuiButton;
import team.creative.creativecore.common.gui.control.simple.GuiStateButton;
import team.creative.creativecore.common.gui.control.tree.GuiTree;
import team.creative.creativecore.common.gui.control.tree.GuiTreeItem;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiSizeRatioRules;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.creativecore.common.util.type.itr.FilterIterator;
import team.creative.creativecore.common.util.type.itr.SingleIterator;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.control.GuiDistanceControl;
import team.creative.littletiles.common.gui.control.animation.GuiAnimationPanel;
import team.creative.littletiles.common.gui.tool.blueprint.test.BlueprintTest;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;

public class GuiBlueprintMove extends GuiLayer {
    
    public GuiBlueprint blueprint;
    public GuiTree tree;
    
    public GuiBlueprintMove(boolean client) {
        super(client, "gui.blueprint.move", 400, 200);
        setFlow(GuiFlow.STACK_Y);
        registerEventChanged(x -> {
            if (x.control.is("modes"))
                ((GuiStateButton<GuiBlueprintMoveMode>) x.control).selected().select(tree);
        });
    }
    
    public void init(GuiBlueprint blueprint) {
        this.blueprint = blueprint;
        clear();
        init();
    }
    
    @Override
    public void closed() {
        if (tree == null)
            return;
        for (GuiTreeItem item : tree.allItems())
            if (item instanceof GuiBlueprintMoveItem move)
                move.structure.resetOffset();
    }
    
    @Override
    public void create() {
        if (blueprint == null)
            return;
        
        GuiParent upper = new GuiParent(this, GuiFlow.STACK_X);
        add(upper);
        
        GuiParent sidebar = new GuiParent(this, GuiFlow.STACK_Y);
        upper.add(sidebar.setDim(new GuiSizeRatioRules().widthRatio(0.3F).maxWidth(100)).setExpandableY());
        
        tree = new GuiTree(this, "tree").setKeepOrder(true).setRootVisibility(false);
        sidebar.add(tree.setExpandable());
        
        for (GuiTreeItem item : blueprint.tree.root().items()) {
            GuiBlueprintMoveItem child = new GuiBlueprintMoveItem(tree, (GuiTreeItemStructure) item);
            tree.root().addItem(child);
            child.addChildren();
        }
        
        GuiStateButton<GuiBlueprintMoveMode> modes = new GuiStateButton<>(sidebar, "modes", new TextMapBuilder<GuiBlueprintMoveMode>().addComponent(GuiBlueprintMoveMode.values(), x -> x
                .title()));
        sidebar.add(modes.setExpandableX());
        
        GuiParent config = new GuiParent(this, GuiFlow.STACK_Y).setAlign(Align.CENTER);
        upper.add(config.setDim(new GuiSizeRatioRules().widthRatio(0.3F)));
        
        config.add(new GuiDistanceControl(config, "distance", LittleGrid.overallDefault(), 1));
        
        GuiParent row1 = new GuiParent(this);
        config.add(row1);
        row1.add(new GuiBlueprintMoveButton(row1, Facing.EAST));
        row1.add(new GuiBlueprintMoveButton(row1, Facing.UP));
        row1.add(new GuiBlueprintMoveButton(row1, Facing.SOUTH));
        
        GuiParent row2 = new GuiParent(this);
        config.add(row2);
        row2.add(new GuiBlueprintMoveButton(row2, Facing.WEST));
        row2.add(new GuiBlueprintMoveButton(row2, Facing.DOWN));
        row2.add(new GuiBlueprintMoveButton(row2, Facing.NORTH));
        
        upper.add(new GuiAnimationPanel(upper, blueprint.tree, blueprint.storage, true, null));
        
        GuiLeftRightBox bottom = new GuiLeftRightBox(this);
        add(bottom);
        bottom.addLeft(new GuiButton(bottom, "cancel", x -> closeThisLayer()).setTranslate("gui.cancel"));
        bottom.addRight(new GuiButton(bottom, "test", x -> {
            blueprint.storage.resetOverlap();
            BlueprintTest.testModule(blueprint, BlueprintTest.OVERLAP_TEST);
        }).setTranslate("gui.blueprint.test.overlap"));
        bottom.addRight(new GuiButton(bottom, "save", x -> {
            for (GuiTreeItem item : tree.allItems())
                if (item instanceof GuiBlueprintMoveItem move)
                    move.structure.applyOffset();
            closeThisLayer();
            blueprint.reloadItem();
        }).setTranslate("gui.save"));
        
        tree.updateTree();
    }
    
    public void move(Facing facing) {
        GuiDistanceControl distance = get("distance");
        GuiStateButton<GuiBlueprintMoveMode> modes = get("modes");
        LittleVec direction = new LittleVec(facing);
        direction.scale(distance.getDistance());
        LittleVecGrid vec = new LittleVecGrid(direction, distance.getDistanceGrid());
        for (GuiBlueprintMoveItem item : modes.selected().iterator(tree))
            item.addOffset(vec);
    }
    
    public class GuiBlueprintMoveButton extends GuiButton {
        
        public GuiBlueprintMoveButton(IGuiParent parent, Facing facing) {
            super(parent, facing.name, x -> move(facing));
            setTitle(facing.translate());
        }
        
    }
    
    public enum GuiBlueprintMoveMode {
        
        DEFAULT(false) {
            
            @Override
            public Iterable<GuiBlueprintMoveItem> iterator(GuiTree tree) {
                if (tree.selected() == null)
                    return Collections.EMPTY_LIST;
                return new SingleIterator<>((GuiBlueprintMoveItem) tree.selected());
            }
        },
        CHECKBOX(true) {
            @Override
            public Iterable<GuiBlueprintMoveItem> iterator(GuiTree tree) {
                return new FilterIterator<>(tree.allItems(), x -> x instanceof GuiBlueprintMoveItem && x.isChecked());
            }
        };
        
        public final boolean checkboxes;
        
        private GuiBlueprintMoveMode(boolean checkboxes) {
            this.checkboxes = checkboxes;
        }
        
        public Component title() {
            return Component.translatable("gui.blueprint.move.mode." + name().toLowerCase());
        }
        
        public void select(GuiTree tree) {
            tree.setCheckboxes(checkboxes, false);
            tree.updateTree();
        }
        
        public abstract Iterable<GuiBlueprintMoveItem> iterator(GuiTree tree);
    }
    
    public class GuiBlueprintMoveItem extends GuiTreeItem {
        
        public final GuiTreeItemStructure structure;
        
        public GuiBlueprintMoveItem(GuiTree tree, GuiTreeItemStructure structure) {
            super(tree, "item", tree);
            this.structure = structure;
            setTitle(Component.literal(structure.getTitle()));
        }
        
        public void addOffset(LittleVecGrid vec) {
            LittleVecGrid offset = structure.getOffset();
            if (offset == null)
                structure.setOffset(vec.copy());
            else
                offset.add(vec);
        }
        
        public void addChildren() {
            for (GuiTreeItem item : structure.items()) {
                GuiBlueprintMoveItem child = new GuiBlueprintMoveItem(tree, (GuiTreeItemStructure) item);
                addItem(child);
                child.addChildren();
            }
        }
        
    }
    
}
