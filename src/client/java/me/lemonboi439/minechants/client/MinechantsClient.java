package me.lemonboi439.minechants.client;

import me.lemonboi439.minechants.Minechants;
import me.lemonboi439.minechants.util.ToggleFeature;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class MinechantsClient implements ClientModInitializer {
    private static final String MINECHANTS_CATEGORY = "key.categories.minechants";
    private static final KeyBinding TOGGLE_SMELTING_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.minechants.toggle_smelting",
            GLFW.GLFW_KEY_J,
            MINECHANTS_CATEGORY
    ));
    private static final KeyBinding TOGGLE_MINER_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.minechants.toggle_miner",
            GLFW.GLFW_KEY_G,
            MINECHANTS_CATEGORY
    ));
    private static final KeyBinding TOGGLE_EXPLOSIVE_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.minechants.toggle_explosive",
            GLFW.GLFW_KEY_H,
            MINECHANTS_CATEGORY
    ));
    private static final KeyBinding TOGGLE_MAGNETIC_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.minechants.toggle_magnetic",
            GLFW.GLFW_KEY_K,
            MINECHANTS_CATEGORY
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
            if (client.player != null && ClientPlayNetworking.canSend(Minechants.TOGGLE_FEATURE_ID)) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeInt(feature.ordinal());
                ClientPlayNetworking.send(Minechants.TOGGLE_FEATURE_ID, buf);
            }
        }
    }
}
