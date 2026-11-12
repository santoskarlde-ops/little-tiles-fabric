package net.neoforged.neoforge.client.event;

import net.neoforged.bus.api.Event;

public class ViewportEvent extends Event {
    public static class ComputeFov extends ViewportEvent {
        public double getFOV() { return 0; }
        public void setFOV(double value) {}
    }
}
