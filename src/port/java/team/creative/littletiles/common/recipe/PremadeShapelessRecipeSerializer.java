package team.creative.littletiles.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo;
import net.minecraft.world.item.crafting.Recipe.CommonInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import team.creative.littletiles.common.item.ItemPremadeStructure;
import team.creative.littletiles.mixin.common.recipe.ShapelessRecipeAccessor;

public final class PremadeShapelessRecipeSerializer {
    public static final MapCodec<ShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ShapelessRecipe.MAP_CODEC.forGetter(recipe -> recipe),
        Codec.STRING.fieldOf("structure").forGetter(recipe -> ItemPremadeStructure.getPremadeId(
            ((ShapelessRecipeAccessor) recipe).littletiles$getResult().create()))
    ).apply(instance, (recipe, structure) -> {
        ShapelessRecipeAccessor accessor = (ShapelessRecipeAccessor) recipe;
        ItemStackTemplate result = ItemStackTemplate.fromNonEmptyStack(
            ItemPremadeStructure.of(accessor.littletiles$getResult().create(), structure));
        return new ShapelessRecipe(new CommonInfo(recipe.showNotification()),
            new CraftingBookInfo(recipe.category(), recipe.group()), result, accessor.littletiles$getIngredients());
    }));

    private PremadeShapelessRecipeSerializer() {}

    public static RecipeSerializer<ShapelessRecipe> create() {
        return new RecipeSerializer<>(CODEC, ShapelessRecipe.STREAM_CODEC);
    }
}
