package me.lemonboi439.minechants.client;

import me.lemonboi439.minechants.config.MinechantsConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class MinechantsConfigScreen {
    private MinechantsConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.minechants.title"));
        ConfigCategory mining = builder.getOrCreateCategory(Text.literal("Mining"));
        ConfigEntryBuilder entries = builder.entryBuilder();

        mining.addEntry(entries.startBooleanToggle(Text.translatable("config.minechants.three_by_three_enabled"), MinechantsConfig.threeByThreeEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(value -> MinechantsConfig.threeByThreeEnabled = value)
                .build());
        mining.addEntry(entries.startBooleanToggle(Text.translatable("config.minechants.explosive_enabled"), MinechantsConfig.explosiveEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(value -> MinechantsConfig.explosiveEnabled = value)
                .build());
        mining.addEntry(entries.startBooleanToggle(Text.translatable("config.minechants.smelting_enabled"), MinechantsConfig.smeltingEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(value -> MinechantsConfig.smeltingEnabled = value)
                .build());
        mining.addEntry(entries.startBooleanToggle(Text.translatable("config.minechants.magnetic_enabled"), MinechantsConfig.magneticEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(value -> MinechantsConfig.magneticEnabled = value)
                .build());
        mining.addEntry(entries.startIntSlider(Text.translatable("config.minechants.explosive_power"), MinechantsConfig.explosivePower, 1, 5)
                .setDefaultValue(2)
                .setSaveConsumer(value -> MinechantsConfig.explosivePower = value)
                .build());

        builder.setSavingRunnable(MinechantsConfig::save);
        return builder.build();
    }
}
