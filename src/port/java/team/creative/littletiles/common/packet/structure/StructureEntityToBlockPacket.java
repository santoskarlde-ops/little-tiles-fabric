package team.creative.littletiles.common.packet.structure;

import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import team.creative.littletiles.client.render.level.transition.LittleLevelTransitionManagerClient;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.packet.entity.LittleEntityPacket;

public class StructureEntityToBlockPacket extends LittleEntityPacket<LittleAnimationEntity> {
    
    public StructureEntityToBlockPacket() {}
    
    public StructureEntityToBlockPacket(LittleAnimationEntity entity) {
        super(entity);
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public void execute(Player player, LittleAnimationEntity entity) {
        requiresClient(player);
        LittleLevelTransitionManagerClient.queue(entity.level(), entity);
    }
    
}
