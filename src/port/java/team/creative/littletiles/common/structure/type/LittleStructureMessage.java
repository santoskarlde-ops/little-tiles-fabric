package team.creative.littletiles.common.structure.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;

public class LittleStructureMessage extends LittleStructure {
    
    public List<String> text;
    public boolean allowRightClick = true;
    public boolean status = false;
    
    public LittleStructureMessage(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    public boolean canInteract() {
        return allowRightClick;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTileContext context, BlockPos pos, Player player, BlockHitResult result) {
        if (allowRightClick) {
            if (!level.isClientSide()) {
                for (int i = 0; i < text.size(); i++)
                    if (status)
                        player.sendOverlayMessage(message(i));
                    else
                        player.sendSystemMessage(message(i));
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(level, context, pos, player, result);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        if (nbt.contains("text"))
            text = Arrays.asList(StringUtils.truncate(nbt.getString("text").orElse(""), LittleTiles.CONFIG.general.messageStructureLength));
        else {
            text = new ArrayList<>();
            ListTag list = nbt.getListOrEmpty("lines");
            for (int i = 0; i < list.size(); i++)
                text.add(StringUtils.truncate(list.getString(i).orElse(""), LittleTiles.CONFIG.general.messageStructureLength));
        }
        allowRightClick = nbt.getBoolean("right").orElse(false);
        status = nbt.getBoolean("status").orElse(false);
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (int i = 0; i < text.size(); i++) {
            list.add(StringTag.valueOf(text.get(i)));
        }
        nbt.put("lines", list);
        nbt.putBoolean("right", allowRightClick);
        nbt.putBoolean("status", status);
    }
    
    public Component message(int i) {
        try {
            return ComponentSerialization.CODEC.parse(getStructureLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE), JsonParser.parseString(text.get(i))).getOrThrow();
        } catch (Exception exception) {
            return Component.literal(text.get(i));
        }
    }
    
    @Override
    public void performInternalOutputChange(InternalSignalOutput output) {
        if (output.component.is("message")) {
            Level level = getStructureLevel();
            if (level.isClientSide())
                return;
            
            final LevelChunk chunk = level.getChunkAt(getStructurePos());
            if (chunk != null)
                ((ServerChunkCache) chunk.getLevel().getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false).forEach(x -> {
                    for (int i = 0; i < text.size(); i++)
                        x.sendSystemMessage(message(i), status);
                });
        }
    }
    
}
