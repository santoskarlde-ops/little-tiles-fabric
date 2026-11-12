package net.neoforged.neoforge.client.event;

import net.neoforged.bus.api.Event;

public class RenderBlockScreenEffectEvent extends Event {
    public enum OverlayType { BLOCK, FIRE, WATER }
    public OverlayType getOverlayType() { return OverlayType.BLOCK; }
}
