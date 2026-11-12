package team.creative.creativecore.common.be;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Fabric version of CreativeCore's BlockEntityCreative (the original lives in src/forge and uses NeoForge hooks).
 *  Differences from the NeoForge version:
 *  - EffectiveSide.get().isClient() is replaced by an environment + thread-name check (only used while level == null).
 *  - onDataPacket(Connection, ValueInput) and handleUpdateTag(ValueInput) are NeoForge extensions and do not exist in vanilla.
 *    Vanilla applies both update packets and chunk tags through loadAdditional(ValueInput), so subclasses override that instead
 *    and the abstract handleUpdate(ValueInput, boolean) is gone (the chunkUpdate flag cannot be reproduced). */
public abstract class BlockEntityCreative extends BlockEntity {
    
    public BlockEntityCreative(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    
    public boolean isClient() {
        if (level != null)
            return level.isClientSide();
        // No level yet: dedicated server is never a client; on the physical client use Minecraft's main thread name as the side hint.
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && "Render thread".equals(Thread.currentThread().getName());
    }
    
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithFullMetadata(provider);
    }
    
    public void markDirty() {
        level.blockEntityChanged(worldPosition);
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    
}
