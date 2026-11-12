package team.creative.littletiles.common.recipe;

import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import team.creative.littletiles.common.item.ItemMultiTiles;
import team.creative.littletiles.common.item.ItemPremadeStructure;

public class StructureIngredient implements CustomIngredient {
    
    public static final MapCodec<StructureIngredient> CODEC = RecordCodecBuilder.<StructureIngredient>mapCodec(instance -> instance.group(Codec.STRING.fieldOf("structure")
            .forGetter(x -> x.structureType)).apply(instance, StructureIngredient::new));
    
    public static final CustomIngredientSerializer<StructureIngredient> SERIALIZER = new CustomIngredientSerializer<>() {
        private final StreamCodec<RegistryFriendlyByteBuf, StructureIngredient> streamCodec = ByteBufCodecs.STRING_UTF8
                .map(StructureIngredient::new, ingredient -> ingredient.structureType).cast();

        @Override
        public Identifier getIdentifier() {
            return Identifier.fromNamespaceAndPath("littletiles", "structure");
        }

        @Override
        public MapCodec<StructureIngredient> getCodec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, StructureIngredient> getStreamCodec() {
            return streamCodec;
        }
    };

    public final String structureType;
    
    public StructureIngredient(String structureType) {
        this.structureType = structureType;
    }
    
    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;
        if (stack.getItem() instanceof ItemMultiTiles)
            return ItemMultiTiles.getStructure(stack).equals(structureType);
        else if (stack.getItem() instanceof ItemPremadeStructure)
            return ItemPremadeStructure.getPremadeId(stack).equals(structureType);
        return false;
    }
    
    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public Stream<Holder<Item>> items() {
        // The item prefilter must include both supported kinds, including registered subclasses.
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof ItemMultiTiles || item instanceof ItemPremadeStructure)
                .map(item -> (Holder<Item>) item.builtInRegistryHolder());
    }

    @Override
    public SlotDisplay display() {
        return new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(ItemPremadeStructure.of(structureType)));
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StructureIngredient i)
            return i.structureType.equals(structureType);
        return super.equals(obj);
    }
    
    @Override
    public int hashCode() {
        return structureType.hashCode();
    }
}
