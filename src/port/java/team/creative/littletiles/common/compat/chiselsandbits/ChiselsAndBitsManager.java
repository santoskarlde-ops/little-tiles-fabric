package team.creative.littletiles.common.compat.chiselsandbits;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;

/**
 * Disabled optional-integration boundary for Fabric.
 */
public final class ChiselsAndBitsManager {
    public static final String chiselsandbitsID = "chiselsandbits";
    public static final int convertingFrom = 16;

    private ChiselsAndBitsManager() {}

    public static boolean isInstalled() {
        return ModList.get().isLoaded(chiselsandbitsID);
    }

    public static LittleGroup getGroup(ItemStack stack) {
        return null;
    }

    public static LittleGroup getGroup(BlockEntity blockEntity) {
        return null;
    }

    public static boolean isChiselsAndBitsStructure(BlockState state) {
        return false;
    }

    public static boolean isChiselsAndBitsStructure(ItemStack stack) {
        return false;
    }

    public static boolean isChiselsAndBitsStructure(BlockEntity blockEntity) {
        return false;
    }
}