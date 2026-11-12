package team.creative.littletiles.server.level.little;

import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.border.WorldBorder.Settings;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.timers.TimerQueue;
import team.creative.littletiles.common.level.little.FakeLevelInfo;

public class FakeServerLevelInfo extends FakeLevelInfo implements ServerLevelData {
    
    public FakeServerLevelInfo(Difficulty difficulty, boolean hardcore, boolean isFlat) {
        super(difficulty, hardcore, isFlat);
    }
    
    @Override
    public String getLevelName() {
        return "fake";
    }
    
    public void setThundering(boolean thundering) {}
    
    public int getRainTime() {
        return 0;
    }
    
    public void setRainTime(int time) {}
    
    public void setThunderTime(int time) {}
    
    public int getThunderTime() {
        return 0;
    }
    
    public int getClearWeatherTime() {
        return 0;
    }
    
    public void setClearWeatherTime(int time) {}
    
    public int getWanderingTraderSpawnDelay() {
        return 0;
    }
    
    public void setWanderingTraderSpawnDelay(int delay) {}
    
    public int getWanderingTraderSpawnChance() {
        return 0;
    }
    
    public void setWanderingTraderSpawnChance(int chance) {}
    
    public UUID getWanderingTraderId() {
        return null;
    }
    
    public void setWanderingTraderId(UUID uuid) {}
    
    @Override
    public GameType getGameType() {
        return GameType.SURVIVAL;
    }
    
    public void setWorldBorder(Settings settings) {}
    
    public Settings getWorldBorder() {
        return Settings.DEFAULT;
    }
    
    @Override
    public boolean isInitialized() {
        return true;
    }
    
    @Override
    public void setInitialized(boolean init) {}
    
    @Override
    public boolean isAllowCommands() {
        return false;
    }
    
    @Override
    public void setGameType(GameType type) {}
    
    @Override
    public void setAllowCommands(boolean allow) {}
    
    public TimerQueue<MinecraftServer> getScheduledEvents() {
        return null;
    }
    
    public float getDayTimeFraction() {
        return 0;
    }
    
    public float getDayTimePerTick() {
        return 0;
    }
    
    public void setDayTimeFraction(float dayTimeFraction) {}
    
    public void setDayTimePerTick(float dayTimePerTick) {}
    
}
