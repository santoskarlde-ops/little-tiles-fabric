package team.creative.littletiles.common.level.little;

import team.creative.littletiles.common.compat.creativecore.VecOriginPose;

import java.util.UUID;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.common.level.context.ILittleLevelContext;

public interface LittleLevel extends IOrientatedLevel, ILittleLevelContext {
    
    public default Level asLevel() {
        return (Level) this;
    }
    
    public void registerBlockChangeListener(LevelBlockChangeListener listener);
    
    @Override
    public Entity getHolder();
    
    @Override
    public void setHolder(Entity entity);
    
    public UUID key();
    
    public void unload(LevelChunk chunk);
    
    @Override
    public void unload();
    
    public Iterable<Entity> entities();
    
    public Iterable<? extends ChunkAccess> chunks();
    
    public void tick();
    
    public default boolean allowPlacement() {
        return true;
    }
    
    public void removeEntityById(int id, RemovalReason reason);
    
    @Environment(EnvType.CLIENT)
    public LittleEntityRenderManager getRenderManager();
    
    @Override
    default Vec3 toFakeWorld(Vec3 vec) {
        return VecOriginPose.of(getOrigin()).transformInverse(vec);
    }
    
    @Override
    default Vec3 toFakeWorld(Vec3 vec, float partialTick) {
        return VecOriginPose.of(getOrigin(), partialTick).transformInverse(vec);
    }
    
    @Override
    default AABB toFakeWorld(AABB bb) {
        return VecOriginPose.of(getOrigin()).transformInverse(bb).toVanilla();
    }
    
    @Override
    default AABB toFakeWorld(AABB bb, float partialTick) {
        return VecOriginPose.of(getOrigin(), partialTick).transformInverse(bb).toVanilla();
    }
    
    @Override
    default Vec3 toRealWorld(Vec3 vec) {
        return VecOriginPose.of(getOrigin()).transform(vec);
    }
    
    @Override
    default Vec3 toRealWorld(Vec3 vec, float partialTick) {
        return VecOriginPose.of(getOrigin(), partialTick).transform(vec);
    }
    
    @Override
    default AABB toRealWorld(AABB bb) {
        return VecOriginPose.of(getOrigin()).transform(bb).toVanilla();
    }
    
    @Override
    default AABB toRealWorld(AABB bb, float partialTick) {
        return VecOriginPose.of(getOrigin(), partialTick).transform(bb).toVanilla();
    }
    
    @Override
    default Matrix4f transform(double x, double y, double z, Vec3 camera, float partialTick) {
        Matrix4f matrix = VecOriginPose.of(getOrigin(), partialTick).transform(x, y, z);
        matrix.translate((float) (x - camera.x), (float) (y - camera.y), (float) (z - camera.z));
        return matrix;
    }
    
    @Override
    default void transformPose(PoseStack pose, double x, double y, double z, Vec3 camera, float partialTick) {
        camera = VecOriginPose.of(getOrigin(), partialTick).setup(pose, camera);
        pose.translate((float) (x - camera.x), (float) (y - camera.y), (float) (z - camera.z));
    }
    
    @Override
    default void transformMatrix(Matrix4fStack matrix, double x, double y, double z, Vec3 camera, float partialTick) {
        camera = VecOriginPose.of(getOrigin(), partialTick).setup(matrix, camera);
        matrix.translate((float) (x - camera.x), (float) (y - camera.y), (float) (z - camera.z));
    }
    
    @Override
    default Matrix4f transformInverse(double x, double y, double z, Vec3 camera, float partialTick) {
        Matrix4f matrix = VecOriginPose.of(getOrigin(), partialTick).transformInverse(x, y, z);
        matrix.translate((float) -(x - camera.x), (float) -(y - camera.y), (float) -(z - camera.z));
        return matrix;
    }
    
    @Override
    default boolean hasId() {
        return true;
    }
    
    @Override
    default String getId() {
        var key = key();
        return key != null ? key.toString() : "";
    }
    
    @Override
    default Level asLevel(Level level) {
        return (Level) this;
    }
    
    @Override
    default boolean is(ILittleLevelContext context) {
        return this == context;
    }
    
    @Override
    default boolean isChildOf(LevelAccessor level) {
        if (level instanceof ISubLevel s) {
            level = s.getParent();
            while (level instanceof ISubLevel sub) {
                if (level == this)
                    return true;
                level = sub.getParent();
            }
        }
        return false;
    }
}
