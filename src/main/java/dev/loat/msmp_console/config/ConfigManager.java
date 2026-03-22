package dev.loat.msmp_console.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import dev.loat.msmp_console.logging.Logger;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Manages configuration files.
 */
public class ConfigManager {
    private static final Map<Class<?>, Config<?>> configs = new HashMap<>();

    public static final String rootDirectory = "msmp/console";

    /**
     * Adds a config to the manager.
     *
     * @param <ConfigFile> config The config type
     * @param config The config class to add
     */
    public static <ConfigFile> void addConfig(Config<ConfigFile> config) {
        configs.put(config.getConfigFileClass(), config);
    }

    /**
     * Gets a config by its type.
     *
     * @param <ConfigFile> type The type of the config
     * @param config The config class to get
     * @return The config, or null if not found
     */
    @SuppressWarnings("unchecked")
    public static <ConfigFile> Config<ConfigFile> getConfig(Class<ConfigFile> config) {
        return (Config<ConfigFile>) configs.get(config);
    }
        
    /**
     * Resolves a config file path relative to the config directory.
     * Creates the config directory if it does not exist.
     *
     * @param configFile The config file name
     * @return The resolved path
     */
    public static Path resolve(String configFile) {
        return ConfigManager.resolve(Path.of(configFile));
    }
    
    /**
     * Resolves a config file path relative to the config directory.
     * Creates the config directory if it does not exist.
     *
     * @param configFile The config file name
     * @return The resolved path
     */
    public static Path resolve(Path configFile) {
        Path configFilePath = FabricLoader
            .getInstance()
            .getConfigDir()
            .resolve(ConfigManager.rootDirectory);

        try {
            Files.createDirectories(configFilePath);
        } catch (IOException e) {
            Logger.error("Could not create config directory:\n%s".formatted(e));
        }

        return configFilePath.resolve(configFile);
    }

    /**
     * Loads all registered config files.
     */
    public static void loadAll() {
        ConfigManager.configs.values().forEach(Config::load);
    }
}
