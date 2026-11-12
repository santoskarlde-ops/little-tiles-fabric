package net.neoforged.neoforge.items.wrapper;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class InvWrapper implements IItemHandlerModifiable {
    protected final Container container;
    public InvWrapper(Container container) { this.container = container; }
    public int getSlots() { return container.getContainerSize(); }
    public ItemStack getStackInSlot(int slot) { return container.getItem(slot); }
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (simulate) return stack;
        container.setItem(slot, stack); return ItemStack.EMPTY;
    }
    public void setStackInSlot(int slot, ItemStack stack) { container.setItem(slot, stack); }
}
