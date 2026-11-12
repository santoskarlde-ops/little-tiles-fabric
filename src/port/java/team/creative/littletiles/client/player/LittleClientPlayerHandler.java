package team.creative.littletiles.client.player;

import java.util.BitSet;
import java.util.Iterator;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.sounds.GuardianAttackSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundCustomReportDetailsPacket;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ClientboundServerLinksPacket;
import net.minecraft.network.protocol.common.ClientboundStoreCookiePacket;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.cookie.ClientboundCookieRequestPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.level.ClientLevelExtender;
import team.creative.littletiles.client.level.little.LittleClientLevel;
import team.creative.littletiles.common.entity.animation.LittleAnimationLevel;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.packet.entity.LittleVanillaPacket;
import team.creative.littletiles.mixin.client.network.ClientPacketListenerAccessor;

public class LittleClientPlayerHandler implements TickablePacketListener, ClientGamePacketListener {
    
    private static final Logger LOGGER = LittleTiles.LOGGER;
    private static final Minecraft mc = Minecraft.getInstance();
    public Level level;
    
    public LittleClientLevel requiresClientLevel() {
        if (level instanceof LittleClientLevel s)
            return s;
        throw new RuntimeException("Cannot run this packet on this level " + level);
    }
    
    public ClientLevelExtender requiresClientLevelExtender() {
        if (level instanceof ClientLevelExtender s)
            return s;
        throw new RuntimeException("Cannot run this packet on this level " + level);
    }
    
    public void ensureRunningOnSameThread(Packet packet) throws RunningOnDifferentThreadException {
        if (!mc.isSameThread()) {
            mc.executeIfPossible(() -> LittleTilesClient.PLAYER_CONNECTION.runInContext((LittleLevel) level, x -> packet.handle(x)));
            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
        }
    }
    
    public ClientPacketListener vanilla() {
        return Minecraft.getInstance().getConnection();
    }
    
    public ClientPacketListenerAccessor vanillaAccessor() {
        return (ClientPacketListenerAccessor) Minecraft.getInstance().getConnection();
    }
    
    @Override
    public void handleLogin(ClientboundLoginPacket packet) {
        vanilla().handleLogin(packet);
    }
    
    @Override
    public void handleDisconnect(ClientboundDisconnectPacket packet) {
        vanilla().handleDisconnect(packet);
    }
    
    @Override
    public void onDisconnect(DisconnectionDetails details) {
        vanilla().onDisconnect(details);
    }
    
    @Override
    public void handleRespawn(ClientboundRespawnPacket packet) {
        vanilla().handleRespawn(packet);
    }
    
    @Override
    public void handleResourcePackPush(ClientboundResourcePackPushPacket packet) {
        vanilla().handleResourcePackPush(packet);
    }
    
    @Override
    public void handleResourcePackPop(ClientboundResourcePackPopPacket packet) {
        vanilla().handleResourcePackPop(packet);
    }
    
    @Override
    public void handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket packet) {
        vanilla().handlePlayerInfoUpdate(packet);
    }
    
    @Override
    public void handlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket packet) {
        vanilla().handlePlayerInfoRemove(packet);
    }
    
    @Override
    public void handleBlockDestruction(ClientboundBlockDestructionPacket packet) {
        ensureRunningOnSameThread(packet);
        level.destroyBlockProgress(packet.getId(), packet.getPos(), packet.getProgress());
    }
    
    @Override
    public void handleExplosion(ClientboundExplodePacket packet) {
        ensureRunningOnSameThread(packet);
        Vec3 center = packet.center();
        if (level instanceof ClientLevel clientLevel) {
            if (packet.playSound())
                clientLevel.playLocalSound(center.x(), center.y(), center.z(), packet.explosionSound().value(), SoundSource.BLOCKS, 4.0F,
                        (1.0F + (clientLevel.getRandom().nextFloat() - clientLevel.getRandom().nextFloat()) * 0.2F) * 0.7F, false);
            clientLevel.addParticle(packet.explosionParticle(), center.x(), center.y(), center.z(), 1.0D, 0.0D, 0.0D);
            clientLevel.trackExplosionEffects(center, packet.radius(), packet.blockCount(), packet.blockParticles());
        }
        packet.playerKnockback().ifPresent(knockback -> mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(knockback)));
    }
    
    @Override
    public void handleBlockEntityData(ClientboundBlockEntityDataPacket packet) {
        ensureRunningOnSameThread(packet);
        BlockPos blockpos = packet.getPos();
        level.getBlockEntity(blockpos, packet.getType()).ifPresent((x) -> {
            try (ProblemReporter.ScopedCollector problems = new ProblemReporter.ScopedCollector(x.problemPath(), org.slf4j.LoggerFactory.getLogger("LittleTiles"))) {
                x.loadWithComponents(TagValueInput.create(problems, level.registryAccess(), packet.getTag()));
            }
            
            if (x instanceof CommandBlockEntity && mc.gui.screen() instanceof CommandBlockEditScreen screen)
                screen.updateGui();
        });
    }
    
    @Override
    public void handleBlockEvent(ClientboundBlockEventPacket packet) {
        ensureRunningOnSameThread(packet);
        level.blockEvent(packet.getPos(), packet.getBlock(), packet.getB0(), packet.getB1());
    }
    
    @Override
    public void handleLevelEvent(ClientboundLevelEventPacket packet) {
        ensureRunningOnSameThread(packet);
        if (packet.isGlobalEvent())
            level.globalLevelEvent(packet.getType(), packet.getPos(), packet.getData());
        else
            level.levelEvent(packet.getType(), packet.getPos(), packet.getData());
        
    }
    
    @Override
    public void handleSoundEvent(ClientboundSoundPacket packet) {
        ensureRunningOnSameThread(packet);
        level.playSeededSound(mc.player, packet.getX(), packet.getY(), packet.getZ(), packet.getSound(), packet.getSource(), packet.getVolume(), packet.getPitch(), packet
                .getSeed());
    }
    
    @Override
    public void handleSoundEntityEvent(ClientboundSoundEntityPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = level.getEntity(packet.getId());
        if (entity != null)
            level.playSeededSound(mc.player, entity, packet.getSound(), packet.getSource(), packet.getVolume(), packet.getPitch(), packet.getSeed());
    }
    
    
    public void send(Packet<?> packet) {
        LittleTiles.NETWORK.sendToServer(new LittleVanillaPacket((LittleLevel) level, packet));
    }
    
    public void send(Packet<?> packet, @Nullable PacketSendListener listener) {
        send(packet);
    }
    
    @Nullable
    private Entity createEntityFromPacket(ClientboundAddEntityPacket packet) {
        EntityType<?> entitytype = packet.getType();
        if (entitytype.getBaseClass() == Player.class) {
            PlayerInfo playerinfo = vanilla().getPlayerInfo(packet.getUUID());
            if (playerinfo == null) {
                LOGGER.warn("Server attempted to add player prior to sending player info (Player id {})", packet.getUUID());
                return null;
            }
            return new RemotePlayer(requiresClientLevel(), playerinfo.getProfile());
        }
        return entitytype.create(this.level, net.minecraft.world.entity.EntitySpawnReason.LOAD);
    }
    
    @Override
    public void handleAddEntity(ClientboundAddEntityPacket packet) {
        ensureRunningOnSameThread(packet);
        
        Entity entity = LittleTilesClient.ANIMATION_HANDLER.pollEntityInTransition(packet);
        if (entity == null)
            entity = createEntityFromPacket(packet);
        
        if (entity != null) {
            entity.recreateFromPacket(packet);
            if (level instanceof LittleAnimationLevel a)
                a.addFreshEntityFromPacket(entity);
            else
                requiresClientLevel().addEntity(entity);
            vanillaAccessor().callPostAddEntitySoundInstance(entity);
        } else
            LOGGER.warn("Skipping Entity with id {}", packet.getType());
    }
    
    
    @Override
    public void handleSetEntityMotion(ClientboundSetEntityMotionPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.id());
        if (entity != null)
            entity.lerpMotion(packet.movement());
    }
    
    @Override
    public void handleSetEntityData(ClientboundSetEntityDataPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.id());
        if (entity != null)
            entity.getEntityData().assignValues(packet.packedItems());
    }
    
    @Override
    public void handleTeleportEntity(ClientboundTeleportEntityPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.id());
        if (entity == null)
            return;
        boolean hasRelative = packet.relatives().contains(net.minecraft.world.entity.Relative.X)
            || packet.relatives().contains(net.minecraft.world.entity.Relative.Y)
            || packet.relatives().contains(net.minecraft.world.entity.Relative.Z);
        boolean ticking = level instanceof ClientLevel client && client.isTickingEntity(entity);
        boolean interpolate = ticking || !entity.isLocalInstanceAuthoritative() || hasRelative;
        boolean interpolated = applyPositionPacket(entity, packet.change(), packet.relatives(), interpolate);
        entity.setOnGround(packet.onGround());
        if (!interpolated && entity.hasIndirectPassenger(mc.player)) {
            entity.positionRider(mc.player);
            mc.player.setOldPosAndRot();
            if (entity.isLocalInstanceAuthoritative())
                send(ServerboundMoveVehiclePacket.fromEntity(entity));
        }
    }

    @Override
    public void handleMoveEntity(ClientboundMoveEntityPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = packet.getEntity(this.level);
        if (entity == null)
            return;
        if (entity.isLocalInstanceAuthoritative()) {
            if (packet.hasPosition()) {
                VecDeltaCodec codec = entity.getPositionCodec();
                var position = packet.getPositionDelta().decode(codec);
                codec.setBase(position.endPosition());
            }
            return;
        }
        if (packet.hasPosition()) {
            VecDeltaCodec codec = entity.getPositionCodec();
            var position = packet.getPositionDelta().decode(codec);
            codec.setBase(position.endPosition());
            if (packet.hasRotation())
                entity.moveOrInterpolateTo(position, packet.getYRot(), packet.getXRot());
            else
                entity.moveOrInterpolateTo(position);
        } else if (packet.hasRotation())
            entity.moveOrInterpolateTo(packet.getYRot(), packet.getXRot());
        entity.setOnGround(packet.isOnGround());
    }

    @Override
    public void handleRotateMob(ClientboundRotateHeadPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = packet.getEntity(this.level);
        if (entity != null) {
            float f = packet.getYHeadRot() * 360 / 256.0F;
            entity.lerpHeadTo(f, 3);
        }
    }
    
    @Override
    public void handleRemoveEntities(ClientboundRemoveEntitiesPacket packet) {
        ensureRunningOnSameThread(packet);
        packet.entityIds().forEach(id -> ((LittleLevel) level).removeEntityById(id, Entity.RemovalReason.DISCARDED));
    }
    
    @Override
    public void handleMovePlayer(ClientboundPlayerPositionPacket packet) {
        ensureRunningOnSameThread(packet);
        Player player = mc.player;
        if (!player.isPassenger())
            applyPositionPacket(player, packet.change(), packet.relatives(), false);
        send(new ServerboundAcceptTeleportationPacket(packet.id(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot()));
        if (mc.gameMode != null)
            mc.gameMode.stopDestroyBlock();
    }

    private static boolean applyPositionPacket(Entity entity, PositionMoveRotation change,
            java.util.Set<net.minecraft.world.entity.Relative> relatives, boolean interpolate) {
        PositionMoveRotation current = PositionMoveRotation.of(entity);
        PositionMoveRotation updated = PositionMoveRotation.calculateAbsolute(current, change, relatives);
        if (interpolate && !(current.position().distanceToSqr(updated.position()) > 4096.0D)) {
            entity.moveOrInterpolateTo(updated.position(), updated.yRot(), updated.xRot());
            entity.setDeltaMovement(updated.deltaMovement());
            return true;
        }
        entity.setPos(updated.position());
        entity.setDeltaMovement(updated.deltaMovement());
        entity.setYRot(updated.yRot());
        entity.setXRot(updated.xRot());
        PositionMoveRotation old = new PositionMoveRotation(entity.oldPosition(), Vec3.ZERO, entity.yRotO, entity.xRotO);
        PositionMoveRotation oldUpdated = PositionMoveRotation.calculateAbsolute(old, change, relatives);
        entity.setOldPosAndRot(oldUpdated.position(), oldUpdated.yRot(), oldUpdated.xRot());
        return false;
    }

    @Override
    public void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket packet) {
        ensureRunningOnSameThread(packet);
        ClientLevel level = requiresClientLevel();
        packet.runUpdates((p_205524_, p_205525_) -> level.setServerVerifiedBlockState(p_205524_, p_205525_, 19));
    }
    
    @Override
    public void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket packet) {
        ensureRunningOnSameThread(packet);
        ClientLevel level = requiresClientLevel();
        this.updateLevelChunk(level, packet.x(), packet.z(), packet.chunkData());
        this.queueLightUpdate(level, packet.x(), packet.z(), packet.lightData());
    }
    
    @Override
    public void handleChunksBiomes(ClientboundChunksBiomesPacket packet) {
        ensureRunningOnSameThread(packet);
        LittleClientLevel level = requiresClientLevel();
        for (ClientboundChunksBiomesPacket.ChunkBiomeData clientboundchunksbiomespacket$chunkbiomedata : packet.chunkBiomeData())
            level.getChunkSource().replaceBiomes(clientboundchunksbiomespacket$chunkbiomedata.pos().x(), clientboundchunksbiomespacket$chunkbiomedata.pos().z(),
                clientboundchunksbiomespacket$chunkbiomedata.getReadBuffer());
        
        for (ClientboundChunksBiomesPacket.ChunkBiomeData clientboundchunksbiomespacket$chunkbiomedata1 : packet.chunkBiomeData())
            level.onChunkLoaded(new ChunkPos(clientboundchunksbiomespacket$chunkbiomedata1.pos().x(), clientboundchunksbiomespacket$chunkbiomedata1.pos().z()));
        
        for (ClientboundChunksBiomesPacket.ChunkBiomeData clientboundchunksbiomespacket$chunkbiomedata2 : packet.chunkBiomeData())
            for (int i = -1; i <= 1; ++i)
                for (int j = -1; j <= 1; ++j)
                    for (int k = this.level.getMinSectionY(); k <= this.level.getMaxSectionY(); ++k)
                        level.setSectionDirty(clientboundchunksbiomespacket$chunkbiomedata2.pos().x() + i, k, clientboundchunksbiomespacket$chunkbiomedata2.pos().z() + j);
    }
    
    private void updateLevelChunk(ClientLevel level, int x, int z, ClientboundLevelChunkPacketData data) {
        level.getChunkSource().replaceWithPacketData(x, z, data);
    }
    
    private void queueLightUpdate(ClientLevel level, int x, int z, ClientboundLightUpdatePacketData data) {
        level.queueLightUpdate(() -> {
            this.applyLightData(level, x, z, data);
            LevelChunk levelchunk = this.level.getChunkSource().getChunk(x, z, false);
            if (levelchunk != null) {
                this.enableChunkLight(level, levelchunk, x, z);
            }
            
        });
    }
    
    private void applyLightData(ClientLevel level, int x, int z, ClientboundLightUpdatePacketData data) {
        LevelLightEngine levellightengine = this.level.getChunkSource().getLightEngine();
        BitSet bitset = data.skyYMask();
        BitSet bitset1 = data.emptySkyYMask();
        Iterator<byte[]> iterator = data.skyUpdates().iterator();
        this.readSectionList(level, x, z, levellightengine, LightLayer.SKY, bitset, bitset1, iterator);
        BitSet bitset2 = data.blockYMask();
        BitSet bitset3 = data.emptyBlockYMask();
        Iterator<byte[]> iterator1 = data.blockUpdates().iterator();
        this.readSectionList(level, x, z, levellightengine, LightLayer.BLOCK, bitset2, bitset3, iterator1);
        levellightengine.setLightEnabled(new ChunkPos(x, z), true);
    }
    
    private void readSectionList(ClientLevel level, int x, int z, LevelLightEngine light, LightLayer layer, BitSet minSet, BitSet maxSet, Iterator<byte[]> data) {
        for (int i = 0; i < light.getLightSectionCount(); ++i) {
            int j = light.getMinLightSection() + i;
            boolean flag = minSet.get(i);
            boolean flag1 = maxSet.get(i);
            if (flag || flag1) {
                light.queueSectionData(layer, SectionPos.of(x, j, z), flag ? new DataLayer(data.next().clone()) : new DataLayer());
                level.setSectionDirtyWithNeighbors(x, j, z);
            }
        }
        
    }
    
    private void enableChunkLight(ClientLevel level, LevelChunk chunk, int x, int z) {
        LevelLightEngine levellightengine = this.level.getChunkSource().getLightEngine();
        LevelChunkSection[] alevelchunksection = chunk.getSections();
        ChunkPos chunkpos = chunk.getPos();
        
        for (int i = 0; i < alevelchunksection.length; ++i) {
            LevelChunkSection levelchunksection = alevelchunksection[i];
            int j = this.level.getSectionYFromSectionIndex(i);
            levellightengine.updateSectionStatus(SectionPos.of(chunkpos, j), levelchunksection.hasOnlyAir());
            level.setSectionDirtyWithNeighbors(x, j, z);
        }
    }
    
    @Override
    public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket packet) {
        ensureRunningOnSameThread(packet);
        ClientLevel level = requiresClientLevel();
        ChunkSource chunkSource = this.level.getChunkSource();
        if (chunkSource instanceof ClientChunkCache client)
            client.drop(packet.pos());
        this.queueLightRemoval(level, packet);
    }
    
    private void queueLightRemoval(ClientLevel level, ClientboundForgetLevelChunkPacket packet) {
        ChunkPos chunkpos = packet.pos();
        level.queueLightUpdate(() -> {
            LevelLightEngine levellightengine = this.level.getLightEngine();
            levellightengine.setLightEnabled(chunkpos, false);
            
            for (int i = levellightengine.getMinLightSection(); i < levellightengine.getMaxLightSection(); ++i) {
                SectionPos sectionpos = SectionPos.of(chunkpos, i);
                levellightengine.queueSectionData(LightLayer.BLOCK, sectionpos, (DataLayer) null);
                levellightengine.queueSectionData(LightLayer.SKY, sectionpos, (DataLayer) null);
            }
            
            for (int j = this.level.getMinSectionY(); j <= this.level.getMaxSectionY(); ++j)
                levellightengine.updateSectionStatus(SectionPos.of(chunkpos, j), true);
        });
    }
    
    @Override
    public void handleBlockUpdate(ClientboundBlockUpdatePacket packet) {
        ensureRunningOnSameThread(packet);
        ClientLevelExtender level = requiresClientLevelExtender();
        level.setServerVerifiedBlockStateExtender(packet.getPos(), packet.getBlockState(), 19);
    }
    
    @Override
    public void handleTakeItemEntity(ClientboundTakeItemEntityPacket packet) {
        ensureRunningOnSameThread(packet);
        ClientLevel level = requiresClientLevel();
        Entity entity = level.getEntity(packet.getItemId());
        LivingEntity livingentity = (LivingEntity) this.level.getEntity(packet.getPlayerId());
        if (livingentity == null)
            livingentity = mc.player;
        
        if (entity != null) {
            RandomSource random = vanillaAccessor().getRandom();
            if (entity instanceof ExperienceOrb)
                level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1F, (random.nextFloat() - random
                        .nextFloat()) * 0.35F + 0.9F, false);
            else
                level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, (random.nextFloat() - random
                        .nextFloat()) * 1.4F + 2.0F, false);
            
            mc.particleEngine.add(new ItemPickupParticle(requiresClientLevel(), mc.getEntityRenderDispatcher().extractEntity(entity, 1.0F), entity, entity.getDeltaMovement()));
            if (entity instanceof ItemEntity itemEntity) {
                ItemStack itemstack = itemEntity.getItem();
                itemstack.shrink(packet.getAmount());
                if (itemstack.isEmpty())
                    level.removeEntity(packet.getItemId(), Entity.RemovalReason.DISCARDED);
            } else if (!(entity instanceof ExperienceOrb))
                level.removeEntity(packet.getItemId(), Entity.RemovalReason.DISCARDED);
        }
        
    }
    
    @Override
    public void handleSystemChat(ClientboundSystemChatPacket packet) {
        vanilla().handleSystemChat(packet);
    }
    
    @Override
    public void handlePlayerChat(ClientboundPlayerChatPacket packet) {
        vanilla().handlePlayerChat(packet);
    }
    
    @Override
    public void handleDisguisedChat(ClientboundDisguisedChatPacket packet) {
        vanilla().handleDisguisedChat(packet);
    }
    
    @Override
    public void handleDeleteChat(ClientboundDeleteChatPacket packet) {
        vanilla().handleDeleteChat(packet);
    }
    
    @Override
    public void handleAnimate(ClientboundAnimatePacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.getId());
        if (entity == null)
            return;
        if (packet.getAction() == 0)
            ((Player) entity).stopSleepInBed(false, false);
        else if (packet.getAction() == 1)
            mc.particleEngine.createTrackingEmitter(entity, ParticleTypes.CRIT);
        else if (packet.getAction() == 2)
            mc.particleEngine.createTrackingEmitter(entity, ParticleTypes.ENCHANTED_HIT);
    }

    @Override
    public void handleHurtAnimation(ClientboundHurtAnimationPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.id());
        if (entity != null)
            entity.animateHurt(packet.yaw());
    }
    
    @Override
    public void handleSetTime(ClientboundSetTimePacket packet) {
        vanilla().handleSetTime(packet);
    }
    
    @Override
    public void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket packet) {
        vanilla().handleSetSpawn(packet);
    }
    
    @Override
    public void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.getVehicle());
        if (entity == null) {
            LOGGER.warn("Received passengers for unknown entity");
            return;
        }
        
        Player player = mc.player;
        boolean flag = entity.hasIndirectPassenger(player);
        entity.ejectPassengers();
        
        for (int i : packet.getPassengers()) {
            Entity entity1 = this.level.getEntity(i);
            if (entity1 != null) {
                entity1.startRiding(entity, true, false);
                if (entity1 == player && !flag) {
                    if (entity instanceof AbstractBoat) {
                        player.yRotO = entity.getYRot();
                        player.setYRot(entity.getYRot());
                        player.setYHeadRot(entity.getYRot());
                    }
                    
                    Component component = Component.translatable("mount.onboard", mc.options.keyShift.getTranslatedKeyMessage());
                    mc.gui.hud.setOverlayMessage(component, false);
                    mc.getNarrator().saySystemNow(component);
                }
            }
            
        }
    }
    
    @Override
    public void handleEntityLinkPacket(ClientboundSetEntityLinkPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.getSourceId());
        if (entity instanceof Mob)
            ((Mob) entity).setDelayedLeashHolderId(packet.getDestId());
    }
    
    private static ItemStack findTotem(Player p_104928_) {
        for (InteractionHand interactionhand : InteractionHand.values()) {
            ItemStack itemstack = p_104928_.getItemInHand(interactionhand);
            if (itemstack.is(Items.TOTEM_OF_UNDYING)) {
                return itemstack;
            }
        }
        
        return new ItemStack(Items.TOTEM_OF_UNDYING);
    }
    
    @Override
    public void handleEntityEvent(ClientboundEntityEventPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = packet.getEntity(this.level);
        if (entity != null) {
            if (packet.getEventId() == 21)
                mc.getSoundManager().play(new GuardianAttackSoundInstance((Guardian) entity));
            else if (packet.getEventId() == 35) {
                mc.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
                this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);
                if (entity == mc.player)
                    mc.player.displayItemActivation(findTotem(mc.player));
            } else
                entity.handleEntityEvent(packet.getEventId());
        }
        
    }
    
    @Override
    public void handleDamageEvent(ClientboundDamageEventPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.entityId());
        if (entity != null)
            entity.handleDamageEvent(packet.getSource(this.level));
    }
    
    @Override
    public void handleSetHealth(ClientboundSetHealthPacket packet) {
        vanilla().handleSetHealth(packet);
    }
    
    @Override
    public void handleSetExperience(ClientboundSetExperiencePacket packet) {
        vanilla().handleSetExperience(packet);
    }
    
    
    @Override
    public void handleOpenScreen(ClientboundOpenScreenPacket packet) {
        vanilla().handleOpenScreen(packet);
    }
    
    @Override
    public void handleContainerSetSlot(ClientboundContainerSetSlotPacket packet) {
        vanilla().handleContainerSetSlot(packet);
    }
    
    @Override
    public void handleContainerContent(ClientboundContainerSetContentPacket packet) {
        vanilla().handleContainerContent(packet);
    }
    
    @Override
    public void handleOpenSignEditor(ClientboundOpenSignEditorPacket packet) {
        ensureRunningOnSameThread(packet);
        BlockPos blockpos = packet.pos();
        BlockEntity blockentity = this.level.getBlockEntity(blockpos);
        if (!(blockentity instanceof SignBlockEntity)) {
            BlockState blockstate = this.level.getBlockState(blockpos);
            blockentity = new SignBlockEntity(blockpos, blockstate);
            blockentity.setLevel(this.level);
        }
        
        mc.player.openTextEdit((SignBlockEntity) blockentity, packet.slot());
    }
    
    @Override
    public void handleContainerSetData(ClientboundContainerSetDataPacket packet) {
        vanilla().handleContainerSetData(packet);
    }
    
    @Override
    public void handleSetEquipment(ClientboundSetEquipmentPacket packet) {
        ensureRunningOnSameThread(packet);
        if (this.level.getEntity(packet.getEntity()) instanceof LivingEntity living)
            packet.getSlots().forEach((slot) -> living.setItemSlot(slot.getFirst(), slot.getSecond()));
    }
    
    @Override
    public void handleContainerClose(ClientboundContainerClosePacket packet) {
        vanilla().handleContainerClose(packet);
    }
    
    @Override
    public void handleGameEvent(ClientboundGameEventPacket packet) {
        vanilla().handleGameEvent(packet);
    }
    
    @Override
    public void handleMapItemData(ClientboundMapItemDataPacket packet) {
        vanilla().handleMapItemData(packet);
    }
    
    @Override
    public void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket packet) {
        vanilla().handleUpdateAdvancementsPacket(packet);
    }
    
    @Override
    public void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket packet) {
        vanilla().handleSelectAdvancementsTab(packet);
    }
    
    @Override
    public void handleCommands(ClientboundCommandsPacket packet) {
        vanilla().handleCommands(packet);
    }
    
    @Override
    public void handleStopSoundEvent(ClientboundStopSoundPacket packet) {
        vanilla().handleStopSoundEvent(packet);
    }
    
    @Override
    public void handleCommandSuggestions(ClientboundCommandSuggestionsPacket packet) {
        vanilla().handleCommandSuggestions(packet);;
    }
    
    @Override
    public void handleUpdateRecipes(ClientboundUpdateRecipesPacket packet) {
        vanilla().handleUpdateRecipes(packet);
    }
    
    @Override
    public void handleLookAt(ClientboundPlayerLookAtPacket packet) {
        ensureRunningOnSameThread(packet);
        Vec3 vec3 = packet.getPosition(this.level);
        if (vec3 != null)
            mc.player.lookAt(packet.getFromAnchor(), vec3);
    }
    
    @Override
    public void handleTagQueryPacket(ClientboundTagQueryPacket packet) {
        vanilla().handleTagQueryPacket(packet);
    }
    
    @Override
    public void handleAwardStats(ClientboundAwardStatsPacket packet) {
        vanilla().handleAwardStats(packet);
    }
    
    
    @Override
    public void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.getEntityId());
        if (entity instanceof LivingEntity living) {
            Holder<MobEffect> mobeffect = packet.getEffect();
            MobEffectInstance instance = new MobEffectInstance(mobeffect, packet.getEffectDurationTicks(), packet.getEffectAmplifier(), packet.isEffectAmbient(), packet
                    .isEffectVisible(), packet.effectShowsIcon(), (MobEffectInstance) null);
            if (!packet.shouldBlend())
                instance.skipBlending();
            living.forceAddEffect(instance, (Entity) null);
        }
    }
    
    @Override
    public void handleUpdateTags(ClientboundUpdateTagsPacket packet) {
        vanilla().handleUpdateTags(packet);
    }
    
    @Override
    public void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket packet) {}
    
    @Override
    public void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket packet) {}
    
    @Override
    public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.playerId());
        if (entity == mc.player)
            if (mc.player.shouldShowDeathScreen())
                mc.gui.setScreen(new DeathScreen(packet.message(), this.level.getLevelData().isHardcore(), mc.player));
            else
                mc.player.respawn();
    }
    
    @Override
    public void handleChangeDifficulty(ClientboundChangeDifficultyPacket packet) {
        vanilla().handleChangeDifficulty(packet);
    }
    
    @Override
    public void handleSetCamera(ClientboundSetCameraPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = packet.getEntity(this.level);
        if (entity != null)
            mc.setCameraEntity(entity);
    }
    
    @Override
    public void handleInitializeBorder(ClientboundInitializeBorderPacket packet) {
        vanilla().handleInitializeBorder(packet);
    }
    
    @Override
    public void handleSetBorderCenter(ClientboundSetBorderCenterPacket packet) {
        vanilla().handleSetBorderCenter(packet);
    }
    
    @Override
    public void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket packet) {
        vanilla().handleSetBorderLerpSize(packet);
    }
    
    @Override
    public void handleSetBorderSize(ClientboundSetBorderSizePacket packet) {
        vanilla().handleSetBorderSize(packet);
    }
    
    @Override
    public void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket packet) {
        vanilla().handleSetBorderWarningDistance(packet);
    }
    
    @Override
    public void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket packet) {
        vanilla().handleSetBorderWarningDelay(packet);
    }
    
    @Override
    public void handleTitlesClear(ClientboundClearTitlesPacket packet) {
        vanilla().handleTitlesClear(packet);
    }
    
    @Override
    public void handleServerData(ClientboundServerDataPacket packet) {
        vanilla().handleServerData(packet);
    }
    
    @Override
    public void handleCustomChatCompletions(ClientboundCustomChatCompletionsPacket packet) {
        vanilla().handleCustomChatCompletions(packet);
    }
    
    @Override
    public void setActionBarText(ClientboundSetActionBarTextPacket packet) {
        vanilla().setActionBarText(packet);
    }
    
    @Override
    public void setTitleText(ClientboundSetTitleTextPacket packet) {
        vanilla().setTitleText(packet);
    }
    
    @Override
    public void setSubtitleText(ClientboundSetSubtitleTextPacket packet) {
        vanilla().setSubtitleText(packet);
    }
    
    @Override
    public void setTitlesAnimation(ClientboundSetTitlesAnimationPacket packet) {
        vanilla().setTitlesAnimation(packet);
    }
    
    @Override
    public void handleTabListCustomisation(ClientboundTabListPacket packet) {
        vanilla().handleTabListCustomisation(packet);
    }
    
    @Override
    public void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = packet.getEntity(this.level);
        if (entity instanceof LivingEntity living)
            living.removeEffectNoUpdate(packet.effect());
    }
    
    @Override
    public void handleKeepAlive(ClientboundKeepAlivePacket packet) {
        vanilla().handleKeepAlive(packet);
    }
    
    @Override
    public void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket packet) {
        vanilla().handlePlayerAbilities(packet);
    }
    
    @Override
    public void handleBossUpdate(ClientboundBossEventPacket packet) {
        vanilla().handleBossUpdate(packet);
    }
    
    @Override
    public void handleItemCooldown(ClientboundCooldownPacket packet) {
        vanilla().handleItemCooldown(packet);
    }
    
    @Override
    public void handleMoveVehicle(ClientboundMoveVehiclePacket packet) {
        ensureRunningOnSameThread(packet);
        Entity vehicle = mc.player.getRootVehicle();
        if (vehicle != mc.player && vehicle.isLocalInstanceAuthoritative()) {
            var target = packet.movingTo();
            Vec3 targetPos = target.position();
            if (targetPos.distanceTo(vehicle.getClientPosition()) > (double) 1.0E-5F) {
                if (vehicle.isInterpolating())
                    vehicle.getInterpolation().cancel();
                vehicle.absSnapTo(targetPos.x(), targetPos.y(), targetPos.z(), target.yRot(), target.xRot());
            }
            send(ServerboundMoveVehiclePacket.fromEntity(vehicle));
        }
    }

    @Override
    public void handleOpenBook(ClientboundOpenBookPacket packet) {
        vanilla().handleOpenBook(packet);
    }
    
    @Override
    public void handleCustomPayload(ClientboundCustomPayloadPacket packet) {
        vanilla().handleCustomPayload(packet);
    }
    
    @Override
    public void handleAddObjective(ClientboundSetObjectivePacket packet) {
        vanilla().handleAddObjective(packet);
    }
    
    @Override
    public void handleSetScore(ClientboundSetScorePacket packet) {
        vanilla().handleSetScore(packet);
    }
    
    @Override
    public void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket packet) {
        vanilla().handleSetDisplayObjective(packet);
    }
    
    @Override
    public void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket packet) {
        vanilla().handleSetPlayerTeamPacket(packet);
    }
    
    @Override
    public void handleParticleEvent(ClientboundLevelParticlesPacket packet) {
        ensureRunningOnSameThread(packet);
        if (packet.count() == 0) {
            tryAddLittleParticle(packet, packet.x(), packet.y(), packet.z(),
                packet.xMaxSpeed() * packet.xDist(), packet.yMaxSpeed() * packet.yDist(), packet.zMaxSpeed() * packet.zDist());
            return;
        }
        RandomSource random = vanillaAccessor().getRandom();
        var randomization = packet.randomizationType();
        for (int i = 0; i < packet.count(); ++i) {
            double dx, dy, dz, vx, vy, vz;
            if (randomization.isAlternative()) {
                dx = random.nextDouble() * packet.xDist();
                dy = random.nextDouble() * packet.yDist();
                dz = random.nextDouble() * packet.zDist();
                vx = packet.xMaxSpeed();
                vy = packet.yMaxSpeed();
                vz = packet.zMaxSpeed();
                if (randomization == ClientboundLevelParticlesPacket.RandomizationType.ALTERNATIVE_WITH_SPEED) {
                    vx *= random.nextDouble();
                    vy *= random.nextDouble();
                    vz *= random.nextDouble();
                }
            } else {
                dx = random.nextGaussian() * packet.xDist();
                dy = random.nextGaussian() * packet.yDist();
                dz = random.nextGaussian() * packet.zDist();
                vx = random.nextGaussian() * packet.xMaxSpeed();
                vy = random.nextGaussian() * packet.yMaxSpeed();
                vz = random.nextGaussian() * packet.zMaxSpeed();
            }
            if (!tryAddLittleParticle(packet, packet.x() + dx, packet.y() + dy, packet.z() + dz, vx, vy, vz))
                return;
        }
    }

    private boolean tryAddLittleParticle(ClientboundLevelParticlesPacket packet, double x, double y, double z, double vx, double vy, double vz) {
        try {
            this.level.addParticle(packet.particle(), packet.overrideLimiter(), packet.alwaysShow(), x, y, z, vx, vy, vz);
            return true;
        } catch (Throwable throwable) {
            LOGGER.warn("Could not spawn particle effect {}", packet.particle());
            return false;
        }
    }

    @Override
    public void handlePing(ClientboundPingPacket packet) {
        vanilla().handlePing(packet);
    }
    
    @Override
    public void handleUpdateAttributes(ClientboundUpdateAttributesPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.getEntityId());
        if (entity != null) {
            if (!(entity instanceof LivingEntity))
                throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + entity + ")");
            
            AttributeMap attributemap = ((LivingEntity) entity).getAttributes();
            
            for (ClientboundUpdateAttributesPacket.AttributeSnapshot attribute : packet.getValues()) {
                AttributeInstance attributeinstance = attributemap.getInstance(attribute.attribute());
                if (attributeinstance == null)
                    LOGGER.warn("Entity {} does not have attribute {}", entity, attribute.attribute().getRegisteredName());
                else {
                    attributeinstance.setBaseValue(attribute.base());
                    attributeinstance.removeModifiers();
                    
                    for (AttributeModifier attributemodifier : attribute.modifiers())
                        attributeinstance.addTransientModifier(attributemodifier);
                }
            }
        }
    }
    
    @Override
    public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket packet) {
        vanilla().handlePlaceRecipe(packet);
    }
    
    @Override
    public void handleLightUpdatePacket(ClientboundLightUpdatePacket packet) {
        ensureRunningOnSameThread(packet);
        ClientLevel level = requiresClientLevel();
        level.queueLightUpdate(() -> this.applyLightData(level, packet.x(), packet.z(), packet.lightData()));
    }
    
    @Override
    public void handleMerchantOffers(ClientboundMerchantOffersPacket packet) {
        vanilla().handleMerchantOffers(packet);
    }
    
    @Override
    public void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket packet) {
        vanilla().handleSetChunkCacheRadius(packet);
    }
    
    @Override
    public void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket packet) {
        vanilla().handleSetSimulationDistance(packet);
    }
    
    @Override
    public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket packet) {
        vanilla().handleSetChunkCacheCenter(packet);
    }
    
    @Override
    public void handleBundlePacket(ClientboundBundlePacket packet) {
        ensureRunningOnSameThread(packet);
        for (Packet<? super ClientGamePacketListener> sub : packet.subPackets())
            sub.handle(this);
    }
    
    @Override
    public void handleBlockChangedAck(ClientboundBlockChangedAckPacket packet) {
        ensureRunningOnSameThread(packet);
        ClientLevelExtender level = requiresClientLevelExtender();
        level.handleBlockChangedAckExtender(packet.sequence());
    }
    
    @Override
    public void tick() {}
    
    @Override
    public boolean isAcceptingMessages() {
        return vanilla().isAcceptingMessages();
    }
    
    @Override
    public void handlePongResponse(ClientboundPongResponsePacket packet) {
        vanilla().handlePongResponse(packet);
    }
    
    @Override
    public void handleStoreCookie(ClientboundStoreCookiePacket packet) {
        vanilla().handleStoreCookie(packet);
    }
    
    @Override
    public void handleTransfer(ClientboundTransferPacket packet) {
        vanilla().handleTransfer(packet);
    }
    
    @Override
    public void handleCustomReportDetails(ClientboundCustomReportDetailsPacket packet) {
        vanilla().handleCustomReportDetails(packet);
    }
    
    @Override
    public void handleServerLinks(ClientboundServerLinksPacket packet) {
        vanilla().handleServerLinks(packet);
    }
    
    @Override
    public void handleRequestCookie(ClientboundCookieRequestPacket packet) {
        vanilla().handleRequestCookie(packet);
    }
    
    
    @Override
    public void handleResetScore(ClientboundResetScorePacket packet) {
        vanilla().handleResetScore(packet);
    }
    
    @Override
    public void handleTickingState(ClientboundTickingStatePacket packet) {
        ensureRunningOnSameThread(packet);
        TickRateManager tickratemanager = level.tickRateManager();
        tickratemanager.setTickRate(packet.tickRate());
        tickratemanager.setFrozen(packet.isFrozen());
    }
    
    @Override
    public void handleTickingStep(ClientboundTickingStepPacket packet) {
        ensureRunningOnSameThread(packet);
        if (level != null) {
            TickRateManager tickratemanager = level.tickRateManager();
            tickratemanager.setFrozenTicksToRun(packet.tickSteps());
        }
    }
    
    @Override
    public void handleConfigurationStart(ClientboundStartConfigurationPacket packet) {
        vanilla().handleConfigurationStart(packet);
    }
    
    @Override
    public void handleChunkBatchStart(ClientboundChunkBatchStartPacket packet) {
        vanilla().handleChunkBatchStart(packet);
    }
    
    @Override
    public void handleChunkBatchFinished(ClientboundChunkBatchFinishedPacket packet) {
        vanilla().handleChunkBatchFinished(packet);
    }
    
    @Override
    public void handleDebugSample(ClientboundDebugSamplePacket packet) {
        vanilla().handleDebugSample(packet);
    }
    
    @Override
    public void handleProjectilePowerPacket(ClientboundProjectilePowerPacket packet) {
        if (level.getEntity(packet.getId()) instanceof AbstractHurtingProjectile abstracthurtingprojectile)
            abstracthurtingprojectile.accelerationPower = packet.getAccelerationPower();
    }
    
    @Override
    public void handleSwingAnimation(net.minecraft.network.protocol.game.ClientboundSwingAnimationPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level.getEntity(packet.entityId());
        if (entity instanceof LivingEntity living)
            living.swing(packet.hand(), packet.animation(), false);
    }
    
    @Override
    public void handleRecipeBookAdd(net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket packet) {
        vanilla().handleRecipeBookAdd(packet);
    }
    
    @Override
    public void handleRecipeBookRemove(net.minecraft.network.protocol.game.ClientboundRecipeBookRemovePacket packet) {
        vanilla().handleRecipeBookRemove(packet);
    }
    
    @Override
    public void handleRecipeBookSettings(net.minecraft.network.protocol.game.ClientboundRecipeBookSettingsPacket packet) {
        vanilla().handleRecipeBookSettings(packet);
    }
    
    @Override
    public void handleMountScreenOpen(net.minecraft.network.protocol.game.ClientboundMountScreenOpenPacket packet) {
        vanilla().handleMountScreenOpen(packet);
    }
    
    @Override
    public void handleAddTransientBlockPacket(net.minecraft.network.protocol.game.ClientboundAddTransientBlockPacket packet) {
        vanilla().handleAddTransientBlockPacket(packet);
    }
    
    @Override
    public void handleMinecartAlongTrack(net.minecraft.network.protocol.game.ClientboundMoveMinecartPacket packet) {
        vanilla().handleMinecartAlongTrack(packet);
    }
    
    @Override
    public void handleRotatePlayer(net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket packet) {
        vanilla().handleRotatePlayer(packet);
    }
    
    @Override
    public void handleGameRuleValues(net.minecraft.network.protocol.game.ClientboundGameRuleValuesPacket packet) {
        vanilla().handleGameRuleValues(packet);
    }
    
    @Override
    public void handleSetHeldSlot(net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket packet) {
        vanilla().handleSetHeldSlot(packet);
    }
    
    @Override
    public void handleEntityPositionSync(net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket packet) {
        vanilla().handleEntityPositionSync(packet);
    }
    
    @Override
    public void handleSetCursorItem(net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket packet) {
        vanilla().handleSetCursorItem(packet);
    }
    
    @Override
    public void handleSetPlayerInventory(net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket packet) {
        vanilla().handleSetPlayerInventory(packet);
    }
    
    @Override
    public void handleTestInstanceBlockStatus(net.minecraft.network.protocol.game.ClientboundTestInstanceBlockStatus packet) {
        vanilla().handleTestInstanceBlockStatus(packet);
    }
    
    @Override
    public void handleWaypoint(net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket packet) {
        vanilla().handleWaypoint(packet);
    }
    
    @Override
    public void handleDebugChunkValue(net.minecraft.network.protocol.game.ClientboundDebugChunkValuePacket packet) {
        vanilla().handleDebugChunkValue(packet);
    }
    
    @Override
    public void handleDebugBlockValue(net.minecraft.network.protocol.game.ClientboundDebugBlockValuePacket packet) {
        vanilla().handleDebugBlockValue(packet);
    }
    
    @Override
    public void handleDebugEntityValue(net.minecraft.network.protocol.game.ClientboundDebugEntityValuePacket packet) {
        vanilla().handleDebugEntityValue(packet);
    }
    
    @Override
    public void handleDebugEvent(net.minecraft.network.protocol.game.ClientboundDebugEventPacket packet) {
        vanilla().handleDebugEvent(packet);
    }
    
    @Override
    public void handleGameTestHighlightPos(net.minecraft.network.protocol.game.ClientboundGameTestHighlightPosPacket packet) {
        vanilla().handleGameTestHighlightPos(packet);
    }
    
    @Override
    public void handleLowDiskSpaceWarning(net.minecraft.network.protocol.game.ClientboundLowDiskSpaceWarningPacket packet) {
        vanilla().handleLowDiskSpaceWarning(packet);
    }
    
    @Override
    public void handlePostEffects(net.minecraft.network.protocol.common.ClientboundPostEffectsPacket packet) {
        vanilla().handlePostEffects(packet);
    }
    
    @Override
    public void handleClearDialog(net.minecraft.network.protocol.common.ClientboundClearDialogPacket packet) {
        vanilla().handleClearDialog(packet);
    }
    
    @Override
    public void handleShowDialog(net.minecraft.network.protocol.common.ClientboundShowDialogPacket packet) {
        vanilla().handleShowDialog(packet);
    }
    
    @Override
    public net.minecraft.network.protocol.PacketFlow flow() {
        return net.minecraft.network.protocol.PacketFlow.CLIENTBOUND;
    }
}
