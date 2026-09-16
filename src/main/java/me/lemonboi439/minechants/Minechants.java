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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.item.Items;

public class Minechants implements ModInitializer {
    public static final String MOD_ID = "minechants";

    @Override
    public void onInitialize() {
        MinechantsConfig.load();
        this.minechants$registerOptionalTreasurePacks();
        ModEnchantments.registerModEnchantments();
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (source.isBuiltin() && key.equals(BuiltInLootTables.STRONGHOLD_LIBRARY)) {
                var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
                for (var enchantmentKey : new net.minecraft.resources.ResourceKey[] {
                        ModEnchantments.SMELTING, ModEnchantments.EXPLOSIVE,
                        ModEnchantments.THREE_BY_THREE_MINER, ModEnchantments.MAGNETIC}) {
                    var enchantment = enchantments.getOrThrow(enchantmentKey);
                    tableBuilder.withPool(net.minecraft.world.level.storage.loot.LootPool.lootPool()
                            .add(LootItem.lootTableItem(Items.BOOK)
                                    .apply(new EnchantRandomlyFunction.Builder().withEnchantment(enchantment))));
                }
            }
        });
        PayloadTypeRegistry.serverboundPlay().register(ToggleFeaturePayload.TYPE, ToggleFeaturePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ExplosiveConfigPayload.TYPE, ExplosiveConfigPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ToggleFeaturePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                ToggleFeature feature = payload.feature();
                boolean enabled = FeatureToggleState.toggle(player, feature);
                player.sendSystemMessage(
                        Component.translatable(
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

    private void minechants$registerOptionalTreasurePacks() {
        var modContainer = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow();

        if (!MinechantsConfig.threeByThreeTreasureOnly) {
            ResourceManagerHelper.registerBuiltinResourcePack(
                    Identifier.fromNamespaceAndPath(MOD_ID, "three_by_three_non_treasure"),
                    modContainer,
                    ResourcePackActivationType.DEFAULT_ENABLED
            );
        }

        if (!MinechantsConfig.explosiveTreasureOnly) {
            ResourceManagerHelper.registerBuiltinResourcePack(
                    Identifier.fromNamespaceAndPath(MOD_ID, "explosive_non_treasure"),
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







