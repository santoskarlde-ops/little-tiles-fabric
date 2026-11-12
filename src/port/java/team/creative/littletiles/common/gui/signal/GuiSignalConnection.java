package team.creative.littletiles.common.gui.signal;

import team.creative.littletiles.client.gui.GuiClientCompat;
import javax.annotation.Nullable;

import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNode;

public record GuiSignalConnection(GuiSignalNode from, GuiSignalNodeAnchor fromAnchor, GuiSignalNode to, GuiSignalNodeAnchor toAnchor) {
    
    public static GuiSignalConnection connect(GuiSignalNode from, @Nullable GuiSignalNodeAnchor fromAnchor, GuiSignalNode to, @Nullable GuiSignalNodeAnchor toAnchor) {
        var connection = new GuiSignalConnection(from, fromAnchor != null ? fromAnchor : from.connectionAnchor(true, to), to, toAnchor != null ? toAnchor : to.connectionAnchor(
            false, from));
        from.connect(connection);
        to.connect(connection);
        return connection;
    }
    
    public static GuiSignalConnection connect(GuiSignalNode from, GuiSignalNode to) {
        var connection = new GuiSignalConnection(from, from.connectionAnchor(true, to), to, to.connectionAnchor(false, from));
        from.connect(connection);
        to.connect(connection);
        return connection;
    }
    
    public GuiSignalConnection(GuiSignalNode from, GuiSignalNode to) {
        this(from, GuiSignalNodeAnchor.RIGHT, to, GuiSignalNodeAnchor.LEFT);
    }
    
    public float fromX() {
        return fromAnchor.x(from) + GuiClientCompat.rect(from).getX();
    }
    
    public float fromY() {
        return fromAnchor.y(from) + GuiClientCompat.rect(from).getY();
    }
    
    public float toX() {
        return toAnchor.x(to) + GuiClientCompat.rect(to).getX();
    }
    
    public float toY() {
        return toAnchor.y(to) + GuiClientCompat.rect(to).getY();
    }
    
    public void disconnect(GuiSignalController controller) {
        from.disconnect(this);
        to.disconnect(this);
        
        controller.raiseEvent(new GuiControlChangedEvent(controller));
    }
    
}
