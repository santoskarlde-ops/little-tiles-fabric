package team.creative.littletiles.common.level.little;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.storage.LevelData.RespawnData;
import net.minecraft.world.level.storage.WritableLevelData;

public class FakeLevelInfo implements WritableLevelData {
    
    private final boolean hardcore;
    private final GameRules gameRules;
    private final boolean isFlat;
    private RespawnData respawn = RespawnData.DEFAULT;
    private long gameTime;
    private long dayTime;
    private boolean raining;
    private Difficulty difficulty;
    private boolean difficultyLocked;
    
    public FakeLevelInfo(Difficulty difficulty, boolean hardcore, boolean isFlat) {
        this.difficulty = difficulty;
        this.hardcore = hardcore;
        this.isFlat = isFlat;
        this.gameRules = new GameRules(FeatureFlagSet.of());
    }
    
    public BlockPos getSpawnPos() {
        return respawn.pos();
    }
    
    public float getSpawnAngle() {
        return respawn.yaw();
    }
    
    @Override
    public RespawnData getRespawnData() {
        return respawn;
    }
    
    @Override
    public void setSpawn(RespawnData data) {
        this.respawn = data;
    }
    
    public void setSpawn(BlockPos pos, float angle) {
        this.respawn = new RespawnData(GlobalPos.of(respawn.dimension(), pos.immutable()), angle, respawn.pitch());
    }
    
    @Override
    public long getGameTime() {
        return this.gameTime;
    }
    
        public long getDayTime() {
        return this.dayTime;
    }
    
    public void setGameTime(long p_239155_1_) {
        this.gameTime = p_239155_1_;
    }
    
    public void setDayTime(long p_239158_1_) {
        this.dayTime = p_239158_1_;
    }
    
        public boolean isThundering() {
        return false;
    }
    
        public boolean isRaining() {
        return this.raining;
    }
    
        public void setRaining(boolean p_76084_1_) {
        this.raining = p_76084_1_;
    }
    
    @Override
    public boolean isHardcore() {
        return this.hardcore;
    }
    
        public GameRules getGameRules() {
        return this.gameRules;
    }
    
    @Override
    public Difficulty getDifficulty() {
        return this.difficulty;
    }
    
    @Override
    public boolean isDifficultyLocked() {
        return this.difficultyLocked;
    }
    
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
    
    public void setDifficultyLocked(boolean p_239157_1_) {
        this.difficultyLocked = p_239157_1_;
    }
    
    public double getHorizonHeight() {
        return this.isFlat ? 0.0D : 63.0D;
    }
    
    public double getClearColorScale() {
        return this.isFlat ? 1.0D : 0.03125D;
    }
}
