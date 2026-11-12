package team.creative.littletiles.client.render.item;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.box.ItemModelBox;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.client.LittleTilesClient;

public class LittleModelItemTiles extends ItemModelBox {
    
    public LittleModelItemTiles() {}
    public List<? extends RenderBox> getBoxes(ItemStack stack, boolean translucent) {
        ILittlePlacer tool = (ILittlePlacer) stack.getItem();
        if (tool.hasTiles(stack) && tool.shouldRenderInHand(stack))
            return tool.getTiles(stack).getRenderingBoxes(translucent);
        return Collections.EMPTY_LIST;
    }
    public boolean checkTranslucentLayer(ItemStack stack) {
        ILittlePlacer tool = (ILittlePlacer) stack.getItem();
        if (tool.hasTiles(stack))
            return tool.getTiles(stack).hasTranslucentBlocksInGui();
        return false;
    }
    public boolean hasTranslucentLayer(ItemStack stack) {
        return LittleTilesClient.ITEM_RENDER_CACHE.hasTranslucent(stack);
    }
    public void saveCachedModel(boolean translucent, List<BakedQuad> cachedQuads, ItemStack stack, boolean threaded) {}
    public List<BakedQuad> getCachedModel(boolean translucent, ItemStack stack, boolean threaded) {
        return LittleTilesClient.ITEM_RENDER_CACHE.requestCache(stack, translucent);
    }
    
}
