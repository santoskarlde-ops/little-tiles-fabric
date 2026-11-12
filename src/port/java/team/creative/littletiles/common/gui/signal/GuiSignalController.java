package team.creative.littletiles.common.gui.signal;

import team.creative.creativecore.common.gui.IGuiParent;
import team.creative.littletiles.client.gui.GuiClientCompat;
import team.creative.littletiles.client.gui.GuiClientSignalController;
import net.fabricmc.loader.api.FabricLoader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;



import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.client.gui.GuiControlRect;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.event.GuiEvent;
import team.creative.creativecore.common.util.math.vec.SmoothValue;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignal;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNode;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNodeComparator;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNodeGate;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNodeInput;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNodeNotOperator;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNodeOperator;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNodeOutput;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNodeVirtualInput;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNodeVirtualNumberInput;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.SignalContext;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputConditionNot;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputConditionNotBitwise;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputVirtualNumber;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputVirtualVariable;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalPosition;
import team.creative.littletiles.common.structure.signal.input.SignalInputVariable;
import team.creative.littletiles.common.structure.signal.logic.SignalInputConditionGate;
import team.creative.littletiles.common.structure.signal.logic.SignalLogicComparator;
import team.creative.littletiles.common.structure.signal.logic.SignalLogicComparator.SignalInputConditionComparator;
import team.creative.littletiles.common.structure.signal.logic.SignalLogicOperator;
import team.creative.littletiles.common.structure.signal.logic.SignalLogicOperator.SignalInputConditionOperatorStackable;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget;

public class GuiSignalController extends GuiParent implements SignalContext {

    static {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
            GuiClientSignalController.register();
    }
    
    protected int cellWidth = 60;
    protected int cellHeight = 40;
    public SmoothValue scrolledX = new SmoothValue(200);
    public SmoothValue scrolledY = new SmoothValue(200);
    public SmoothValue zoom = new SmoothValue(200, 1);
    public double startScrollX;
    public double startScrollY;
    public int dragX;
    public int dragY;
    public boolean scrolling;
    
    private List<List<GuiControl>> grid = new ArrayList<>();
    
    public final List<GuiSignalComponent> inputs;
    private final Map<String, ISignalComponent> testComponents = new Object2ObjectArrayMap<>();
    private GuiSignalNodeOutput output;
    
    private GuiSignalNode dragged;
    private boolean startedDragging = false;
    private GuiSignalNode selected;
    private Function<Boolean, GuiSignalNodeAnchor> selectedAnchor;
    
    
    private boolean testing;
    private boolean displayNumbers;
    
    public GuiSignalController(IGuiParent parent, String name, GuiSignalComponent output, List<GuiSignalComponent> inputs) {
        super(parent, name);
        this.inputs = inputs;
        setOutput(4, output, null);
    }
    
    public GuiSignalComponent getComponent(String component) {
        for (GuiSignalComponent c : inputs)
            if (c.name().equals(component))
                return c;
        return null;
    }
    
    public SignalState getInputState(String component, int bandwidth) {
        var c = testComponents.get(component);
        if (c != null)
            try {
                return c.getState();
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        
        return SignalState.create(bandwidth);
    }
    
    public void setInputState(String component, SignalState state) {
        var c = testComponents.get(component);
        if (c != null)
            try {
                c.overwriteState(state);
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        else {
            var guiComponent = getComponent(component);
            if (guiComponent != null)
                testComponents.put(component, new TestingSignalComponent(guiComponent, state));
        }
        
        for (List<GuiControl> row : grid)
            for (GuiControl control : row)
                if (control instanceof GuiSignalNode n)
                    n.testInputChanged();
    }
    
    public boolean testingDisplayNumber() {
        return displayNumbers;
    }
    
    public void setTestingDisplayNumber(boolean displayNumbers) {
        this.displayNumbers = displayNumbers;
        test();
    }
    
    public void setTesting(boolean enabled) {
        this.testing = enabled;
        test();
    }
    
    public void test() {
        for (List<GuiControl> row : grid)
            for (GuiControl control : row)
                if (control instanceof GuiSignalNode c)
                    c.resetTest();
                
        if (testing)
            try {
                output.generateCondition(new ArrayList<>(), this);
            } catch (GeneratePatternException e) {}
        
        reflow();
    }
    
    public Component testSignalOutput(SignalState state, int bandwidth) {
        if (bandwidth < 0)
            bandwidth = SignalState.getRequiredBandwidth(state.number());
        if (displayNumbers)
            return Component.literal("" + (Math.min(state.number(), SignalState.maxNumber(bandwidth))));
        return Component.literal(state.print(Math.max(1, bandwidth)));
    }
    public void raiseEvent(GuiEvent event) {
        if (event instanceof GuiControlChangedEvent c && c.control == this)
            testingChanged();
        super.raiseEvent(event);
    }
    
    public void testingChanged() {
        test();
    }
    private void flowCell(GuiControlRect child, int x, int y) {
        if (getParent() == null)
            return;
        
        int widthNode = Math.min(cellWidth, child.getPreferredWidth(cellWidth));
        child.setWidth(widthNode, cellWidth);
        child.setX(cellWidth * x + cellWidth / 2 - child.getWidth() / 2);
        child.flowX();
        int heightNode = Math.min(cellHeight, child.getPreferredHeight(cellHeight));
        child.setHeight(heightNode, cellHeight);
        child.setY(cellHeight * y + cellHeight / 2 - child.getHeight() / 2);
        child.flowY();
    }
    public void flowX(int width, int preferred) {
        for (int x = 0; x < grid.size(); x++) {
            List<GuiControl> rows = grid.get(x);
            for (int y = 0; y < rows.size(); y++) {
                GuiControl child = rows.get(y);
                if (child == null)
                    continue;
                int widthNode = Math.min(cellWidth, GuiClientCompat.rect(child).getPreferredWidth(cellWidth));
                GuiClientCompat.rect(child).setWidth(widthNode, cellWidth);
                GuiClientCompat.rect(child).setX(cellWidth * x + cellWidth / 2 - GuiClientCompat.rect(child).getWidth() / 2);
                GuiClientCompat.rect(child).flowX();
            }
        }
    }
    public void flowY(int width, int height, int preferred) {
        for (int x = 0; x < grid.size(); x++) {
            List<GuiControl> rows = grid.get(x);
            for (int y = 0; y < rows.size(); y++) {
                GuiControl child = rows.get(y);
                if (child == null)
                    continue;
                int heightNode = Math.min(cellHeight, GuiClientCompat.rect(child).getPreferredHeight(cellHeight));
                GuiClientCompat.rect(child).setHeight(heightNode, cellHeight);
                GuiClientCompat.rect(child).setY(cellHeight * y + cellHeight / 2 - GuiClientCompat.rect(child).getHeight() / 2);
                GuiClientCompat.rect(child).flowY();
            }
        }
    }
    public boolean mouseClicked(double x, double y, int button) {
        startedDragging = false;
        if (button == 2) {
            zoom.set(1);
            scrolledX.set(0);
            scrolledY.set(0);
            return true;
        }
        select(null, null);
        scrolling = true;
        dragX = (int) x;
        dragY = (int) y;
        startScrollX = scrolledX.current();
        startScrollY = scrolledY.current();
        return true;
    }
    public boolean mouseScrolled(double x, double y, double delta) {
        zoom.set(Mth.clamp(zoom.aimed() + delta * 0.2, 0.1, 2));
        return true;
    }
    public void mouseDragged(double x, double y, int button, double dragX, double dragY, double time) {
        if (time > 0.2 && dragged != null) {
            set(dragged, (int) Math.max(0, (x / zoom.current() + scrolledX.current()) / cellWidth), (int) Math.max(0, (y / zoom.current() + scrolledY.current()) / cellHeight));
            startedDragging = true;
        }
    }
    public void mouseMoved(double x, double y) {
        if (scrolling) {
            scrolledX.set(Mth.clamp(dragX - x + startScrollX, -40, sizeX() * cellWidth));
            scrolledY.set(Mth.clamp(dragY - y + startScrollY, -40, sizeY() * cellHeight));
        }
    }
    public void mouseReleased(double x, double y, int button) {
        scrolling = false;
        if (dragged != null && !startedDragging)
            select(dragged, selectedAnchor);
        startedDragging = false;
        dragged = null;
    }
    
    public void setOutput(int cell, GuiSignalComponent output, @Nullable SignalPosition position) {
        if (this.output != null)
            this.output.remove();
        this.output = new GuiSignalNodeOutput(this, output, position);
        if (position == null)
            setToFreeCell(cell, this.output);
        else
            set(this.output, position.x(), position.y());
        raiseEvent(new GuiControlChangedEvent(this));
    }
    
    public void setCondition(@Nullable SignalPosition outputPosition, SignalInputCondition condition, GuiDialogSignal signal) {
        reset();
        try {
            List<List<GuiSignalNode>> parsed = new ArrayList<>();
            GuiSignalNode node = fill(condition, signal, parsed, 0);
            for (int i = parsed.size() - 1; i >= 0; i--) {
                List<GuiSignalNode> rows = parsed.get(i);
                for (int j = rows.size() - 1; j >= 0; j--) {
                    var signalNode = rows.get(j);
                    if (signalNode.x() == -1)
                        set(signalNode, parsed.size() - i - 1, rows.size() - j - 1);
                    else
                        set(signalNode, signalNode.x(), signalNode.y());
                }
            }
            setOutput(parsed.size(), output.component, outputPosition);
            
            GuiSignalConnection connection = new GuiSignalConnection(node, output);
            node.connect(connection);
            output.connect(connection);
            return;
        } catch (ParseException e) {
            LittleTiles.LOGGER.catching(e);
            reset();
        }
        setOutput(4, output.component, outputPosition);
    }
    
    private GuiSignalNode fill(SignalInputCondition condition, GuiDialogSignal signal, List<List<GuiSignalNode>> parsed, int level) throws ParseException {
        GuiSignalNode node;
        if (condition instanceof SignalInputConditionNot || condition instanceof SignalInputConditionNotBitwise) {
            boolean bitwise = condition instanceof SignalInputConditionNotBitwise;
            node = new GuiSignalNodeNotOperator(this, bitwise, condition.position);
            GuiSignalNode child = fill(bitwise ? ((SignalInputConditionNotBitwise) condition).condition : ((SignalInputConditionNot) condition).condition, signal, parsed,
                level + 1);
            GuiSignalConnection connection = new GuiSignalConnection(child, node);
            node.connect(connection);
            child.connect(connection);
            
        } else if (condition instanceof SignalInputConditionOperatorStackable stack) {
            node = new GuiSignalNodeOperator(this, stack.operator(), stack.position);
            for (SignalInputCondition subCondition : stack.conditions) {
                GuiSignalNode child = fill(subCondition, signal, parsed, level + 1);
                GuiSignalConnection connection = new GuiSignalConnection(child, node);
                node.connect(connection);
                child.connect(connection);
            }
        } else if (condition instanceof SignalInputVariable in)
            node = new GuiSignalNodeInput(this, in, signal.getInput(in.target), in.position);
        else if (condition instanceof SignalInputVirtualVariable variable)
            node = new GuiSignalNodeVirtualInput(this, variable, variable.position);
        else if (condition instanceof SignalInputVirtualNumber number)
            node = new GuiSignalNodeVirtualNumberInput(this, number, number.position);
        else if (condition instanceof SignalInputConditionComparator com) {
            node = new GuiSignalNodeComparator(this, com.comparator, com.position);
            
            GuiSignalNode child = fill(com.conditions[0], signal, parsed, level + 1);
            GuiSignalConnection.connect(child, null, node, GuiSignalNodeComparator.UPPER_BUTTON);
            
            child = fill(com.conditions[1], signal, parsed, level + 1);
            GuiSignalConnection.connect(child, null, node, GuiSignalNodeComparator.LOWER_BUTTON);
        } else if (condition instanceof SignalInputConditionGate gate) {
            node = new GuiSignalNodeGate(this, gate.invert, gate.position);
            
            GuiSignalNode child = fill(gate.gate, signal, parsed, level + 1);
            GuiSignalConnection.connect(child, null, node, GuiSignalNodeGate.UPPER_BUTTON);
            
            child = fill(gate.through, signal, parsed, level + 1);
            GuiSignalConnection.connect(child, null, node, GuiSignalNodeAnchor.LEFT);
        } else
            throw new ParseException("Invalid condition type", 0);
        while (parsed.size() <= level)
            parsed.add(new ArrayList<>());
        parsed.get(level).add(node);
        return node;
    }
    
    public GuiSignalNodeVirtualInput addVirtualInput() {
        return setToFreeCell(0, new GuiSignalNodeVirtualInput(this, null));
    }
    
    public GuiSignalNodeVirtualNumberInput addVirtualNumberInput() {
        return setToFreeCell(0, new GuiSignalNodeVirtualNumberInput(this, null));
    }
    
    public GuiSignalNodeInput addInput(GuiSignalComponent input) {
        return setToFreeCell(0, new GuiSignalNodeInput(this, input, null));
    }
    
    public GuiSignalNodeNotOperator addNotOperator(boolean bitwise) {
        return setToFreeCell(1, new GuiSignalNodeNotOperator(this, bitwise, null));
    }
    
    public GuiSignalNodeOperator addOperator(SignalLogicOperator operator) {
        return setToFreeCell(1, new GuiSignalNodeOperator(this, operator, null));
    }
    
    public GuiSignalNodeComparator addComparator(SignalLogicComparator comparator) {
        return setToFreeCell(1, new GuiSignalNodeComparator(this, comparator, null));
    }
    
    public GuiSignalNodeGate addGate(boolean invert) {
        return setToFreeCell(1, new GuiSignalNodeGate(this, invert, null));
    }
    
    public GuiSignalNode selected() {
        return selected;
    }
    
    public void drag(GuiSignalNode node, @Nullable Function<Boolean, GuiSignalNodeAnchor> anchor) {
        dragged = node;
        selectedAnchor = anchor;
    }
    
    public void select(GuiSignalNode node, @Nullable Function<Boolean, GuiSignalNodeAnchor> anchor) {
        if (selected != null)
            selected.setDefaultColor(ColorUtils.WHITE);
        selected = node;
        selectedAnchor = anchor;
        if (selected != null)
            selected.setDefaultColor(ColorUtils.YELLOW);
    }
    
    public void selectOrConnect(GuiSignalNode node, @Nullable Function<Boolean, GuiSignalNodeAnchor> anchor, boolean shouldDrag) {
        if (selected() != null)
            tryToggleConnectionToSelected(node, anchor);
        else if (shouldDrag)
            drag(node, anchor);
        else
            select(node, anchor);
    }
    
    public void tryToggleConnectionToSelected(GuiSignalNode node, @Nullable Function<Boolean, GuiSignalNodeAnchor> anchor) {
        if (selected != node) {
            GuiSignalConnection connection = selected.getConnectionTo(node);
            if (connection != null) {
                connection.disconnect(this);
                raiseEvent(new GuiControlChangedEvent(this));
            } else {
                var fromAnchor = selectedAnchor != null ? selectedAnchor.apply(false) : null;
                var toAnchor = anchor != null ? anchor.apply(true) : null;
                if (selected.canConnectTo(node, fromAnchor) && node.canConnectFrom(selected, toAnchor)) {
                    GuiSignalConnection.connect(selected, fromAnchor, node, toAnchor);
                    raiseEvent(new GuiControlChangedEvent(this));
                }
            }
        }
        select(null, null);
    }
    
    public SignalInputCondition generatePattern() throws GeneratePatternException {
        return output.generateCondition(new ArrayList<>(), null);
    }
    
    public SignalPosition outputPosition() {
        return output.position();
    }
    
    public <T extends GuiSignalNode> T setToFreeCell(int startCol, T node) {
        for (int i = 0; i < 50 - startCol; i++) {
            if (grid.size() <= i)
                grid.add(new ArrayList<>());
            if (i < startCol)
                continue;
            List<GuiControl> rows = grid.get(i);
            for (int j = 0; j < 50; j++) {
                if (rows.size() <= j)
                    rows.add(null);
                if (rows.get(j) == null) {
                    set(node, i, j);
                    return node;
                }
            }
        }
        throw new RuntimeException("There are no empty cells left");
    }
    
    public int sizeX() {
        return grid.size();
    }
    
    public int sizeY() {
        int rows = 0;
        for (int i = 0; i < grid.size(); i++)
            rows = Math.max(rows, grid.get(i).size());
        return rows;
    }
    
    public void removeNode(GuiSignalNode node) {
        if (selected != null)
            selected.setDefaultColor(ColorUtils.WHITE);
        selected = null;
        dragged = null;
        remove(node.x(), node.y());
        node.remove();
        raiseEvent(new GuiControlChangedEvent(this));
    }
    
    public GuiControl remove(int col, int row) {
        if (col < grid.size()) {
            List<GuiControl> rows = grid.get(col);
            if (row < rows.size()) {
                GuiControl removed = rows.set(row, null);
                if (removed != null)
                    super.remove(removed);
                return removed;
            }
        }
        return null;
    }
    
    public void set(GuiSignalNode node, int col, int row) {
        boolean added = node.added();
        GuiControl.setParent(node, this);
        if (added)
            if (node.x() == col && node.y() == row)
                return;
        while (grid.size() <= col)
            grid.add(new ArrayList<>());
        List<GuiControl> rows = grid.get(col);
        while (rows.size() <= row)
            rows.add(null);
        if (rows.get(row) == null) {
            GuiControl child = null;
            if (added)
                child = remove(node.x(), node.y());
            if (child == null)
                child = node;
            rows.set(row, child);
            super.add(child);
            node.updatePosition(col, row);
            if (child.dist() != null)
                flowCell(GuiClientCompat.rect(child), col, row);
        }
    }
    
    public void reset() {
        while (size() > 0)
            remove(0);
        grid.clear();
        output.remove();
        dragged = null;
        selected = null;;
    }
    public SignalContext getNestedSignalContext(int child) throws CorruptedConnectionException, NotYetConnectedException {
        return new PrefixSignalContext("c" + child + ".");
    }
    public boolean hasSignalContextParent() {
        return true;
    }
    public SignalContext getParentSignalContext() throws CorruptedConnectionException, NotYetConnectedException {
        return new PrefixSignalContext("p.");
    }
    public ISignalComponent getInput(int id, boolean external) {
        return testComponents.get(SignalTarget.name(external, true, id));
    }
    public ISignalComponent getOutput(int id, boolean external) {
        return testComponents.get(SignalTarget.name(external, false, id));
    }
    
    public static class TestingSignalComponent implements ISignalComponent {
        
        public final GuiSignalComponent component;
        private SignalState state;
        
        public TestingSignalComponent(GuiSignalComponent component, SignalState state) {
            this.component = component;
            this.state = state;
        }
        public int getBandwidth() throws CorruptedConnectionException, NotYetConnectedException {
            return component.bandwidth();
        }
        public void changed() throws CorruptedConnectionException, NotYetConnectedException {}
        public SignalState getState() throws CorruptedConnectionException, NotYetConnectedException {
            return state;
        }
        public void overwriteState(SignalState state) throws CorruptedConnectionException, NotYetConnectedException {
            this.state = state;
        }
        public SignalComponentType getComponentType() {
            return component.input() ? SignalComponentType.INPUT : SignalComponentType.OUTPUT;
        }
        public LittleStructure getStructure() {
            throw new UnsupportedOperationException();
        }
        public Level getStructureLevel() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    private class PrefixSignalContext implements SignalContext {
        
        private String prefix;
        
        public PrefixSignalContext(String prefix) {
            this.prefix = prefix;
        }
        public SignalContext getNestedSignalContext(int child) throws CorruptedConnectionException, NotYetConnectedException {
            prefix += "c" + child + ".";
            return this;
        }
        public boolean hasSignalContextParent() {
            return true;
        }
        public SignalContext getParentSignalContext() throws CorruptedConnectionException, NotYetConnectedException {
            prefix += "p.";
            return this;
        }
        public ISignalComponent getInput(int id, boolean external) {
            return testComponents.get(prefix + SignalTarget.name(external, true, id));
        }
        public ISignalComponent getOutput(int id, boolean external) {
            return testComponents.get(prefix + SignalTarget.name(external, false, id));
        }
        
    }
    
}
