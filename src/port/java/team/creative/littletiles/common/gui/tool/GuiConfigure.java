package team.creative.littletiles.common.gui.tool;

import com.mojang.serialization.DataResult;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.client.input.KeyEvent;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.client.LittleTilesClient;

public abstract class GuiConfigure extends GuiLayer {
    
    public ContainerSlotView tool;
    public final GuiSyncLocal<CompoundTag> SAVE_CONFIG = getSyncHolder().register("save_config", nbt -> {
        DataResult<DataComponentPatch> result = DataComponentPatch.CODEC.parse(NbtOps.INSTANCE, nbt);
        tool.get().applyComponents(result.getOrThrow());
        tool.changed();
    });
    
    public GuiConfigure(boolean client, String name, int width, int height, ContainerSlotView tool) {
        super(client, name, width, height);
        this.tool = tool;
    }
    
    public GuiConfigure(boolean client, String name, ContainerSlotView tool) {
        super(client, name);
        this.tool = tool;
    }
    
    protected boolean save(PatchedDataComponentMap data) {
        return saveConfiguration(data);
    }
    
    /** @param data
     *            all changes to save to
     * @return if true the data will be send to the server and changes will be applied */
    public abstract boolean saveConfiguration(PatchedDataComponentMap data);
    public boolean keyPressed(KeyEvent event) {
        if (LittleTilesClient.KEY_CONFIGURE.matches(event)) {
            closeTopLayer();
            return true;
        }
        return false;
    }
    public void closed() {
        if (isClient()) {
            PatchedDataComponentMap map = new PatchedDataComponentMap(DataComponentMap.EMPTY);
            if (save(map)) {
                DataResult<Tag> dataresult = DataComponentPatch.CODEC.encode(map.asPatch(), NbtOps.INSTANCE, new CompoundTag());
                SAVE_CONFIG.send((CompoundTag) dataresult.getOrThrow());
            }
        }
        super.closed();
    }
    
}
