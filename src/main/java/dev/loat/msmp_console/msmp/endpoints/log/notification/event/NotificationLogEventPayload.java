package dev.loat.msmp_console.msmp.endpoints.log.notification.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.server.jsonrpc.api.Schema;


/**
 * Payload for the {@code console:notification/log/event} notification.
 *  
 * <p>Fired when a new log event occurs.</p>
 * 
 * <p>Example JSON representation:</p>
 * <pre><code>
 * {
 *   "timestamp": ""
 *   "level": ""
 *   "thread": ""
 *   "logger": ""
 *   "message": ""
 *   "throwable": ""
 * }
 * </code></pre>
 * 
 * @param timestamp ISO-8601 timestamp of the log event
 * @param level log level (e.g. {@code INFO}, {@code WARN}, {@code ERROR})
 * @param thread name of the thread that produced the event
 * @param logger fully qualified name of the originating logger
 * @param message fully interpolated log message
 * @param throwable serialized stacktrace, or {@code null} if no exception was thrown
 */
public record NotificationLogEventPayload(
    String timestamp,
    String level,
    String thread,
    String logger,
    String message,
    String throwable
) {

    public static final Codec<NotificationLogEventPayload> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.STRING.fieldOf("timestamp").forGetter(NotificationLogEventPayload::timestamp),
        Codec.STRING.fieldOf("level").forGetter(NotificationLogEventPayload::level),
        Codec.STRING.fieldOf("thread").forGetter(NotificationLogEventPayload::thread),
        Codec.STRING.fieldOf("logger").forGetter(NotificationLogEventPayload::logger),
        Codec.STRING.fieldOf("message").forGetter(NotificationLogEventPayload::message),
        Codec.STRING.fieldOf("throwable").forGetter(NotificationLogEventPayload::throwable)
    ).apply(i, NotificationLogEventPayload::new));

    public static final Schema<NotificationLogEventPayload> SCHEMA = Schema.record(CODEC)
        .withField("timestamp", Schema.STRING_SCHEMA)
        .withField("level", Schema.STRING_SCHEMA)
        .withField("thread", Schema.STRING_SCHEMA)
        .withField("logger", Schema.STRING_SCHEMA)
        .withField("message", Schema.STRING_SCHEMA)
        .withField("throwable", Schema.STRING_SCHEMA);
}
