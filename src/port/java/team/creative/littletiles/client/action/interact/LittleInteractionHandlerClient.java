package team.creative.littletiles.client.action.interact;

import team.creative.littletiles.fabric.client.ClientFrameEvents;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.level.LevelAwareHandler;
import team.creative.littletiles.common.action.LittleInteraction;
import team.creative.littletiles.common.packet.action.LittleInteractionPacket;

public class LittleInteractionHandlerClient implements LevelAwareHandler {
    
    private int index;
    private LittleInteraction interaction;
    
    public LittleInteractionHandlerClient() {
        ClientFrameEvents.START.register(this::clientTick);
    }
    
    public boolean start(boolean rightclick) {
        if (this.interaction == null) {
            this.interaction = new LittleInteraction(index++, rightclick);
            LittleTiles.NETWORK.sendToServer(new LittleInteractionPacket(interaction, true));
            return true;
        }
        return false;
    }
    
    public boolean can() {
        return interaction == null;
    }
    
    public void finish() {
        LittleTiles.NETWORK.sendToServer(new LittleInteractionPacket(interaction, false));
        interaction = null;
    }
    
    public void clientTick() {
        if (interaction != null)
            finish();
    }
    
    @Override
    public void unload() {
        interaction = null;
    }
}
