package dev.loat.msmp_console.config.files.log;

import dev.loat.config_lib.annotation.Annotation;


public class LogConfig {

    public static enum LogLevel {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL
    }

    @Annotation.Comment("""
        The minimum log level that gets forwarded as a console:notification/log_event.
        Events below this level are ignored entirely and never sent to connected clients.
    """)
    public LogLevel level = LogLevel.INFO;
}
