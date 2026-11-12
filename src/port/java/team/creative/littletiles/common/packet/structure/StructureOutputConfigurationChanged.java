package team.creative.littletiles.common.packet.structure;

import java.text.ParseException;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.signal.output.SignalExternalOutputHandler;

public class StructureOutputConfigurationChanged extends CreativePacket {
    
    public CompoundTag nbt;
    public StructureLocation location;
    
    public StructureOutputConfigurationChanged(StructureLocation location, CompoundTag nbt) {
        this.location = location;
        this.nbt = nbt;
    }
    
    public StructureOutputConfigurationChanged() {}
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {
        try {
            LittleStructure structure = location.find(player.level());
            
            ListTag list = nbt.getListOrEmpty("internal");
            for (int i = 0; i < list.size(); i++)
                structure.getOutput(i).loadConfiguration(list.getCompound(i).orElse(new CompoundTag()));
            
            CompoundTag external = nbt.getCompound("external").orElse(new CompoundTag());
            Int2ObjectMap<SignalExternalOutputHandler> map = new Int2ObjectArrayMap<>();
            for (String key : external.keySet())
                try {
                    map.put(Integer.parseInt(key), new SignalExternalOutputHandler(structure, external.getCompound(key).orElse(new CompoundTag())));
                } catch (NumberFormatException | ParseException e) {}
            structure.setExternalOutputs(map);
            
            structure.updateStructure();
            structure.notifyChange();
            
        } catch (LittleActionException e) {}
    }
    
}
