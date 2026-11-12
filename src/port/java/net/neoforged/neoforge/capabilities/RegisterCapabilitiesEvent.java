package net.neoforged.neoforge.capabilities;

import java.util.function.BiFunction;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.Event;
import team.creative.littletiles.common.block.entity.BETiles;

public class RegisterCapabilitiesEvent extends Event {
    public void registerBlockEntity(Object capability, Object type, BiFunction<BETiles, Direction, ?> provider) {}
}
