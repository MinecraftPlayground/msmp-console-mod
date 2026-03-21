package dev.loat.msmp_console.mixin;

import net.minecraft.server.jsonrpc.Connection;
import net.minecraft.server.jsonrpc.ManagementServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import java.util.function.Consumer;


/**
 * Mixin accessor for the package-private
 * {@code ManagementServer#forEachConnection(Consumer)} method.
 *
 * <p>{@link ManagementServer#forEachConnection(Consumer)} is package-private and therefore
 * not directly callable from outside {@code net.minecraft.server.jsonrpc}. This accessor
 * exposes it so that {@link dev.loat.msmp_console.MSMPConsole} can iterate over all
 * active WebSocket connections and dispatch notifications to each one.</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * ((ManagementServerAccessor) managementServer)
 *     .invokeForEachConnection(conn ->
 *         conn.sendNotification(CONSOLE_MESSAGE, payload)
 *     );
 * }</pre>
 */
@Mixin(ManagementServer.class)
public interface ManagementServerAccessor {

    /**
     * Invokes the package-private {@code forEachConnection(Consumer)} method on
     * {@link ManagementServer}, calling the given {@code consumer} for each currently
     * active {@link Connection}.
     *
     * @param consumer the action to perform for each active connection,
     *                 e.g. sending a notification via {@link Connection#sendNotification}
     */
    @Invoker("forEachConnection")
    void invokeForEachConnection(Consumer<Connection> consumer);
}
