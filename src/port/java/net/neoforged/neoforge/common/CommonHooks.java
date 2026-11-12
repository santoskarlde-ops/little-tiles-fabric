package net.neoforged.neoforge.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Compile-only hook surface; Fabric event wiring replaces these hooks later. */
public final class CommonHooks {
    private CommonHooks() {}
    public static PlayerInteractEvent.LeftClickBlock onLeftClickBlock(Player player, BlockPos pos, Direction direction, PlayerInteractEvent.LeftClickBlock.Action action) { return new PlayerInteractEvent.LeftClickBlock(); }
    public static PlayerInteractEvent.LeftClickBlock onLeftClickBlock(Player player, BlockPos pos, Direction direction, net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action action) { return new PlayerInteractEvent.LeftClickBlock(); }
    public static PlayerInteractEvent.LeftClickBlock onClientMineHold(Player player, BlockPos pos, Direction direction) { return new PlayerInteractEvent.LeftClickBlock(); }
    public static PlayerInteractEvent.RightClickBlock onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, BlockHitResult hit) { return new PlayerInteractEvent.RightClickBlock(); }
    public static InteractionResult onItemRightClick(Player player, InteractionHand hand) { return InteractionResult.PASS; }
    public static InteractionResult onInteractEntityAt(Player player, Entity entity, Object hit, InteractionHand hand) { return InteractionResult.PASS; }
    public static BlockEvent.BreakEvent fireBlockBreak(Level level, GameType gameType, ServerPlayer player, BlockPos pos, BlockState state) { return new BlockEvent.BreakEvent(); }
}
