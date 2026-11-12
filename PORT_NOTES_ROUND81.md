# Round 81 — chunk-data event stub, light emission, block-entity off-screen check (uncompiled)

Paste this entry at the top of PORT_NOTES.md (separate file so notes for other rounds are not overwritten).

- Round 80 result, from comparing build_1.log and build_d8b2e6.log (assumes only Round 80 was added between them): the summary fell from 561 to 557 errors. The 4 targeted locations are gone and no new one appeared.
- `ChunkDataEvent` (local NeoForge event stub): `Load(ChunkAccess, CompoundTag, ChunkType)` and `Save(ChunkAccess, LevelAccessor, CompoundTag)` constructors added, with the no-argument ones kept. The argument types are the ones javac listed as `found:` for the two `LittleChunkSerializer` calls; `ChunkType` is the `world.level.chunk.status` type that file already imports. Like the other stubs it is never dispatched. `LittleChunkSerializer` itself is not edited (its source here is older than your tree).
- `LittleRenderPipelineForge`: `BlockStateBase.getLightEmission(level, pos)` no longer exists; the no-argument `getLightEmission()` (listed in the API dump and used elsewhere in this tree) replaces it. Block lights that depended on the position are no longer honoured. `Minecraft.useAmbientOcclusion()` on the same line is still open: it is not in the API dump.
- `LittleAnimationRenderManager`: `BlockEntityRenderer.shouldRenderOffScreen(entity)` is now `shouldRenderOffScreen()` (API dump). The check is no longer per block entity.
- Source-reviewed only. The three files parsed with the JDK compiler (no syntax errors). Expected change: 4 locations (2 `ChunkDataEvent`, 1 `getLightEmission`, 1 `shouldRenderOffScreen`). No new count is claimed.
