package team.creative.littletiles.client.render.level;

import org.joml.Vector3d;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState.WaterOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.fabric.client.ClientPlayerRenderEvents;

public class LittleClientEventHandler {
    public static void register() {
        ClientPlayerRenderEvents.AFTER_EXTRACT.register(LittleClientEventHandler::renderOverlay);
    }

    private static void renderOverlay(PlayerRenderState state) {
        WaterOverlay overlay = state.waterOverlay;
        Player player = Minecraft.getInstance().player;
        // The old WATER event only ran when vanilla was preparing a water overlay.
        if (overlay == null || player == null)
            return;
        BlockPos blockpos = BlockPos.containing(player.getEyePosition(TickUtils.getFrameTime(player.level())));
        BlockEntity blockEntity = player.level().getBlockEntity(blockpos);
        if (blockEntity instanceof BETiles be) {
            AABB bb = player.getBoundingBox();
            for (Pair<IParentCollection, LittleTile> pair : be.allTiles()) {
                LittleTile tile = pair.value;
                if (tile.isFluid(FluidTags.WATER) && tile.intersectsWith(bb, pair.key)) {
                    float brightness = Lightmap.getBrightness(player.level().dimensionType(), player.level().getMaxLocalRawBrightness(blockpos));
                    Vector3d color = ColorUtils.toVec(tile.color);
                    state.waterOverlay = new WaterOverlay(ARGB.colorFromFloat(0.5F, brightness * (float) color.x,
                        brightness * (float) color.y, brightness * (float) color.z), overlay.uOffset(), overlay.vOffset());
                    return;
                }
            }
        }
    }
}
