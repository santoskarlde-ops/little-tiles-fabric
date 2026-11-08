# NeoForge -> Fabric replacement map (Phase 2, still unported)

55 distinct `net.neoforged.*` APIs are still imported (build_a6bf46.log). None can be fixed by a codemod: each `@SubscribeEvent` handler
has to be rewritten against a Fabric callback. This file is the plan, with what is verified and what is not.

## Verified
- Fabric API 0.144.3+26.1 javadoc lists, in `net.fabricmc.fabric.api.event.player`: PlayerBlockBreakEvents.{Before,After,Canceled}, AttackBlockCallback,
  AttackEntityCallback, UseBlockCallback, BlockEvents.{UseItemOnCallback,UseWithoutItemCallback}, ItemEvents.{UseCallback,UseOnCallback}.
  (26.1 docs, not 0.165.0+26.3: re-check the exact names before writing code.)
- Since 26.1 Fabric API names follow Mojang's official names; Fabric publishes a rename list and an IntelliJ migration map.
- Renderer API and Indigo were ported to 26.2 (Fabric API 0.152.x changelog), so Phase 4 can use them on 26.3.

## Candidate mapping (from general Fabric API knowledge, NOT yet checked against 0.165.0+26.3)
| NeoForge | Fabric candidate | Notes |
|---|---|---|
| PlayerInteractEvent.{RightClickBlock,LeftClickBlock,EntityInteract*,RightClickItem} | UseBlockCallback, AttackBlockCallback, UseEntityCallback, UseItemCallback | RightClickEmpty/LeftClickEmpty are client-only in Neo: need a client packet or mixin |
| BlockEvent.BreakEvent, BlockSnapshot | PlayerBlockBreakEvents.Before/After | |
| ServerTickEvent, LevelTickEvent | ServerTickEvents, ServerLevelTickEvents | |
| EntityJoinLevelEvent, EntityLeaveLevelEvent | ServerEntityEvents.ENTITY_LOAD / ENTITY_UNLOAD | |
| PlayerLoggedIn/OutEvent | ServerPlayConnectionEvents.JOIN / DISCONNECT | |
| ChunkEvent, LevelEvent | ServerChunkEvents, ServerWorldEvents | |
| StartTracking / StopTracking | EntityTrackingEvents | |
| FMLCommonSetupEvent | ModInitializer | |
| RenderLevelStageEvent, RenderHighlightEvent, RenderBlockScreenEffectEvent, RenderGuiEvent | WorldRenderEvents, HudRenderCallback or mixin | render pipeline changed in 26.x: Phase 4 |
| InputEvent, KeyConfig (KeyModifier) | KeyBindingHelper + manual modifier polling | decision pending |
| Capabilities / IItemHandler / InvWrapper | Fabric Transfer API (ItemStorage) | |
| ICustomIngredient / IngredientType | Fabric CustomIngredient | |
| AttachmentHolder | Fabric Data Attachment API | |
| ModelData / ModelDataManager | Renderer API + block entity render data | Phase 4 |
| IClientBlockExtensions, AddSectionGeometryEvent | Renderer API / mixin | Phase 4 |
| DeferredRegister / DeferredHolder / NeoForgeRegistries / DeferredSoundType | Registry.register at init, SoundType | |
| PartEntity | no API: own entity-part handling | |
| CanPlayerSleepEvent, CanContinueSleepingEvent, PlayerWakeUpEvent, BreakSpeed, ItemEntityPickupEvent | no Fabric API expected: Mixin | verify first |
| CommonHooks, EventHooks, ClientHooks, ServerLifecycleHooks, ILevelExtension, ConnectionType, TriState | case by case | |
