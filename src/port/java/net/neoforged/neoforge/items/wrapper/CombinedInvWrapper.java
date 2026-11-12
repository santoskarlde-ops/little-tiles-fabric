package net.neoforged.neoforge.items.wrapper;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class CombinedInvWrapper implements IItemHandlerModifiable {
    private final IItemHandlerModifiable[] handlers;
    public CombinedInvWrapper(IItemHandlerModifiable... handlers) { this.handlers = handlers; }
    public int getSlots() { int n = 0; for (var h : handlers) n += h.getSlots(); return n; }
    private int[] locate(int slot) { for (var h : handlers) { int n = h.getSlots(); if (slot < n) return new int[] { handlersIndex(h), slot }; slot -= n; } return new int[] { -1, -1 }; }
    private int handlersIndex(IItemHandlerModifiable h) { for (int i=0;i<handlers.length;i++) if (handlers[i] == h) return i; return -1; }
    public ItemStack getStackInSlot(int slot) { int[] x = locate(slot); return x[0] < 0 ? ItemStack.EMPTY : handlers[x[0]].getStackInSlot(x[1]); }
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { int[] x = locate(slot); return x[0] < 0 ? stack : handlers[x[0]].insertItem(x[1], stack, simulate); }
    public void setStackInSlot(int slot, ItemStack stack) { int[] x = locate(slot); if (x[0] >= 0) handlers[x[0]].setStackInSlot(x[1], stack); }
}
