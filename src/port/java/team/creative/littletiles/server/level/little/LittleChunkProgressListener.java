package team.creative.littletiles.server.level.little;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/** The previous progress listener was intentionally silent. */
public class LittleChunkProgressListener implements LevelLoadListener {
    public static final LittleChunkProgressListener INSTANCE = new LittleChunkProgressListener();
    @Override public void start(Stage stage, int total) {}
    @Override public void update(Stage stage, int current, int total) {}
    @Override public void finish(Stage stage) {}
    @Override public void updateFocus(ResourceKey<Level> dimension, ChunkPos pos) {}
}
