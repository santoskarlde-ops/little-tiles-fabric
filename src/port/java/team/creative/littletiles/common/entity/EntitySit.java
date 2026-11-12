package team.creative.littletiles.common.entity;

import team.creative.littletiles.common.compat.creativecore.VecOriginPose;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.connection.ILevelPositionProvider;
import team.creative.littletiles.common.structure.connection.children.StructureChildConnection;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.type.LittleChair;

public class EntitySit extends Entity implements ILevelPositionProvider, INoPushEntity {
    
    /*
     * CompoundTag is no longer a synced-data serializer in 26.3. Keep the
     * persistent representation as a compound, but send its SNBT form over
     * entity data until a dedicated stream codec is wired in.
     */
    public static final EntityDataAccessor<String> CONNECTION = SynchedEntityData.defineId(EntitySit.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Float> CHAIRX = SynchedEntityData.defineId(EntitySit.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> CHAIRY = SynchedEntityData.defineId(EntitySit.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> CHAIRZ = SynchedEntityData.defineId(EntitySit.class, EntityDataSerializers.FLOAT);
    private StructureChildConnection temp;
    
    public EntitySit(LittleChair chair, Level level, double x, double y, double z) {
        super(LittleTilesRegistry.SIT_TYPE.get(), level);
        entityData.set(CHAIRX, (float) x);
        entityData.set(CHAIRY, (float) y);
        entityData.set(CHAIRZ, (float) z);
        this.temp = chair.children.generateConnection(this);
        entityData.set(CONNECTION, temp.save(new CompoundTag()).toString());
        setPos(x, y, z);
    }
    
    public EntitySit(EntityType<? extends EntitySit> type, Level level) {
        super(type, level);
        noPhysics = true;
    }
    
    @Override
    public boolean isPushable() {
        return false;
    }
    
    @Override
    public void tick() {
        super.tick();
        StructureChildConnection connection;
        try {
            connection = StructureChildConnection.load(this, TagParser.parseCompoundFully(entityData.get(CONNECTION)), false);
        } catch (RuntimeException e) {
            return;
        }
        if (!level().isClientSide() && !isVehicle()) {
            try {
                
                LittleStructure structure = connection.getStructure();
                if (structure instanceof LittleChair)
                    ((LittleChair) structure).setPlayer(null);
                discard();
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            
        } else {
            try {
                LittleStructure structure = connection.getStructure();
                if (structure.getStructureLevel() instanceof IOrientatedLevel l) {
                    Vec3d vec = new Vec3d(entityData.get(CHAIRX), entityData.get(CHAIRY), entityData.get(CHAIRZ));
                    VecOriginPose.of(l.getOrigin()).transform(vec);
                    setPos(vec.x, vec.y, vec.z);
                }
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        }
    }
    
    @Override
    public Vec3 getPassengerRidingPosition(Entity entity) {
        return position();
    }
    
    @Override
    protected void defineSynchedData(Builder builder) {
        builder.define(CONNECTION, "");
        builder.define(CHAIRX, 0F);
        builder.define(CHAIRY, 0F);
        builder.define(CHAIRZ, 0F);
    }
    
    @Override
    protected void addAdditionalSaveData(ValueOutput nbt) {
        try {
            nbt.store("connection", CompoundTag.CODEC, TagParser.parseCompoundFully(entityData.get(CONNECTION)));
        } catch (RuntimeException e) {
            nbt.store("connection", CompoundTag.CODEC, new CompoundTag());
        }
        nbt.putFloat("chairX", entityData.get(CHAIRX));
        nbt.putFloat("chairY", entityData.get(CHAIRY));
        nbt.putFloat("chairZ", entityData.get(CHAIRZ));
    }
    
    @Override
    protected void readAdditionalSaveData(ValueInput nbt) {
        entityData.set(CONNECTION, nbt.read("connection", CompoundTag.CODEC).orElse(new CompoundTag()).toString());
        entityData.set(CHAIRX, nbt.getFloatOr("chairX", 0F));
        entityData.set(CHAIRY, nbt.getFloatOr("chairY", 0F));
        entityData.set(CHAIRZ, nbt.getFloatOr("chairZ", 0F));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }
    
    @Override
    public Level getStructureLevel() {
        return level();
    }
    
    @Override
    public BlockPos getStructurePos() {
        return BlockPos.ZERO;
    }
    
    @Override
    public void structureDestroyed() {}
    
}
