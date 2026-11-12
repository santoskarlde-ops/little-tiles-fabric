package net.neoforged.neoforge.client;

import java.util.Collections;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent.AdditionalSectionRenderer;
import net.neoforged.neoforge.client.event.InputEvent;

public final class ClientHooks {
    private ClientHooks() {}
    public static float getGuiFarPlane() { return 1000.0F; }
    public static InputEvent.InteractionKeyMappingTriggered onClickInput(int button, KeyMapping key, InteractionHand hand) { return new InputEvent.InteractionKeyMappingTriggered(); }
    public static List<AdditionalSectionRenderer> gatherAdditionalRenderers(BlockPos pos, Level level) { return Collections.emptyList(); }
}
