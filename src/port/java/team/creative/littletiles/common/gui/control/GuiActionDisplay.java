package team.creative.littletiles.common.gui.control;

import team.creative.creativecore.common.gui.IGuiParent;
import net.fabricmc.loader.api.FabricLoader;
import team.creative.littletiles.client.gui.GuiClientActionDisplay;
import java.util.ArrayList;
import java.util.List;


import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import team.creative.creativecore.client.render.text.CompiledText;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.style.ControlFormatting;

public class GuiActionDisplay extends GuiControl {
    static {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
            GuiClientActionDisplay.register();
    }
    
    public List<ActionMessage> messages = new ArrayList<>();
    
    private int maxActions;
    private int displayTime;
    private int fadeTime;
    private int totalTime;
    
    public GuiActionDisplay(IGuiParent parent, String name) {
        super(parent, name);
        setTimer(5, 1);
        setMessageCount(6);
    }
    public ControlFormatting getControlFormatting() {
        return ControlFormatting.TRANSPARENT;
    }
    public void flowX(int width, int preferred) {
        for (int i = 0; i < messages.size(); i++)
            messages.get(i).setDimension(width, Integer.MAX_VALUE);
    }
    public void flowY(int width, int height, int preferred) {}
    protected int minWidth(int availableWidth) {
        return 10;
    }
    public int preferredWidth(int availableWidth) {
        int max = 0;
        for (int i = 0; i < messages.size(); i++)
            max = Math.max(max, messages.get(i).getTotalWidth());
        return max;
    }
    protected int minHeight(int width, int availableHeight) {
        return Minecraft.getInstance().font.lineHeight;
    }
    public int preferredHeight(int width, int availableHeight) {
        int height = 0;
        for (int i = 0; i < messages.size(); i++)
            height += messages.get(i).getTotalHeight();
        return height;
    }
    
    public GuiActionDisplay setMessageCount(int count) {
        this.maxActions = count;
        List<ActionMessage> newMessages = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            if (i >= count)
                break;
            newMessages.add(messages.get(i));
        }
        this.messages = newMessages;
        return this;
    }
    
    public GuiActionDisplay setTimer(int displaySeconds, int fadeSeconds) {
        this.displayTime = displaySeconds * 1000;
        this.fadeTime = fadeSeconds * 1000;
        this.totalTime = displayTime + fadeTime;
        return this;
    }
    
    public void clearMessages() {
        messages.clear();
        reflow();
    }
    
    public void addMessage(List<Component> message) {
        messages.add(0, new ActionMessage(message));
        if (messages.size() > maxActions)
            for (int i = maxActions; i < messages.size(); i++)
                messages.remove(i);
        reflow();
    }
    
    public void removeMessage(int index) {
        messages.remove(index);
        reflow();
    }
    public int displayTime() { return displayTime; }
    public int fadeTime() { return fadeTime; }
    public int totalTime() { return totalTime; }
    public void closed() {}
    public void init() {}
    public void tick() {}
    
    public static class ActionMessage extends CompiledText {
        
        public final long timestamp;
        
        public ActionMessage(List<Component> message) {
            super(Integer.MAX_VALUE, Integer.MAX_VALUE);
            setText(message);
            this.timestamp = System.currentTimeMillis();
        }
        
    }
}
