package dev.loat.msmp_console;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.jsonrpc.api.Schema;


/**
 * Represents the payload of a console log notification sent over the
 * Minecraft Server Management Protocol (MSMP).
 *
 * <p>Each instance corresponds to a single log event captured by the
 * {@link dev.loat.msmp_console.logging.ConsoleNotificationAppender} and
 * transmitted as a JSON-RPC notification to all connected management clients
 * via {@code console:notification/message}.</p>
 *
 * <p>Example JSON representation:</p>
 * <pre>{@code
 * {
 *   "timestamp": "2026-03-21T15:06:06.146Z",
 *   "level": "INFO",
 *   "thread": "Server thread",
 *   "logger": "net.minecraft.server.MinecraftServer",
 *   "message": "Done (1.019s)! For help, type \"help\"",
 *   "throwable": ""
 * }
 * }</pre>
 *
 * @param timestamp ISO-8601 timestamp of when the log event occurred
 * @param level     log level of the event (e.g. {@code INFO}, {@code WARN}, {@code ERROR})
 * @param thread    name of the thread that produced the log event
 * @param logger    fully qualified name of the logger (usually the class name)
 * @param message   the fully interpolated log message
 * @param throwable serialized stacktrace if an exception was attached, empty string otherwise
 */
public record ConsoleLogPayload(
    String timestamp,
    String level,
    String thread,
    String logger,
    String message,
    String throwable
) {
    /**
     * Mojang Serialization {@link Codec} for encoding and decoding
     * {@link ConsoleLogPayload} instances to and from JSON.
     *
     * <p>{@code throwable} is optional in deserialization and defaults
     * to an empty string if absent.</p>
     */
    public static final Codec<ConsoleLogPayload> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.STRING.fieldOf("timestamp").forGetter(ConsoleLogPayload::timestamp),
        Codec.STRING.fieldOf("level").forGetter(ConsoleLogPayload::level),
        Codec.STRING.fieldOf("thread").forGetter(ConsoleLogPayload::thread),
        Codec.STRING.fieldOf("logger").forGetter(ConsoleLogPayload::logger),
        Codec.STRING.fieldOf("message").forGetter(ConsoleLogPayload::message),
        Codec.STRING.optionalFieldOf("throwable", "").forGetter(ConsoleLogPayload::throwable)
    ).apply(i, ConsoleLogPayload::new));

    /**
     * JSON-RPC API {@link Schema} describing the structure of {@link ConsoleLogPayload}.
     *
     * <p>Used by the MSMP discovery endpoint ({@code rpc.discover}) to expose
     * the shape of the {@code console:notification/message} notification to
     * connected clients.</p>
     */
    public static final Schema<ConsoleLogPayload> SCHEMA = Schema.record(CODEC)
        .withField("timestamp", Schema.STRING_SCHEMA)
        .withField("level", Schema.STRING_SCHEMA)
        .withField("thread", Schema.STRING_SCHEMA)
        .withField("logger", Schema.STRING_SCHEMA)
        .withField("message", Schema.STRING_SCHEMA)
        .withField("throwable", Schema.STRING_SCHEMA);
}
