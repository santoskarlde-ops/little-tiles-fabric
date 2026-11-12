package team.creative.littletiles.mixin.common.entity;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.creative.littletiles.fabric.common.PlayerActionEvents;

@Mixin(ItemEntity.class)
public abstract class ItemEntityPickupMixin {
    @Inject(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"), cancellable = true)
    private void littletiles$handlePickup(Player player, CallbackInfo ci) {
        if (PlayerActionEvents.ITEM_PICKUP.invoker().handle(player, (ItemEntity) (Object) this))
            ci.cancel();
    }
}
