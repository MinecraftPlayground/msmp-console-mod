package dev.loat.msmp_console.config.files;

import dev.loat.config_lib.annotation.Annotation;


@Annotation.Comment("""
    Main configuration file for MSMP Entity.
""")
public class MSMPEntityConfigFile {
    private MSMPEntityConfigFile() {}

    @Annotation.Comment("Example")
    public String example = "42";
}
