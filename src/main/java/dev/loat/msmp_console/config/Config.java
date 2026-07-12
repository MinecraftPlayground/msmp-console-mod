package dev.loat.msmp_console.config;

import dev.loat.config_lib.ConfigManager;
import dev.loat.msmp_console.config.files.MSMPEntityConfigFile;


public class Config {
    
    private Config() {}
    
    private static final String ROOT_DIRECTORY = "msmp/console";
    private static final ConfigManager CONFIG_MANAGER = new ConfigManager(ROOT_DIRECTORY);

    public static void register() {
        
        CONFIG_MANAGER.add("config.yml", MSMPEntityConfigFile.class);
    }

    public static MSMPEntityConfigFile getConfig() {

        return CONFIG_MANAGER.get(MSMPEntityConfigFile.class);
    }
}
