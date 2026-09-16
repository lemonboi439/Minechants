package me.lemonboi439.minechants.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import me.lemonboi439.minechants.util.ToggleFeature;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.MathHelper;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MinechantsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("minechants.json");

    public static boolean threeByThreeEnabled = true;
    public static boolean explosiveEnabled = true;
    public static boolean smeltingEnabled = true;
    public static boolean magneticEnabled = true;
    public static int explosivePower = 2;
    public static boolean threeByThreeTreasureOnly = false;
    public static boolean explosiveTreasureOnly = true;
    public static boolean disableExplosiveOnServer = false;

    private MinechantsConfig() {
    }

    public static void load() {
        ConfigData loaded = new ConfigData();

        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                ConfigData parsed = GSON.fromJson(reader, ConfigData.class);
                if (parsed != null) {
                    loaded = parsed;
                }
            } catch (IOException | JsonParseException ignored) {
                loaded = new ConfigData();
            }
        }

        threeByThreeEnabled = loaded.threeByThreeEnabled;
        explosiveEnabled = loaded.explosiveEnabled;
        smeltingEnabled = loaded.smeltingEnabled;
        magneticEnabled = loaded.magneticEnabled;
        explosivePower = MathHelper.clamp(loaded.explosivePower, 1, 5);
        threeByThreeTreasureOnly = loaded.threeByThreeTreasureOnly;
        explosiveTreasureOnly = loaded.explosiveTreasureOnly;
        disableExplosiveOnServer = loaded.disableExplosiveOnServer;

        save();
    }

    public static void save() {
        ConfigData data = new ConfigData();
        data.threeByThreeEnabled = threeByThreeEnabled;
        data.explosiveEnabled = explosiveEnabled;
        data.smeltingEnabled = smeltingEnabled;
        data.magneticEnabled = magneticEnabled;
        data.explosivePower = MathHelper.clamp(explosivePower, 1, 5);
        data.threeByThreeTreasureOnly = threeByThreeTreasureOnly;
        data.explosiveTreasureOnly = explosiveTreasureOnly;
        data.disableExplosiveOnServer = disableExplosiveOnServer;

        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException exception) {
            throw new RuntimeException("Failed to save Minechants config", exception);
        }
    }

    public static boolean isFeatureEnabled(ToggleFeature feature) {
        return switch (feature) {
            case THREE_BY_THREE_MINER -> threeByThreeEnabled;
            case EXPLOSIVE -> explosiveEnabled;
            case SMELTING -> smeltingEnabled;
            case MAGNETIC -> magneticEnabled;
        };
    }

    public static boolean isEnabledByDefault(ToggleFeature feature) {
        return isFeatureEnabled(feature);
    }

    private static final class ConfigData {
        private boolean threeByThreeEnabled = true;
        private boolean explosiveEnabled = true;
        private boolean smeltingEnabled = true;
        private boolean magneticEnabled = true;
        private int explosivePower = 2;
        private boolean threeByThreeTreasureOnly = false;
        private boolean explosiveTreasureOnly = true;
        private boolean disableExplosiveOnServer = false;
    }
}
