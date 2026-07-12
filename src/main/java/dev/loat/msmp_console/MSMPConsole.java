package dev.loat.msmp_console;

import dev.loat.msmp.MSMPNamespace;
import dev.loat.msmp.MSMPServer;
import dev.loat.msmp_console.config.Config;
import dev.loat.msmp_console.logging.ConsoleNotificationAppender;
import dev.loat.msmp_console.logging.Logger;
import dev.loat.msmp_console.mixin.OutgoingRpcMethodBuilderAccessor;
import dev.loat.msmp_console.msmp.endpoints.Endpoints;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.jsonrpc.ManagementServer;
import net.minecraft.server.jsonrpc.OutgoingRpcMethod;


/**
 * Main entrypoint of the MSMP Console mod.
 *
 * <p>This mod extends the Minecraft Server Management Protocol (MSMP) by forwarding
 * every server console log event to all connected WebSocket clients as a JSON-RPC
 * notification under the method {@code console:notification/message}.</p>
 *
 * <p>On initialization the mod:</p>
 * <ol>
 *   <li>Registers a custom {@link OutgoingRpcMethod} under {@code console:notification/message}
 *       via {@link OutgoingRpcMethodBuilderAccessor} to bypass the default {@code minecraft:}
 *       namespace.</li>
 *   <li>Attaches a {@link ConsoleNotificationAppender} to the root Log4j2 logger to
 *       intercept all log events.</li>
 *   <li>Caches the {@link ManagementServer} instance on {@code SERVER_STARTED} and
 *       clears it on {@code SERVER_STOPPED}.</li>
 * </ol>
 */
public class MSMPConsole implements ModInitializer {

    /**
     * The shared {@code entity} namespace used for all MSMP registrations.
     * Attached to the running server in {@code SERVER_STARTED} and detached in {@code SERVER_STOPPED}.
     */
    private static final MSMPNamespace NS = new MSMPNamespace("console");

    /**
     * Provides access to the {@link net.minecraft.server.jsonrpc.ManagementServer}
     * for broadcasting notifications. {@code null} when no server is running.
     */
    private static MSMPServer msmp;

    /**
     * Called by Fabric when the mod is initialized.
     *
     * <p>Registers the Log4j2 appender and the server lifecycle listeners.</p>
     */
    @Override
    public void onInitialize() {
        Logger.setLoggerClass(MSMPConsole.class);

        Config.register();

        Endpoints.register(NS, () -> msmp);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            NS.attach(server);
            msmp = new MSMPServer(server);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            NS.detach();
            msmp = null;
        });

        Logger.info("Mod initialized.");
    }
}
