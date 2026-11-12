package net.neoforged.neoforge.client.event;

import net.neoforged.bus.api.Event;

public class RegisterClientCommandsEvent extends Event {
    public com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> getDispatcher() { return null; }
}
