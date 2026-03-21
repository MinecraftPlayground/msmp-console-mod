package dev.loat.msmp_console;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.jsonrpc.api.Schema;


public record ConsoleLogPayload(
    String timestamp,
    String level,
    String thread,
    String logger,
    String message,
    String throwable
) {
    public static final Codec<ConsoleLogPayload> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.STRING.fieldOf("timestamp").forGetter(ConsoleLogPayload::timestamp),
        Codec.STRING.fieldOf("level")    .forGetter(ConsoleLogPayload::level),
        Codec.STRING.fieldOf("thread")   .forGetter(ConsoleLogPayload::thread),
        Codec.STRING.fieldOf("logger")   .forGetter(ConsoleLogPayload::logger),
        Codec.STRING.fieldOf("message")  .forGetter(ConsoleLogPayload::message),
        Codec.STRING.optionalFieldOf("throwable", "").forGetter(ConsoleLogPayload::throwable)
    ).apply(i, ConsoleLogPayload::new));

    public static final Schema<ConsoleLogPayload> SCHEMA = Schema
        .record(CODEC)
        .withField("timestamp", Schema.STRING_SCHEMA)
        .withField("level",     Schema.STRING_SCHEMA)
        .withField("thread",    Schema.STRING_SCHEMA)
        .withField("logger",    Schema.STRING_SCHEMA)
        .withField("message",   Schema.STRING_SCHEMA)
        .withField("throwable", Schema.STRING_SCHEMA);
}
