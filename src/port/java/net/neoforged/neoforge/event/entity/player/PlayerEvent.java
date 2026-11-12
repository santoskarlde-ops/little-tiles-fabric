package net.neoforged.neoforge.event.entity.player;

import net.neoforged.bus.api.Event;

public class PlayerEvent extends Event {
    public static class BreakSpeed extends PlayerEvent {
        public float getNewSpeed() { return 0; }
        public void setNewSpeed(float value) {}
    }
    public static class PlayerLoggedInEvent extends PlayerEvent {}
    public static class PlayerLoggedOutEvent extends PlayerEvent {}
    public static class StartTracking extends PlayerEvent {}
    public static class StopTracking extends PlayerEvent {}
}
