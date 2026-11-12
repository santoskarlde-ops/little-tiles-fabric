package net.neoforged.neoforge.common.extensions;

import net.neoforged.neoforge.client.model.data.ModelDataManager;

public interface ILevelExtension {
    default ModelDataManager getModelDataManager() { return new ModelDataManager(); }
}
