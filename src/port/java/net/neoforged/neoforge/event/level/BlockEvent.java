package net.neoforged.neoforge.event.level;
import net.neoforged.bus.api.Event;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.BlockSnapshot;
public class BlockEvent extends Event {
    public static class BreakEvent extends BlockEvent {
        public BreakEvent() {}
        public BreakEvent(Level level, BlockPos pos, BlockState state, Player player) {}
    }
    
    /** Compile-compatibility shape of NeoForge's EntityPlaceEvent. Like the other stubs it is not dispatched by any loader. */
    public static class EntityPlaceEvent extends BlockEvent {
        private final BlockSnapshot blockSnapshot;
        private final BlockState placedAgainst;
        private final Entity entity;
        
        public EntityPlaceEvent(BlockSnapshot blockSnapshot, BlockState placedAgainst, Entity entity) {
            this.blockSnapshot = blockSnapshot;
            this.placedAgainst = placedAgainst;
            this.entity = entity;
        }
        
        public BlockSnapshot getBlockSnapshot() {
            return blockSnapshot;
        }
        
        public BlockState getPlacedAgainst() {
            return placedAgainst;
        }
        
        public Entity getPlacingEntity() {
            return entity;
        }
    }
}
