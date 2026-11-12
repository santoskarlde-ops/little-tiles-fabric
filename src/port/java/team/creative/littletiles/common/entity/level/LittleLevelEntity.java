package team.creative.littletiles.common.entity.level;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.level.little.LittleClientLevel;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.client.render.entity.LittleLevelRenderManager;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.level.little.LittleChunkSerializer;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.common.packet.entity.EntityOriginChanged;
import team.creative.littletiles.common.packet.entity.level.LittleLevelInitPacket;
import team.creative.littletiles.server.level.little.LittleServerLevel;
import team.creative.littletiles.server.level.little.SubServerLevel;

public class LittleLevelEntity extends LittleEntity<LittleLevelEntityPhysic> {
    
    public BlockPos center;
    
    public LittleLevelEntity(EntityType<?> type, Level level) {
        super(type, level);
    }
    
    public LittleLevelEntity(Level level, BlockPos pos) {
        super(LittleTilesRegistry.ENTITY_LEVEL.get(), level, new Vec3d(pos));
        this.center = pos;
    }
    
    @Override
    protected LittleSubLevel createLevel() {
        return SubServerLevel.createSubLevel(level());
    }
    
    @Override
    protected LittleLevelEntityPhysic createPhysic() {
        return new LittleLevelEntityPhysic(this);
    }
    
    @Override
    protected Vec3d loadCenter(ValueInput input) {
        return new Vec3d(input.getIntOr("cX", 0), input.getIntOr("cY", 0), input.getIntOr("cZ", 0));
    }
    
    @Override
    public void loadEntity(ValueInput input) {
        center = new BlockPos(input.getIntOr("cX", 0), input.getIntOr("cY", 0), input.getIntOr("cZ", 0));
        LittleServerLevel sub = (LittleServerLevel) subLevel;
        for (CompoundTag chunk : input.listOrEmpty("chunks", CompoundTag.CODEC)) {
            sub.load(new ChunkPos(chunk.getInt("xPos").orElse(0), chunk.getInt("zPos").orElse(0)), chunk);
        }
    }
    
    @Override
    public void saveEntity(ValueOutput output) {
        output.putInt("cX", center.getX());
        output.putInt("cY", center.getY());
        output.putInt("cZ", center.getZ());
        LittleServerLevel sub = (LittleServerLevel) subLevel;
        ValueOutput.TypedOutputList<CompoundTag> chunks = output.list("chunks", CompoundTag.CODEC);
        for (ChunkAccess chunk : sub.chunks())
            chunks.add(LittleChunkSerializer.write(sub, chunk));
        if (chunks.isEmpty())
            output.discard("chunks");
    }
    
    @Override
    public CreativePacket initClientPacket() {
        return new LittleLevelInitPacket(this);
    }
    
    public CompoundTag saveExtraClientData() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("cX", center.getX());
        nbt.putInt("cY", center.getY());
        nbt.putInt("cZ", center.getZ());
        nbt.put("physic", physic.save());
        return nbt;
    }
    
    @Environment(EnvType.CLIENT)
    public void initSubLevelClient(CompoundTag extraData) {
        if (changedLevel) {
            changedLevel = false;
            return;
        }
        setSubLevel(createLevel(), new Vec3d(extraData.getInt("cX").orElse(0), extraData.getInt("cY").orElse(0), extraData.getInt("cZ").orElse(0)));
        
        ((LittleClientLevel) subLevel).renderManager = new LittleLevelRenderManager(this);
        physic.load(extraData.getCompound("physic").orElse(new CompoundTag()));
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public LittleEntityRenderManager getRenderManager() {
        return ((LittleClientLevel) subLevel).renderManager;
    }
    
    @Override
    public void performTick() {
        super.performTick();
        if (!level().isClientSide() && physic.getBlockUpdateLevelSystem().isEntirelyEmpty())
            destroyAnimation();
    }
    
    @Override
    public void syncMovement() {
        var origin = getOrigin();
        if (!level().isClientSide() && (origin.offXLast() != origin.offX() || origin.offYLast() != origin.offY() || origin.offZLast() != origin.offZ() || origin.rotXLast() != origin
                .rotX() || origin.rotYLast() != origin.rotY() || origin.rotZLast() != origin.rotZ()))
            LittleTiles.NETWORK.sendToClientTracking(new EntityOriginChanged(this), this);
    }
    
    @Override
    public void internalTick() {}
    
    @Override
    public void initialTick() {}
    
    @Override
    public void startTracking(ServerPlayer player) {}
    
    @Override
    public void stopTracking(ServerPlayer player) {}
    
    public void setParentLevel(Level subLevel) {
        setLevel(subLevel);
        getSubLevel().setParent(subLevel);
        if (getOrigin() != null)
            getSubLevel().getOrigin().set(getOrigin());
        setOrigin(this.subLevel.getOrigin());
    }
    
    @Override
    public void prepareChangeLevel(Level oldLevel, Level newLevel) {
        setParentLevel(newLevel);
    }
    
    @Override
    public boolean checkEntityInside(Entity entity) {
        return true;
    }
    
}
