package team.creative.littletiles.common.item;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.creative.littletiles.fabric.common.PlayerActionEvents;
import team.creative.littletiles.api.common.ingredient.ILittleIngredientInventory;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.source.LittleActionSource;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.ingredient.NotEnoughIngredientsException;

public class LittleItemHandler {
    
    public static void register() {
        PlayerActionEvents.MINING_SPEED.register((player, state, speed) ->
                player.getMainHandItem().getItem() instanceof ILittleTool ? 0 : speed);
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) ->
                !level.isClientSide() && player.getItemInHand(hand).getItem() instanceof ILittleTool
                        ? InteractionResult.FAIL : InteractionResult.PASS);
        PlayerActionEvents.ITEM_PICKUP.register(LittleItemHandler::onPickup);
    }

    private static boolean onPickup(Player player, ItemEntity entityItem) {
        ItemStack stack = entityItem.getItem();
        
        if (!entityItem.hasPickUpDelay() && stack.getItem() instanceof ILittleIngredientInventory inv && inv.shouldBeMerged()) {
            LittleIngredients ingredients = inv.getInventory(stack);
            LittleInventory inventory = new LittleInventory(player);
            inventory.allowDrop = false;
            
            if (ingredients == null) {
                entityItem.discard();
                return true;
            }
            
            try {
                if (LittleAction.canGive((LittleActionSource) player, inventory, ingredients)) {
                    LittleAction.give((LittleActionSource) player, inventory, ingredients);
                    
                    player.onItemPickup(entityItem);
                    entityItem.discard();
                    
                    player.level().playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((player.level().getRandom()
                            .nextFloat() - player.level().getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    return true;
                }
            } catch (NotEnoughIngredientsException e1) {}
        }
        return false;
    }
}
