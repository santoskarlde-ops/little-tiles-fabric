package team.creative.littletiles.server.level.little;

import java.util.concurrent.Executor;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;
import net.minecraft.world.level.TicketStorage;

public class LittleDistanceManager extends DistanceManager {
    
    protected LittleDistanceManager(TicketStorage tickets, Executor exe, Executor exe2) {
        super(tickets, exe, exe2);
    }
    
    @Override
    protected boolean isChunkToRemove(long pos) {
        return false;
    }
    
    @Override
    protected ChunkHolder getChunk(long pos) {
        return null;
    }
    
    @Override
    protected ChunkHolder updateChunkScheduling(long pos, int oldLevel, ChunkHolder holder, int newLevel) {
        return null;
    }
    
    @Override
    public boolean runAllUpdates(ChunkMap map) {
        return true;
    }
    
    @Override
    public void addPlayer(SectionPos pos, ServerPlayer player) {}
    
    @Override
    public void removePlayer(SectionPos pos, ServerPlayer player) {
        
    }
    
    @Override
    public boolean inEntityTickingRange(long pos) {
        return true;
    }
    
    @Override
    public boolean inBlockTickingRange(long pos) {
        return true;
    }
    
    @Override
    public void updateSimulationDistance(int distance) {}
    
    @Override
    public int getNaturalSpawnChunkCount() {
        return 0;
    }
    
    @Override
    public TriState hasPlayersNearby(long pos) {
        return TriState.FALSE;
    }
    
    @Override
    public String getDebugStatus() {
        return "";
    }
    
    @Override
    public boolean hasTickets() {
        return false;
    }
}
