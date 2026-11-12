package net.neoforged.neoforge.event.entity;
import net.neoforged.bus.api.Event;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
public class EntityLeaveLevelEvent extends Event {
    public EntityLeaveLevelEvent(Entity entity, LevelAccessor level) {}
}
