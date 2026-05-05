package me.lemonboi439.minechants;

import me.lemonboi439.minechants.config.MinechantsConfig;
import me.lemonboi439.minechants.enchantment.ModEnchantments;
import me.lemonboi439.minechants.util.FeatureToggleState;
import me.lemonboi439.minechants.util.ToggleFeature;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Minechants implements ModInitializer {
    public static final String MOD_ID = "minechants";
    public static final Identifier TOGGLE_FEATURE_ID = Identifier.of(MOD_ID, "toggle_feature");

    @Override
    public void onInitialize() {
        MinechantsConfig.load();
        ModEnchantments.registerModEnchantments();
        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_FEATURE_ID, (server, player, handler, buf, responseSender) -> {
            ToggleFeature feature = ToggleFeature.values()[buf.readInt()];
            boolean enabled = FeatureToggleState.toggle(player, feature);
            player.sendMessage(
                    Text.translatable(
                            enabled ? getEnabledMessageKey(feature) : getDisabledMessageKey(feature)
                    ),
                    true
            );
        });
    }

    private static String getEnabledMessageKey(ToggleFeature feature) {
        return switch (feature) {
            case SMELTING -> "message.minechants.smelting_enabled";
            case THREE_BY_THREE_MINER -> "message.minechants.miner_enabled";
            case EXPLOSIVE -> "message.minechants.explosive_enabled";
            case MAGNETIC -> "message.minechants.magnetic_enabled";
        };
    }

    private static String getDisabledMessageKey(ToggleFeature feature) {
        return switch (feature) {
            case SMELTING -> "message.minechants.smelting_disabled";
            case THREE_BY_THREE_MINER -> "message.minechants.miner_disabled";
            case EXPLOSIVE -> "message.minechants.explosive_disabled";
            case MAGNETIC -> "message.minechants.magnetic_disabled";
        };
    }
}
