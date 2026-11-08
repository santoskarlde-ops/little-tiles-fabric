# GUI split plan (verified against CreativeCore 26.x sources)

## How controls map to client classes (GuiClientRegistry, read from source)

- `GuiClientRegistry.register(Class<T extends GuiControl>, Function<T, GuiClientControl>)` maps a control class to its client class.
- Lookup order: exact class match, then `registerSpecial` factories, then the nearest *registered superclass* (scanned in reverse registration order). So a LittleTiles control that adds no client behaviour needs nothing; one that overrides render/flow/mouse needs its own client class and an explicit `register(...)`.
- Server side: `GuiServerRegistry.register(Class, Function<T, GuiServerControl>)`; unmatched controls get `null` (no server dist), which is fine for pure-client controls.
- Client classes extend `GuiClientControl<T>` (or the client class of the control's superclass, e.g. `GuiClientParent`, `GuiClientLabel`) and implement the control's nested `XDist` interface. `getControlFormatting()`, `renderContent(GuiGraphicsExtractor,...)`, `flowX/flowY`, mouse handlers live there.
- Controls get their dist in the `GuiControl` constructor via `parent.createDist(this)`, so the parent chain (GuiLayer client flag) decides client vs server.

## LittleTiles files with GUI override errors (round 12 log)

| file | class extends | overrides flagged |
|---|---|---|
| client/render/block/BETilesRenderer.java | ? extends ? | render |
| client/render/entity/RenderSizedTNTPrimed.java | RenderSizedTNTPrimed extends TntRenderer | render |
| client/render/overlay/OverlayRenderer.java | OverlayGuiLayer extends GuiLayer | flowY, getControlFormatting |
| common/entity/particle/LittleParticle.java | LittleParticle extends TextureSheetParticle | tick |
| common/gui/control/GuiActionDisplay.java | GuiActionDisplay extends GuiControl | flowX, flowY, getControlFormatting, preferredHeight, preferredWidth |
| common/gui/control/GuiColorProgressBar.java | GuiColorProgressBar extends GuiProgressbar | getControlFormatting, getTooltip, mouseClicked, renderProgress |
| common/gui/control/animation/GuiAnimationViewer.java | GuiAnimationViewer extends GuiControl | flowX, flowY, getControlFormatting, keyPressed, keyReleased, mouseClicked, mouseMoved, mouseReleased, mouseScrolled, preferredHeight, preferredWidth |
| common/gui/control/animation/GuiIsoAnimationViewer.java | GuiIsoAnimationViewer extends GuiControl | flowX, flowY, getControlFormatting, mouseClicked, mouseMoved, mouseReleased, mouseScrolled, preferredHeight, preferredWidth |
| common/gui/signal/GuiSignalController.java | GuiSignalController extends GuiParent | flowX, flowY, getControlFormatting, mouseClicked, mouseDragged, mouseMoved, mouseReleased, mouseScrolled |
| common/gui/signal/node/GuiSignalNodeInput.java | GuiSignalNodeInput extends GuiSignalNodeComponent | looseFocus, mouseDoubleClicked |
| common/gui/signal/node/GuiSignalNodeVirtualInput.java | GuiSignalNodeVirtualInput extends GuiSignalNode | mouseDoubleClicked |
| common/gui/signal/node/GuiSignalNodeVirtualNumberInput.java | GuiSignalNodeVirtualNumberInput extends GuiSignalNode | mouseDoubleClicked |
| common/gui/structure/GuiBlankOMatic.java | GuiBlankOMatic extends GuiLayer | getControlFormatting, mouseClicked |
| common/gui/tool/GuiConfigure.java | GuiConfigure extends GuiLayer | keyPressed |
| common/gui/tool/blueprint/GuiBlueprint.java | GuiBlueprint extends GuiConfigure | render |
| common/gui/tool/blueprint/GuiBlueprintAdd.java | GuiBlueprintAdd extends GuiLayer | mouseClicked, render |
| common/gui/tool/blueprint/GuiTreeItemStructure.java | GuiTreeItemStructure extends GuiTreeItem | getControlFormatting |

Next step: for each row, create `<Class>Dist` (client) extending the client class of the superclass, move the flagged methods into it, and register it from the client entrypoint. Rendering bodies need Phase 4 (`GuiGraphicsExtractor`, RenderType, PoseStack replacements).