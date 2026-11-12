package team.creative.littletiles.common.placement.setting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.fabric.PlayerPlacementData;
import team.creative.littletiles.common.packet.action.PlacementPlayerSettingPacket;
import team.creative.littletiles.common.placement.mode.PlacementMode;

public class PlacementPlayerSetting {
    
    public static final String SETTING_KEY = "littletiles:place";
    
    @Environment(EnvType.CLIENT)
    private static LittleGrid gridClient() {
        return LittleTilesClient.ACTION_HANDLER.setting.grid();
    }
    
    public static LittleGrid grid(Player player) {
        if (player.level().isClientSide())
            return gridClient();
        CompoundTag nbt = PlayerPlacementData.get(player);
        return LittleTiles.CONFIG.build.get(player).getOrDefault(nbt.getInt(LittleGrid.GRID_KEY).orElse(-1));
    }
    
    @Environment(EnvType.CLIENT)
    private static PlacementMode placementModeClient() {
        return LittleTilesClient.ACTION_HANDLER.setting.placementMode();
    }
    
    public static PlacementMode placementMode(Player player) {
        if (player.level().isClientSide())
            return placementModeClient();
        CompoundTag nbt = PlayerPlacementData.get(player);
        return PlacementMode.getMode(nbt.getString("mode").orElse(""));
    }
    
    protected PlacementMode mode;
    protected LittleGrid grid;
    private int configuredGrid;
    
    public PlacementPlayerSetting() {
        mode = PlacementMode.getDefault();
        configuredGrid = -1;
    }
    
    public PlacementPlayerSetting(CompoundTag nbt) {
        mode = PlacementMode.getMode(nbt.getString("mode").orElse(""));
        configuredGrid = nbt.getInt(LittleGrid.GRID_KEY).orElse(-1);
    }
    
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(LittleGrid.GRID_KEY, configuredGrid);
        nbt.putString("mode", mode.getId());
        return nbt;
    }
    
    public void receive(Player player, PlacementPlayerSetting settings) {
        this.configuredGrid = settings.configuredGrid;
        this.grid = settings.grid;
        this.mode = settings.mode;
        refreshGrid(player);
    }
    
    public LittleGrid grid() {
        return grid;
    }
    
    public PlacementMode placementMode() {
        return mode;
    }
    
    public void changed() {
        LittleTiles.NETWORK.sendToServer(new PlacementPlayerSettingPacket(this));
    }
    
    public void grid(LittleGrid grid) {
        this.grid = grid;
        this.configuredGrid = grid.count;
        changed();
    }
    
    public void placementMode(PlacementMode mode) {
        this.mode = mode;
        changed();
    }
    
    public void set(LittleGrid grid, PlacementMode mode) {
        this.grid = grid;
        this.configuredGrid = grid.count;
        this.mode = mode;
        changed();
    }
    
    public void refreshGrid(Player player) {
        grid = LittleTiles.CONFIG.build.get(player).getOrDefault(configuredGrid);
    }
    
}
