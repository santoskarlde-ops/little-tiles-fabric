package team.creative.littletiles.common.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import team.creative.creativecore.common.network.CreativeByteBuf;

/**
 * CreativeCore's static NetworkFieldTypes helpers need a CreativeByteBuf, but NetworkFieldTypeClass.writeContent/readContent hand us a plain
 * RegistryFriendlyByteBuf (and the StreamCodec path never has a CreativeByteBuf at all). A cast would fail there, so wrap instead.
 * The wrapper shares the same underlying bytes. The connection is only needed by CreativeCore's packet-in-packet types, which LittleTiles does not nest.
 */
public final class LittleNetBuf {
    
    private LittleNetBuf() {}
    
    public static CreativeByteBuf of(RegistryFriendlyByteBuf buffer) {
        if (buffer instanceof CreativeByteBuf creative)
            return creative;
        return new CreativeByteBuf(buffer, buffer.registryAccess(), null);
    }
    
}
