package team.creative.littletiles.common.gui;

import team.creative.creativecore.common.gui.control.simple.GuiCheckBox;

/** Helpers for CreativeCore 26.x GUI API differences. */
public class GuiCompat {
    
    /** Replacement for the old public GuiCheckBox.value field write: sets the value without notifying listeners
     *  (GuiCheckBox.set(boolean) notifies). Does nothing on the server side, where the control has no dist handler. */
    public static void setSilent(GuiCheckBox box, boolean value) {
        var dist = box.dist();
        if (dist != null)
            dist.set(value, false);
    }
    
}
