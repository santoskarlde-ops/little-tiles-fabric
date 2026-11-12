package team.creative.littletiles.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.creative.littletiles.fabric.client.ClientInputEvents;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerScrollMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "onScroll(JDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ScrollWheelHandler;onMouseScroll(DD)Lorg/joml/Vector2i;"), cancellable = true)
    private void littletiles$scrollInWorld(long window, double horizontal, double vertical, CallbackInfo ci) {
        // Vanilla reaches this point only for the active window, no overlay/screen, and a player.
        boolean discrete = minecraft.options.discreteMouseScroll().get();
        double sensitivity = minecraft.options.mouseWheelSensitivity().get();
        double x = (discrete ? Math.signum(horizontal) : horizontal) * sensitivity;
        double y = (discrete ? Math.signum(vertical) : vertical) * sensitivity;
        if (ClientInputEvents.WORLD_SCROLL.invoker().onScroll(x, y))
            ci.cancel();
    }
    @Redirect(method = "turnPlayer(D)V", at = @At(value = "INVOKE", target = "Ljava/lang/Double;doubleValue()D", ordinal = 0))
    private double littletiles$mouseSensitivity(Double sensitivity) {
        return ClientInputEvents.MOUSE_SENSITIVITY.invoker().applyAsDouble(sensitivity.doubleValue());
    }
}

