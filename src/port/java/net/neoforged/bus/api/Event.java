package net.neoforged.bus.api;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Minimal event base retained for source compatibility. */
public class Event {
    private boolean canceled;
    private InteractionResult cancellationResult = InteractionResult.PASS;
    public boolean isCanceled() { return canceled; }
    public void setCanceled(boolean value) { canceled = value; }
    public InteractionResult getCancellationResult() { return cancellationResult; }
    public void setCancellationResult(InteractionResult value) { cancellationResult = value; }
    public <T extends Entity> T getEntity() { return null; }
    public <T extends Player> T getPlayer() { return null; }
    public <T extends Level> T getLevel() { return null; }
    public <T extends Entity> T getTarget() { return null; }
    public ItemEntity getItemEntity() { return null; }
    public ItemStack getItemStack() { return ItemStack.EMPTY; }
    public BlockPos getPos() { return BlockPos.ZERO; }
    public BlockState getBlockState() { return null; }
    public InteractionHand getHand() { return InteractionHand.MAIN_HAND; }
    public Component getLocalizedMessage() { return Component.empty(); }
    public Component getActionMessage() { return Component.empty(); }
    public <T> T getCause() { return null; }
}
