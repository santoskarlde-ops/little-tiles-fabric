package team.creative.littletiles.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import team.creative.creativecore.client.gui.GuiClientLayer;
import team.creative.creativecore.client.gui.registry.GuiClientRegistry;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.littletiles.common.gui.tool.blueprint.GuiBlueprint;
import team.creative.littletiles.common.gui.tool.blueprint.GuiBlueprintAdd;

@Environment(EnvType.CLIENT)
public final class GuiClientBlueprintLayers {
    private GuiClientBlueprintLayers() {}
    public static void registerGuiBlueprint() { GuiClientRegistry.register(GuiBlueprint.class, Blueprint::new); }
    public static void registerGuiBlueprintAdd() { GuiClientRegistry.register(GuiBlueprintAdd.class, BlueprintAdd::new); }
    private static final class Blueprint extends GuiClientLayer {
        Blueprint(GuiBlueprint control) { super(control); }
        @Override public void render(GuiGraphicsExtractor graphics, Rect controlRect, Rect realRect, double scale, int mouseX, int mouseY) {
            ((GuiBlueprint) control).beforeClientRender();
            super.render(graphics, controlRect, realRect, scale, mouseX, mouseY);
        }
    }
    private static final class BlueprintAdd extends GuiClientLayer {
        BlueprintAdd(GuiBlueprintAdd control) { super(control); }
        @Override public void render(GuiGraphicsExtractor graphics, Rect controlRect, Rect realRect, double scale, int mouseX, int mouseY) {
            ((GuiBlueprintAdd) control).beforeClientRender();
            super.render(graphics, controlRect, realRect, scale, mouseX, mouseY);
        }
    }
}
