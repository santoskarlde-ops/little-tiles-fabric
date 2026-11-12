package net.neoforged.fml.event.lifecycle;

import net.neoforged.bus.api.Event;

public class FMLClientSetupEvent extends Event {
    public void enqueueWork(Runnable work) { if (work != null) work.run(); }
}
