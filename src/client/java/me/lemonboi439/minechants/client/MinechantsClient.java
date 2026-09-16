package me.lemonboi439.minechants.client;

import me.lemonboi439.minechants.network.ToggleFeaturePayload;
import me.lemonboi439.minechants.util.ToggleFeature;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class MinechantsClient implements ClientModInitializer {
    private static final KeyBinding TOGGLE_SMELTING_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.minechants.toggle_smelting",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            "key.categories.minechants"
    ));
    private static final KeyBinding TOGGLE_MINER_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.minechants.toggle_miner",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.minechants"
    ));
    private static final KeyBinding TOGGLE_EXPLOSIVE_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.minechants.toggle_explosive",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.minechants"
    ));
    private static final KeyBinding TOGGLE_MAGNETIC_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.minechants.toggle_magnetic",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.minechants"
    ));

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            this.minechants$handleToggle(client, TOGGLE_SMELTING_KEY, ToggleFeature.SMELTING);
            this.minechants$handleToggle(client, TOGGLE_MINER_KEY, ToggleFeature.THREE_BY_THREE_MINER);
            this.minechants$handleToggle(client, TOGGLE_EXPLOSIVE_KEY, ToggleFeature.EXPLOSIVE);
            this.minechants$handleToggle(client, TOGGLE_MAGNETIC_KEY, ToggleFeature.MAGNETIC);
        });
    }

    private void minechants$handleToggle(net.minecraft.client.MinecraftClient client, KeyBinding keyBinding, ToggleFeature feature) {
        while (keyBinding.wasPressed()) {
            if (client.player != null && ClientPlayNetworking.canSend(ToggleFeaturePayload.ID)) {
                ClientPlayNetworking.send(new ToggleFeaturePayload(feature));
            }
        }
    }
}
