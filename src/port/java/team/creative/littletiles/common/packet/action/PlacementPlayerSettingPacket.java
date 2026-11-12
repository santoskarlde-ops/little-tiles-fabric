package team.creative.littletiles.common.packet.action;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.placement.setting.PlacementPlayerSetting;
import team.creative.littletiles.fabric.PlayerPlacementData;

public class PlacementPlayerSettingPacket extends CreativePacket {
    
    public PlacementPlayerSetting setting;
    
    public PlacementPlayerSettingPacket(PlacementPlayerSetting setting) {
        this.setting = setting;
    }
    
    public PlacementPlayerSettingPacket() {}
    
    @Override
    public void executeClient(Player player) {
        LittleTilesClient.ACTION_HANDLER.setting.receive(player, setting);
    }
    
    @Override
    public void executeServer(ServerPlayer player) {
        PlayerPlacementData.set(player, setting.save());
    }
    
}
