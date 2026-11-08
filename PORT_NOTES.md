
## Round 15 (uncompiled; verified against mc-api.txt / mc-find.txt)
- `RenderType` import -> `net.minecraft.client.renderer.rendertype.RenderType` (mc-find). `PoseStack` unchanged (`com.mojang.blaze3d.vertex`). Still unlocated: MultiBufferSource, Tesselator, BufferUploader, VertexBuffer, GlStateManager (need mc-find2).
- `Item.canAttackBlock` -> `Item.canDestroyBlock(ItemStack, BlockState, Level, BlockPos, LivingEntity)` (8 items). Semantics assumed identical (returns false = creative cannot break): VERIFY in-game.
- `Item.appendHoverText` now `(ItemStack, TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)`; old bodies kept as `appendLegacyTooltip(... List<Component> ...)` bridged via a temp list (Phase 3 may inline).
- `LittleAnimationLevel`: Player->Entity in playSound/playSeededSound/levelEvent; addParticle gained a second boolean; getRecipeManager->recipeAccess(); getMinBuildHeight->getMinY(); added clockManager/environmentAttributes/Respawn data delegating to parentLevel. REMOVED (no longer on Level): setMapData, getFreeMapId (ServerLevel-only), getShade, potionBrewing, DayTime* (replaced by clockManager). Callers of getShade on animation levels need a Phase 4 replacement. More abstract Level methods may still be missing: next log will say.
- `LittlePacketTypes` line ~459: last bare buffer wrapped in `LittleNetBuf.of`.
- Tool: `tools/round15_port.py` (idempotent).

## Round 16 (uncompiled)
- mc-find2: `MultiBufferSource`, `Tesselator`, `BufferUploader`, `VertexBuffer`, `PotionBrewing` have NO class in the 26.3 jar (replaced by SubmitNodeCollector / StagedVertexBuffer / ByteBufferBuilder / MeshData / BufferBuilder; render rewrite is Phase 4). `GlStateManager` is now `com.mojang.renderpearl.backend.opengl.GlStateManager` (backend class, not imported by us). `VertexFormat` -> `com.mojang.renderpearl.api.vertex.VertexFormat` (Mode nested type NOT yet verified). `WaterAnimal` -> `animal.fish.WaterAnimal`. `BakedQuad` -> `client.resources.model.geometry`.
- `Player.displayClientMessage(c, true/false)` -> `sendOverlayMessage(c)` / `sendSystemMessage(c)` (mapping inferred from names: VERIFY actionbar vs chat).
- `registryOrThrow` -> `lookupOrThrow` (RegistryAccess); `Entity.absMoveTo` -> `snapTo` (same args).
- Fabric `CreativeNetwork` has `getPacketType` (forge variant: `getType`); LittlePacketTypes uses it.
- `LittleAnimationLevel`: added `dragonParts()` (empty) and 13-arg `explode` (transforms position into the real level; behavior unverified).
- Tool: `tools/round16_port.py`.

## Round 17 (uncompiled)
- `Level` ctor in 26.3: `(WritableLevelData, ResourceKey<Level>, RegistryAccess, Holder<DimensionType>, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates)`: profiler supplier removed. Fixed in LittleAnimationLevel (SubClientLevel/ClientLevel ctor still TODO).
- `LevelReader.getSeaLevel()` is abstract: delegated to parent in LittleAnimationLevel.
- Blocked, need sources: `IVecOrigin.pose()` no longer exists (~70 errors), `LevelData$RespawnData` shape (FakeLevelInfo: LevelData lost getSpawnPos/getSpawnAngle/getDayTime/isThundering/isRaining/getGameRules; WritableLevelData now only `setSpawn(RespawnData)`), `VertexFormat.Mode` location.

## Round 18 (uncompiled)
- ROOT CAUSE of ~70 `pose()` errors: the checked-out CreativeCore 26.x `IVecOrigin` has NO `pose()`/`pose(float)` (render setup is commented out with "TODO Reimplement for LittleTiles"); the LittleTiles source targets a newer CreativeCore. Added `common/compat/creativecore/VecOriginPose` (adapter: `VecOriginPose.of(origin[, partialTick])`). `transform`/`transformInverse`/`translation`/`rotateWithoutCenter` are 1:1 from IVecOrigin (verified); `transform(cam)`/`setup(...)` matrices are rebuilt from the commented-out `setupRenderingInternal` and are UNVERIFIED (Phase 4). `of(origin, partial)` interpolates via `origin.copy()` per call (perf risk). Alternative: restore pose() in CreativeCore itself. 67 sites rewritten from build.log positions by `tools/round18_port.py`; VecOriginSable (excluded) untouched.
- `VertexFormat.Mode` does not exist in 26.3 (only `VertexFormat$Builder`): drawing-mode handling is a Phase 4 item.
- `FakeLevelInfo`/`FakeServerLevelInfo` ported to LevelData{getRespawnData,getGameTime,isHardcore,getDifficulty,isDifficultyLocked}, WritableLevelData.setSpawn(RespawnData), ServerLevelData; removed `CommonHooks.onDifficultyChange` (NeoForge). `GameRules` ctor, day-time/weather members now plain methods (no @Override): check callers.
- Still open: entity ValueInput/ValueOutput ports; ServerGamePacketListener/ClientGamePacketListener have many new abstract handlers (mc-api3).

## Round 19 (uncompiled)
- LittleServerPlayerHandler / LittleClientPlayerHandler: added the 14 / 24 handlers that 26.3 added to the Server/ClientGamePacketListener and common listeners; each forwards to the vanilla listener like the existing handlers (`getVanilla()` / `vanilla()`). Removed handlers whose packets no longer exist: ServerboundPickItemPacket (now PickItemFromBlock/FromEntity), ServerboundSwingPacket (handleAnimate; swing/attack now `handlePunch`/`handleAttack`, NOT mapped to the old swing logic: VERIFY), ServerboundDebugSampleSubscriptionPacket (now handleDebugSubscriptionRequest), ClientboundAddExperienceOrbPacket (orbs arrive via handleAddEntity), ClientboundSetCarriedItemPacket (SetHeldSlot), ClientboundHorseScreenOpenPacket (MountScreenOpen forwards to vanilla: LT's horse-screen handling for sub levels dropped), ClientboundRecipePacket (RecipeBookAdd/Remove/Settings).
- `@Override` dropped on disconnect/send/getConnection/getMainThreadEventLoop/getConnectionType (no longer on the base types); NeoForge `ConnectionType` still to replace.
- `flow()` added to both handlers. LittleAnimationLevel.getWorldBorder() delegates to the parent.
- Tool: tools/round19_port.py (takes mc-api3.txt).
