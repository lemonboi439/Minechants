package me.lemonboi439.minechants.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import me.lemonboi439.minechants.util.ToggleFeature;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MinechantsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("minechants.json");

    private static ConfigData data = new ConfigData();

    private MinechantsConfig() {
    }

    public static void load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
                data = loaded != null ? loaded : new ConfigData();
            } catch (IOException | JsonParseException exception) {
                data = new ConfigData();
            }
        }

        save();
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException exception) {
            throw new RuntimeException("Failed to save Minechants config", exception);
        }
    }

    public static boolean isEnabledByDefault(ToggleFeature feature) {
        return switch (feature) {
            case SMELTING -> data.smeltingEnabledByDefault;
            case THREE_BY_THREE_MINER -> data.threeByThreeMinerEnabledByDefault;
            case EXPLOSIVE -> data.explosiveEnabledByDefault;
            case MAGNETIC -> data.magneticEnabledByDefault;
        };
    }

    public static final class ConfigData {
        public boolean smeltingEnabledByDefault = true;
        public boolean threeByThreeMinerEnabledByDefault = true;
        public boolean explosiveEnabledByDefault = true;
        public boolean magneticEnabledByDefault = true;
    }
}
