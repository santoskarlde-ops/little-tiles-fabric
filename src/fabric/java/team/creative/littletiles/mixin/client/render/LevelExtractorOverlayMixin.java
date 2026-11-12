package team.creative.littletiles.mixin.client.render;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.creative.littletiles.fabric.client.ClientPlayerRenderEvents;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorOverlayMixin {
    @Inject(method = "extractPlayerState(Lnet/minecraft/client/Camera;Lnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/renderer/state/level/PlayerRenderState;)V", at = @At("RETURN"))
    private void littletiles$afterPlayerExtraction(Camera camera, DeltaTracker delta, float partial, PlayerRenderState state, CallbackInfo ci) {
        ClientPlayerRenderEvents.AFTER_EXTRACT.invoker().afterExtract(state);
    }
    @Inject(method = "getViewBlockingState(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/client/renderer/culling/Frustum;)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("RETURN"), cancellable = true)
    private static void littletiles$filterBlockOverlay(LocalPlayer player, Frustum frustum, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = cir.getReturnValue();
        if (state != null && !ClientPlayerRenderEvents.BLOCK_OVERLAY.invoker().shouldRender(state))
            cir.setReturnValue(null);
    }
}

