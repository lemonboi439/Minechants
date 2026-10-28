package me.lemonboi439.minechants.enchantment;

import me.lemonboi439.minechants.Minechants;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

public class ModEnchantments {
    public static final ResourceKey<Enchantment> SMELTING = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(Minechants.MOD_ID, "smelting")
    );
    public static final ResourceKey<Enchantment> EXPLOSIVE = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(Minechants.MOD_ID, "explosive")
    );
    public static final ResourceKey<Enchantment> THREE_BY_THREE_MINER = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(Minechants.MOD_ID, "three_by_three_miner")
    );
    public static final ResourceKey<Enchantment> MAGNETIC = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(Minechants.MOD_ID, "magnetic")
    );
    public static final ResourceKey<Enchantment> EXTENSION = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(Minechants.MOD_ID, "extension")
    );

    public static void registerModEnchantments() {
        System.out.println("Registering mod enchantments for " + Minechants.MOD_ID);
    }
}








