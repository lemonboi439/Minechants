package me.lemonboi439.minechants;

import me.lemonboi439.minechants.config.MinechantsConfig;
import me.lemonboi439.minechants.enchantment.ModEnchantments;
import me.lemonboi439.minechants.network.ToggleFeaturePayload;
import me.lemonboi439.minechants.util.FeatureToggleState;
import me.lemonboi439.minechants.util.ToggleFeature;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class Minechants implements ModInitializer {
    public static final String MOD_ID = "minechants";

    @Override
    public void onInitialize() {
        MinechantsConfig.load();
        ModEnchantments.registerModEnchantments();
        PayloadTypeRegistry.playC2S().register(ToggleFeaturePayload.ID, ToggleFeaturePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ToggleFeaturePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                ToggleFeature feature = payload.feature();
                boolean enabled = FeatureToggleState.toggle(player, feature);
                player.sendMessage(
                        Text.translatable(
                                enabled ? this.minechants$getEnabledMessageKey(feature) : this.minechants$getDisabledMessageKey(feature)
                        ),
                        true
                );
            });
        });
    }

    private String minechants$getEnabledMessageKey(ToggleFeature feature) {
        return switch (feature) {
            case SMELTING -> "message.minechants.smelting_enabled";
            case THREE_BY_THREE_MINER -> "message.minechants.miner_enabled";
            case EXPLOSIVE -> "message.minechants.explosive_enabled";
            case MAGNETIC -> "message.minechants.magnetic_enabled";
        };
    }

    private String minechants$getDisabledMessageKey(ToggleFeature feature) {
        return switch (feature) {
            case SMELTING -> "message.minechants.smelting_disabled";
            case THREE_BY_THREE_MINER -> "message.minechants.miner_disabled";
            case EXPLOSIVE -> "message.minechants.explosive_disabled";
            case MAGNETIC -> "message.minechants.magnetic_disabled";
        };
    }
}
