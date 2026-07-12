package dev.loat.msmp_console.config.files.send;

import dev.loat.config_lib.annotation.Annotation;


public class SendConfig {

    @Annotation.Comment("""
        Enable logging for the execution of a command in the console, this prevents echoing the send command.
    """)
    @Annotation.Key("log-command-execution")
    public boolean logCommandExecution = true;
}
