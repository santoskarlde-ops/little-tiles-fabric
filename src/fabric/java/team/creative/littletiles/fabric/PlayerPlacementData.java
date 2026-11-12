package team.creative.littletiles.fabric;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/** Persistent, per-player placement settings; initialized before player data is loaded. */
public final class PlayerPlacementData {
    private static final AttachmentType<CompoundTag> SETTINGS = AttachmentRegistry.create(
        Identifier.fromNamespaceAndPath("littletiles", "placement_settings"),
        builder -> builder.initializer(CompoundTag::new).persistent(CompoundTag.CODEC).copyOnDeath());

    private PlayerPlacementData() {}

    public static void init() {
        // Trigger registration during the Fabric mod initializer.
    }

    public static CompoundTag get(Player player) {
        return player.getAttachedOrElse(SETTINGS, new CompoundTag());
    }

    public static void set(Player player, CompoundTag settings) {
        player.setAttached(SETTINGS, settings.copy());
    }
}
