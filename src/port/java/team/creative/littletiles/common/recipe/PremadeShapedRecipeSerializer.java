package team.creative.littletiles.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo;
import net.minecraft.world.item.crafting.Recipe.CommonInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import team.creative.littletiles.common.item.ItemPremadeStructure;
import team.creative.littletiles.mixin.common.recipe.ShapedRecipeAccessor;

public final class PremadeShapedRecipeSerializer {
    // Use vanilla's recipe codec for its current fields and validation, adding our structure id.
    public static final MapCodec<ShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ShapedRecipe.MAP_CODEC.forGetter(recipe -> recipe),
        Codec.STRING.fieldOf("structure").forGetter(recipe -> ItemPremadeStructure.getPremadeId(
            ((ShapedRecipeAccessor) recipe).littletiles$getResult().create()))
    ).apply(instance, (recipe, structure) -> {
        ShapedRecipeAccessor accessor = (ShapedRecipeAccessor) recipe;
        ItemStackTemplate result = ItemStackTemplate.fromNonEmptyStack(
            ItemPremadeStructure.of(accessor.littletiles$getResult().create(), structure));
        return new ShapedRecipe(new CommonInfo(recipe.showNotification()),
            new CraftingBookInfo(recipe.category(), recipe.group()), accessor.littletiles$getPattern(), result);
    }));

    private PremadeShapedRecipeSerializer() {}

    public static RecipeSerializer<ShapedRecipe> create() {
        return new RecipeSerializer<>(CODEC, ShapedRecipe.STREAM_CODEC);
    }
}
