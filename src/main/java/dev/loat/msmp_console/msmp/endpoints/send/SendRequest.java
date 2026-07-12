package dev.loat.msmp_console.msmp.endpoints.send;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.server.jsonrpc.api.Schema;


/**
 * Response payload for the {@code console:send} method.
 *
 * <p>Example JSON representation:</p>
 * <pre><code>
 * { "command": "say Hello" }
 * </code></pre>
 *
 * @param command The command
 * @param success If the command was ran successful
 */
public record SendRequest(String command) {

    /**
     * Codec for serializing and deserializing {@link SendResponse} instances.
     */
    public static final Codec<SendRequest> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.STRING.fieldOf("command").forGetter(SendRequest::command)
    ).apply(i, SendRequest::new));

    /**
     * MSMP schema for {@link SendRequest}, used for protocol discovery.
     */
    public static final Schema<SendRequest> SCHEMA = Schema.record(CODEC)
        .withField("command", Schema.STRING_SCHEMA);
    
}
