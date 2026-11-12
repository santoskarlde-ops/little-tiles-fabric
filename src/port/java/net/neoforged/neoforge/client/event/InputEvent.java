package net.neoforged.neoforge.client.event;

import net.neoforged.bus.api.Event;
import net.minecraft.world.InteractionHand;

public class InputEvent extends Event {
    public static class InteractionKeyMappingTriggered extends InputEvent {
        public boolean isPickBlock() { return false; }
        public boolean isCanceled() { return super.isCanceled(); }
        public void setCanceled(boolean value) { super.setCanceled(value); }
    }
    public static class MouseButton extends InputEvent {
        public static class Pre extends MouseButton {
            public int getButton() { return 0; }
            public int getAction() { return 0; }
        }
    }
}
