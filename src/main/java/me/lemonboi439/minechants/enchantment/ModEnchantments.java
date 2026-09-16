package me.lemonboi439.minechants.enchantment;

import me.lemonboi439.minechants.Minechants;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEnchantments {
    public static final RegistryKey<Enchantment> SMELTING = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of(Minechants.MOD_ID, "smelting")
    );
    public static final RegistryKey<Enchantment> EXPLOSIVE = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of(Minechants.MOD_ID, "explosive")
    );
    public static final RegistryKey<Enchantment> THREE_BY_THREE_MINER = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of(Minechants.MOD_ID, "three_by_three_miner")
    );
    public static final RegistryKey<Enchantment> MAGNETIC = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of(Minechants.MOD_ID, "magnetic")
    );

    public static void registerModEnchantments() {
        System.out.println("Registering mod enchantments for " + Minechants.MOD_ID);
    }
}
