package team.creative.littletiles.common.entity;

import team.creative.littletiles.common.compat.creativecore.VecOriginPose;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.math.collision.CollisionCoordinator;
import team.creative.creativecore.common.util.math.matrix.ChildVecOrigin;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.type.itr.FilterIterator;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.client.entity.LevelTransitionListener;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.common.math.vec.LittleHitResult;

public abstract class LittleEntity<T extends LittleEntityPhysic> extends Entity implements OrientationAwareEntity, INoPushEntity, LevelTransitionListener {
    
    protected LittleSubLevel subLevel;
    private IVecOrigin origin;
    public final T physic = createPhysic();
    private List<Entity> entitiesToAdd;
    protected boolean changedLevel;
    public boolean tickedAlready;
    
    // ================Constructors================
    
    public LittleEntity(EntityType<?> type, Level level) {
        super(type, level);
    }
    
    public LittleEntity(EntityType<?> type, Level level, Vec3d center) {
        super(type, level);
        setSubLevel(createLevel(), center);
        setPos(center.x, center.y, center.z);
        origin.tick();
        physic.ignoreCollision(() -> initialTick());
    }
    
    public LittleEntity(EntityType<?> type, Level level, LittleSubLevel subLevel, Vec3d center) {
        super(type, level);
        setSubLevel(subLevel, center);
        setPos(center.x, center.y, center.z);
        origin.tick();
        physic.ignoreCollision(() -> initialTick());
    }
    
    protected abstract LittleSubLevel createLevel();
    
    protected abstract T createPhysic();
    
    public boolean isReal() {
        Level level = level();
        if (level() instanceof ISubLevel sub)
            level = sub.getRealLevel();
        return !(level instanceof IOrientatedLevel);
    }
    
    // ================Origin================
    
    protected void setOrigin(IVecOrigin origin) {
        this.origin = origin;
        originReplaced();
    }
    
    protected void originReplaced() {
        for (OrientationAwareEntity entity : children())
            entity.parentVecOriginChange(origin);
    }
    protected void defineSynchedData(Builder builder) {}
    public IVecOrigin getOrigin() {
        return origin;
    }
    
    public LittleSubLevel getSubLevel() {
        return subLevel;
    }
    
    public Level getRealLevel() {
        if (level() instanceof ISubLevel sub)
            return sub.getRealLevel();
        return level();
    }
    
    public LittleEntity getTopLevelEntity() {
        if (level() instanceof ISubLevel sub)
            return ((LittleEntity) sub.getHolder()).getTopLevelEntity();
        return this;
    }
    
    public abstract CreativePacket initClientPacket();
    
    public abstract void startTracking(ServerPlayer player);
    
    public abstract void stopTracking(ServerPlayer player);
    
    protected void setSubLevel(LittleSubLevel subLevel, Vec3d center) {
        this.subLevel = subLevel;
        this.subLevel.setHolder(this);
        this.subLevel.setOrigin(center);
        this.origin = subLevel.getOrigin();
        physic.setSubLevel(subLevel);
    }
    public void parentVecOriginChange(IVecOrigin origin) {
        if (this.origin instanceof ChildVecOrigin child)
            child.parent = origin;
    }
    public void transform(CollisionCoordinator coordinator) {
        physic.transform(coordinator);
    }
    public void changedLevel(Level oldLevel, Level newLevel) {
        changedLevel = true;
    }
    
    // ================Children================
    
    public Iterable<Entity> entities() {
        return subLevel.entities();
    }
    
    public Iterable<OrientationAwareEntity> children() {
        return new FilterIterator<OrientationAwareEntity>(entities(), x -> x instanceof OrientationAwareEntity e && e.hasLoaded());
    }
    
    // ================Rendering================
    
    @Environment(EnvType.CLIENT)
    public abstract LittleEntityRenderManager getRenderManager();
    
    // ================Ticking================
    public void tick() {
        if (tickedAlready)
            return;
        super.tick();
    }
    
    public abstract void initialTick();
    
    public abstract void internalTick();
    public void performTick() {
        if (tickedAlready) {
            tickedAlready = false;
            return;
        }
        if (entitiesToAdd != null) {
            for (Entity entity : entitiesToAdd)
                subLevel.addFreshEntity(entity);
            entitiesToAdd = null;
        }
        if (level() instanceof ISubLevel) {
            if (!level().isClientSide())
                this.setSharedFlag(6, this.isCurrentlyGlowing());
            super.baseTick();
        } else
            super.tick();
        
        children().forEach(x -> x.performTick());
        internalTick();
        subLevel.tick();
        
        physic.tick();
        origin.tick();
        
        Vec3 center = physic.getCenter();
        setPos(center.x, center.y, center.z);
        
        syncMovement();
        
        physic.updateBoundingBox();
    }
    
    public abstract void syncMovement();
    
    // ================Save&Load================
    
    protected abstract Vec3d loadCenter(ValueInput input);
    protected void readAdditionalSaveData(ValueInput input) {
        setSubLevel(createLevel(), loadCenter(input));
        
        physic.load(input.read("physic", CompoundTag.CODEC).orElseGet(CompoundTag::new));
        
        loadEntity(input);
        
        physic.updateBoundingBox();
        if (input.list("entities", CompoundTag.CODEC).isPresent()) {
            entitiesToAdd = new ArrayList<>();
            for (CompoundTag entityTag : input.listOrEmpty("entities", CompoundTag.CODEC))
                EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING, subLevel.registryAccess(), entityTag), (Level) subLevel,
                    new EntitySpawnRequest(EntitySpawnReason.LOAD, true))
                    .ifPresent(entitiesToAdd::add);
        } else
            entitiesToAdd = null;
    }
    
    public abstract void loadEntity(ValueInput input);
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("physic", CompoundTag.CODEC, physic.save());
        
        saveEntity(output);
        
        ValueOutput.TypedOutputList<CompoundTag> list = output.list("entities", CompoundTag.CODEC);
        boolean hasEntities = false;
        for (Entity entity : entities()) {
            TagValueOutput entityOutput = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
            entity.save(entityOutput);
            list.add(entityOutput.buildResult());
            hasEntities = true;
        }
        if (!hasEntities)
            output.discard("entities");
    }
    
    public abstract void saveEntity(ValueOutput output);
    
    public void onAddedToLevel() {
        if (subLevel != null)
            entities().forEach(x -> {
                if (x instanceof LittleEntity little)
                    little.onAddedToLevel();
            });
        LittleTiles.ANIMATION_HANDLERS.get(level()).add(this);
    }
    
    public void onRemovedFromLevel() {
        if (subLevel != null)
            entities().forEach(x -> {
                if (x instanceof LittleEntity little)
                    little.onRemovedFromLevel();
            });
        var handler = LittleTiles.ANIMATION_HANDLERS.getWithoutCreate(level());
        if (handler != null)
            handler.remove(this);
    }
    
    // ================MC Hooks================
    public void setPos(double x, double y, double z) {
        super.setPosRaw(x, y, z); // Fixes bounding box
    }
    
    public boolean isOnFire() {
        return false;
    }
    
    public boolean fireImmune() {
        return true;
    }
    
    public boolean displayFireAnimation() {
        return false;
    }
    
    public void destroyAnimation() {
        this.kill();
    }
    
    public void kill() {
        subLevel.unload();
        if (level() instanceof ServerLevel server)
            super.kill(server);
        else
            remove(Entity.RemovalReason.KILLED);
    }
    public boolean canBeCollidedWith() {
        return false;
    }
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }
    public InteractionResult interact(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        return InteractionResult.PASS;
    }
    
    // ================Hit Result================
    
    public LittleHitResult rayTrace(Vec3 pos, Vec3 look, float partialTicks) {
        LittleHitResult result = null;
        double distance = 0;
        for (Entity entity : entities()) {
            if (entity instanceof LittleEntity levelEntity) {
                if (!levelEntity.hasLoaded())
                    continue;
                Vec3 newPos = VecOriginPose.of(levelEntity.origin, partialTicks).transformInverse(pos);
                Vec3 newLook = VecOriginPose.of(levelEntity.origin, partialTicks).transformInverse(look);
                
                if (levelEntity.physic.getOBB().intersects(newPos, newLook)) {
                    LittleHitResult tempResult = levelEntity.rayTrace(pos, look, partialTicks);
                    if (tempResult == null)
                        continue;
                    double tempDistance = newPos.distanceTo(tempResult.hit.getLocation());
                    if (result == null || tempDistance < distance) {
                        result = tempResult;
                        distance = tempDistance;
                    }
                }
            } else {
                Vec3 newPos = VecOriginPose.of(origin, partialTicks).transformInverse(pos);
                Vec3 newLook = VecOriginPose.of(origin, partialTicks).transformInverse(look);
                if (entity.getBoundingBox().intersects(newPos, newLook)) {
                    LittleHitResult tempResult = new LittleHitResult(this, new EntityHitResult(entity, entity.getBoundingBox().clip(newPos, newLook).get()), subLevel);
                    double tempDistance = newPos.distanceTo(tempResult.hit.getLocation());
                    if (result == null || tempDistance < distance) {
                        result = tempResult;
                        distance = tempDistance;
                    }
                }
            }
        }
        
        Vec3 newPos = VecOriginPose.of(origin, partialTicks).transformInverse(pos);
        Vec3 newLook = VecOriginPose.of(origin, partialTicks).transformInverse(look);
        HitResult tempResult = subLevel.clip(new ClipContext(newPos, newLook, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, CollisionContext.empty()));
        if (tempResult == null || tempResult.getType() != Type.BLOCK || !(tempResult instanceof BlockHitResult))
            return result;
        if (result == null || newPos.distanceTo(tempResult.getLocation()) < distance)
            return new LittleHitResult(this, tempResult, subLevel);
        return result;
    }
    
    // ================SPECIAL================
    
    public boolean noClip() {
        return false;
    }
    
    public abstract boolean checkEntityInside(Entity entity);
    
    // ================CLIENT================
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }
    public boolean hasLoaded() {
        return subLevel != null;
    }
    
}
