package team.creative.littletiles.common.gui.structure;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.control.inventory.GuiInventoryGrid;
import team.creative.creativecore.common.gui.control.inventory.GuiPlayerInventoryGrid;
import team.creative.creativecore.common.gui.control.inventory.IGuiInventory;
import team.creative.creativecore.common.gui.control.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.control.parent.GuiPanel;
import team.creative.creativecore.common.gui.control.parent.GuiScrollY;
import team.creative.creativecore.common.gui.control.simple.GuiButton;
import team.creative.creativecore.common.gui.control.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.control.simple.GuiTextfield;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.creativecore.common.util.mc.TooltipUtils;
import team.creative.littletiles.common.structure.type.premade.Little3dPrinter;

public class Gui3dPrinter extends GuiLayer {
    
    public Little3dPrinter printer;
    public SimpleContainer blueprint = new SimpleContainer(1);
    public final GuiSyncLocal<EndTag> SET = getSyncHolder().register("craft", x -> {
        printer.setBlueprint(blueprint.getItem(0));
        
        sendUpdate();
    });
    
    private boolean first = true;
    private CompoundTag sendNBT;
    
    private GuiLabel indexLabel;
    private GuiScrollY errorScroll;
    private GuiTextfield skipField;
    private GuiTextfield amountField;
    private GuiLabel speedLabel;
    private GuiLabel activeLabel;
    private GuiCheckBox continueBox;
    private GuiCheckBox rememberBox;
    private GuiLabel blueprintLabel;
    
    public final GuiSyncLocal<CompoundTag> UPDATE = getSyncHolder().register("update", nbt -> {
        printer.load(nbt, provider());
        
        errorScroll.clear();
        for (List<Component> error : printer.errors())
            errorScroll.add(new GuiLabel(errorScroll, "").setTitle(error));
        indexLabel.setTitle(Component.translatable("gui.3dprinter.index", printer.printIndex()));
        
        skipField.setText("" + printer.ticksToSkip);
        amountField.setText("" + printer.amountPerTick);
        
        activeLabel.setTranslate(printer.getOutput(0).getState().any() ? "gui.3dprinter.active" : "gui.3dprinter.inactive");
        blueprintLabel.setTitle(printer.blueprintInfo());
        
        updateSpeedLabel();
        reflow();
    });
    public final GuiSyncLocal<EndTag> TOGGLE = getSyncHolder().register("toggle", x -> {
        printer.getOutput(0).toggle();
        sendUpdate();
    });
    public final GuiSyncLocal<EndTag> RESET = getSyncHolder().register("reset", x -> {
        printer.resetKeepErrors();
        sendUpdate();
    });
    public final GuiSyncLocal<EndTag> CLEAR_ERRORS = getSyncHolder().register("clear_errors", x -> {
        printer.clearErrors();
        sendUpdate();
    });
    public final GuiSyncLocal<CompoundTag> SAVE = getSyncHolder().register("save", nbt -> {
        printer.ticksToSkip = Math.max(0, nbt.getInt("skip").orElse(0));
        printer.amountPerTick = Math.max(1, nbt.getInt("amount").orElse(1));
        
        printer.continueOnError = nbt.getBoolean("continue").orElse(false);
        printer.rememberStructures = nbt.getBoolean("remember").orElse(false);
        
    });
    
    public Gui3dPrinter(boolean client, Little3dPrinter printer) {
        super(client, "3dprinter", 300, 200);
        this.printer = printer;
        registerEventChanged(x -> {
            if (x.control.is("skip", "amount"))
                updateSpeedLabel();
        });
    }
    
    public void sendUpdate() {
        if (isClient())
            return;
        
        if (first) {
            first = false;
            return;
        }
        CompoundTag nbt = new CompoundTag();
        printer.save(nbt, provider());
        
        if (!nbt.equals(sendNBT)) {
            UPDATE.send(nbt);
            sendNBT = nbt;
        }
    }
    
    protected void updateSpeedLabel() {
        double amountPerSecond = amountField.parseInteger() / (double) (skipField.parseInteger() + 1) * 20;
        speedLabel.setTitle(Component.translatable("gui.3dprinter.speed", TooltipUtils.print(amountPerSecond)));
    }
    
    @Override
    public void create() {
        setFlow(GuiFlow.STACK_Y);
        setAlign(Align.CENTER);
        
        GuiPanel savable = new GuiPanel(this, GuiFlow.STACK_Y);
        add(savable);
        
        GuiParent speed = new GuiParent(this).setVAlign(VAlign.CENTER);
        savable.add(speed);
        speed.add(new GuiLabel(speed, "skipLabel").setTranslate("gui.3dprinter.skip"));
        speed.add(skipField = new GuiTextfield(speed, "skip", "" + printer.ticksToSkip).setDim(25, 7).setNumbersOnly());
        speed.add(new GuiLabel(speed, "amountLabel").setTranslate("gui.3dprinter.amount"));
        speed.add(amountField = new GuiTextfield(speed, "amount", "" + printer.amountPerTick).setDim(25, 7).setNumbersOnly());
        speed.add(speedLabel = new GuiLabel(speed, "speed"));
        
        GuiParent config = new GuiParent(this).setVAlign(VAlign.CENTER);
        savable.add(config);
        config.add(continueBox = new GuiCheckBox(config, "continue", printer.continueOnError).setTranslate("gui.3dprinter.continue"));
        config.add(rememberBox = new GuiCheckBox(config, "remember", printer.rememberStructures).setTranslate("gui.3dprinter.remember"));
        config.add(new GuiButton(config, "save", x -> {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("skip", skipField.parseInteger());
            nbt.putInt("amount", amountField.parseInteger());
            
            nbt.putBoolean("continue", continueBox.get());
            nbt.putBoolean("remember", rememberBox.get());
            SAVE.send(nbt);
        }).setTranslate("gui.save"));
        
        GuiParent content = new GuiParent(this, GuiFlow.STACK_X).setVAlign(VAlign.CENTER);
        content.setSpacing(5);
        add(content);
        content.add(new GuiInventoryGrid(content, "blueprint", blueprint));
        content.add(new GuiButton(content, "set", x -> SET.send(EndTag.INSTANCE)).setTranslate("gui.3dprinter.set"));
        content.add(blueprintLabel = new GuiLabel(content, "blueprint_info"));
        
        GuiPanel state = new GuiPanel(this, GuiFlow.STACK_Y);
        add(state);
        GuiParent active = new GuiParent(this).setVAlign(VAlign.CENTER);
        active.setSpacing(5);
        state.add(active);
        active.add(activeLabel = new GuiLabel(active, "active"));
        active.add(new GuiButton(active, "toggle", x -> TOGGLE.send(EndTag.INSTANCE)).setTranslate("gui.3dprinter.toggle"));
        active.add(indexLabel = new GuiLabel(active, "index"));
        active.add(new GuiButton(active, "reset", x -> RESET.send(EndTag.INSTANCE)).setTranslate("gui.3dprinter.reset"));
        
        GuiLeftRightBox errors = new GuiLeftRightBox(this);
        errors.setSpacing(5);
        state.add(errors);
        errors.getLeft().setFlow(GuiFlow.STACK_Y);
        errors.getLeft().setFixedX();
        errors.addLeft(new GuiLabel(errors, "errors").setTranslate("gui.3dprinter.errors"));
        errors.addLeft(new GuiButton(errors, "clear", x -> CLEAR_ERRORS.send(EndTag.INSTANCE)).setTranslate("gui.3dprinter.clear"));
        errorScroll = new GuiScrollY(this);
        errors.addRight(errorScroll.setDim(40, 40).setExpandableX());
        
        GuiParent inventories = new GuiParent(this);
        add(inventories);
        GuiParent printerInventory = new GuiParent(this, GuiFlow.STACK_Y);
        inventories.add(printerInventory);
        printerInventory.add(new GuiLabel(printerInventory, "inventory_label").setTranslate("gui.3dprinter.inventory"));
        printerInventory.add(new GuiInventoryGrid(printerInventory, "printer_inventory", printer.inventory, 5));
        
        GuiParent playerInventory = new GuiParent(this, GuiFlow.STACK_Y).setAlign(Align.CENTER);
        inventories.add(playerInventory.setExpandable());
        playerInventory.add(new GuiLabel(playerInventory, "inventory_label").setTranslate("gui.player.inventory"));
        playerInventory.add(new GuiPlayerInventoryGrid(playerInventory).setFixed());
        
        updateSpeedLabel();
    }
    
    @Override
    public Iterable<IGuiInventory> inventoriesToInsert() {
        List<IGuiInventory> inventories = new ArrayList<>();
        for (IGuiInventory inv : super.inventoriesToInsert())
            inventories.add(0, inv);
        return inventories;
    }
    
    @Override
    public void tick() {
        super.tick();
        sendUpdate();
    }
    
    @Override
    public void closed() {
        super.closed();
        PlayerUtils.addOrDrop(getPlayer(), blueprint);
    }
}
