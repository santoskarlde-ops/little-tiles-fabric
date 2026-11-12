package net.neoforged.neoforge.common.crafting;

import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.world.item.ItemStack;

/**
 * Narrow compatibility interface for the custom ingredient implementation
 * retained by the port.
 */
public interface ICustomIngredient extends Predicate<ItemStack> {

    boolean isSimple();

    Stream<ItemStack> getItems();

    IngredientType<?> getType();
}