package team.creative.littletiles.common.compat.creativecore;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.creativecore.common.util.math.vec.Vec3d;

/**
 * Adapter that gives the CreativeCore 26.x {@link IVecOrigin} (which has no pose()) the {@link IOriginPose} view LittleTiles was written against.
 * Transform maths is taken 1:1 from IVecOrigin.transformPointToWorld / transformPointToFakeWorld. The rendering matrices are rebuilt from the
 * commented-out IVecOrigin.setupRenderingInternal and are UNVERIFIED (see PORT_NOTES.md).
 */
public class VecOriginPose implements IOriginPose {
    
    private final IVecOrigin origin;
    
    /** @param origin already holds the (possibly interpolated) values to use */
    public VecOriginPose(IVecOrigin origin) {
        this.origin = origin;
    }
    
    public static IOriginPose of(IVecOrigin origin) {
        return new VecOriginPose(origin);
    }
    
    public static IOriginPose of(IVecOrigin origin, float partialTicks) {
        if (partialTicks >= 1 || !origin.hasChanged())
            return new VecOriginPose(origin);
        IVecOrigin snapshot = origin.copy();
        snapshot.off(lerp(origin.offXLast(), origin.offX(), partialTicks), lerp(origin.offYLast(), origin.offY(), partialTicks), lerp(origin.offZLast(), origin.offZ(), partialTicks));
        snapshot.rot(lerp(origin.rotXLast(), origin.rotX(), partialTicks), lerp(origin.rotYLast(), origin.rotY(), partialTicks), lerp(origin.rotZLast(), origin.rotZ(), partialTicks));
        return new VecOriginPose(snapshot);
    }
    
    private static double lerp(double last, double now, float partial) {
        return last + (now - last) * partial;
    }
    
    @Override
    public double translation(Axis axis) {
        return origin.translationCombined(axis);
    }
    
    @Override
    public void rotateWithoutCenter(Vec3d vec) {
        origin.onlyRotateWithoutCenter(vec);
    }
    
    @Override
    public void transform(Vec3d vec) {
        origin.transformPointToWorld(vec);
    }
    
    @Override
    public void transformInverse(Vec3d vec) {
        origin.transformPointToFakeWorld(vec);
    }
    
    /** UNVERIFIED: translate(off), translate(center - cam), rotate, translate(-center + cam) as in the old setupRenderingInternal. */
    @Override
    public Matrix4f transform(double camX, double camY, double camZ) {
        Vec3d center = origin.center();
        return new Matrix4f().translate((float) origin.offX(), (float) origin.offY(), (float) origin.offZ())
                .translate((float) (center.x - camX), (float) (center.y - camY), (float) (center.z - camZ))
                .rotate(new Quaternionf().rotationXYZ((float) Math.toRadians(origin.rotX()), (float) Math.toRadians(origin.rotY()), (float) Math.toRadians(origin.rotZ())))
                .translate((float) (-center.x + camX), (float) (-center.y + camY), (float) (-center.z + camZ));
    }
    
    @Override
    public Matrix4f transformInverse(double camX, double camY, double camZ) {
        return transform(camX, camY, camZ).invert();
    }
    
    @Override
    public Vec3 setup(Matrix4fStack matrixStack, Vec3 cam) {
        matrixStack.mul(transform(cam.x, cam.y, cam.z));
        return cam;
    }
    
    @Override
    public Vec3 setup(PoseStack matrixStack, Vec3 cam) {
        matrixStack.mulPose(transform(cam.x, cam.y, cam.z));
        return cam;
    }
    
}
