package team.creative.littletiles.common.gui.tool;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import team.creative.creativecore.common.gui.control.collection.GuiStackSelector;
import team.creative.creativecore.common.gui.control.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.control.simple.GuiButton;
import team.creative.creativecore.common.gui.control.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.control.simple.GuiColorPicker;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.dialog.DialogGuiLayer.DialogButton;
import team.creative.creativecore.common.gui.dialog.GuiDialogHandler;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiSizeRules;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionColorBoxes;
import team.creative.littletiles.common.action.LittleActionDestroyBoxes;
import team.creative.littletiles.common.action.LittleActionPlace;
import team.creative.littletiles.common.action.LittleActionPlace.PlaceAction;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.action.LittleActions;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.filter.TileFilters;
import team.creative.littletiles.common.gui.LittleGuiUtils;
import team.creative.littletiles.common.gui.control.filter.GuiElementFilter;
import team.creative.littletiles.common.level.LittleLevelScanner;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mode.PlacementMode;

public class GuiScrewdriver extends GuiConfigure {
    
    public static BiFilter<IParentCollection, LittleTile> lastSelectedFilter = TileFilters.and();
    public static ItemStack lastSelectedReplaceStack;
    
    protected GuiElementFilter filter;
    
    public GuiScrewdriver(boolean client, ContainerSlotView view) {
        super(client, "screwdriver", 200, 205, view);
        setFlow(GuiFlow.STACK_Y);
    }
    
    @Override
    public void create() {
        if (!isClient())
            return;
        
        //add(new GuiCheckBox("no_structure", lastSelectedFilter instanceof TileNoStructureFilter).setTranslate("gui.no_structure"));
        add(new GuiLabel(this, "filter_label").setTranslate("gui.filter"));
        add(filter = (GuiElementFilter) GuiElementFilter.ofGroup(this, getPlayer(), lastSelectedFilter).setExpandableX().setDim(new GuiSizeRules().prefHeight(100)));
        add(new GuiCheckBox(this, "remove", false).setTranslate("gui.remove"));
        
        add(new GuiCheckBox(this, "replace", false).setTranslate("gui.replace_with"));
        
        GuiStackSelector selector = new GuiStackSelector(this, "replacement", LittleGuiUtils.getCollector(getPlayer()), true);
        if (lastSelectedReplaceStack != null)
            selector.setSelectedForce(lastSelectedReplaceStack);
        add(selector.setExpandableX());
        
        Color color = new Color(255, 255, 255, 255);
        add(new GuiCheckBox(this, "colorize", false).setTranslate("gui.colorize"));
        
        add(new GuiColorPicker(this, "picker", color, LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG.getMinimumTransparency(getPlayer())).setExpandableX());
        
        GuiLeftRightBox actions = new GuiLeftRightBox(this).addLeft(new GuiButton(this, "undo", x -> {
            try {
                LittleTilesClient.ACTION_HANDLER.undo();
            } catch (LittleActionException e) {
                getPlayer().sendSystemMessage(Component.literal(e.getLocalizedMessage()));
            }
        }).setTranslate("gui.undo")).addLeft(new GuiButton(this, "redo", x -> {
            try {
                LittleTilesClient.ACTION_HANDLER.redo();
            } catch (LittleActionException e) {
                getPlayer().sendSystemMessage(Component.literal(e.getLocalizedMessage()));
            }
        }).setTranslate("gui.redo")).addRight(new GuiButton(this, "run", x -> {
            LittleAction action = getDesiredAction();
            if (action != null && action.wasSuccessful(LittleTilesClient.ACTION_HANDLER.execute(action))) {
                if (dist() != null)
                    dist().playSound(SoundEvents.LEVER_CLICK);
            }
        }).setTranslate("gui.run"));
        add(actions);
    }
    
    public LittleAction getDesiredAction() {
        BlockPos pos = this.tool.get().get(LittleTilesRegistry.FIRST_POS.value());
        if (pos == null)
            return null;
        
        BlockPos pos2 = this.tool.get().get(LittleTilesRegistry.SECOND_POS.value());
        if (pos2 == null)
            return null;
        
        BiFilter<IParentCollection, LittleTile> filter = this.filter.get();
        
        Level level = getPlayer().level();
        LittleBoxes boxes = LittleLevelScanner.scan(level, pos, pos2, filter);
        
        if (boxes.isEmpty())
            return null;
        
        boolean remove = get("remove", GuiCheckBox.class).get();
        boolean replace = get("replace", GuiCheckBox.class).get();
        boolean colorize = get("colorize", GuiCheckBox.class).get();
        
        if (remove)
            return new LittleActionDestroyBoxes(level, boxes);
        else {
            List<LittleAction> actions = new ArrayList<>();
            
            if (replace) {
                GuiStackSelector replacement = get("replacement");
                ItemStack stackReplace = replacement.getSelected();
                if (stackReplace != null) {
                    Block replacementBlock = Block.byItem(stackReplace.getItem());
                    if (!LittleAction.isBlockValid(replacementBlock.defaultBlockState())) {
                        GuiDialogHandler.openDialog(getIntegratedParent(), "screwdriver_dialog", Component.translatable("dialog.screwdriver.invalid_replacement"), (x, y) -> {},
                            DialogButton.OK);
                        return null;
                    }
                    actions.add(new LittleActionDestroyBoxes(level, boxes));
                    LittleGroupAbsolute previews = new LittleGroupAbsolute(pos);
                    previews.add(boxes.grid, new LittleElement(replacementBlock.defaultBlockState(), ColorUtils.WHITE), boxes);
                    
                    actions.add(new LittleActionDestroyBoxes(level, boxes));
                    actions.add(new LittleActionPlace(PlaceAction.ABSOLUTE, PlacementPreview.absolute(level, PlacementMode.FILL, previews)));
                }
            }
            
            if (colorize) {
                GuiColorPicker picker = get("picker");
                actions.add(new LittleActionColorBoxes(level, boxes, picker.color.toInt(), false));
            }
            
            if (!actions.isEmpty())
                return new LittleActions(actions.toArray(new LittleAction[0]));
        }
        
        if (!remove && !replace && !colorize)
            GuiDialogHandler.openDialog(getIntegratedParent(), "screwdriver_dialog", Component.translatable("dialog.screwdriver.no_task"), (x, y) -> {}, DialogButton.OK);
        
        return null;
    }
    
    @Override
    public boolean saveConfiguration(PatchedDataComponentMap data) {
        if (isClient()) {
            lastSelectedFilter = filter.get();
            lastSelectedReplaceStack = ((GuiStackSelector) get("replacement")).getSelected();
        }
        return false;
    }
}
