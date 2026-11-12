package team.creative.littletiles.mixin.common.entity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.creative.littletiles.fabric.common.PlayerActionEvents;

@Mixin(Player.class)
public abstract class PlayerMiningSpeedMixin {
    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void littletiles$adjustMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(PlayerActionEvents.MINING_SPEED.invoker().adjust((Player) (Object) this, state, cir.getReturnValueF()));
    }
}
