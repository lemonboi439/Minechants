package me.lemonboi439.minechants;

import me.lemonboi439.minechants.config.MinechantsConfig;
import me.lemonboi439.minechants.enchantment.ModEnchantments;
import me.lemonboi439.minechants.network.ExplosiveConfigPayload;
import me.lemonboi439.minechants.network.ToggleFeaturePayload;
import me.lemonboi439.minechants.util.FeatureToggleState;
import me.lemonboi439.minechants.util.ToggleFeature;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.loot.LootTables;

public class Minechants implements ModInitializer {
    public static final String MOD_ID = "minechants";

    @Override
    public void onInitialize() {
        MinechantsConfig.load();
        this.minechants$registerOptionalTreasurePacks();
        this.minechants$registerStrongholdLibraryLoot();
        ModEnchantments.registerModEnchantments();
        PayloadTypeRegistry.playC2S().register(ToggleFeaturePayload.ID, ToggleFeaturePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ExplosiveConfigPayload.ID, ExplosiveConfigPayload.CODEC);
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
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                ServerPlayNetworking.send(handler.player, new ExplosiveConfigPayload(MinechantsConfig.disableExplosiveOnServer))
        );
    }

    private void minechants$registerStrongholdLibraryLoot() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (key.equals(LootTables.STRONGHOLD_LIBRARY_CHEST) && source.isBuiltin()) {
                tableBuilder.pool(LootPool.builder()
                        .with(LootTableEntry.builder(Identifier.of(MOD_ID, "chests/stronghold_minechants"))));
            }
        });
    }

    private void minechants$registerOptionalTreasurePacks() {
        var modContainer = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow();

        if (!MinechantsConfig.threeByThreeTreasureOnly) {
            ResourceManagerHelper.registerBuiltinResourcePack(
                    Identifier.of(MOD_ID, "three_by_three_non_treasure"),
                    modContainer,
                    ResourcePackActivationType.DEFAULT_ENABLED
            );
        }

        if (!MinechantsConfig.explosiveTreasureOnly) {
            ResourceManagerHelper.registerBuiltinResourcePack(
                    Identifier.of(MOD_ID, "explosive_non_treasure"),
                    modContainer,
                    ResourcePackActivationType.DEFAULT_ENABLED
            );
        }
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
