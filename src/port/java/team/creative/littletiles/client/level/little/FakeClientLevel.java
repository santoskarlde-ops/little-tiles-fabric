package team.creative.littletiles.client.level.little;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.creativecore.common.util.math.matrix.VecOrigin;
import team.creative.creativecore.common.util.math.vec.Vec3d;

@Environment(EnvType.CLIENT)
public class FakeClientLevel extends LittleClientLevel {
    
    public boolean shouldRender;
    private final Scoreboard scoreboard = new Scoreboard();
    
    public static FakeClientLevel createFakeWorldClient(String name, ClientLevelData info) {
        return new FakeClientLevel(info, false, 0);
    }
    
    protected FakeClientLevel(ClientLevelData worldInfo, boolean debug, long seed) {
        super(worldInfo, OVERWORLD, debug, seed, Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getSeaLevel() : 63, Minecraft.getInstance().getConnection().registryAccess());
    }
    public FeatureFlagSet enabledFeatures() {
        return FeatureFlagSet.of();
    }
    public UUID key() {
        return null;
    }
    public IVecOrigin getOrigin() {
        return origin;
    }
    public void setOrigin(Vec3d vec) {
        this.origin = new VecOrigin(vec);
    }
    public Holder<Biome> getUncachedNoiseBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
        return this.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
    }
    public float getShade(Direction p_230487_1_, boolean p_230487_2_) {
        var type = dimensionType().cardinalLightType();
        return p_230487_2_ ? type.get().byFace(p_230487_1_) : type == net.minecraft.world.level.CardinalLighting.Type.NETHER ? 0.9F : 1.0F;
    }
    public void playSound(Player p_184148_1_, double p_184148_2_, double p_184148_4_, double p_184148_6_, SoundEvent p_184148_8_, SoundSource p_184148_9_, float p_184148_10_, float p_184148_11_) {}
    public void playSound(Player p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_, SoundSource p_217384_4_, float p_217384_5_, float p_217384_6_) {}
    public void playSeededSound(Player p_220363_, double p_220364_, double p_220365_, double p_220366_, SoundEvent p_220367_, SoundSource p_220368_, float p_220369_, float p_220370_, long p_220371_) {}
    public void playSeededSound(Player p_263514_, Entity p_263536_, Holder<SoundEvent> p_263518_, SoundSource p_263487_, float p_263538_, float p_263524_, long p_263509_) {}
    public Scoreboard getScoreboard() {
        return scoreboard;
    }
    public RecipeAccess recipeAccess() {
        return Minecraft.getInstance().getConnection().recipes();
    }
    public void levelEvent(Player p_217378_1_, int p_217378_2_, BlockPos p_217378_3_, int p_217378_4_) {}
    public void gameEvent(Entity p_151549_, Holder<GameEvent> p_151550_, BlockPos p_151551_) {}
    public String toString() {
        return "FakeClientLevel";
    }
    
}
