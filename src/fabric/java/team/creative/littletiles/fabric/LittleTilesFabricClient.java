package team.creative.littletiles.fabric;

import net.fabricmc.api.ClientModInitializer;

public class LittleTilesFabricClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        LittleTilesFabric.LOGGER.info("LittleTiles client skeleton initialised");
    }
}
