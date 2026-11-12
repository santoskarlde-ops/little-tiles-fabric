package net.neoforged.neoforge.event.entity;
import net.neoforged.bus.api.Event;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
public class EntityJoinLevelEvent extends Event {
    public EntityJoinLevelEvent(Entity entity, LevelAccessor level) {}
    public EntityJoinLevelEvent(Entity entity, LevelAccessor level, boolean loadedFromDisk) {}
}
