package net.neoforged.neoforge.event.level;
import net.neoforged.bus.api.Event;
public class ChunkEvent extends Event { public static class Load extends ChunkEvent {} public static class Unload extends ChunkEvent {} }
