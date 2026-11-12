package team.creative.littletiles.mixin.client.render;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.creative.littletiles.fabric.client.ClientCameraEvents;

@Mixin(Camera.class)
public abstract class CameraFovMixin {
    @Inject(method = "calculateFov(F)F", at = @At("RETURN"), cancellable = true)
    private void littletiles$worldFov(float partialTick, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(ClientCameraEvents.WORLD_FOV.invoker().modify(cir.getReturnValueF()));
    }
}
