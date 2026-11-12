package team.creative.littletiles.common.level.tick;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.level.handler.LevelHandlers;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;
import team.creative.littletiles.common.structure.signal.schedule.ISignalSchedulable;
import team.creative.littletiles.common.structure.signal.schedule.SignalScheduleTicket;

public class LittleTickers extends LevelHandlers<LittleTicker> {
    
    private static final List<SignalScheduleTicket> UNSORTED_TICKETS = new ArrayList<>();
    private LittleTicker client;
    
    public LittleTickers() {
        super();
        ServerTickEvents.START_LEVEL_TICK.register(this::levelTick);
        ServerTickEvents.END_SERVER_TICK.register(server -> serverTick());
    }
    
    @Override
    protected Function<Level, LittleTicker> createFactory() {
        return level -> {
            LittleTicker ticker = new LittleTicker(level);
            if (level.isClientSide())
                client = ticker;
            return ticker;
        };
    }
    
    @Override
    protected void unloadHandler(LittleTicker handler) {
        super.unloadHandler(handler);
        if (handler == client)
            client = null;
    }
    
    @Environment(EnvType.CLIENT)
    public void clientTick(Minecraft minecraft) {
        if (client != null && !minecraft.isPaused())
            client.tick();
    }
    
    public void levelTick(Level level) {
        LittleTicker ticker = getWithoutCreate(level);
        if (ticker != null)
            ticker.tick();
    }
    
    public void serverTick() {
        if (!UNSORTED_TICKETS.isEmpty())
            for (Iterator<SignalScheduleTicket> iterator = UNSORTED_TICKETS.iterator(); iterator.hasNext();) {
                SignalScheduleTicket ticket = iterator.next();
                Level level = ticket.getLevel();
                if (level != null) {
                    LittleTiles.TICKERS.get(level).schedule(ticket.delay, ticket);
                    iterator.remove();
                }
            }
    }
    
    public synchronized List<SignalScheduleTicket> findTickets(ISignalComponent component, SignalOutputHandler condition) {
        Level level = component.getStructureLevel();
        if (level != null && !level.isClientSide()) {
            List<SignalScheduleTicket> tickets = new ArrayList<>();
            for (LittleTickTicket ticket : LittleTiles.TICKERS.get(level))
                if (ticket.get() instanceof SignalScheduleTicket s && s.is(condition))
                    tickets.add(s);
            return tickets;
        }
        return Collections.EMPTY_LIST;
    }
    
    public void markSignalChanged(Level level, ISignalSchedulable schedulable) {
        if (level == null)
            return;
        get(level).markSignalChanged(schedulable);
    }
    
    public synchronized SignalScheduleTicket schedule(SignalOutputHandler handler, SignalState result, int delay) {
        SignalScheduleTicket ticket = new SignalScheduleTicket(handler, result, delay);
        Level level = handler.component.getStructureLevel();
        if (level == null)
            UNSORTED_TICKETS.add(ticket);
        else
            LittleTiles.TICKERS.get(level).schedule(delay, ticket);
        return ticket;
    }
    
    public synchronized void markUpdate(LittleStructure structure, boolean notifyNeighbours) {
        get(structure.getStructureLevel()).markUpdate(structure, notifyNeighbours);
    }
    
    public synchronized void queueNexTick(LittleStructure structure) {
        get(structure.getStructureLevel()).queueNextTick(structure);
    }
    
}
