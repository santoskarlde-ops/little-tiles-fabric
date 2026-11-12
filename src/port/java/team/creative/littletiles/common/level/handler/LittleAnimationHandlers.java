package team.creative.littletiles.common.level.handler;

import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.world.entity.Entity;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.level.LittleAnimationHandlerClient;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.server.level.handler.LittleAnimationHandlerServer;

public class LittleAnimationHandlers extends LevelHandlers<LittleAnimationHandler> {
    
    @Environment(EnvType.CLIENT)
    private static LittleAnimationHandler createClient(Level level) {
        return LittleTilesClient.ANIMATION_HANDLER = new LittleAnimationHandlerClient(level);
    }
    
    public LittleAnimationHandlers() {
        super(level -> {
            if (level.isClientSide())
                return createClient(level);
            return new LittleAnimationHandlerServer(level);
        });
        ServerTickEvents.START_LEVEL_TICK.register(this::tickLevel);
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> entityLoaded(entity));
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> entityUnloaded(entity));
        EntityTrackingEvents.START_TRACKING.register(this::trackEntity);
        EntityTrackingEvents.STOP_TRACKING.register(this::stopTrackEntity);
    }
    
    public LittleEntity find(boolean client, UUID uuid) {
        if (uuid == null)
            return null;
        for (LittleAnimationHandler handler : handlers(client)) {
            LittleEntity entity = handler.find(uuid);
            if (entity != null)
                return entity;
        }
        return null;
    }
    
    public void tickLevel(Level level) {
        get(level).tick();
    }

    public void entityLoaded(Entity entity) {
        if (entity instanceof LittleEntity little)
            little.onAddedToLevel();
    }

    public void entityUnloaded(Entity entity) {
        if (entity instanceof LittleEntity little)
            little.onRemovedFromLevel();
    }

    public void trackEntity(Entity entity, ServerPlayer player) {
        if (entity instanceof LittleEntity levelEntity) {
            LittleTiles.NETWORK.sendToClient(levelEntity.initClientPacket(), player);
            levelEntity.startTracking(player);
        }
    }

    public void stopTrackEntity(Entity entity, ServerPlayer player) {
        if (entity instanceof LittleEntity levelEntity)
            levelEntity.stopTracking(player);
    }
}
