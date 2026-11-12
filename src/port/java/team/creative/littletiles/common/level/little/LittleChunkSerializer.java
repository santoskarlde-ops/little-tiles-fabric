package team.creative.littletiles.common.level.little;

import java.util.EnumSet;
import java.util.Map;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.SavedTick;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import team.creative.littletiles.server.level.little.LittleServerLevel;

public class LittleChunkSerializer {
    
    private static final Codec<List<SavedTick<Block>>> BLOCK_TICKS_CODEC = SavedTick.codec(BuiltInRegistries.BLOCK.byNameCodec()).listOf();
    private static final Codec<List<SavedTick<Fluid>>> FLUID_TICKS_CODEC = SavedTick.codec(BuiltInRegistries.FLUID.byNameCodec()).listOf();
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public static LevelChunk read(LittleLevel level, CompoundTag nbt) {
        ChunkPos chunkpos = new ChunkPos(nbt.getInt("xPos").orElse(0), nbt.getInt("zPos").orElse(0));
        
        ListTag listtag = nbt.getListOrEmpty("sections");
        int i = level.getSectionsCount();
        LevelChunkSection[] alevelchunksection = new LevelChunkSection[i];
        boolean chunkSkyLight = level.dimensionType().hasSkyLight();
        ChunkSource chunksource = level.getChunkSource();
        LevelLightEngine levellightengine = chunksource.getLightEngine();
        PalettedContainerFactory factory = PalettedContainerFactory.create(level.registryAccess());
        Codec<PalettedContainer<BlockState>> blockStateCodec = factory.blockStatesContainerCodec();
        boolean retained = false;
        
        for (int j = 0; j < listtag.size(); ++j) {
            CompoundTag compoundtag = listtag.getCompound(j).orElse(new CompoundTag());
            int y = compoundtag.getByte("Y").orElse((byte) 0);
            int l = level.getSectionIndexFromSectionY(y);
            if (l >= 0 && l < alevelchunksection.length) {
                PalettedContainer<BlockState> states;
                if (compoundtag.contains("block_states"))
                    states = blockStateCodec.parse(NbtOps.INSTANCE, compoundtag.getCompound("block_states").orElse(new CompoundTag())).promotePartial(error -> logErrors(chunkpos, y, error)).getOrThrow(
                        SerializableChunkData.ChunkReadException::new);
                else
                    states = factory.createForBlockStates();
                
                LevelChunkSection levelchunksection = new LevelChunkSection(states, factory.createForBiomes());
                alevelchunksection[l] = levelchunksection;
                //poiManager.checkConsistencyWithBlocks(pos, levelchunksection);
            }
            
            boolean hasBlockLight = compoundtag.contains("BlockLight");
            boolean hasSkyLight = chunkSkyLight && compoundtag.contains("SkyLight");
            if (hasBlockLight || hasSkyLight) {
                if (!retained) {
                    levellightengine.retainData(chunkpos, true);
                    retained = true;
                }
                
                if (hasBlockLight)
                    levellightengine.queueSectionData(LightLayer.BLOCK, SectionPos.of(chunkpos, y), new DataLayer(compoundtag.getByteArray("BlockLight").orElse(new byte[0])));
                
                if (hasSkyLight)
                    levellightengine.queueSectionData(LightLayer.SKY, SectionPos.of(chunkpos, y), new DataLayer(compoundtag.getByteArray("SkyLight").orElse(new byte[0])));
            }
        }
        
        LevelChunkTicks<Block> blockTicks = new LevelChunkTicks<>(SavedTick.filterTickListForChunk(
            nbt.read("block_ticks", BLOCK_TICKS_CODEC).orElse(List.of()), chunkpos));
        LevelChunkTicks<Fluid> fluidTicks = new LevelChunkTicks<>(SavedTick.filterTickListForChunk(
            nbt.read("fluid_ticks", FLUID_TICKS_CODEC).orElse(List.of()), chunkpos));
        
        LevelChunk chunk = new LevelChunk(level.asLevel(), chunkpos, UpgradeData.EMPTY, blockTicks, fluidTicks, nbt.getLong("InhabitedTime").orElse(0L), alevelchunksection, postLoadChunk(
            level, nbt), null);
        
        chunk.setLightCorrect(nbt.getBoolean("isLightOn").orElse(false));
        
        CompoundTag heightmaps = nbt.getCompound("Heightmaps").orElse(new CompoundTag());
        EnumSet<Heightmap.Types> enumset = EnumSet.noneOf(Heightmap.Types.class);
        
        for (Heightmap.Types type : chunk.getPersistedStatus().heightmapsAfter()) {
            String s = type.getSerializationKey();
            if (heightmaps.getLongArray(s).isPresent())
                chunk.setHeightmap(type, heightmaps.getLongArray(s).orElse(new long[0]));
            else
                enumset.add(type);
        }
        
        Heightmap.primeHeightmaps(chunk, enumset);
        
        if (nbt.getBoolean("shouldSave").orElse(false))
            chunk.markUnsaved();
        
        ListTag blockEntities = nbt.getListOrEmpty("block_entities");
        
        for (int j = 0; j < blockEntities.size(); ++j)
            chunk.setBlockEntityNbt(blockEntities.getCompound(j).orElse(new CompoundTag()));
        
        NeoForge.EVENT_BUS.post(new ChunkDataEvent.Load(chunk, nbt, ChunkType.LEVELCHUNK));
        return chunk;
    }
    
    private static void logErrors(ChunkPos pos, int y, String error) {
        LOGGER.error("Recoverable errors when loading section [" + pos.x() + ", " + y + ", " + pos.z() + "]: " + error);
    }
    
    public static CompoundTag write(LittleServerLevel level, ChunkAccess chunk) {
        ChunkPos chunkpos = chunk.getPos();
        CompoundTag nbt = NbtUtils.addCurrentDataVersion(new CompoundTag());
        nbt.putInt("xPos", chunkpos.x());
        nbt.putInt("yPos", chunk.getMinSectionY());
        nbt.putInt("zPos", chunkpos.z());
        nbt.putLong("LastUpdate", level.getGameTime());
        nbt.putLong("InhabitedTime", chunk.getInhabitedTime());
        
        LevelChunkSection[] alevelchunksection = chunk.getSections();
        ListTag sections = new ListTag();
        LevelLightEngine levellightengine = level.getChunkSource().getLightEngine();
        Codec<PalettedContainer<BlockState>> blockStateCodec = PalettedContainerFactory.create(level.registryAccess()).blockStatesContainerCodec();
        
        for (int i = levellightengine.getMinLightSection(); i < levellightengine.getMaxLightSection(); ++i) {
            int j = chunk.getSectionIndexFromSectionY(i);
            boolean flag1 = j >= 0 && j < alevelchunksection.length;
            DataLayer blockLight = levellightengine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkpos, i));
            DataLayer skyLight = levellightengine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkpos, i));
            if (flag1 || blockLight != null || skyLight != null) {
                CompoundTag sectionTag = new CompoundTag();
                if (flag1) {
                    LevelChunkSection levelchunksection = alevelchunksection[j];
                    sectionTag.put("block_states", blockStateCodec.encodeStart(NbtOps.INSTANCE, levelchunksection.getStates()).getOrThrow());
                }
                
                if (blockLight != null && !blockLight.isEmpty())
                    sectionTag.putByteArray("BlockLight", blockLight.getData());
                
                if (skyLight != null && !skyLight.isEmpty())
                    sectionTag.putByteArray("SkyLight", skyLight.getData());
                
                if (!sectionTag.isEmpty()) {
                    sectionTag.putByte("Y", (byte) i);
                    sections.add(sectionTag);
                }
            }
        }
        nbt.put("sections", sections);
        
        if (chunk.isLightCorrect())
            nbt.putBoolean("isLightOn", true);
        
        ListTag blockEntities = new ListTag();
        for (BlockPos blockpos : chunk.getBlockEntitiesPos()) {
            CompoundTag blockEntityTag = chunk.getBlockEntityNbtForSaving(blockpos, level.registryAccess());
            if (blockEntityTag != null)
                blockEntities.add(blockEntityTag);
        }
        nbt.put("block_entities", blockEntities);
        
        saveTicks(nbt, chunk.getTicksForSerialization(level.getGameTime()));
        
        CompoundTag heightmaps = new CompoundTag();
        for (Map.Entry<Heightmap.Types, Heightmap> entry : chunk.getHeightmaps())
            if (chunk.getPersistedStatus().heightmapsAfter().contains(entry.getKey()))
                heightmaps.put(entry.getKey().getSerializationKey(), new LongArrayTag(entry.getValue().getRawData()));
        nbt.put("Heightmaps", heightmaps);
        
        NeoForge.EVENT_BUS.post(new ChunkDataEvent.Save(chunk, level, nbt));
        return nbt;
    }
    
    private static void saveTicks(CompoundTag nbt, ChunkAccess.PackedTicks ticks) {
        nbt.store("block_ticks", BLOCK_TICKS_CODEC, ticks.blocks());
        nbt.store("fluid_ticks", FLUID_TICKS_CODEC, ticks.fluids());
    }

    @Nullable
    private static LevelChunk.PostLoadProcessor postLoadChunk(LittleLevel level, CompoundTag nbt) {
        ListTag entities = getListOfCompoundsOrNull(nbt, "entities");
        ListTag blockEntities = getListOfCompoundsOrNull(nbt, "block_entities");
        return entities == null && blockEntities == null ? null : (x) -> {
            if (entities != null && level instanceof LittleServerLevel sLevel) {
                try (var problems = new ProblemReporter.ScopedCollector(x.problemPath(), LOGGER)) {
                    var inputs = TagValueInput.create(problems, level.registryAccess(), nbt).childrenListOrEmpty("entities");
                    sLevel.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(inputs, level.asLevel(), EntitySpawnReason.LOAD));
                }
            }
            
            if (blockEntities != null)
                for (int i = 0; i < blockEntities.size(); ++i) {
                    CompoundTag blockTag = blockEntities.getCompound(i).orElse(new CompoundTag());
                    if (blockTag.getBoolean("keepPacked").orElse(false))
                        x.setBlockEntityNbt(blockTag);
                    else {
                        BlockPos blockpos = BlockEntity.getPosFromTag(x.getPos(), blockTag);
                        BlockEntity blockentity = BlockEntity.loadStatic(blockpos, x.getBlockState(blockpos), blockTag, level.registryAccess());
                        if (blockentity != null)
                            x.setBlockEntity(blockentity);
                    }
                }
        };
    }
    
    @Nullable
    private static ListTag getListOfCompoundsOrNull(CompoundTag nbt, String key) {
        ListTag listtag = nbt.getListOrEmpty(key);
        return listtag.isEmpty() ? null : listtag;
    }
}
