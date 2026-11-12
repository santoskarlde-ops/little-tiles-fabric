# Round 80 — Block.getFriction and LiquidBlock fluid access (uncompiled)

Paste this entry at the top of PORT_NOTES.md (separate file so no notes for other rounds are overwritten).

- Round 79 result, from comparing the two supplied logs (build.log and build_1.log; this assumes only Round 79 was added between them): the summary stays at 561 errors. The 4 targeted locations (`ILittleMCBlock` 127, `LittleMCBlock` 129, `Placement` 207, `SubServerLevel` 55) are gone, including the unverified `BlockTags.ENCHANTMENT_POWER_PROVIDER`, which produced no error. 4 other locations appeared in the same two files (`getFriction`, `LiquidBlock.fluid`), so the net change is 0.
- `ILittleMCBlock` / `LittleMCBlock` `getFriction`: `Block.getFriction(state, level, pos, entity)` no longer exists; `Block.getFriction()` takes no arguments (mc-api.txt lists it on `Block`). The call now uses it. This is NeoForge's default (the block's own friction); a modded block that overrode the old per-position hook is no longer asked.
- `isFluid`: `LiquidBlock.fluid` is protected. It now asks the block's default state: `defaultBlockState().getFluidState().getType().is(fluid)`. A liquid block's default state is level 0, the source fluid, which is what the old field held. `Fluid.is(TagKey)` is the call the old line used.
- Source-reviewed against the API dump only; the two files were checked with the JDK parser (no syntax errors). Expected change: at most 4 locations. No new count is claimed.
