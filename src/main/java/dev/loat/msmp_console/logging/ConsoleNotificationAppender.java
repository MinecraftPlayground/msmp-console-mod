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
import java.util.function.Consumer;

/**
 * A Log4j2 {@link Appender} that intercepts log events and forwards them to a consumer.
 */
@Plugin(
    name = "ConsoleNotificationAppender",
    category = Core.CATEGORY_NAME,
    elementType = Appender.ELEMENT_TYPE
)
public class ConsoleNotificationAppender extends AbstractAppender {

    /**
     * Per-thread flag that prevents recursive invocation of {@link #append(LogEvent)}.
     * Set to {@code true} while a notification is being dispatched, and reset to
     * {@code false} in the {@code finally} block.
     */
    private static final ThreadLocal<Boolean> IS_APPENDING =
        ThreadLocal.withInitial(() -> false);

    private final Consumer<LogPayload> listener;

    /**
     * Creates a new {@code ConsoleNotificationAppender} with the given name and filter.
     *
     * @param name   the name of this appender
     * @param filter an optional Log4j2 filter, or {@code null} for no filtering
     */
    protected ConsoleNotificationAppender(String name, Filter filter, Consumer<LogPayload> listener) {
        super(name, filter, null, true, null);
        this.listener = listener;
    }

    /**
     * Factory method used by Log4j2's plugin system to instantiate this appender.
     *
     * @param name The name of the appender instance
     * @param listener Callback for the appender
     * @return A new {@code ConsoleNotificationAppender}
     */
    @PluginFactory
    public static ConsoleNotificationAppender createAppender(String name, Consumer<LogPayload> listener) {
        return new ConsoleNotificationAppender(name, null, listener);
    }

    /**
     * Intercepts a log event and forwards it to {@link MSMPConsole#sendConsoleNotification(LogPayload)}.
     *
     * <p>Guarded by {@link #IS_APPENDING} to prevent infinite recursion. If the flag
     * is already set on the current thread, the event is silently dropped.</p>
     *
     * @param event the log event to process
     */
    @Override
    public void append(LogEvent event) {
        if (IS_APPENDING.get()) return;
        IS_APPENDING.set(true);
        try {
            if (listener != null) {
                listener.accept(buildPayload(event));
            }
        } finally {
            IS_APPENDING.set(false);
        }
    }

    /**
     * Builds a {@link LogPayload} from the given {@link LogEvent}.
     *
     * <p>If the event contains a {@link Throwable}, its class name, message,
     * and full stack trace are serialized into a single string.</p>
     *
     * @param event the log event to extract data from
     * @return a {@link LogPayload} containing all relevant fields of the event
     */
    private static LogPayload buildPayload(LogEvent event) {
        String throwable = "";
        if (event.getThrown() != null) {
            Throwable thrown = event.getThrown();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(thrown.getClass().getName());
            if (thrown.getMessage() != null) {
                stringBuilder.append(": ").append(thrown.getMessage());
            }
            for (StackTraceElement el : thrown.getStackTrace()) {
                stringBuilder.append("\n\tat ").append(el);
            }
            throwable = stringBuilder.toString();
        }

        String message = event.getMessage() == null ? "" : event.getMessage().getFormattedMessage();

        return new LogPayload(
            Instant.ofEpochMilli(event.getTimeMillis()).toString(),
            event.getLevel().name(),
            event.getThreadName(),
            event.getLoggerName(),
            message,
            throwable
        );
    }

    /**
     * Immutable data transfer object representing a single captured log event.
     *
     * @param timestamp ISO-8601 timestamp of the log event
     * @param level log level (e.g. {@code INFO}, {@code WARN}, {@code ERROR})
     * @param thread name of the thread that produced the event
     * @param logger fully qualified name of the originating logger
     * @param message fully interpolated log message
     * @param throwable serialized stacktrace, or {@code null} if no exception was thrown
     */
    public record LogPayload(
        String timestamp,
        String level,
        String thread,
        String logger,
        String message,
        String throwable
    ) {}
}
