package team.creative.littletiles.common.entity;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;

public class PrimedSizedTnt extends PrimedTnt {
    
    private static final EntityDataAccessor<String> TNTSIZE = SynchedEntityData.defineId(PrimedSizedTnt.class, EntityDataSerializers.STRING);
    
    public LittleGrid grid;
    public LittleVec size;
    
    public PrimedSizedTnt(EntityType<? extends PrimedSizedTnt> type, Level level) {
        super(type, level);
        // Matches the default synced size, even when the default value is not sent.
        this.grid = LittleGrid.MIN;
        this.size = new LittleVec(1, 1, 1);
        refreshDimensions();
    }
    
    public PrimedSizedTnt(Level level, double x, double y, double z, @Nullable LivingEntity igniter, LittleGrid grid, LittleVec size) {
        this(LittleTilesRegistry.SIZED_TNT_TYPE.get(), level);
        this.setPos(x, y, z);
        double d0 = level.getRandom().nextDouble() * ((float) Math.PI * 2F);
        this.setDeltaMovement(-Math.sin(d0) * 0.02D, 0.2F, -Math.cos(d0) * 0.02D);
        this.setFuse(80);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        setSize(grid, size);
    }
    
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        // The base constructor can call this before our fields have been initialized.
        return grid == null || size == null ? super.getDimensions(pose) : EntitySizeHandler.dimensions(this);
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TNTSIZE, "1.1.1.1");
    }
    
    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        tag.putIntArray("size", new int[] { size.x, size.y, size.z });
        if (!grid.isDefault)
            tag.putInt("grid", grid.count);
        else
            tag.discard("grid");
        super.addAdditionalSaveData(tag);
    }
    
    @Override
    protected void readAdditionalSaveData(ValueInput tag) {
        int[] values = tag.getIntArray("size").orElse(new int[] { 1, 1, 1 });
        LittleVec loadedSize = new LittleVec(values[0], values[1], values[2]);
        LittleGrid loadedGrid = LittleGrid.get(tag.getInt("grid").orElse(LittleGrid.overallDefault().count));
        super.readAdditionalSaveData(tag);
        setSize(loadedGrid, loadedSize);
    }
    
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (TNTSIZE.equals(key)) {
            String data = this.entityData.get(TNTSIZE);
            String[] coords = data.split("\\.");
            this.size = new LittleVec(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
            this.grid = LittleGrid.get(Integer.parseInt(coords[3]));
            refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }
    
    public void setSize(LittleGrid grid, LittleVec size) {
        this.size = size;
        this.grid = grid;
        String encoded = size.x + "." + size.y + "." + size.z + "." + grid.count;
        if (encoded.equals(this.entityData.get(TNTSIZE)))
            refreshDimensions();
        else
            this.entityData.set(TNTSIZE, encoded);
    }
    
    protected void explode() {
        this.level().explode(this, this.getX(), this.getY(0.0625D), this.getZ(), (float) (4.0D * size.getPercentVolume(grid)), Level.ExplosionInteraction.TNT);
    }
    
}
