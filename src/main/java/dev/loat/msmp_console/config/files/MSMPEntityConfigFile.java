package dev.loat.msmp_console.config.files;

import dev.loat.config_lib.annotation.Annotation;
import dev.loat.msmp_console.config.files.log.LogConfig;
import dev.loat.msmp_console.config.files.send.SendConfig;


@Annotation.Comment("""
    Main configuration file for MSMP Entity.
""")
public class MSMPEntityConfigFile {
    private MSMPEntityConfigFile() {}

    @Annotation.Comment("Configuration for log related settings.")
    public LogConfig log = new LogConfig();
    
    @Annotation.Comment("Configuration for send related settings.")
    public SendConfig send = new SendConfig();
}
