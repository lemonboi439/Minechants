package me.lemonboi439.minechants.client;

import me.lemonboi439.minechants.Minechants;
import me.lemonboi439.minechants.network.ExplosiveConfigPayload;
import me.lemonboi439.minechants.network.ToggleFeaturePayload;
import me.lemonboi439.minechants.util.ServerConfigState;
import me.lemonboi439.minechants.util.ToggleFeature;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class MinechantsClient implements ClientModInitializer {
    private static final KeyMapping.Category MINECHANTS_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Minechants.MOD_ID, "minechants"));
    private static final KeyMapping TOGGLE_SMELTING_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.minechants.toggle_smelting",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            MINECHANTS_CATEGORY
    ));
    private static final KeyMapping TOGGLE_MINER_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.minechants.toggle_miner",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            MINECHANTS_CATEGORY
    ));
    private static final KeyMapping TOGGLE_EXPLOSIVE_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.minechants.toggle_explosive",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            MINECHANTS_CATEGORY
    ));
    private static final KeyMapping TOGGLE_MAGNETIC_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.minechants.toggle_magnetic",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            MINECHANTS_CATEGORY
    ));

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ExplosiveConfigPayload.ID, (payload, context) ->
                ServerConfigState.setDisableExplosiveOnServer(payload.disableExplosiveOnServer())
        );
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ServerConfigState.reset());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            this.minechants$handleToggle(client, TOGGLE_SMELTING_KEY, ToggleFeature.SMELTING);
            this.minechants$handleToggle(client, TOGGLE_MINER_KEY, ToggleFeature.THREE_BY_THREE_MINER);
            this.minechants$handleToggle(client, TOGGLE_EXPLOSIVE_KEY, ToggleFeature.EXPLOSIVE);
            this.minechants$handleToggle(client, TOGGLE_MAGNETIC_KEY, ToggleFeature.MAGNETIC);
        });
    }

    private void minechants$handleToggle(net.minecraft.client.Minecraft client, KeyMapping keyBinding, ToggleFeature feature) {
        while (keyBinding.consumeClick()) {
            if (client.player != null && ClientPlayNetworking.canSend(ToggleFeaturePayload.ID)) {
                ClientPlayNetworking.send(new ToggleFeaturePayload(feature));
            }
        }
    }
}
