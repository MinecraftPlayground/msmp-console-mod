package dev.loat.msmp_console.logging;

import dev.loat.msmp_console.MSMPConsole;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import java.time.Instant;


@Plugin(name = "ConsoleNotificationAppender",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE)
public class ConsoleNotificationAppender extends AbstractAppender {

    private static final ThreadLocal<Boolean> IS_APPENDING =
            ThreadLocal.withInitial(() -> false);

    protected ConsoleNotificationAppender(String name, Filter filter) {
        super(name, filter, null, true, null);
    }

    @PluginFactory
    public static ConsoleNotificationAppender createAppender(String name) {
        return new ConsoleNotificationAppender(name, null);
    }

    @Override
    public void append(LogEvent event) {
        if (IS_APPENDING.get()) return;
        IS_APPENDING.set(true);
        try {
            MSMPConsole.sendConsoleNotification(buildPayload(event));
        } finally {
            IS_APPENDING.set(false);
        }
    }

    private static LogPayload buildPayload(LogEvent event) {
        String throwable = null;
        if (event.getThrown() != null) {
            Throwable t = event.getThrown();
            StringBuilder sb = new StringBuilder();
            sb.append(t.getClass().getName());
            if (t.getMessage() != null)
                sb.append(": ").append(t.getMessage());
            for (StackTraceElement el : t.getStackTrace())
                sb.append("\n\tat ").append(el);
            throwable = sb.toString();
        }

        return new LogPayload(
            Instant.ofEpochMilli(event.getTimeMillis()).toString(),
            event.getLevel().name(),
            event.getThreadName(),
            event.getLoggerName(),
            event.getMessage().getFormattedMessage(),
            throwable
        );
    }

    public record LogPayload(
        String timestamp,
        String level,
        String thread,
        String logger,
        String message,
        String throwable
    ) {}
}
