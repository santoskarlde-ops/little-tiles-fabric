package net.neoforged.fml.event.lifecycle;

import net.neoforged.bus.api.Event;

public class FMLCommonSetupEvent extends Event {
    public void enqueueWork(Runnable work) { if (work != null) work.run(); }
}
