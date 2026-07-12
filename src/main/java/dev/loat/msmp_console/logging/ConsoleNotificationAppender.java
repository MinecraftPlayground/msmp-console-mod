package dev.loat.msmp_console.logging;

import dev.loat.msmp_console.config.Config;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import java.time.Instant;
import java.util.function.Consumer;


/**
 * A Log4j2 appender that forwards every log event at or above the configured minimum
 * level to a listener as a {@link LogPayload}.
 *
 * <p>Attached directly to the root logger via {@link #register(Consumer)} - there is no
 * Log4j2 config/plugin discovery involved, so no {@code @Plugin} metadata is needed.</p>
 */
public class ConsoleNotificationAppender extends AbstractAppender {

    /**
     * Prevents feedback loops: if forwarding an event causes something to log again
     * (e.g. an MSMP send failure), that new event must not be captured too.
     */
    private static final ThreadLocal<Boolean> IS_APPENDING = ThreadLocal.withInitial(() -> false);

    private final Consumer<LogPayload> listener;

    private ConsoleNotificationAppender(Consumer<LogPayload> listener) {
        super("ConsoleNotificationAppender", null, null, true, null);
        this.listener = listener;
    }

    /**
     * Creates, starts, and attaches a new {@link ConsoleNotificationAppender} to the root
     * logger, forwarding every captured event to {@code listener}.
     *
     * @param listener Called for every log event at or above the configured minimum level
     * @return The attached appender
     */
    public static ConsoleNotificationAppender register(Consumer<LogPayload> listener) {
        ConsoleNotificationAppender appender = new ConsoleNotificationAppender(listener);
        appender.start();

        ((LoggerContext) LogManager.getContext(false)).getRootLogger().addAppender(appender);

        return appender;
    }

    /**
     * Intercepts a log event and.
     *
     * <p>Guarded by {@link #IS_APPENDING} to prevent infinite recursion. If the flag
     * is already set on the current thread, the event is silently dropped.</p>
     *
     * @param event the log event to process
     */
    @Override
    public void append(LogEvent event) {
        if (IS_APPENDING.get()) return;

        Level minLevel = Level.toLevel(Config.getConfig().log.level.toString(), Level.INFO);
        if (!event.getLevel().isMoreSpecificThan(minLevel)) return;

        IS_APPENDING.set(true);
        try {
            if (listener != null) {
                listener.accept(toPayload(event));
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
    private static LogPayload toPayload(LogEvent event) {
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

        return new ConsoleNotificationAppender.LogPayload(
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
