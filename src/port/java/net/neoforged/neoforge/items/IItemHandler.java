package net.neoforged.neoforge.items;

import net.minecraft.world.item.ItemStack;

public interface IItemHandler {
    int getSlots();
    ItemStack getStackInSlot(int slot);
    default ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }
    default ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
    default int getSlotLimit(int slot) { return 64; }
    default boolean isItemValid(int slot, ItemStack stack) { return true; }
}
