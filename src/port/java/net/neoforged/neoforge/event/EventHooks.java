package net.neoforged.neoforge.event;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;

/** Compile-only common hook surface; Fabric wiring is added in the port handlers. */
public final class EventHooks {
    private EventHooks() {}
    public static void onPlayerDestroyItem(Player player, ItemStack stack, InteractionHand hand) {}
    public static boolean doPlayerHarvestCheck(Player player, BlockState state, Level level, BlockPos pos) { return true; }
    public static Either<Player.BedSleepingProblem, Unit> canPlayerStartSleeping(Player player, BlockPos pos, Either<Player.BedSleepingProblem, Unit> result) { return result; }
    public static Event fireEntityTickPre(Entity entity) { return new Event(); }
    public static void fireEntityTickPost(Entity entity) {}
}
