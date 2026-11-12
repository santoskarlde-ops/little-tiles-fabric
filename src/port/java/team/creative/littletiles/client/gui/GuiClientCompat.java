package team.creative.littletiles.client.gui;

import team.creative.creativecore.client.gui.GuiClientControl;
import team.creative.creativecore.client.gui.GuiControlRect;
import team.creative.creativecore.common.gui.GuiControl;

/** Client-only accessors for state that CreativeCore 26.x moved from GuiControl fields into GuiClientControl
 *  (the control's dist handler on the client). Only call from client code: on the server dist() is not a GuiClientControl. */
public class GuiClientCompat {
    
    public static GuiClientControl<?> dist(GuiControl control) {
        return (GuiClientControl<?>) control.dist();
    }
    
    public static GuiControlRect rect(GuiControl control) {
        return dist(control).rect;
    }
    
    public static boolean isEnabled(GuiControl control) {
        return dist(control).enabled;
    }
    
    public static boolean isVisible(GuiControl control) {
        return dist(control).visible;
    }
    
}
