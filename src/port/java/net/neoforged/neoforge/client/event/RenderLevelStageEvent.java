package net.neoforged.neoforge.client.event;

import net.neoforged.bus.api.Event;

public class RenderLevelStageEvent extends Event {
    public enum Stage {
        AFTER_SKY, AFTER_SOLID_BLOCKS, AFTER_CUTOUT_BLOCKS, AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS,
        AFTER_TRANSLUCENT_BLOCKS, AFTER_TRIPWIRE_BLOCKS, AFTER_WEATHER, AFTER_BLOCK_ENTITIES
    }
    public Stage getStage() { return Stage.AFTER_SOLID_BLOCKS; }
    public <T> T getPoseStack() { return null; }
    public <T> T getMultiBufferSource() { return null; }
}
