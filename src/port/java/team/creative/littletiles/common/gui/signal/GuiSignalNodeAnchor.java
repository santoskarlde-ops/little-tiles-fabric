package team.creative.littletiles.common.gui.signal;

import team.creative.littletiles.client.gui.GuiClientCompat;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNode;

public abstract class GuiSignalNodeAnchor<T extends GuiSignalNode> {
    
    public static final GuiSignalNodeAnchor LEFT = new GuiSignalNodeAnchor() {
        
        @Override
        public float x(GuiSignalNode node) {
            return GuiClientCompat.rect(node.button).getX();
        }
        
        @Override
        public float y(GuiSignalNode node) {
            return GuiClientCompat.rect(node.button).centerY();
        }
        
    };
    public static final GuiSignalNodeAnchor TOP = new GuiSignalNodeAnchor() {
        
        @Override
        public float x(GuiSignalNode node) {
            return GuiClientCompat.rect(node.button).centerX();
        }
        
        @Override
        public float y(GuiSignalNode node) {
            return GuiClientCompat.rect(node.button).getY();
        }
        
    };
    public static final GuiSignalNodeAnchor RIGHT = new GuiSignalNodeAnchor() {
        
        @Override
        public float x(GuiSignalNode node) {
            return GuiClientCompat.rect(node.button).getRight();
        }
        
        @Override
        public float y(GuiSignalNode node) {
            return GuiClientCompat.rect(node.button).centerY();
        }
        
    };
    public static final GuiSignalNodeAnchor BOTTOM = new GuiSignalNodeAnchor() {
        
        @Override
        public float x(GuiSignalNode node) {
            return GuiClientCompat.rect(node.button).centerX();
        }
        
        @Override
        public float y(GuiSignalNode node) {
            return GuiClientCompat.rect(node.button).getBottom();
        }
        
    };
    
    public abstract float x(T node);
    
    public abstract float y(T node);
    
}