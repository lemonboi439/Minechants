package me.lemonboi439.minechants.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            if (!FabricLoader.getInstance().isModLoaded("cloth-config")) {
                return parent;
            }

            try {
                return MinechantsConfigScreen.create(parent);
            } catch (LinkageError exception) {
                return parent;
            }
        };
    }
}
