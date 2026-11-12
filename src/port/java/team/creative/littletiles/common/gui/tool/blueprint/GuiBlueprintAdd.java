package team.creative.littletiles.common.gui.tool.blueprint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import team.creative.littletiles.client.gui.GuiClientBlueprintLayers;
import net.fabricmc.api.Environment;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.IGuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.control.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.control.parent.GuiPanel;
import team.creative.creativecore.common.gui.control.parent.GuiScrollY;
import team.creative.creativecore.common.gui.control.simple.GuiButton;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.control.simple.GuiShowItem;
import team.creative.creativecore.common.gui.control.tree.GuiTree;
import team.creative.creativecore.common.gui.control.tree.GuiTreeItem;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiSizeRatioRules;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.client.level.little.FakeClientLevel;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.gui.AnimationPreview;
import team.creative.littletiles.common.gui.control.animation.GuiAnimationPanel;
import team.creative.littletiles.common.gui.control.animation.GuiAnimationViewerStorage;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.server.level.little.FakeServerLevel;

public class GuiBlueprintAdd extends GuiLayer implements GuiAnimationViewerStorage {
    static {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
            GuiClientBlueprintLayers.registerGuiBlueprintAdd();
    }

    
    public static String generateGroupName(LittleGroup group) {
        String name = group.getStructureName();
        if (name != null)
            return name;
        
        LittleStructureType type = group.getStructureType();
        if (type != null)
            return type.id;
        return "none";
    }
    
    public GuiBlueprint blueprint;
    private ItemStack selected;
    
    private volatile int requestedPreview = 0;
    private volatile AnimationPreview current;
    private volatile int executedPreview = 0;
    private AtomicReference<GuiBlueprintAddAnimationRequest> scheduled = new AtomicReference<>();
    
    public GuiBlueprintAdd(boolean client) {
        super(client, "blueprint.add", 400, 200);
        setFlow(GuiFlow.STACK_Y);
    }
    
    public void init(GuiBlueprint blueprint) {
        this.blueprint = blueprint;
        clear();
        init();
    }
    
    private List<ItemStack> collectTiles() {
        List<ItemStack> blueprints = new ArrayList<>();
        Inventory inv = getPlayer().getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof ILittlePlacer placer && placer.hasTiles(stack)) {
                LittleGroup group = placer.getTiles(stack);
                if (group == null || group.isEmptyIncludeChildren())
                    continue;
                blueprints.add(stack);
            }
        }
        return blueprints;
    }
    
    private void addGroup(GuiTreeItem parent, LittleGroup group) {
        GuiBlueprintAddTreeItem item = new GuiBlueprintAddTreeItem(parent.tree, group);
        parent.addItem(item);
        
        for (LittleGroup child : group.children.children())
            addGroup(item, child);
    }
    
    public void setAnimation(int index, AnimationPreview preview) {
        synchronized (scheduled) {
            if (requestedPreview == -1 || requestedPreview > index) {
                if (preview != null)
                    preview.unload();
            } else
                scheduled.set(new GuiBlueprintAddAnimationRequest(preview, index));
        }
    }
    
    public void select(ItemStack stack) {
        if (this.selected == stack)
            return;
        
        this.selected = stack;
        GuiTree tree = get("tree");
        tree.root().clearItems();
        
        ILittlePlacer placer = (ILittlePlacer) stack.getItem();
        if (placer.hasTiles(stack)) {
            LittleGroup group = placer.getTiles(stack);
            
            if (group.isEmpty())
                for (LittleGroup child : group.children.children())
                    addGroup(tree.root(), child);
            else
                addGroup(tree.root(), group);
            
            tree.updateTree();
            synchronized (scheduled) {
                requestedPreview++;
            }
            final int request = requestedPreview;
            FakeClientLevel fakeLevel = FakeServerLevel.createClient("animationViewer");
            CompletableFuture.supplyAsync(() -> {
                try {
                    return new AnimationPreview(fakeLevel, null, group, provider());
                } catch (LittleActionException e) {
                    throw new RuntimeException(e);
                }
            }).whenComplete((preview, throwable) -> {
                setAnimation(request, preview);
                if (throwable != null)
                    throwable.printStackTrace();
            });
            get("save").setEnabled(true);
            return;
        }
        synchronized (scheduled) {
            requestedPreview++;
        }
        setAnimation(requestedPreview, null);
        get("save").setEnabled(false);
    }
    public void create() {
        GuiParent upper = new GuiParent(this);
        add(upper.setExpandable());
        
        GuiScrollY items = new GuiScrollY(this);
        upper.add(items.setDim(new GuiSizeRatioRules().widthRatio(0.3F).maxWidth(150)));
        
        List<ItemStack> stacks = collectTiles();
        for (ItemStack stack : stacks)
            items.add(new GuiBlueprintAddEntry(items, stack, getPlayer()));
        
        GuiTree tree = new GuiTree(this, "tree").setRootVisibility(false).setCheckboxes(true, true);
        upper.add(tree.setDim(new GuiSizeRatioRules().widthRatio(0.3F).maxWidth(100)).setExpandableY());
        
        upper.add(new GuiAnimationPanel(upper, tree, this, false, null).setExpandable());
        
        GuiLeftRightBox bottom = new GuiLeftRightBox(this);
        add(bottom);
        
        bottom.addLeft(new GuiButton(bottom, "cancel", x -> closeThisLayer()).setTranslate("gui.cancel"));
        bottom.addLeft(new GuiButton(bottom, "reload", x -> {
            this.selected = null;
            clear();
            init();
        }).setTranslate("gui.reload"));
        bottom.addRight(new GuiButton(bottom, "save", x -> {
            if (selected == null)
                return;
            
            GuiTreeItem parent = blueprint.tree.selected();
            if (parent == null)
                parent = blueprint.tree.root();
            
            LittleGroup group = reconstructBlueprint();
            if (group == null || group.isEmptyIncludeChildren())
                return;
            blueprint.buildStructureTree(blueprint.tree, parent, group, parent.itemsCount());
            
            closeThisLayer();
            
            blueprint.tree.updateTree();
        }).setTranslate("gui.import").setEnabled(false));
    }
    public void beforeClientRender() {
        synchronized (scheduled) {
            GuiBlueprintAddAnimationRequest request = scheduled.getAndSet(null);
            if (request != null && executedPreview < request.index) {
                if (current != null)
                    current.unload();
                current = request.preview;
                executedPreview = request.index;
            }
        }
    }
    
    protected LittleGroup reconstructBlueprint(GuiBlueprintAddTreeItem item) {
        List<LittleGroup> children = new ArrayList<>();
        for (GuiTreeItem child : item.itemsChecked())
            children.add(reconstructBlueprint((GuiBlueprintAddTreeItem) child));
        return new LittleGroup(item.group.getStructureTag(), item.group.copyExceptChildren(), children);
    }
    
    protected LittleGroup reconstructBlueprint() {
        GuiTree tree = get("tree");
        if (tree.root().itemsCount() == 1)
            return reconstructBlueprint((GuiBlueprintAddTreeItem) tree.root().items().iterator().next());
        List<LittleGroup> children = new ArrayList<>();
        for (GuiTreeItem child : tree.root().itemsChecked())
            children.add(reconstructBlueprint((GuiBlueprintAddTreeItem) child));
        if (children.isEmpty())
            return null;
        return new LittleGroup((CompoundTag) null, children);
    }
    public void closed() {
        super.closed();
        requestedPreview = -1;
        if (current != null)
            current.unload();
    }
    
    public class GuiBlueprintAddEntry extends GuiPanel {
        
        public final ItemStack stack;
        
        public GuiBlueprintAddEntry(IGuiParent parent, ItemStack stack, Player player) {
            super(parent);
            this.stack = stack;
            setFlow(GuiFlow.STACK_X);
            
            add(new GuiShowItem(this, "show", stack).setDim(40, 40));
            GuiParent right = new GuiParent(this, GuiFlow.STACK_Y);
            add(right.setVAlign(VAlign.CENTER).setExpandable());
            
            for (Component line : stack.getTooltipLines(TooltipContext.EMPTY, player, TooltipFlag.NORMAL))
                right.add(new GuiLabel(right, "label").setTitle(line));
        }
        public boolean mouseClicked(double x, double y, int button) {
            select(stack);
            if (dist() != null)
                dist().playSound(SoundEvents.UI_BUTTON_CLICK);
            return true;
        }
        
    }
    public boolean isReady() {
        return current != null;
    }
    public double longestSide() {
        return Math.max(current.box.maxX - current.box.minX, Math.max(current.box.maxY - current.box.minY, current.box.maxZ - current.box.minZ));
    }
    public AABB overall() {
        return current.box;
    }
    public Vec3d center() {
        return new Vec3d(current.box.getCenter());
    }
    public boolean highlightSelected() {
        return false;
    }
    public void highlightSelected(boolean value) {}
    @Environment(EnvType.CLIENT)
    public Iterable<AnimationPreview> previewsToRender() {
        return Collections.EMPTY_LIST;
    }
    @Environment(EnvType.CLIENT)
    public void renderAll(PoseStack pose, Matrix4f projection, Minecraft mc) {
        renderPreview(pose, projection, current, mc);
    }
    
    public static class GuiBlueprintAddTreeItem extends GuiTreeItem {
        
        public final LittleGroup group;
        
        public GuiBlueprintAddTreeItem(GuiTree tree, LittleGroup group) {
            super(tree, "item", tree);
            this.group = group;
            this.setTitle(Component.literal(generateGroupName(group)));
        }
        
    }
    
    private static record GuiBlueprintAddAnimationRequest(AnimationPreview preview, int index) {}
    
}
