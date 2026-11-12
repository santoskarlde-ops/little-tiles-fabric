package team.creative.littletiles.mixin.common.recipe;

import java.util.List;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShapelessRecipe.class)
public interface ShapelessRecipeAccessor {
    @Accessor("result")
    ItemStackTemplate littletiles$getResult();

    @Accessor("ingredients")
    List<Ingredient> littletiles$getIngredients();
}
