package net.neoforged.neoforge.event.entity.player;

import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.util.TriState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public class PlayerInteractEvent extends Event {
    public TriState getUseBlock() { return TriState.DEFAULT; }
    public TriState getUseItem() { return TriState.DEFAULT; }
    public void setUseBlock(TriState value) {}
    public void setUseItem(TriState value) {}
    public static class EntityInteract extends PlayerInteractEvent {}
    public static class EntityInteractSpecific extends PlayerInteractEvent {}
    public static class LeftClickEmpty extends PlayerInteractEvent {}
    public static class RightClickBlock extends PlayerInteractEvent {
        public RightClickBlock() {}
        public RightClickBlock(Player player, InteractionHand hand, BlockPos pos, BlockHitResult hit) {}
    }
    public static class RightClickEmpty extends PlayerInteractEvent {}
    public static class RightClickItem extends PlayerInteractEvent {}
    public static class LeftClickBlock extends PlayerInteractEvent {
        public enum Action { START, ABORT, STOP, START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK }
        public LeftClickBlock() {}
        public LeftClickBlock(Player player, BlockPos pos, Direction direction, Action action) {}
        public Action getAction() { return Action.START; }
    }
}
