package team.creative.littletiles.mixin.client.render;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.creative.littletiles.fabric.client.ClientFrameEvents;

@Mixin(GameRenderer.class)
public abstract class GameRendererFrameMixin {
    @Inject(method = "update(Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"))
    private void littletiles$startFrame(DeltaTracker delta, CallbackInfo ci) {
        ClientFrameEvents.START.invoker().run();
    }

    @Inject(method = "render()V", at = @At("RETURN"))
    private void littletiles$endFrame(CallbackInfo ci) {
        ClientFrameEvents.END.invoker().run();
    }
}
