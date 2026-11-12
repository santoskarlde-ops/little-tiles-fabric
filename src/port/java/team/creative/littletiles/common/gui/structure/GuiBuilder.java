package team.creative.littletiles.common.gui.structure;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.control.collection.GuiComboBox;
import team.creative.creativecore.common.gui.control.collection.GuiStackSelector;
import team.creative.creativecore.common.gui.control.inventory.GuiInventoryGrid;
import team.creative.creativecore.common.gui.control.inventory.GuiPlayerInventoryGrid;
import team.creative.creativecore.common.gui.control.parent.GuiColumn;
import team.creative.creativecore.common.gui.control.parent.GuiLabeledControl;
import team.creative.creativecore.common.gui.control.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.control.parent.GuiRow;
import team.creative.creativecore.common.gui.control.parent.GuiTable;
import team.creative.creativecore.common.gui.control.simple.GuiButton;
import team.creative.creativecore.common.gui.control.simple.GuiCounter;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.control.simple.GuiStateButton;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.action.source.LittleActionSource;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.LittleGuiUtils;
import team.creative.littletiles.common.item.ItemLittleBlueprint;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder.LittleStructureBuilderType;

public class GuiBuilder extends GuiLayer {
    
    public LittleStructureBuilder builder;
    public final GuiSyncLocal<CompoundTag> CRAFT = getSyncHolder().register("craft", nbt -> {
        if ((getPlayer().isCreative() && builder.inventory.getItem(0).isEmpty()) || builder.inventory.getItem(0).getItem() instanceof ItemLittleBlueprint) {
            int width = nbt.getInt("width").orElse(0);
            int height = nbt.getInt("height").orElse(0);
            int thickness = nbt.getInt("thickness").orElse(0);
            Block block = BuiltInRegistries.BLOCK.get(Identifier.parse(nbt.getString("block").orElse(""))).orElseThrow().value();
            LittleGrid grid = LittleGrid.get(nbt.getInt("grid").orElse(0));
            
            builder.lastBlockState = block.defaultBlockState();
            builder.lastSizeX = width;
            builder.lastSizeY = height;
            builder.lastThickness = thickness;
            builder.lastGrid = grid.count;
            builder.lastStructureType = nbt.getString("type").orElse("");
            builder.updateStructure();
            
            LittleStructureBuilderType type = LittleStructureBuilder.REGISTRY.get(builder.lastStructureType);
            if (type != null) {
                ItemStack stack = builder.inventory.getItem(0);
                if (!((LittleActionSource) getPlayer()).needsIngredients() && stack.isEmpty()) {
                    stack = new ItemStack(LittleTilesRegistry.BLUEPRINT.value());
                    builder.inventory.setItem(0, stack);
                }
                if (stack.getItem() instanceof ItemLittleBlueprint blue)
                    blue.saveTiles(stack, type.construct(grid, width, height, thickness, block.defaultBlockState()));
            }
        }
    });
    
    public GuiBuilder(boolean client, LittleStructureBuilder builder) {
        super(client, "structure_builder", 200, 200);
        this.builder = builder;
    }
    
    @Override
    public void create() {
        setFlow(GuiFlow.STACK_Y);
        GuiTable table = new GuiTable(this);
        add(table.setExpandableX());
        GuiRow row = new GuiRow(this);
        table.addRow(row);
        GuiColumn col = new GuiColumn(this);
        row.addColumn(col);
        col.add(new GuiLabeledControl(col, "gui.structure_builder.width", new GuiCounter(col, "width", builder.lastSizeX, 1, Integer.MAX_VALUE)));
        
        col = new GuiColumn(this);
        row.addColumn(col);
        col.add(new GuiLabeledControl(col, "gui.structure_builder.height", new GuiCounter(col, "height", builder.lastSizeX, 1, Integer.MAX_VALUE)));
        
        row = new GuiRow(this);
        table.addRow(row);
        col = new GuiColumn(this);
        row.addColumn(col);
        col.add(new GuiLabeledControl(col, "gui.structure_builder.thickness", new GuiCounter(col, "thickness", builder.lastThickness, 1, Integer.MAX_VALUE)));
        
        var gridSelect = new GuiStateButton<LittleGrid>(this, "grid", LittleGrid.mapBuilder());
        gridSelect.select(LittleTiles.CONFIG.build.get(getPlayer()).defaultGrid());
        col = new GuiColumn(this);
        row.addColumn(col);
        col.add(new GuiLabeledControl(col, "gui.grid", gridSelect));
        
        GuiComboBox<LittleStructureBuilderType> box = new GuiComboBox<>(this, "type", new TextMapBuilder<LittleStructureBuilderType>().addEntrySet(LittleStructureBuilder.REGISTRY
                .entrySet(), x -> Component.translatable("structure." + x.getKey())));
        box.select(LittleStructureBuilder.REGISTRY.get(builder.lastStructureType));
        add(box.setExpandableX());
        
        GuiStackSelector selector = new GuiStackSelector(this, "preview", LittleGuiUtils.getCollector(getPlayer()), true);
        selector.setSelectedForce(new ItemStack(builder.lastBlockState.getBlock()));
        add(selector.setExpandableX());
        
        add(new GuiLeftRightBox(this).addLeft(new GuiLabel(this, "failed").setTranslate("gui.structure_builder.failed").setVisible(false)).addRight(new GuiButton(this, "craft", x -> {
            if ((getPlayer().isCreative() && builder.inventory.getItem(0).isEmpty()) || builder.inventory.getItem(0).getItem() instanceof ItemLittleBlueprint) {
                get("failed").setVisible(false);
                CompoundTag nbt = new CompoundTag();
                GuiCounter width = (GuiCounter) get("width");
                nbt.putInt("width", width.getValue());
                GuiCounter height = (GuiCounter) get("height");
                nbt.putInt("height", height.getValue());
                GuiCounter thickness = (GuiCounter) get("thickness");
                nbt.putInt("thickness", thickness.getValue());
                GuiComboBox<LittleStructureBuilderType> type = (GuiComboBox<LittleStructureBuilderType>) get("type");
                nbt.putString("type", type.selected().type.id);
                GuiStateButton<LittleGrid> gridButton = (GuiStateButton<LittleGrid>) get("grid");
                LittleGrid grid = gridButton.selected();
                nbt.putInt("grid", grid.count);
                ItemStack stack = selector.getSelected();
                Block block = Block.byItem(stack.getItem());
                nbt.putString("block", block.builtInRegistryHolder().key().identifier().toString());
                CRAFT.send(nbt);
            } else
                get("failed").setVisible(true);
            
        }).setTranslate("gui.structure_builder.craft")));
        
        add(new GuiInventoryGrid(this, "builder", builder.inventory));
        add(new GuiPlayerInventoryGrid(this));
    }
    
}
