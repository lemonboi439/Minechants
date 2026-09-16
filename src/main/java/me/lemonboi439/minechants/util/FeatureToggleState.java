package me.lemonboi439.minechants.util;

import me.lemonboi439.minechants.config.MinechantsConfig;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FeatureToggleState {
    private static final Map<UUID, EnumMap<ToggleFeature, Boolean>> PLAYER_STATES = new ConcurrentHashMap<>();

    private FeatureToggleState() {
    }

    public static boolean isEnabled(ServerPlayerEntity player, ToggleFeature feature) {
        if (!MinechantsConfig.isFeatureEnabled(feature)) {
            return false;
        }

        return PLAYER_STATES
                .getOrDefault(player.getUuid(), new EnumMap<>(ToggleFeature.class))
                .getOrDefault(feature, MinechantsConfig.isEnabledByDefault(feature));
    }

    public static boolean toggle(ServerPlayerEntity player, ToggleFeature feature) {
        EnumMap<ToggleFeature, Boolean> toggles = PLAYER_STATES.computeIfAbsent(
                player.getUuid(),
                uuid -> new EnumMap<>(ToggleFeature.class)
        );
        boolean enabled = !toggles.getOrDefault(feature, MinechantsConfig.isEnabledByDefault(feature));
        toggles.put(feature, enabled);
        return enabled;
    }
}
