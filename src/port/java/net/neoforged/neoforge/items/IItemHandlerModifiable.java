package net.neoforged.neoforge.items;

import net.minecraft.world.item.ItemStack;

public interface IItemHandlerModifiable extends IItemHandler {
    default void setStackInSlot(int slot, ItemStack stack) {}
}
