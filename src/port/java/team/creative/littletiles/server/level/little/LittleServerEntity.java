package team.creative.littletiles.server.level.little;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.UpdateInterval;

/** Routes vanilla tracker packets into the animation level rather than the root level. */
public class LittleServerEntity extends ServerEntity {
    private final Entity entity;
    private final TrackingSynchronizer synchronizer;

    public LittleServerEntity(ServerLevel level, Entity entity, int updateInterval, boolean trackDelta,
            BiConsumer<ServerPlayer, Packet<?>> send) {
        this(level, entity, updateInterval, trackDelta, new TrackingSynchronizer(entity, send));
    }

    private LittleServerEntity(ServerLevel level, Entity entity, int updateInterval, boolean trackDelta,
            TrackingSynchronizer synchronizer) {
        super(level, entity, UpdateInterval.periodic(updateInterval), trackDelta, synchronizer);
        this.entity = entity;
        this.synchronizer = synchronizer;
    }

    @Override
    public void addPairing(ServerPlayer player) {
        if (entity == player || !entity.broadcastToPlayer(player) || !synchronizer.seenBy.add(player))
            return;
        List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
        sendPairingData(player, packets::add);
        // CreativeCore's packet wrapper uses a zero count for a non-bundle,
        // so do not serialize an empty bundle as if it held a single packet.
        if (!packets.isEmpty())
            synchronizer.send.accept(player, new ClientboundBundlePacket(packets));
        entity.startSeenByPlayer(player);
    }

    // Retained for the legacy excluded mixin until it has been migrated.
    public void sendPacket(ServerPlayer player, Packet<?> packet) {
        synchronizer.send.accept(player, packet);
    }

    @Override
    public void removePairing(ServerPlayer player) {
        if (!synchronizer.seenBy.remove(player))
            return;
        entity.stopSeenByPlayer(player);
        synchronizer.send.accept(player, new ClientboundRemoveEntitiesPacket(entity.getId()));
    }

    private static final class TrackingSynchronizer implements Synchronizer {
        private final Entity entity;
        private final BiConsumer<ServerPlayer, Packet<?>> send;
        private final Set<ServerPlayer> seenBy = new LinkedHashSet<>();

        private TrackingSynchronizer(Entity entity, BiConsumer<ServerPlayer, Packet<?>> send) {
            this.entity = entity;
            this.send = send;
        }

        @Override
        public void sendToTrackingPlayers(Packet<? super ClientGamePacketListener> packet) {
            for (ServerPlayer player : seenBy)
                send.accept(player, packet);
        }

        @Override
        public void sendToTrackingPlayersAndSelf(Packet<? super ClientGamePacketListener> packet) {
            sendToTrackingPlayers(packet);
            if (entity instanceof ServerPlayer player && !seenBy.contains(player))
                send.accept(player, packet);
        }

        @Override
        public void sendToTrackingPlayersFiltered(Packet<? super ClientGamePacketListener> packet, Predicate<ServerPlayer> filter) {
            for (ServerPlayer player : seenBy)
                if (filter.test(player))
                    send.accept(player, packet);
        }
    }
}
