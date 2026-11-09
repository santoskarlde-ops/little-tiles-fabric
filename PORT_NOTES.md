# Round 53 — measured Round 52 result, client command event type and two GUI parents (uncompiled)

- Round 52 result, from comparing the supplied logs: the summary line fell from 882 to 875 errors (7 fewer). The 7 diagnostics Round 52 targeted (six `getCommands` in `LittleTiles`, one `AreaSelectionMode`) are gone and no new diagnostic appeared. This assumes the new log had only Round 52 added; it compares two logs, not a controlled build.
- `RegisterClientCommandsEvent` (local NeoForge event stub): `getDispatcher()` was a generic `<T> T`, so `event.getDispatcher().register(...)` in `LittleTilesClient` resolved against `Object`. It now returns `CommandDispatcher<CommandSourceStack>`, the type the call site already registers a `LiteralArgumentBuilder<CommandSourceStack>` on. `LittleTilesClient.commands` is its only user. Expect any errors inside that lambda to surface afterwards.
- `LittleDoorAdvancedGui.GuiTimelineConfigSame`: the constructor now takes the parent control first and passes it to `GuiAnimationTimelinePanel`, whose constructor requires one (this also removes the follow-on `GuiBlueprintAnimationHandler cannot be converted to GuiTreeItemStructure`). The "same transition" tab is created first and passed as parent, matching how `GuiTimelineConfigDifferent` already gets its tab.
- `OverlayRenderer.OverlayGuiLayer`: `GuiActionDisplay` is created with the layer as parent, as its constructor requires.
- Source-reviewed against the log and local/CreativeCore sources only; no 26.3 jar or Gradle run was available. Apply after Round 52. This overlay holds the four changed source files and this entry.
- Still open, not guessed: `BuildingModeRules` (`GuiExtensionCreator` now needs a `GuiClientControl`, i.e. a client-side GUI split), `GuiElementFilter` (needs a parent argument through all subclasses), `OverlayRenderer` abstract `createDist`, the three `EntityRenderer`/`BlockEntityRenderer` generic-arity errors, and the rendering/cache cluster.

# Round 52 — measured Round 51 result, server event type and area-box corners (uncompiled)

- Round 51 result, from comparing the two supplied logs: the summary line fell from 895 to 882 errors (13 fewer). All 13 diagnostic locations Round 51 targeted are gone, plus one cascading `LittleRenderPipelineForge` diagnostic. This assumes the new log was built with only Round 51 added; it is a comparison of two logs, not a controlled build.
- One Round 51 fix was incomplete: `AABB` has no `BlockPos, BlockPos` constructor in 26.3 (`BlockPos cannot be converted to Vec3`). `AreaSelectionMode` now builds that box from the six block coordinates with the `AABB` coordinate constructor, which keeps the old min/max, no-extra-block extent.
- `ServerStartingEvent` (local NeoForge event stub): `getServer()` was a generic `<T> T`, so `event.getServer().getCommands()` resolved against `Object` and produced 6 diagnostics in `LittleTiles`. It now returns `MinecraftServer`; the API dump lists `MinecraftServer.getCommands()` and `Commands.getDispatcher()`. `LittleTiles` is the only user of this event.
- Two `LittleAnimationLevelServerCallback` diagnostics moved line numbers because Round 51 removed one line; they are the same open `isSpawningAnimals`/`areNpcsEnabled` errors, not new ones.
- Source-reviewed only: no Minecraft 26.3 jar or Gradle run was available. Fixing `getCommands` may expose errors inside the command lambdas that were hidden before. Apply after Round 51. This overlay holds the two changed source files and this entry.

# Round 51 — chunk map access, area-selection boxes and shim API gaps (uncompiled)

- The supplied build log (taken after Round 50 was applied; it has no `isAllowCommands` or `PotionBrewing` diagnostics) reports 895 javac errors on its summary line. Gradle prints every diagnostic twice, so the log holds 1,790 `error:` lines. Of its 837 distinct file/line/message locations, 828 matched the supplied source tree exactly; the other 9 are in `RenderingThread.java` and `RenderingLevelHandler.java`, which are absent from the supplied tree and were not touched.
- Round numbering: the hand-over brief named Round 47 as the latest, but the supplied tree and notes already contain Rounds 48-50, so this is Round 51 and replaces the planned "Round 48" overlay. Apply it only after Round 50.
- `LittleChunkMap`: read the level through `ServerChunkCache.getLevel()` (the `level` field is private in 26.3; the API dump lists `getLevel()` returning `Level`), casting to `ServerLevel` / `LittleLevel` where the old field type was used. `getUpdatingChunkIfPresent` is now `public` to match `ChunkMap`.
- `AreaSelectionMode`: `ABB` has no `BlockPos` constructors in the supplied CreativeCore, so the three calls now build the box through `new ABB(new AABB(...))`, which uses the existing `ABB(AABB)` constructor.
- `LittleEntity`: `ChildVecOrigin.setParent` no longer exists; the public `parent` field is assigned directly.
- `GuiChildEventPanel`: `getValueAt` widened to `public`, as declared by `GuiTimelineChannel`.
- `LittleAnimationLevelServerCallback`: removed the `entity.onRemovedFromLevel()` call. It is not in the 26.3 `Entity` API dump (it was a NeoForge hook); the Fabric/NeoForge event-stub post that follows is unchanged. Any mod relying on that hook would no longer be notified.
- Local shims `ChunkLayerMap` (`getByIndex(int)`) and `ChunkLayerMapList` (`isEmpty()`, `Iterable<T>` over every value of every layer) gain the methods their callers in `BERenderManager` and `AdditionalBuffers` already use. These are small API completions, not rendering stubs; both files still depend on `RenderType.CHUNK_BUFFER_LAYERS`, which does not resolve in 26.3.
- Targets 13 logged diagnostic locations. These are source-reviewed against the log, the CreativeCore sources and the API dump; there was no Minecraft 26.3 jar and no Gradle run here, so nothing was compiled against the game. The edited files were only parsed with the JDK compiler (no syntax errors); their remaining errors are unresolved Minecraft symbols. No error-count change is claimed.
- Left open and not guessed (the API dump does not cover them): `LittleBed` (bed/dimension/respawn APIs, `hasImpulse`), `BlankOMaticRecipeRegistry`/`ItemIngredientEntry` (`CreativeIngredient.is` now needs a `Level`), recipe serializers, `PERSISTED_NBT_TAG` users (needs a Fabric persistence design), `MinecraftServer.isSpawningAnimals`/`areNpcsEnabled`, `LittleBlockRegistry.saveState` (`getValues()` is now a `Stream`), dye/wool item names, `VectorFan.setLineNormal` users, and the excluded mixin accessors. The rendering/cache/GUI-drawing cluster (about 600 diagnostics: `RenderSystem`, `VertexBuffer`, `BufferUploader`, `Tesselator`, `GuiGraphics`, `RenderType`, Sodium/Iris) needs a real 26.3 rendering port.

# Round 50 — command-block gate and unused import (uncompiled)

- The supplied post-Round-49 build log reports 898 javac errors (the summary line; the diagnostics are not duplicated in this log), versus 922 in the log behind Round 49. The `LittleFace`, `LittleServerFace` and `LittleChair` diagnostics from that round no longer appear, consistent with Round 49 having been applied. This is a comparison of two supplied logs, not a measured effect of any single round.
- Only the 25 files of the Round 49 overlay were available as exact source here; the complete source tree, the Minecraft 26.3 jar, the CreativeCore API tools and `javac` were not. 31 of the 898 diagnostics fall in three of those files, and all 31 source lines matched the supplied files. The other 867 diagnostics, in about 120 files, were not edited because their source was unavailable.
- `LittleServerPlayerHandler`: the command-block and command-minecart handlers called `MinecraftServer.isAllowCommands()`, which does not exist in 26.3. They now call `player.level().isCommandBlockEnabled()`. The checked-in 26.3 API dump (`mc-api.txt`) lists `isCommandBlockEnabled()` on `ServerLevel` and shows `ServerPlayer.level()` returning `ServerLevel`. The setting is now read from the player's own server level, not server-wide as in the original, so check command-block and command-minecart editing with the game rule on and off.
- `LittleAnimationLevel`: remove the `PotionBrewing` import, which no longer resolves in 26.3 and has no use in the file.
- Left open in the supplied files: three `ServerGamePacketListenerImplAccessor` references in `LittleServerPlayerHandler` (the `mixin/` package is excluded from the port, so the accessor needs either a registered mixin or another way to reach the private awaiting-position and text-filter members), and 25 rendering/GUI diagnostics in `GuiSignalController`. The latter needs a client-side class extending CreativeCore's `GuiClientParent` that renders with `GuiGraphicsExtractor`. The replacement for the removed `Tesselator`/`BufferUploader` line drawing could not be verified without the 26.3 jar, so no stub was added.
- These changes are source-reviewed against the supplied log and the API dump; they were not compiled or run. No claim is made about the new error count. Apply after Round 49. This overlay contains the two changed source files and this notes entry only.

# Round 49 — signal GUI parents and remaining 26.3 compatibility fixes (compile-verified)

- The newly supplied build log reports 922 javac diagnostics. Its source lines match the retained Round-48 tree wherever the files are present; 10 logged locations refer to `RenderingThread.java` and `RenderingLevelHandler.java`, which are absent from that retained tree. The local verification below therefore uses the retained source tree rather than claiming to reproduce the supplied checkout exactly.
- Update `LittleFace` for the 26.3 `FaceInfo.VertexInfo` record and extent enum, and update `LittleServerFace` for the current `Block.shouldRenderFace` signature. Adapt chair connection synchronization, fake level game rules, and fake world-border settings.
- Make the signal GUI node hierarchy explicitly parent-aware for CreativeCore 26.x, pass the controller as the node parent, and pass the actual panel/node parent to signal dialog controls. Replace the removed text-field focus call with the current distribution handler.
- Add the constructor shapes required by the existing NeoForge event compatibility stubs, remove obsolete entity lifecycle hook calls and an invalid functional-interface annotation, and migrate the server entity tag query to `TagValueOutput`.
- A fresh local Gradle `compileJava` run with the retained Round-48 tree measured 963 diagnostics before these changes and 939 after them, 24 fewer in that local comparison. The build still fails; remaining errors are concentrated in the unfinished rendering/cache integration and missing retained rendering sources. No runtime testing was performed.
- Apply after Round 48. This overlay contains the Round-49 source changes and this notes entry only.

# Round 48 — packet, block, entity and GUI API cleanup (compile-verified)

- The supplied post-Round-47 build log contains 985 javac diagnostics after collapsing Gradle's repeated summary/diagnostic presentation. The complete supplied Round-47 tree was compiled independently in this sandbox with the bundled JDK compiler shim: the reproducible local baseline was 1,026 diagnostics, and the latest Round-48 compile reports 963. This is a local verification comparison, not a claimed reduction of the supplied 985-error build.
- Update `BlockSignalConverter` and `BlockTile` for 26.3 neighbor orientation, friction, map color, explosion, particle, plant-support and server-level APIs. Remove the obsolete respawn-position override and adapt block destruction/harvesting callbacks.
- Update `LittleServerPlayerHandler` and `LittleClientPlayerHandler` for 26.3 command permissions, command-block updates, item use/harvesting, entity creation, explosion packets, block-entity component loading, pickup particles, packet listeners and removed connection helpers.
- Remove duplicate generic accessors from the four NeoForge event stubs, update the client key-mapping call, and remove stale common-CreativeCore GUI superclass calls. Preserve the existing GUI behavior where possible rather than adding rendering or event stubs.
- Source-reviewed fixes were checked against the matching source, CreativeCore sources and available 26.3 class signatures/decompiled handlers. The 1,026-to-963 comparison is compile-verified by a fresh local Gradle `compileJava` run; the build still fails with 963 errors, mostly in the remaining rendering/cache integration. No runtime testing was performed.
- Apply only after Round 47. This overlay contains the changed source files and this notes entry; it does not include build outputs or the supplied diagnostic log.

# Round 47 — inventory, level and GUI API migrations (uncompiled)

- The supplied post-Round-46 build reports 1,018 javac errors, down from 1,092 (74 fewer); Gradle repeats the diagnostics. Round 47 has not been compiled, so its effect on the count is unknown.
- Storage and 3D-printer inventories now load with CreativeCore's returned-container API while retaining the storage slot filter. Override `setChanged` to notify the structure after loading rather than using the removed container-listener API; loading itself does not trigger these callbacks. The printer's nested error list uses the 26.3 list accessor.
- Item-holder and storage filters pass their structure level to CreativeCore's ingredient test. Item-holder NBT uses the optional item-stack codec with registry-aware ops, and selected-slot access uses the public getter.
- Fake and sub-levels use the 26.3 recipe-access return types and delegate to the appropriate connection, server or parent; particle forwarding preserves both 26.3 visibility flags. Client and server level behavior requires in-game testing.
- Update horizontal facing to `EnumProperty<Direction>`, block solid-render checks, server-player level access, and sound identifiers. Update the sound timeline visibility and GUI sound dispatch, the blueprint tree constructor/reflow, and printer inventory-grid sizing for current CreativeCore APIs.
- These are source-reviewed against the supplied build, matching CreativeCore sources, and official 26.3 class signatures/selected decompiled implementations, not compile- or runtime-verified. Check item/filter serialization, inventory notifications, blueprint UI, particles and sub-level recipes in game. Rendering and other Fabric integration remain unfinished. Apply only on top of the complete Round 46 tree.

# Round 46 — packet, GUI, input and accessor migrations (uncompiled)

- The supplied post-Round-45 build reports 1,092 javac errors, down from 1,237 (145 fewer). Gradle repeats diagnostics. All 1,092 diagnostic source lines matched this Round 45 working tree. This round edits 64 distinct source locations with logged diagnostics; this is not a measured reduction and other diagnostics may change or be exposed.
- `LittleClientPlayerHandler`: migrate entity teleport to 26.3 relative position/motion interpolation, rider repositioning and vehicle acknowledgement; share the relative-position calculation with player teleports. Migrate passenger riding, mount HUD/narration, item-activation, sign-editor and death-screen calls. Route swing packets to the little level, align legacy animation actions with 26.3, and fix accidental self-recursion in delete-chat handling.
- `LittleServerPlayerHandler`: migrate use-item packet record accessors, range and inclusive-height APIs, sign packet fields, profile name and server lookup. Swing handling now takes the item's interaction animation and respects the 26.3 predicted-swing flag. Other server interaction and excluded accessor migrations remain unfinished.
- Pass actual GUI parents into particle, preview, timeline, structure and measurement controls; migrate three stack-selector constructors. The structure editor and measurement list construct with an existing GUI parent and are subsequently reparented when inserted by CreativeCore.
- Update HUD visibility, current mouse button constants, server access and registry holder lookups. Import CreativeCore's functional `Filter` for the existing `BlockFilter`; its separate missing `FilterRegistry` remains unresolved. Mouse event adapters are still placeholders and must be wired and tested for Fabric.
- Teleport behavior is source-adapted from vanilla, not runtime-tested: non-ClientLevel little worlds have no equivalent ticking-entity query, and vanilla's removed-vehicle fallback is not implemented. Round 45's teleport prediction-handler synchronization remains open. Test relative teleports, passengers, signs, block interaction and GUI behavior.
- Source-reviewed against the supplied log, matching CreativeCore sources and official 26.3 client signatures/decompiled handlers. No `javac` is available here; no compile or runtime test was performed. Rendering, particles and NeoForge/Fabric integration remain substantial open work. Apply only to the complete Round 45 tree.

# Round 45 — 26.3 packet and accessor migrations (uncompiled)

- The supplied post-Round-44 log reports 1,237 javac errors; Gradle prints diagnostics twice. The provided 855-file source archive matched all logged diagnostic source lines. Changes touch 110 distinct source locations that had logged diagnostics; this is not a count of errors fixed or a measured reduction.
- `LittleClientPlayerHandler`: update entity motion and movement, player teleport/acknowledgement, vehicle movement/acknowledgement, entity removal, chunk packet data, light packet accessors, section bounds, and particle randomization to the 26.3 APIs. The code follows the corresponding 26.3 vanilla packet algorithms while preserving the LittleTiles sub-level send/level routing. Standalone light data is now applied to the receiving little level and its sections are marked dirty.
- Player teleport uses the 26.3 relative position/rotation calculation, sends the expanded acknowledgement, and stops block destruction. Vanilla also clears its block-state prediction handler on teleport; that helper is not accessible here and is not called. Little-level teleport and prediction synchronization require follow-up and multiplayer testing.
- Replace old NBT accessors, packed chunk position calls, block-position centers, section size/bounds, advancement/projectile/boat imports, camera/partial-tick/screen accessors, and four CreativeCore fixed-width GUI calls with their current equivalents.
- Reviewed against the supplied source/log, CreativeCore sources, and official 26.3 client class signatures and decompiled packet implementation. No `javac` is available: no compilation or runtime verification was performed. New errors may emerge; rendering and excluded integrations remain unfinished. Check player/vehicle movement, relative teleports, particles, chunk lighting, GUI sizing, and placement in-game. Apply only on top of the complete Round 44 tree.

# Round 44 — workbench horizontal sizing (uncompiled)

- The supplied post-Round-43 log reports 1,238 javac errors, down from 1,255. Gradle prints diagnostics twice.
- `GuiWorkbench`: replace the removed `setUnexpandableX()` with CreativeCore 26.x `setFixedX()`, which sets the client control's `expandableX` flag to false. This targets one logged diagnostic; the new count is not measured.
- Source-reviewed against the matching CreativeCore implementation, not compiled or runtime-tested. The rendering and GUI rewrites remain open.
- The complete post-Round-43 source tree was not available in the sandbox; only files supplied in Round 40 and subsequently edited are available. All 134 current diagnostic source lines in those files matched the log. Apply this overlay only to the complete post-Round-43 tree.

# Round 43 — CreativeCore controls and 26.3 client/inventory APIs (uncompiled)

- The supplied post-Round-42 log reports 1,255 javac errors, down from 1,263. Gradle prints the diagnostics twice. This round targets 17 logged diagnostic instances; the new total is not measured.
- `GuiGridConfig`: replace removed `GuiButtonHoldSlim` with CreativeCore's parent-aware `GuiButtonHold` for previous/next. Hold-to-repeat remains available; button sizing/appearance may differ and needs testing.
- `GuiBlueprintAdd` and `GuiBlueprintMove`: supply the tree as both GUI parent and tree to the current `GuiTreeItem` constructor. `LittleDoorAxisGui`: supply the parent to `GuiTabsMapped`.
- Route five removed GUI `playSound` calls through the existing, null-checked control distribution handler. This does not migrate the separate legacy input and render callbacks, which remain unfinished.
- `GuiBlankOMatic`: replace removed `SimpleContainer.addListener` with a `setChanged` override that marks the whitener dirty once initialized.
- `GuiBlankOMatic`, `GuiWorkbench`, and `GuiBag`: supply `Prediction.PREDICTED` to existing item-drop calls, following CreativeCore Fabric's `PlayerUtils.addOrDrop`; existing boolean arguments are preserved. Vanilla's prediction choice affects swing broadcasting, so GUI close/crafting and multiplayer behavior require testing.
- `LittleToolMeasure`: query `Minecraft.gui.screen()`. `LittleToolWrench`: obtain camera orientation from the entity render dispatcher's camera `rotation()`; the surrounding renderer still needs its larger API migration.
- Reviewed against the log, CreativeCore 26.x sources (35 supplied source files match byte-for-byte) and SHA-1-verified official 26.3 client classes. Not compiled or runtime-tested. New errors may emerge, and no resulting count can be claimed before a fresh build.
- Only the provided Round 40 overlay and the subsequent edited files are available as an exact source baseline here; all 151 diagnostic source lines in those available files matched the log. Apply this incremental archive only to the complete post-Round-42 tree.

# Round 42 — targeted GUI and 26.3 value accessors (uncompiled)

- The post-Round-41 build reports 1,263 javac errors, down from 1,274. Gradle prints each diagnostic twice; the summary is not another compilation.
- `GuiBag`: replace the removed `StringTag.getAsString()` with the 26.3 `StringTag.value()` accessor.
- `GuiAnimationViewer`: replace `Minecraft.getTimer()` with `getDeltaTracker()` while retaining `getGameTimeDeltaTicks()`.
- `GuiAnimationTimelinePanel`: replace `SoundEvent.getLocation()` with `location()`.
- `GuiShapeConfiguration`: pass the actual scroll parent into the current four-argument config registry `create` method.
- `GuiBlueprintAdd`: pass the items scroll parent to `GuiBlueprintAddEntry` and its `GuiPanel` constructor.
- `LittleDoorAdvancedGui`: pass the new tab parent to `GuiTimelineConfigDifferent`; initialize fixed timeline keys through the CreativeCore channel distribution hook and position them using `timeline.adjustKeyPositionX`.
- These changes target eight diagnostic instances, not a measured error-count reduction. They were source-reviewed against the log, CreativeCore sources and the SHA-1-verified official 26.3 client class signatures; they were not compiled or runtime-tested. Timeline positioning and GUI behavior require local testing. The larger rendering rewrite and other GUI/API issues remain open.
- Only the files supplied in the Round 40 overlay plus the Round 41 changes were available as an exact baseline; all 159 compiler diagnostic source lines for these available Java files matched this build log. Apply this overlay only to the complete post-Round-41 tree.

# Round 41 — targeted CreativeCore GUI compatibility fixes (uncompiled)

- Baseline: the supplied post-Round-40 build log reports 1,274 javac errors (1,180 distinct file/line/message tuples across 176 files). Gradle repeats the diagnostics; the repeated summary is not a second compilation.
- `GuiChisel` and `GuiGlove`: use the CreativeCore `GuiStackSelector(IGuiParent, String, StackCollector, boolean)` constructor, dropping the obsolete player argument and passing the actual `left` parent. The collector still receives the player.
- `GuiBag.BagSlot`: use `Slot.getContainerSlot()` for the three ingredient-list index reads.
- `GuiBlankOMatic`: read `IntTag.intValue()` and supply the `GuiVariantSelector` parent to `GuiSlotViewer`.
- `GuiSignalController`: combine `super.iterator()` (which visits hover controls followed by normal controls) with the grid iterator; use `GuiControl.setParent(node, this)`; clear normal controls through `remove(0)` so hover controls remain untouched and CreativeCore's parallel distribution lists stay synchronized.
- The changes target 11 diagnostic instances in the supplied log, not a measured error-count reduction. Renderer changes, item drop prediction, and other GUI migration issues remain open. All changes are source-reviewed only, not compiled or runtime-tested; a fresh local Gradle build is required.
- The provided Round 40 overlay was the only available source for these edited Java files; all 170 diagnostic source lines for files in that overlay matched the log. The complete post-Round-40 checkout was not available in this sandbox. Apply this incremental archive only on top of the complete Round 40 baseline.

# Round 40 — Screen modifier keys, PoseStack rotation, GUI subclass constructors

- Round 39 result (build_7d5f5d.log): **1,369 -> 1,315 errors** (unique file+line+message 1,273 -> 1,220); the log matched the
  Round 39 sources line for line (1,220/1,220).
- 40 files edited by `tools/round40_fixes.py` (idempotent; the tool also reads build.log for the mulPose lines):
  - `Screen.hasControlDown/hasShiftDown/hasAltDown()` -> `Minecraft.getInstance().hasXDown()` (15 sites, import added). The static
    Screen methods are gone (javac); the replacement matches a real MC 1.21.9+ mod migration (BetterControls). Not compiled on 26.3.
  - `pose.mulPose(<Quaternionf>)` -> `pose.mulPose(new org.joml.Matrix4f().rotation(<q>))` (10 sites): javac lists only
    `PoseStack.mulPose(Matrix4fc)` and `mulPose(Transformation)`; JOML's `Matrix4f.rotation(Quaternionfc)` builds the same rotation.
  - 15 GUI subclass constructors now take `IGuiParent parent` first and forward it to super(...) (GuiActionDisplay,
    GuiColorProgressBar, GuiDirectionIndicator, GuiDistanceControl, GuiGridConfig, GuiPhysicalStateControl, GuiShapeConfiguration,
    GuiAnimationPanel, GuiAnimationViewer, GuiIsoAnimationViewer, GuiFilterConfiguration, GuiSignalController, GuiVariantSelector,
    CraftingGrid, GuiBlueprintMoveButton); 39 `new X(...)` sites got `<enclosing add() receiver>` or `this`. A `this` inside an
    anonymous or non-GUI class will surface as a compile error: fix by hand.
- Still pending by hand: `OverlayRenderer.actionDisplay` (field initializer, no GUI parent in scope).
- Deliberately not touched: GuiSignalNode (`super()` with no name/flow: needs a decision), GuiSlotControlSelect and the three
  tree-item classes (GuiTreeItem constructor layout unverified), `swing(hand, true)` (new SwingAnimation parameter),
  `drop(stack, false)` (new Prediction parameter), `loadWithComponents(CompoundTag, ...)` (needs ValueInput), packet getters
  (`ClientboundTeleportEntityPacket.getX()` etc.), and the whole rendering rewrite.
- Verification: source-reviewed and archive-verified; nothing compiled (no javac in the sandbox).

# Round 39 — evidence-backed API fixes applied to the real tree (supersedes the Round 38 overlay)

- Compiler count entering this round: **1,369**. The supplied java.zip and build_76dcf6.log match line for line (2,738/2,738).
- 31 source files edited by `tools/round39_fixes.py` (error-driven, idempotent; rerun: `python3 tools/round39_fixes.py build.log src/port/java`):
  - `new GuiPlayerInventoryGrid(parent, player)` -> `(parent)` (8 sites; javac: `required: IGuiParent`). Where the player now
    comes from is unverified: check at runtime.
  - `align/valign/spacing = x` -> `setAlign/setVAlign/setSpacing(x)` (28 sites; public GuiParent methods in the CreativeCore 26.x sources).
  - `ResourceKey.location()` -> `identifier()` (5 lines; Mojang rename in 1.21.11, from third-party release notes).
  - `<x>.getProfiler()` on Minecraft/MinecraftServer -> `Profiler.get()` + import (11 lines; the 1.21.2+ replacement; not compiled
    against 26.3).
  - Bare `controls.size()/get(i)/remove(x)` in GuiParent subclasses -> `size()/get(i)/remove(x)` (4 sites).
- Deliberately NOT changed: `controls.clear()` (GuiSignalController:608), `controls.iterator()` (GuiSignalController:201),
  `for (GuiControl c : controls)` (LittleDoorActivatorGui:74): `GuiParent.clear()` also clears hover controls, the old field did not.
- Not touched: the rendering rewrite (RenderSystem, Tesselator, BufferUploader, VertexBuffer, MultiBufferSource, GuiGraphics ->
  GuiGraphicsExtractor), GuiParent subclass constructors (`super(name)` without a parent), `Screen.hasControlDown()` (no verified
  26.3 replacement), `Minecraft.screen`/`getTimer()`/`getItemColors()`, packet getters.
- Verification: source-reviewed (every changed line was diffed) and archive-verified only. Nothing was compiled; the sandbox has no javac.

# Round 37 — 26.3 interaction and package migrations

- Added the missing NeoForge `TriState.isTrue/isFalse/isDefault` helpers used
  by the interaction bridges.
- Migrated item `use` call sites from removed `InteractionResultHolder` to
  the 26.3 `InteractionResult` return type.
- Updated `ItemInteractionResult` call sites to the current interaction
  result surface and migrated clear package moves for `GameRules`,
  `BlockAndTintGetter`, and `AbstractHorse`.
- Replaced the removed identifier exception import with a runtime parse
  guard; behavior remains “use the missing block” on malformed identifiers.
- This round is source-reviewed and archive-verified only because the
  sandbox still has no `javac`.

# Round 36 — BlockTile 26.3 override cleanup

- Removed 30 `@Override` annotations from `BlockTile` that the supplied
  26.3 compiler log explicitly rejected.
- The method bodies and existing behavior were left unchanged; this only
  acknowledges methods that are no longer declared on the 26.3 base classes.
- This round is source-reviewed and archive-verified only because the
  sandbox still has no `javac`.

# Round 35 — key and silent-sound API alignment

- Aligned the Fabric-side `KeyConfig` shim with the 26.3 input API:
  `InputConstants.Type.KEYBOARD` is used and modifier checks are kept
  compile-safe when the current Fabric-side `KeyModifier` has no Forge-only
  helper methods.
- Replaced the Forge-only deferred sound wrapper with vanilla's empty sound
  type for LittleTiles' intentionally silent block sound.
- This round is source-reviewed and archive-verified only because the
  sandbox still has no `javac`.

# Round 34 — disabled optional integration boundaries

- Moved the compile-time Chisels-and-Bits and The One Probe references used
  by non-excluded LittleTiles code behind `common.compat` boundaries.
- The Fabric boundaries deliberately report those integrations as unavailable
  and return no external preview data; this preserves the existing call sites
  without restoring Forge-only behavior.
- This round is source-reviewed and archive-verified only because the sandbox
  still has no `javac`.

# Round 33 — optional-loader and keybinding compatibility boundaries

- Added the missing CreativeCore `KeyConfig` surface used by the Fabric-side
  building modes: key matching, modifier lookup, and translated display text.
- Added narrow compile-only boundaries for the remaining optional NeoForge
  references: block snapshots, multipart-entity markers, server lifecycle
  lookup, attachment NBT keys, and optional-mod detection.
- These boundaries intentionally disable NeoForge-only behavior; they are not
  replacements for Fabric event dispatch, multipart entity handling, or
  capability persistence.
- This round is source-reviewed and archive-verified only because the
  sandbox still has no `javac`.

- Migrated `LittleEntity` and its level/animation subclasses from the removed `CompoundTag` entity hooks to the 26.3 `ValueInput`/`ValueOutput` hooks.
- Kept the existing top-level NBT keys and nested entity/chunk/block lists by using `CompoundTag.CODEC`, so the existing save format is retained.
- Migrated `BETiles` and its side-solid cache to the 26.3 block-entity hooks; update packets now enter through `loadWithComponents(TagValueInput)`.
- Added `ValueInput`/`ValueOutput` overloads for `LittleGrid` and `StructureAbsolute`.
- This round is source-reviewed against `mc-api.txt`/`mc-api3.txt` and the supplied 26.x Value I/O documentation, but remains uncompiled until the user runs the local Gradle build.

# Round 25 — latest 26.3 API fixes

- Replaced cooldown checks that passed `Item` with the new `ItemStack` overload.
- Updated `ServerEntity` construction to use `UpdateInterval.periodic(...)` and the 26.3 synchronizer callback.
- Migrated TNT and sitting-entity save/load hooks to `ValueInput`/`ValueOutput`; nested entity serialization now uses `TagValueInput`/`TagValueOutput`.
- Updated custom level neighbor callbacks to use 26.3 `Orientation`, and adapted block-entity packet loading.
- Fixed long render-tick narrowing, structure-location Optional unwrapping, and entity discard calls.

# Round 26 — registry and loader compatibility cleanup

- Added a small Fabric-side compatibility layer for eager registry registration, deferred holders, custom ingredient codecs, loader distribution checks, and the legacy event-bus references.
- Converted LittleTiles registry fields and component call sites to the wrapper's concrete values/holders.
- Migrated custom recipe serializers to the 26.3 `RecipeSerializer` record form.
- Replaced the removed `ModelResourceLocation` type with `Identifier`, and removed two excluded mixin accessor dependencies.

# Round 28 — entity, block-entity, and optional-integration cleanup

- Replaced the removed `BlockEntityType.Builder` calls with Fabric's `FabricBlockEntityTypeBuilder`.
- Finished the compiler-confirmed entity save/load migration: `EntitySpawnRequest`, `ValueInput.getFloatOr`, the missing `hurtServer` hooks, `TagValueOutput`, and the 26.3 `BlockEntity.getPosFromTag(ChunkPos, CompoundTag)` contract.
- Replaced removed CreativeCore origin methods with the current `IVecOrigin` surface and disabled the old auxiliary-light/capability hooks without deleting their call sites.
- Added a disabled Fabric Sable compatibility boundary and redirected all non-excluded callers to it. Sable behavior remains intentionally unavailable until a Fabric implementation is chosen.
- This overlay is incremental after Round 27. It is not compiled in this sandbox because only a Java runtime is available, not `javac`.

## Round 24 (uncompiled)
- Continued the 26.3 NBT/Optional migration through entity, block-entity, inventory, animation, structure-location, and blueprint code.
- Replaced removed `ChunkPos(BlockPos)` construction with section-coordinate constructors, and changed remaining protected `Level.random` reads to `getRandom()`.
- Updated nested `CompoundTag` reads and entity/structure import paths; this round is based on the 2,275-error build log.
- Changes are not compiled in this sandbox: only a Java 25 runtime is available, not `javac`.

## Round 23 (uncompiled)
- Fixed the current tag-key and creative-tab builder signatures (`TagKey.create`, `CreativeModeTab.builder(Row, column)`), plus item component access.
- Continued 26.3 Optional/NBT migration for registries, structure imports, chunk serialization, signals, and nested structure names; replaced `TagParser.parseTag` with `parseCompoundFully`.
- Updated current `NumericTag` accessors, `IVecOrigin.off/rot`, registry-key identifiers, and remaining `ChunkPos` long packing/construction sites.
- Changes are not compiled in this sandbox: only a Java 25 runtime is available, not `javac`.

## Round 22 (uncompiled)
- Reduced the next 2,371-error build set by converting compiler-confirmed 26.3 `CompoundTag` Optional reads to explicit defaults across grid, selection, signal, printer, placement, ingredient, and math code.
- Replaced direct `ChunkPos.x/z` field access with `x()/z()` across chunk serialization, biome updates, dirty-section updates, and render checks.
- Updated `LevelStem` lookup to unwrap the holder reference, and adapted block-tag/entity registration calls to the current identifier/key signatures.
- Changes are not compiled in this sandbox: only a Java 25 runtime is available, not `javac`.

## Round 21 (uncompiled)
- Fixed the remaining 26.3 `GuiComboBox` constructor call sites in `GuiChisel` and `GuiModeSelector`; both now pass their actual GUI parent.
- Converted the old NBT parser's remaining primitive `CompoundTag` reads to the 26.3 `Optional` getter forms, using explicit defaults; event IDs/sounds use `getStringOr`.
- Also updated the CreativeCore GUI constructor call sites identified from the provided CreativeCore sources so collection controls consistently receive a parent.
- Changes are not compiled in this sandbox: only a Java 25 runtime is available, not `javac`.


## Round 20 (uncompiled; verified against cached 26.3 classes and mc-api.txt)
- Updated the server-level/chunk-cache constructor bridges to the 26.3 `ServerLevel` and `ServerChunkCache` signatures; `Registry.get(LevelStem.OVERWORLD)` now unwraps its `Optional`.
- Replaced `ChunkPos.asLong/toLong` with `ChunkPos.pack`, `ResourceKey.location()` with `identifier()` in the chunk executor label, and changed `DistanceManager.hasPlayersNearby` to return `TriState`.
- Removed obsolete 26.3 ticket/broadcast overrides (`TicketType<T>`, region-ticket helpers, `removeTicketsOnClosing`, `getTickingGenerated`); `updateChunkForced` now matches the boolean return type but remains a no-op pending a real ticket/forced-chunk design.
- Adapted `LittleEntity` lifecycle/kill/bounding-box code to removed/final 26.3 Entity hooks. Entity nested save/load still needs the `TagValueInput`/`TagValueOutput` bridge.
- Changes are not compiled in this sandbox: only a Java 25 runtime is available, not `javac`.

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
