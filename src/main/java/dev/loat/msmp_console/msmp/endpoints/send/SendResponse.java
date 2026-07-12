package dev.loat.msmp_console.msmp.endpoints.send;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.server.jsonrpc.api.Schema;


/**
 * Response payload for the {@code console:send} method.
 *
 * <p>Example response:</p>
 * <pre><code>
 * {
 *   "command": "say Hello",
 *   "success": true
 * }
 * </code></pre>
 *
 * @param command The command
 * @param success If the command was ran successful
 */
public record SendResponse(String command, String result, boolean success) {
    
    /**
     * Codec for serializing and deserializing {@link SendResponse} instances.
     */
    public static final Codec<SendResponse> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.STRING.fieldOf("command").forGetter(SendResponse::command),
        Codec.STRING.fieldOf("result").forGetter(SendResponse::result),
        Codec.BOOL.fieldOf("success").forGetter(SendResponse::success)
    ).apply(i, SendResponse::new));

    /**
     * MSMP schema for {@link SendResponse}, used for protocol discovery.
     */
    public static final Schema<SendResponse> SCHEMA = Schema.record(CODEC)
        .withField("command", Schema.STRING_SCHEMA)
        .withField("result", Schema.STRING_SCHEMA)
        .withField("success", Schema.BOOL_SCHEMA);
}
