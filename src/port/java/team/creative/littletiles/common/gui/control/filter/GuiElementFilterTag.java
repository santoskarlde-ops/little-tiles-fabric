package team.creative.littletiles.common.gui.control.filter;

import java.util.Optional;

import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import team.creative.creativecore.common.gui.IGuiParent;
import team.creative.creativecore.common.gui.control.collection.GuiComboBox;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.filter.TileFilters;

public class GuiElementFilterTag extends GuiElementFilter {
    
    protected GuiComboBox<TagKey<Block>> box;
    
    public GuiElementFilterTag(IGuiParent parent, TagKey<Block> selected) {
        super(parent);
        add(box = new GuiComboBox<>(this, "tag", new TextMapBuilder<TagKey<Block>>().addComponents(BuiltInRegistries.BLOCK.getTags().map(Named::key).toList(), x -> {
            TextBuilder builder = new TextBuilder();
            Optional<Named<Block>> tag = BuiltInRegistries.BLOCK.get(x);
            if (tag.isPresent() && tag.get().size() > 0)
                builder.stack(new ItemStack(tag.get().get(0).value()));
            return builder.text(x.location().toString()).build();
        })));
        box.setSearchbar(true);
        box.select(selected);
    }
    
    @Override
    public BiFilter<IParentCollection, LittleTile> get() {
        TagKey<Block> selected = box.selected();
        if (selected != null)
            return TileFilters.tag(selected);
        return null;
    }
    
}
