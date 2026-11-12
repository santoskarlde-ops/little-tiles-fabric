package team.creative.littletiles.common.placement.shape.config;

import java.util.List;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public abstract class LittleShapeConfig {
    
    public abstract List<Component> information();
    
    @Environment(EnvType.CLIENT)
    public abstract boolean react(Player player, KeyMapping key);
    
}
