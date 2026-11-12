package team.creative.littletiles.common.gui.control;

import team.creative.creativecore.common.gui.IGuiParent;
import java.util.function.Consumer;

import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.control.collection.GuiComboBox;
import team.creative.creativecore.common.gui.control.simple.GuiButtonHold;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.grid.LittleGrid;

public class GuiGridConfig extends GuiParent {
    
    public final GuiComboBox<LittleGrid> comboBox;
    public Consumer<LittleGrid> consumer;
    
    public GuiGridConfig(IGuiParent parent, String name, Player player, LittleGrid grid, Consumer<LittleGrid> consumer) {
        super(parent, name);
        setFlow(GuiFlow.STACK_X);
        setVAlign(VAlign.CENTER);
        comboBox = new GuiComboBox<LittleGrid>(this, "grid", LittleTiles.CONFIG.build.get(player).gridBuilder());
        comboBox.select(grid);
        add(new GuiButtonHold<>(this, "left", x -> comboBox.previous()).setTranslate("gui.previous"));
        add(comboBox);
        add(new GuiButtonHold<>(this, "right", x -> comboBox.next()).setTranslate("gui.next"));
        this.consumer = consumer;
        registerEventChanged(x -> {
            if (x.control.is("grid") && this.consumer != null)
                this.consumer.accept(get());
        });
    }
    
    public LittleGrid get() {
        return comboBox.selected();
    }
    
    public void select(LittleGrid grid) {
        comboBox.select(grid);
    }
    
}
