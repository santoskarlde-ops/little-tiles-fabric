package net.neoforged.bus.api;

/** Ordering values retained for source compatibility; Fabric dispatch is separate. */
public enum EventPriority {
    HIGHEST,
    HIGH,
    NORMAL,
    LOW,
    LOWEST
}