package team.creative.littletiles.fabric.common;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/** Common-side callbacks at vanilla's mining and item-pickup decision points. */
public final class PlayerActionEvents {
    private PlayerActionEvents() {}

    @FunctionalInterface
    public interface MiningSpeed {
        float adjust(Player player, BlockState state, float speed);
    }

    @FunctionalInterface
    public interface ItemPickup {
        /** Return true if pickup was handled and vanilla insertion must be skipped. */
        boolean handle(Player player, ItemEntity entity);
    }

    public static final Event<MiningSpeed> MINING_SPEED = EventFactory.createArrayBacked(MiningSpeed.class, listeners -> (player, state, speed) -> {
        for (MiningSpeed listener : listeners)
            speed = listener.adjust(player, state, speed);
        return speed;
    });

    public static final Event<ItemPickup> ITEM_PICKUP = EventFactory.createArrayBacked(ItemPickup.class, listeners -> (player, entity) -> {
        for (ItemPickup listener : listeners)
            if (listener.handle(player, entity))
                return true;
        return false;
    });
}
