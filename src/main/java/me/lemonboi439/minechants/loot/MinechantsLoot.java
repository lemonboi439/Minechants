package me.lemonboi439.minechants.loot;

import me.lemonboi439.minechants.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class MinechantsLoot {
    private MinechantsLoot() {
    }

    public static void register() {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            if (!key.equals(BuiltInLootTables.STRONGHOLD_LIBRARY)) {
                return;
            }

            HolderLookup.RegistryLookup<net.minecraft.world.item.enchantment.Enchantment> enchantments =
                    registries.lookupOrThrow(Registries.ENCHANTMENT);
            addEnchantedBook(table, enchantments, ModEnchantments.SMELTING);
            addEnchantedBook(table, enchantments, ModEnchantments.EXPLOSIVE);
            addEnchantedBook(table, enchantments, ModEnchantments.THREE_BY_THREE_MINER);
            addEnchantedBook(table, enchantments, ModEnchantments.MAGNETIC);
        });
    }

    private static void addEnchantedBook(
            LootTable.Builder table,
            HolderLookup.RegistryLookup<net.minecraft.world.item.enchantment.Enchantment> enchantments,
            net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> enchantment
    ) {
        table.pool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                        .apply(EnchantRandomlyFunction.randomEnchantment()
                                .withEnchantment(enchantments.getOrThrow(enchantment)))
                        .build())
                .build());
    }
}
