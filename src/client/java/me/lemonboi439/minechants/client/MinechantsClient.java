package me.lemonboi439.minechants.client;

import com.mojang.blaze3d.platform.InputConstants;
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
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class MinechantsClient implements ClientModInitializer {
    private static final KeyMapping.Category MINECHANTS_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Minechants.MOD_ID, "minechants"));

    private static final KeyMapping TOGGLE_SMELTING_KEY = minechants$register("toggle_smelting", InputConstants.KEY_J);
    private static final KeyMapping TOGGLE_MINER_KEY = minechants$register("toggle_miner", InputConstants.KEY_G);
    private static final KeyMapping TOGGLE_EXPLOSIVE_KEY = minechants$register("toggle_explosive", InputConstants.KEY_H);
    private static final KeyMapping TOGGLE_MAGNETIC_KEY = minechants$register("toggle_magnetic", InputConstants.KEY_K);

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ExplosiveConfigPayload.TYPE, (payload, context) ->
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

    // 26.1 still runs on GLFW: InputConstants.Type.KEYBOARD does not exist yet (it arrived with the
    // SDL migration), so the keyboard category is KEYSYM and the KEY_* constants are GLFW keycodes.
    private static KeyMapping minechants$register(String name, int defaultKey) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.minechants." + name,
                InputConstants.Type.KEYSYM,
                defaultKey,
                MINECHANTS_CATEGORY
        ));
    }

    private void minechants$handleToggle(Minecraft client, KeyMapping keyMapping, ToggleFeature feature) {
        while (keyMapping.consumeClick()) {
            if (client.player != null && ClientPlayNetworking.canSend(ToggleFeaturePayload.TYPE)) {
                ClientPlayNetworking.send(new ToggleFeaturePayload(feature));
            }
        }
    }
}
