package dev.loat.msmp_console.config.files;

import dev.loat.msmp_console.config.annotation.Comment;


/**
 * Represents the structure of the console configuration file.
 */
public final class MSMPConsoleConfigFile {
    @Comment("""        
    The default log level to use for the console logger.
    Log levels are used to determine the severity of log messages, and can be used to filter out less important messages.
    
    This can be one of the following values:
    - TRACE
    - DEBUG
    - INFO
    - WARN
    - ERROR
    - FATAL
    """)
    public String defaultLogLevel = "INFO";
}
