package team.creative.littletiles.common.gui.tool.blueprint;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.PatchedDataComponentMap;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.control.collection.GuiComboBox;
import team.creative.creativecore.common.gui.control.simple.GuiColorPicker;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.gui.control.GuiGridConfig;
import team.creative.littletiles.common.gui.tool.GuiConfigureTool;
import team.creative.littletiles.common.item.ItemLittleBlueprint;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.placement.setting.PlacementPlayerSetting;

public class GuiBlueprintSecondary extends GuiConfigureTool {
    
    public GuiBlueprintSecondary(boolean client, ContainerSlotView view) {
        super(client, "blueprint_secondary", 160, 240, view);
        setFlow(GuiFlow.STACK_Y);
        setAlign(Align.STRETCH);
        setSpacing(4);
        registerEventChanged(x -> {
            if (x.control.is("mode")) {
                GuiComboBox<PlacementMode> modeBox = (GuiComboBox<PlacementMode>) x.control;
                TextBuilder builder = new TextBuilder();
                if (modeBox.selected().canPlaceStructures())
                    builder.text("" + ChatFormatting.BOLD).translate("placement.mode.placestructure").text("" + ChatFormatting.WHITE).newLine();
                builder.translate(modeBox.selected().translatableKey() + ".tooltip");
                ((GuiLabel) get("text")).setTitle(builder.build());
                LittleTilesClient.placementMode(modeBox.selected());
            }
        });
    }
    
    @Override
    public boolean saveConfiguration(PatchedDataComponentMap data) {
        data.set(LittleTilesRegistry.COLOR.get(), get("picker", GuiColorPicker.class).color.toInt());
        data.set(LittleTilesRegistry.COLOR_SECONDARY.value(), get("picker2", GuiColorPicker.class).color.toInt());
        return true;
    }
    
    @Override
    public void create() {
        if (!isClient())
            return;
        add(new GuiLabel(this, "label").setTranslate("gui.blueprint.color.primary"));
        add(new GuiColorPicker(this, "picker", new Color(tool.get().getOrDefault(LittleTilesRegistry.COLOR.value(), ItemLittleBlueprint.DEFAULT_COLOR)), LittleTiles.CONFIG.isTransparencyEnabled(
            getPlayer()), LittleTiles.CONFIG.getMinimumTransparency(getPlayer())));
        
        add(new GuiLabel(this, "label").setTranslate("gui.blueprint.color.secondary"));
        add(new GuiColorPicker(this, "picker2", new Color(tool.get().getOrDefault(LittleTilesRegistry.COLOR_SECONDARY.value(), ItemLittleBlueprint.DEFAULT_COLOR_SECONDARY)), LittleTiles.CONFIG
                .isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG.getMinimumTransparency(getPlayer())));
        
        add(new GuiGridConfig(this, "grid", getPlayer(), PlacementPlayerSetting.grid(getPlayer()), LittleTilesClient::grid));
        
        GuiComboBox<PlacementMode> modeBox = new GuiComboBox<>(this, "mode", PlacementMode.map());
        modeBox.select(PlacementPlayerSetting.placementMode(getPlayer()));
        add(modeBox);
        add(new GuiLabel(this, "text"));
        raiseEvent(new GuiControlChangedEvent(modeBox));
    }
    
}
