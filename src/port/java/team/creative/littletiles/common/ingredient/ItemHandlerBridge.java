package team.creative.littletiles.common.ingredient;

import javax.annotation.Nullable;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/** Replaces NeoForge's item-handler capability lookups, which do not exist on Fabric. */
public final class ItemHandlerBridge {
    
    /** Hotbar plus main inventory; armor and offhand slots are not part of it (as with NeoForge's player main-inventory handler). */
    public static final int PLAYER_MAIN_SLOTS = 36;
    
    private ItemHandlerBridge() {}
    
    public static IItemHandler ofPlayer(Player player) {
        return new PlayerMainHandler(player.getInventory());
    }
    
    /** Not wired to Fabric's Transfer API yet: items that hold an inventory (shulker boxes, bags) are not searched through this path. */
    @Nullable
    public static IItemHandler ofStack(ItemStack stack) {
        return null;
    }
    
    private static class PlayerMainHandler implements IItemHandler {
        
        private final Container container;
        
        private PlayerMainHandler(Container container) {
            this.container = container;
        }
        
        private boolean valid(int slot) {
            return slot >= 0 && slot < getSlots();
        }
        
        @Override
        public int getSlots() {
            return Math.min(PLAYER_MAIN_SLOTS, container.getContainerSize());
        }
        
        @Override
        public ItemStack getStackInSlot(int slot) {
            return valid(slot) ? container.getItem(slot) : ItemStack.EMPTY;
        }
        
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty() || !valid(slot) || !container.getItem(slot).isEmpty())
                return stack;
            if (!simulate)
                container.setItem(slot, stack.copy());
            return ItemStack.EMPTY;
        }
        
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0 || !valid(slot))
                return ItemStack.EMPTY;
            ItemStack existing = container.getItem(slot);
            if (existing.isEmpty())
                return ItemStack.EMPTY;
            int count = Math.min(amount, existing.getCount());
            if (simulate) {
                ItemStack copy = existing.copy();
                copy.setCount(count);
                return copy;
            }
            return existing.split(count);
        }
    }
}
