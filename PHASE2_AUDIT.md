# Phase 2 audit: CreativeCore gap for LittleTiles

Baseline: LittleTiles `1.21` (builds against CreativeCore 2.13.52) vs CreativeCore `26.x` @ e9dd5bde (2.14.24).
LittleTiles imports 232 distinct CreativeCore classes. 209 exist unchanged in 26.x. The other 23 break down as follows.
Method: import list diffed against the 26.x file tree, then re-checked after the relocation script. Not yet compiled.

## A. Moved packages: fixed mechanically (9 classes, 22 files)
`python3 tools/relocate_cc_imports.py <src dir>` (verified on a scratch copy; 23 missing -> 14, plus 2 forge-only).
- common.util.math.origin.{VecOrigin, ChildVecOrigin, IVecOrigin} -> common.util.math.matrix
- common.gui.GuiControlRect -> client.gui
- common.gui.extension.GuiExtensionCreator (+ nested ExtensionDirection) -> client.gui.extension
- common.gui.integration.ScreenEventListener -> client.gui.integration

## B. Forge-only in CreativeCore 26.x: no Fabric implementation (2 classes)
| Class | LT users | Notes |
|---|---|---|
| BlockEntityCreative | BETiles | 54 lines, only uses NeoForge EffectiveSide. Reimplement in LT's Fabric package. |
| KeyConfig | 5 BuildingMode* files | 85 lines, uses NeoForge KeyModifier + InputEvent. Fabric has no KeyModifier: needs a design decision (modifier combos). |

## C. Removed from CreativeCore, small and self-contained: copy into LT with LGPL headers (4 classes)
| Class | LT users | Notes |
|---|---|---|
| BlockFilter | 3 | 251 lines, only CreativeCore-internal deps. Check the premade/BlockFilters replacement first. |
| IOriginPose | 2 (Sable integration) | 155 lines. Optional integration only. |
| GuiButtonHoldSlim | 1 (GuiGridConfig) | 37 lines. Removed in the 2025 GUI rework; adapt to the split client/common GuiButtonHold. |
| GuiRenderHelper | 3 | 179 lines. Removed at the 1.21.6 port; needs the current GUI render API in client/render/gui. |

## D. Rendering classes removed upstream: real rewrite, belongs to Phase 4 (8 classes)
- ChunkLayerMap (18 LT files), ChunkLayerMapList (8): deleted in CreativeCore's "Ported to 26.3" commit (2026-09-16).
- CreativeBakedQuad, CreativeQuadLighter, CreativeBakedItemLayer, CreativeBakedBoxItemLayer, CreativeBakedLayerModel, ItemModelBox, ItemModelPreview, ForgeModelBlockRendererAccessor: removed at CreativeCore's 1.21.5 port (the item/model system was rewritten). Each has 1-3 LT users, all in item and preview rendering.

## Still to inventory (needs LittleTiles source in the tree: Phase 2b/3)
- NeoForge usage inside LittleTiles itself: ~31 NeoForge.EVENT_BUS uses, IItemHandler/Capabilities -> Transfer API, ModelData, IngredientType, CommonHooks/ClientHooks/EventHooks, ModList.
- Whether signatures of the 209 unchanged-by-name classes changed. Only the compiler can tell.

## Decision needed
KeyConfig: Fabric has no key modifiers. Options: (1) Fabric key mappings with no modifier support, (2) detect Ctrl/Shift/Alt manually in a client tick hook, (3) use Controlify/Fabric key API extras if present. I will default to (2) unless you say otherwise.
