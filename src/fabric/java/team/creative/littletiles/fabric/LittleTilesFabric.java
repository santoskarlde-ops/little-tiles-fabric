package team.creative.littletiles.fabric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import team.creative.littletiles.LittleTilesRef;

/** Phase 1 entrypoint: loads, logs, does nothing else. Original mod by CreativeMD and N247S (LGPL-3.0). */
public class LittleTilesFabric implements ModInitializer {
    
    public static final Logger LOGGER = LoggerFactory.getLogger(LittleTilesRef.MOD_ID);
    
    @Override
    public void onInitialize() {
        PlayerPlacementData.init();
        LOGGER.info("LittleTiles (Fabric port) initialised - phase 1 skeleton");
    }
}
